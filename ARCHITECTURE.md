# Architecture

## Overview

Bitplane converts files into fixed-size grayscale image frames at 1920×1080, one byte per pixel. The implementation is
centered around four parts: the frame builder, the decoder, the video builder, and the workspace cleanup service. This
document explains how those pieces are built and how they fit together.

## Frame Builder

### What it does

The frame builder turns a file into one metadata frame and a sequence of payload frames.

### Main classes

| Component           | Class                                     | Responsibility                                                                         |
|---------------------|-------------------------------------------|----------------------------------------------------------------------------------------|
| File encoding       | `FileToImageEncoder`                      | Reads the input file, creates metadata and payload frames, and converts them to images |
| Payload chunking    | `PayloadFrameBuilder`                     | Splits file bytes into fixed-size payload chunks                                       |
| Metadata model      | `MetadataFrame`                           | Stores file name, file size, and total payload frame count                             |
| Payload model       | `PayloadFrame`                            | Stores frame index and payload bytes                                                   |
| Frame serialization | `MetadataFrameCodec`, `PayloadFrameCodec` | Converts frame objects to and from byte arrays                                         |
| Image conversion    | `ImageGenerator`, `RasterCodec`           | Converts serialized frame bytes to grayscale `BufferedImage` objects                   |

### Frame layout

| Frame          | Layout                                         |
|----------------|------------------------------------------------|
| Metadata frame | 4-byte length prefix, then JSON metadata bytes |
| Payload frame  | 4-byte frame index, then payload bytes         |

The total frame capacity is `Constants.FRAME_BYTE_CAPACITY`, which is `Constants.WIDTH × Constants.HEIGHT`.

### How it is built

1. `FileToImageEncoder.encode(Path inputFile)` reads the file bytes.
2. `PayloadFrameBuilder.build(byte[] payload)` splits the payload into chunks of `FRAME_BYTE_CAPACITY - Integer.BYTES`.
3. The encoder creates a `MetadataFrame` using the original file name, total file size, and payload frame count.
4. `MetadataFrameCodec` serializes the metadata frame as JSON with a length prefix.
5. `PayloadFrameCodec` serializes each payload frame with a 4-byte frame index prefix.
6. `RasterCodec` writes the serialized bytes into a grayscale `BufferedImage`.
7. `ImageGenerator` returns an `ImageFrameSet` containing the metadata image and the payload images.

### Important behavior

| Topic          | Behavior                                                              |
|----------------|-----------------------------------------------------------------------|
| Chunk size     | `FRAME_BYTE_CAPACITY - 4` bytes per payload frame                     |
| Frame ordering | Deterministic, starting at frame index 0                              |
| Memory model   | The current encoder reads the full input into memory                  |
| Error handling | Invalid input and serialization failures are propagated as exceptions |

### Example

For a 5 MiB file:

- Frame capacity: `1920 × 1080 = 2,073,600` bytes
- Payload per frame: `2,073,600 - 4 = 2,073,596` bytes
- Payload frames required: `ceil(5,242,880 / 2,073,596) = 3`

### Notes

- Metadata must fit in a single frame.
- The current implementation does not stream file reads.
- When writing frames to disk, temporary files and atomic rename are preferred to avoid partial output.

## Video Builder

### What it does

The video builder takes the generated frame images and assembles them into a video file.

### Main classes

| Component         | Class            | Responsibility                                 |
|-------------------|------------------|------------------------------------------------|
| Video assembly    | `VideoEncoder`   | Writes frame images to disk and invokes ffmpeg |
| Process execution | `FFmpegExecutor` | Starts ffmpeg and checks the exit status       |
| Frame naming      | `FrameFileName`  | Provides the frame file pattern used by ffmpeg |
| Image I/O         | `ImageIOCodec`   | Writes the generated images as PNG files       |

### How it is built

1. `VideoEncoder.encode(ImageFrameSet imageFrameSet, Path jobDirectory)` creates a `frames` directory inside the job
   directory.
2. The metadata image is written as `frame_000000.png`.
3. The payload images are written in order as `frame_000001.png`, `frame_000002.png`, and so on.
4. A `ProcessBuilder` is created for ffmpeg using the `frame_%06d.png` input pattern.
5. `FFmpegExecutor.execute(...)` starts ffmpeg and waits for completion.
6. If ffmpeg returns a non-zero exit code, the operation fails and the error is surfaced to the caller.

