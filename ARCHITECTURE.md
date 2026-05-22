# Current Architecture Design - Building the Frame

## Core Idea

The system converts a file into multiple fixed-size image frames.

Each frame is:

```text
1920 × 1080 grayscale
```

which gives:

```text
2,073,600 bytes capacity per frame
```

because:

```text
1 pixel = 1 byte
```

# Pipeline

We now separate:

```text
metadata transport
```

from:

```text
payload transport
```

This removed:

* recursive sizing problems
* dynamic payload calculations
* unstable frame layouts

# Frame Types

## Frame 0

Metadata Frame

Dedicated entirely to protocol metadata.

Contains:

* file name
* total payload/file size
* total number of frames

No payload data is stored here.

## Frame 1+

Payload Frames

Contain only:

* frame index
* payload bytes

These are fixed-layout transport frames.

# Global Header

```java
public record GlobalHeader(
        String fileName,
        long fileSize,
        long totalFrames
) {
}
```

Stored ONLY in Frame 0.

# Frame Header

```java
public record FrameHeader(
        int frameIndex
) {
}
```

Stored in every payload frame.

Purpose:

* reconstruction ordering
* future multithreaded decoding support

# Metadata Frame Layout (Frame 0)

```text
[4 bytes metadataLength]
[metadataBytes]
[unused padding]
```

## Notes

* `metadataBytes` are serialized using Jackson.
* Remaining unused bytes in Frame 0 are ignored.
* Entire metadata must fit within one frame.

# Payload Frame Layout (Frame 1+)

```text
[4 bytes frameIndex]
[payloadBytes]
```

## Notes

* `frameIndex` is fixed-width.
* Remaining frame space is pure payload capacity.
* No payload size is stored per frame.

# Why Payload Size Was Removed

Earlier architecture stored:

```java
payloadSize
```

inside each frame header.

This created recursive dependency problems because:

* payload size depended on frame header size
* frame header size depended on serialized metadata

The new design removes this completely.

# Reconstruction Logic

Decoder:

* reads metadata from Frame 0
* gets:

    * file name
    * total file size
    * total frames

Then:

* decodes payload frames
* arranges them using `frameIndex`
* keeps reconstructing bytes until:

```text
reconstructedBytes == fileSize
```

This removes the need for:

* per-frame payload size
* special handling for last frame

# Serializer Structure

## MetadataFrameSerializer

Used only for Frame 0.

Responsibility:

```text
GlobalHeader → byte[]
```

Uses:

* Jackson serialization

## PayloadFrameSerializer

Used for Frame 1+.

Responsibility:

```text
frameIndex + payload → byte[]
```

Likely binary/manual serialization.

No JSON needed.

# Important Protocol Constraint

Metadata must fit inside one frame.

If serialized metadata exceeds metadata frame capacity:

```text
throw encoding exception
```

This is intentional protocol simplification.
