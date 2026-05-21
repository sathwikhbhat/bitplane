package com.sathwikhbhat.protocol;

public record Frame(GlobalHeader globalHeader, FrameHeader frameHeader, byte[] payload) {
}
