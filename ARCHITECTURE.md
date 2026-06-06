# Architecture

## Overview

Bitplane converts files into fixed-size grayscale image frames at 1920×1080, one byte per pixel. The implementation is
centered around four parts: the encode pipeline, the decode pipeline, the video layer, and the workspace cleanup
service. This document explains how those pieces are built and how they fit together.

## Encode Pipeline

### What it does

The encode pipeline turns an input file into one metadata frame and a sequence of payload frames, then hands them off
to the video layer as an `ImageFrameSet`.

### Main classes

| Component           | Class                                     | Responsibility                                                                         |
|---------------------|-------------------------------------------|----------------------------------------------------------------------------------------|
| Pipeline entry      | `EncodePipeline`                          | Reads the input file, creates metadata and payload frames, and converts them to images |
| Payload chunking    | `PayloadFrameBuilder`                     | Splits file bytes into fixed-size payload chunks                                       |
| Metadata model      | `MetadataFrame`                           | Stores file name, file size, and total payload frame count                             |
| Payload model       | `PayloadFrame`                            | Stores frame index and payload bytes                                                   |
| Frame serialization | `MetadataFrameCodec`, `PayloadFrameCodec` | Converts frame objects to and from byte arrays                                         |
| Image conversion    | `ImageGenerator`, `RasterCodec`           | Converts serialized frame bytes to grayscale `BufferedImage` objects                   |
| Frame handoff       | `ImageFrameSet`                           | Record holding the metadata image and list of payload images between pipeline stages   |

### Frame layout

| Frame          | Layout                                         |
|----------------|------------------------------------------------|
| Metadata frame | 4-byte length prefix, then JSON metadata bytes |
| Payload frame  | 4-byte frame index, then payload bytes         |

The total frame capacity is `Constants.FRAME_BYTE_CAPACITY`, which is `Constants.WIDTH × Constants.HEIGHT`.

### How it works

1. `EncodePipeline.encode(Path inputFile, String originalFileName)` reads all file bytes with `Files.readAllBytes`.
2. `PayloadFrameBuilder.build(byte[] payload)` splits the payload into chunks of `FRAME_BYTE_CAPACITY - Integer.BYTES`.
3. A `MetadataFrame` is created using the original file name, total file size, and payload frame count.
4. `ImageGenerator.metadataToImage` serializes the `MetadataFrame` via `MetadataFrameCodec` and converts the bytes to a `BufferedImage` via `RasterCodec.serialize`.
5. `ImageGenerator.payloadToImage` serializes each `PayloadFrame` via `PayloadFrameCodec` and converts the bytes to a `BufferedImage` via `RasterCodec.serialize`.
6. `EncodePipeline` returns an `ImageFrameSet` containing the metadata image and all payload images.

### Important behavior

| Topic          | Behavior                                                              |
|----------------|-----------------------------------------------------------------------|
| Chunk size     | `FRAME_BYTE_CAPACITY - 4` bytes per payload frame                     |
| Frame ordering | Deterministic, starting at frame index 0                              |
| Memory model   | The encoder reads the full input file into memory                     |
| Error handling | Invalid input and serialization failures are propagated as exceptions |

### Example

For a 5 MiB file:

- Frame capacity: `1920 × 1080 = 2,073,600` bytes
- Payload per frame: `2,073,600 - 4 = 2,073,596` bytes
- Payload frames required: `ceil(5,242,880 / 2,073,596) = 3`

### Notes

- Metadata must fit in a single frame.
- The current implementation does not stream file reads.

## Video Layer

### What it does

`VideoEncoder` takes an `ImageFrameSet`, writes all frames as PNGs, and assembles them into a video file.
`VideoDecoder` takes a video file, extracts all frames as PNGs via FFmpeg, and returns an `ImageFrameSet`.

### Main classes

| Component         | Class            | Responsibility                                          |
|-------------------|------------------|---------------------------------------------------------|
| Video encoding    | `VideoEncoder`   | Writes frame images to disk and invokes FFmpeg          |
| Video decoding    | `VideoDecoder`   | Invokes FFmpeg to extract frames, returns `ImageFrameSet` |
| Process execution | `FFmpegExecutor` | Starts FFmpeg subprocess and checks the exit status     |
| Frame naming      | `FrameFileName`  | Provides the `frame_%06d.png` pattern used by FFmpeg    |
| Image I/O         | `ImageIOCodec`   | Reads and writes `BufferedImage` objects as PNG files   |

### How encoding works

1. `VideoEncoder.encode(ImageFrameSet imageFrameSet, Path jobDirectory)` creates a `frames/` directory inside the job
   directory.
2. The metadata image is written as `frame_000000.png`.
3. Payload images are written in order as `frame_000001.png`, `frame_000002.png`, and so on.
4. A `ProcessBuilder` is created for FFmpeg using the `frame_%06d.png` input pattern.
5. `FFmpegExecutor.execute(...)` starts FFmpeg and waits for completion.
6. If FFmpeg returns a non-zero exit code, the operation fails and the error is surfaced to the caller.

### How decoding works

1. `VideoDecoder.decode(Path videoPath, Path jobDirectory)` creates an `extracted_frames/` directory.
2. FFmpeg extracts all frames as PNGs into `extracted_frames/`, starting at index 0.
3. The extracted frame paths are listed and sorted.
4. The first path is read as the metadata image; the rest are read as payload images.
5. An `ImageFrameSet` is returned with all images loaded into memory.

### FFmpeg command — encoding

```bash
ffmpeg -y -nostdin -v error -framerate 30 -i frames/frame_%06d.png -c:v libx264rgb -pix_fmt rgb24 -crf 0 -preset veryslow output.mp4
```

