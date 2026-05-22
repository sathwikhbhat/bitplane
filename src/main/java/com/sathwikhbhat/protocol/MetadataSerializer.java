package com.sathwikhbhat.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwikhbhat.util.ByteUtil;

public class MetadataSerializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] serialize(MetadataHeader metadataHeader) throws JsonProcessingException {
        byte[] metadataBytes = mapper.writeValueAsBytes(metadataHeader);
        byte[] metadataLengthBytes = ByteUtil.intToBytes(metadataBytes.length);

        int totalMetadataFrameSize = metadataLengthBytes.length + metadataBytes.length;

        byte[] serializedMetadataFrame = new byte[totalMetadataFrameSize];

        int offset = 0;
        System.arraycopy(metadataLengthBytes, 0, serializedMetadataFrame, offset, metadataLengthBytes.length);
        offset += metadataLengthBytes.length;
        System.arraycopy(metadataBytes, 0, serializedMetadataFrame, offset, metadataBytes.length);

        return serializedMetadataFrame;
    }
}
