package com.sathwikhbhat.protocol.serializer;

import com.sathwikhbhat.protocol.frame.PayloadFrame;
import com.sathwikhbhat.util.ByteUtil;

public class PayloadFrameSerializer {

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
}