### ffmpeg command used

```bash
ffmpeg -y -nostdin -v error -framerate 30 -i frames/frame_%06d.png -c:v libx264rgb -pix_fmt rgb24 -crf 0 -preset veryslow output.mp4
```

### Notes

| Topic         | Behavior                                           |
|---------------|----------------------------------------------------|
| Assembly mode | File-based, not streaming                          |
| Codec choice  | `libx264rgb` with `-crf 0` to reduce pixel changes |
| Output        | `output.mp4` in the job directory                  |
| Trade-off     | Simple and debuggable, but heavier on disk I/O     |

## Decoder

### What it does

The decoder reconstructs the original file from a metadata image and a list of payload images.

### Main classes

| Component             | Class                                     | Responsibility                                   |
|-----------------------|-------------------------------------------|--------------------------------------------------|
| File decoding         | `ImageToFileDecoder`                      | Reconstructs the original file from frame images |
| Frame deserialization | `MetadataFrameCodec`, `PayloadFrameCodec` | Converts byte arrays back into frame objects     |
| Image conversion      | `RasterCodec`                             | Converts grayscale images back to byte arrays    |

### How it is built

1. `ImageToFileDecoder.decode(ImageFrameSet, Path jobDirectory)` reads the metadata image and payload images.
2. `RasterCodec.deserialize(...)` converts each image back into a byte array.
3. `MetadataFrameCodec.deserialize(...)` parses the metadata frame.
4. `PayloadFrameCodec.deserialize(...)` parses each payload frame.
5. The decoder validates the expected payload frame count.
6. Payload frames are sorted by `frameIndex`.
7. The payload bytes are concatenated and truncated to the original file size.
8. The reconstructed file is written to the job directory.

### Notes

| Topic      | Behavior                                         |
|------------|--------------------------------------------------|
| Ordering   | Strict, based on `frameIndex`                    |
| Validation | Fails if frame count or ordering is invalid      |
| Output     | Reconstructed file in the supplied job directory |

## Isolated Workspace and Cleanup

### What it does

Workspaces keep each operation isolated from the others and make cleanup predictable.

### Main classes and paths

| Component       | Class or Path              | Responsibility                               |
|-----------------|----------------------------|----------------------------------------------|
| Workspace root  | `Constants.TEMP_DIRECTORY` | Default temp location, currently `data/temp` |
| Cleanup service | `WorkspaceCleanupService`  | Removes stale workspaces on a schedule       |

### How workspaces are used

1. The caller creates a unique `jobDirectory`, usually under `data/temp`.
2. Input, frames, output, and logs can be stored in separate subdirectories inside that workspace.
3. The encoder and decoder operate inside the same workspace path for the duration of the job.
4. The video builder writes frame files and output video inside the workspace.
5. The cleanup service removes old workspaces after they expire.

### Cleanup behavior

| Topic               | Behavior                                                                                                                                          |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| Immediate deletion  | Controller endpoints stream the generated output to the client and delete the job workspace as soon as the response body has finished sending     |
| Schedule (fallback) | `WorkspaceCleanupService` still runs every 300000 ms and removes directories older than 10 minutes as a safety net for failed or crashed requests |
| Deletion            | Uses recursive deletion (controller uses FileSystemUtils.deleteRecursively; scheduler does the same)                                              |
| Failure handling    | Deletion errors are ignored in both immediate and scheduled cleanup paths                                                                         |

### Practical notes

- Workspace names should be unique, typically UUID based.
- Temporary files should be written atomically where possible.
- Logs can be kept inside the workspace for debugging until cleanup runs.
- Workspace names should be unique, typically UUID based.
- Controller endpoints now stream file responses and remove the workspace in a completion/finally hook so successful
  requests do not rely on the scheduled sweep.
- The scheduled cleanup service remains enabled as a fallback to reclaim workspaces for requests that fail before the
  controller's completion hook runs (process crash, OOM, etc.).
- Temporary files should be written atomically where possible.
- Logs can be kept inside the workspace for debugging until cleanup runs.

## End-to-End Flow

1. The caller creates a job directory.
2. `FileToImageEncoder` turns the input file into metadata and payload images.
3. `VideoEncoder` can assemble the images into a video.
4. `ImageToFileDecoder` reconstructs the original file from the images.
5. `WorkspaceCleanupService` removes stale job directories later.
