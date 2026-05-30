package com.sathwikhbhat.image.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sathwikhbhat.image.raster.RasterCodec;
import com.sathwikhbhat.protocol.codec.MetadataFrameCodec;
import com.sathwikhbhat.protocol.codec.PayloadFrameCodec;
import com.sathwikhbhat.protocol.frame.MetadataFrame;
import com.sathwikhbhat.protocol.frame.PayloadFrame;

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