### FFmpeg command — decoding

```bash
ffmpeg -y -nostdin -v error -i input.mp4 -start_number 0 extracted_frames/frame_%06d.png
```

### Notes

| Topic         | Behavior                                           |
|---------------|----------------------------------------------------|
| Codec choice  | `libx264rgb` with `-crf 0` for lossless encoding   |
| Output        | `output.mp4` in the job directory                  |
| Memory model  | All frames are loaded into memory during decoding  |

## Decode Pipeline

### What it does

The decode pipeline reconstructs the original file from an `ImageFrameSet` containing a metadata image and a list of
payload images.

### Main classes

| Component             | Class                                     | Responsibility                                   |
|-----------------------|-------------------------------------------|--------------------------------------------------|
| Pipeline entry        | `DecodePipeline`                          | Reconstructs the original file from frame images |
| Frame deserialization | `MetadataFrameCodec`, `PayloadFrameCodec` | Converts byte arrays back into frame objects     |
| Image conversion      | `RasterCodec`                             | Converts grayscale images back to byte arrays    |

### How it works

1. `DecodePipeline.decode(ImageFrameSet imageFrameSet, Path jobDirectory)` reads the metadata and payload images.
2. `RasterCodec.deserialize` converts each `BufferedImage` back into a byte array.
3. `MetadataFrameCodec.deserialize` parses the metadata frame (original file name, file size, expected frame count).
4. `PayloadFrameCodec.deserialize` parses each payload frame.
5. The decoder validates that the actual payload frame count matches the expected count from the metadata.
6. Payload frames are sorted by `frameIndex`.
7. Frame order is validated — each `frameIndex` must match its position in the sorted list.
8. The payload bytes are concatenated and truncated to the exact original file size using `Arrays.copyOf`.
9. The reconstructed file is written to the job directory using its original file name.

### Notes

| Topic      | Behavior                                         |
|------------|--------------------------------------------------|
| Ordering   | Strict, based on `frameIndex`                    |
| Validation | Fails if frame count or ordering is invalid      |
| Output     | Reconstructed file in the supplied job directory |

## Service Layer

### What it does

`CodecService` wires the encode and decode pipelines together with the video layer. It is the single entry point
called by the controller.

### Encode flow

```
CodecService.encode(inputFile, originalFileName, jobDirectory)
  → EncodePipeline.encode(inputFile, originalFileName)   → ImageFrameSet
  → VideoEncoder.encode(imageFrameSet, jobDirectory)     → Path (output.mp4)
```

### Decode flow

```
CodecService.decode(videoPath, jobDirectory)
  → VideoDecoder.decode(videoPath, jobDirectory)         → ImageFrameSet
  → DecodePipeline.decode(imageFrameSet, jobDirectory)   → Path (original file)
```

## Isolated Workspace and Cleanup

### What it does

Workspaces keep each operation isolated from the others and make cleanup predictable.

### Main classes and paths

| Component       | Class or Path              | Responsibility                               |
|-----------------|----------------------------|----------------------------------------------|
| Workspace root  | `Constants.TEMP_DIRECTORY` | Default temp location, currently `data/temp` |
| Cleanup service | `WorkspaceCleanupService`  | Removes stale workspaces on a schedule       |

### How workspaces are used

1. The controller creates a unique UUID job directory under `data/temp/`.
2. The encoder writes the input file, `frames/`, and `output.mp4` inside that directory.
3. The decoder writes the uploaded video, `extracted_frames/`, and the reconstructed file inside that directory.
4. The controller streams the response to the client and deletes the workspace in a `finally` block.
5. The cleanup service removes any workspace that survives beyond the expiry threshold.

### Cleanup behavior

| Topic               | Behavior                                                                                                                                       |
|---------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Immediate deletion  | Controller endpoints stream the generated output to the client and delete the job workspace as soon as the response body has finished sending  |
| Schedule (fallback) | `WorkspaceCleanupService` runs every 300,000 ms (5 minutes) and removes directories older than 10 minutes as a safety net for failed requests  |
| Deletion            | Uses `FileSystemUtils.deleteRecursively` in both the controller and the scheduler                                                              |
| Failure handling    | Deletion errors are silently ignored in both immediate and scheduled cleanup paths                                                             |

### Practical notes

- Workspace names are UUID-based to guarantee uniqueness per request.
- The scheduled cleanup is a fallback for requests that fail before the controller's `finally` block runs (process
  crash, OOM, etc.).

## End-to-End Flow

```
Encode request
──────────────
Controller → CodecService.encode
  EncodePipeline.encode
    Files.readAllBytes              → byte[]
    PayloadFrameBuilder.build       → List<PayloadFrame>
    new MetadataFrame(...)
    ImageGenerator.metadataToImage  → BufferedImage
    ImageGenerator.payloadToImage   → List<BufferedImage>
    return ImageFrameSet
  VideoEncoder.encode
    ImageIOCodec.write              → frame_000000.png … frame_NNNNNN.png
    FFmpegExecutor.execute          → output.mp4
  StreamingResponseBody → client
  finally: delete job directory

Decode request
──────────────
Controller → CodecService.decode
  VideoDecoder.decode
    FFmpegExecutor.execute          → extracted_frames/frame_000000.png …
    ImageIOCodec.read               → ImageFrameSet
  DecodePipeline.decode
    RasterCodec.deserialize         → byte[] per frame
    MetadataFrameCodec.deserialize  → MetadataFrame
    PayloadFrameCodec.deserialize   → List<PayloadFrame>
    validate count and order
    Arrays.copyOf + Files.write     → original file
  StreamingResponseBody → client
  finally: delete job directory
```
