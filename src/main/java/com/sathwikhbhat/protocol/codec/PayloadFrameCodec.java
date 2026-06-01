package com.sathwikhbhat.protocol.codec;

import com.sathwikhbhat.protocol.frame.PayloadFrame;

import java.nio.ByteBuffer;

public class PayloadFrameCodec {

    public byte[] serialize(PayloadFrame payloadFrame) {
        byte[] payload = payloadFrame.payload();

        return ByteBuffer
                .allocate(Integer.BYTES + payload.length)
                .putInt(payloadFrame.frameIndex())
                .put(payload)
                .array();
    }

    public PayloadFrame deserialize(byte[] serializedPayloadFrame) {
        ByteBuffer buffer = ByteBuffer.wrap(serializedPayloadFrame);

        if (buffer.remaining() < Integer.BYTES) {
            throw new IllegalArgumentException("Invalid payload frame");
        }

        int frameIndex = buffer.getInt();

        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);

        return new PayloadFrame(frameIndex, payload);
    }
}
