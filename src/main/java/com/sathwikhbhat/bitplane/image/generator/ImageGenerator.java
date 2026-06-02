package com.sathwikhbhat.bitplane.image.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sathwikhbhat.bitplane.image.raster.RasterCodec;
import com.sathwikhbhat.bitplane.protocol.codec.MetadataFrameCodec;
import com.sathwikhbhat.bitplane.protocol.codec.PayloadFrameCodec;
import com.sathwikhbhat.bitplane.protocol.frame.MetadataFrame;
import com.sathwikhbhat.bitplane.protocol.frame.PayloadFrame;
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
