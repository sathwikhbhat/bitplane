package com.sathwikhbhat.bitplane.protocol.codec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwikhbhat.bitplane.protocol.frame.MetadataFrame;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MetadataFrameCodec {

    private static final ObjectMapper mapper = new ObjectMapper();

    public byte[] serialize(MetadataFrame metadataFrame) throws JsonProcessingException {
        byte[] metadataBytes = mapper.writeValueAsBytes(metadataFrame);

        return ByteBuffer
                .allocate(Integer.BYTES + metadataBytes.length)
                .putInt(metadataBytes.length)
                .put(metadataBytes)
                .array();
    }

    public MetadataFrame deserialize(byte[] serializedMetadataFrame) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(serializedMetadataFrame);
        int metadataLength = buffer.getInt();

        if (metadataLength < 0 || metadataLength > buffer.remaining()) {
            throw new IllegalArgumentException("Invalid metadata length");
        }

        byte[] metadataBytes = new byte[metadataLength];
        buffer.get(metadataBytes);

        return mapper.readValue(metadataBytes, MetadataFrame.class);
    }
}
