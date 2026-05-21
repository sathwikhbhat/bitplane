package com.sathwikhbhat.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwikhbhat.util.ByteUtil;

public class FrameSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] serialize(Frame frame) throws JsonProcessingException {
        byte[] globalHeaderBytes = mapper.writeValueAsBytes(frame.globalHeader());
        byte[] frameHeaderBytes = mapper.writeValueAsBytes(frame.frameHeader());

        byte[] globalHeaderLengthBytes = ByteUtil.intToBytes(globalHeaderBytes.length);
        byte[] frameHeaderLengthBytes = ByteUtil.intToBytes(frameHeaderBytes.length);

        int totalFrameSize =
                globalHeaderLengthBytes.length
                        + frameHeaderLengthBytes.length
                        + globalHeaderBytes.length
                        + frameHeaderBytes.length
                        + frame.payload().length;

        byte[] serializedFrame = new byte[totalFrameSize];
        int offset = 0;

        System.arraycopy(globalHeaderLengthBytes, 0, serializedFrame, offset, globalHeaderLengthBytes.length);
        offset += globalHeaderLengthBytes.length;

        System.arraycopy(frameHeaderLengthBytes, 0, serializedFrame, offset, frameHeaderLengthBytes.length);
        offset += frameHeaderLengthBytes.length;

        System.arraycopy(globalHeaderBytes, 0, serializedFrame, offset, globalHeaderBytes.length);
        offset += globalHeaderBytes.length;

        System.arraycopy(frameHeaderBytes, 0, serializedFrame, offset, frameHeaderBytes.length);
        offset += frameHeaderBytes.length;

        System.arraycopy(frame.payload(), 0, serializedFrame, offset, frame.payload().length);

        return serializedFrame;
    }
}
