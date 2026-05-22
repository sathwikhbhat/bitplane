# Current Architecture Design - Building the Frame

## Core Idea

The system converts a file into multiple fixed-size grayscale image frames.

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

## Pipeline

```text
File
↓
Metadata + Payload Separation
↓
Frame Serialization
↓
Image Encoding
↓
Video Generation
```

## Architectural Principle

The system separates:

```text
metadata transport
```

from:

```text
payload transport
```

This removes:

* recursive sizing problems
* dynamic payload calculations
* unstable frame layouts
* packet dependency loops

## Frame Types

### Frame 0

Metadata Frame

Dedicated entirely to protocol metadata.

Contains:

* file name
* total file size
* total payload frame count

No payload data is stored here.

### Frame 1+

Payload Frames

Contain:

* frame index
* payload bytes

These are fixed-layout transport frames.

## Metadata Header

```java
public record MetadataHeader(
        String fileName,
        long fileSize,
        int totalPayloadFrames
) {
}
```

Purpose:

* reconstruction metadata
* payload reconstruction termination
* future protocol extensibility

Stored only in Frame 0.

## Payload Header

```java
public record PayloadHeader(
        int frameIndex
) {
}
```

Purpose:

* payload ordering
* future multithreaded decoding support

Stored in every payload frame.

## Metadata Frame Layout (Frame 0)

```text
[4 bytes metadataLength]
[metadataBytes]
[unused padding]
```

### Notes

* `metadataBytes` are serialized using Jackson.
* Remaining unused bytes in Frame 0 are ignored.
* Entire metadata must fit within one frame.
* If metadata exceeds frame capacity:

    * throw encoding exception

## Payload Frame Layout (Frame 1+)

```text
[4 bytes frameIndex]
[payloadBytes]
```

### Notes

* `frameIndex` is fixed-width.
* `frameIndex` uses 4 bytes (`int`).
* Remaining frame space is pure payload capacity.
* No payload size is stored per frame.

## Payload Capacity

### Total Frame Capacity

```text
1920 × 1080
=
2,073,600 bytes
```

### Payload Frame Capacity

```text
2,073,600 - 4
=
2,073,596 bytes
```

because:

* first 4 bytes store frame index

## Why Payload Size Was Removed

Earlier architecture stored:

```java
payloadSize
```

inside each payload frame.

This created recursive dependency problems because:

* payload size depended on header size
* header size depended on serialized metadata
* frame count calculations became unstable

The new architecture removes this completely.

## Reconstruction Logic

### Step 1

Decode Frame 0.

Extract:

* file name
* file size
* total payload frames

### Step 2

Decode payload frames.

Use:

```text
frameIndex
```

to arrange payload frames correctly.

### Step 3

Reconstruct bytes until:

```text
reconstructedBytes == fileSize
```

This removes the need for:

* per-frame payload size
* special handling for last frame

## Serializer Structure

### MetadataSerializer

Used only for Frame 0.

Responsibility:

```text
MetadataHeader → byte[]
```

Uses:

* Jackson serialization

### PayloadSerializer

Used for Frame 1+.

Responsibility:

```text
PayloadHeader + payload → byte[]
```

Uses:

* manual binary serialization
* direct byte array manipulation

No JSON is used for payload frames.

## Important Protocol Constraint

Metadata must fit inside one frame.

If serialized metadata exceeds metadata frame capacity:

```text
throw encoding exception
```

This is an intentional protocol simplification.
