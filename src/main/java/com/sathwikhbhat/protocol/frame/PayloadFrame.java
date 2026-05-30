package com.sathwikhbhat.protocol.frame;

public record PayloadFrame(int frameIndex, byte[] payload) {
}
