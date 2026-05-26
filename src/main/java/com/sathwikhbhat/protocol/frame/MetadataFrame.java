package com.sathwikhbhat.protocol.frame;

public record MetadataFrame(String fileName, long fileSize, int totalPayloadFrames) {
}
