package com.sathwikhbhat.protocol;

import com.sathwikhbhat.util.ByteUtil;

public class PayloadSerializer {

    public byte[] serialize(PayloadHeader payloadHeader, byte[] payload) {
        byte[] frameIndexBytes = ByteUtil.intToBytes(payloadHeader.frameIndex());
        int totalPayloadFrameSize = frameIndexBytes.length + payload.length;

        byte[] serializedPayloadFrame = new byte[totalPayloadFrameSize];

        int offset = 0;

        System.arraycopy(frameIndexBytes, 0, serializedPayloadFrame, offset, frameIndexBytes.length);
        offset += frameIndexBytes.length;
        System.arraycopy(payload, 0, serializedPayloadFrame, offset, payload.length);

        return serializedPayloadFrame;
    }
}
