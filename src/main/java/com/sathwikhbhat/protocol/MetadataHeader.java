package com.sathwikhbhat.protocol;

public record MetadataHeader(String fileName, long fileSize, int totalPayloadFrames) {
}
