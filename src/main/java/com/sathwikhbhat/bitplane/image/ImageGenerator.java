package com.sathwikhbhat.bitplane.image;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sathwikhbhat.bitplane.frame.codec.MetadataFrameCodec;
import com.sathwikhbhat.bitplane.frame.codec.PayloadFrameCodec;
import com.sathwikhbhat.bitplane.frame.model.MetadataFrame;
import com.sathwikhbhat.bitplane.frame.model.PayloadFrame;
import java.awt.image.BufferedImage;

public class ImageGenerator {

    private final RasterCodec rasterCodec = new RasterCodec();
    private final MetadataFrameCodec metadataFrameCodec = new MetadataFrameCodec();
    private final PayloadFrameCodec payloadFrameCodec = new PayloadFrameCodec();

    public BufferedImage metadataToImage(MetadataFrame frame) throws JsonProcessingException {
        return rasterCodec.serialize(metadataFrameCodec.serialize(frame));
    }

    public BufferedImage payloadToImage(PayloadFrame frame) {
        return rasterCodec.serialize(payloadFrameCodec.serialize(frame));
    }
}
