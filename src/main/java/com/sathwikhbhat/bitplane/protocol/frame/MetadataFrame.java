package com.sathwikhbhat.bitplane.protocol.frame;

public record MetadataFrame(String fileName, long fileSize, int totalPayloadFrames) {
}
