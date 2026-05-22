package com.sathwikhbhat.protocol.header;

public record MetadataHeader(String fileName, long fileSize, int totalPayloadFrames) {
}
