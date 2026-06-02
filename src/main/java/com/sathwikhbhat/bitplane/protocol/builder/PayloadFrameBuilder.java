package com.sathwikhbhat.bitplane.protocol.builder;

import com.sathwikhbhat.bitplane.constants.Constants;
import com.sathwikhbhat.bitplane.protocol.frame.PayloadFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayloadFrameBuilder {

    public List<PayloadFrame> build(byte[] payload) {
        int maxPayloadSize = Constants.FRAME_BYTE_CAPACITY - Integer.BYTES;
        int totalFrames = (payload.length + maxPayloadSize - 1) / maxPayloadSize;

        List<PayloadFrame> payloadFrames = new ArrayList<>();

        for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
            int start = frameIndex * maxPayloadSize;
            int end = Math.min(start + maxPayloadSize, payload.length);

            byte[] chunk = Arrays.copyOfRange(payload, start, end);
            PayloadFrame payloadFrame = new PayloadFrame(frameIndex, chunk);

            payloadFrames.add(payloadFrame);
        }

        return payloadFrames;
    }
}
