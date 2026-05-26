package com.sathwikhbhat.protocol.codec;

import com.sathwikhbhat.protocol.frame.PayloadFrame;
import com.sathwikhbhat.util.ByteUtil;

import java.nio.ByteBuffer;

public class PayloadFrameCodec {

    public byte[] serialize(PayloadFrame payloadFrame) {
        byte[] frameIndexBytes = ByteUtil.intToBytes(payloadFrame.frameIndex());
        byte[] payload = payloadFrame.payload();

        int totalPayloadFrameSize = frameIndexBytes.length + payload.length;

        byte[] serializedPayloadFrame = new byte[totalPayloadFrameSize];

        int offset = 0;

        System.arraycopy(frameIndexBytes, 0, serializedPayloadFrame, offset, frameIndexBytes.length);
        offset += frameIndexBytes.length;
        System.arraycopy(payload, 0, serializedPayloadFrame, offset, payload.length);

        return serializedPayloadFrame;
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
