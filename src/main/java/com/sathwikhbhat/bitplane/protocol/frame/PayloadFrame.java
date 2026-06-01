package com.sathwikhbhat.bitplane.protocol.frame;

public record PayloadFrame(int frameIndex, byte[] payload) {
}
