package com.sathwikhbhat.codec;

import com.sathwikhbhat.constants.ImageConstants;
import com.sathwikhbhat.image.raster.RasterCodec;
import com.sathwikhbhat.protocol.codec.MetadataFrameCodec;
import com.sathwikhbhat.protocol.codec.PayloadFrameCodec;
import com.sathwikhbhat.protocol.frame.MetadataFrame;
import com.sathwikhbhat.protocol.frame.PayloadFrame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ImageToFileDecoder {

    private final RasterCodec rasterCodec = new RasterCodec();
    private final MetadataFrameCodec metadataFrameCodec = new MetadataFrameCodec();
    private final PayloadFrameCodec payloadFrameCodec = new PayloadFrameCodec();

    public void decode(EncodedData encodedData) throws IOException {
        BufferedImage metadataFrameImage = encodedData.metadataImage();
        List<BufferedImage> payloadFrameImages = encodedData.payloadImages();

        System.out.println("\nBuffered Images read successfully");

        byte[] metadataFrameBytes = rasterCodec.deserialize(metadataFrameImage, ImageConstants.FRAME_BYTE_CAPACITY);
        MetadataFrame metadataFrame = metadataFrameCodec.deserialize(metadataFrameBytes);

        System.out.println(metadataFrame);

        System.out.println("=======================================================");

        List<PayloadFrame> payloadFrames = new ArrayList<>();
        for (BufferedImage payloadFrameImage : payloadFrameImages) {
            byte[] payloadFrameBytes = rasterCodec.deserialize(payloadFrameImage, ImageConstants.FRAME_BYTE_CAPACITY);
            PayloadFrame payloadFrame = payloadFrameCodec.deserialize(payloadFrameBytes);
            payloadFrames.add(payloadFrame);
        }

        payloadFrames.sort(Comparator.comparingInt(PayloadFrame::frameIndex));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (PayloadFrame payloadFrame : payloadFrames) {
            outputStream.write(payloadFrame.payload());
        }

        byte[] originalFileBytes = Arrays.copyOf(outputStream.toByteArray(), (int) metadataFrame.fileSize());

        System.out.println("File reconstructed successfully. Size: " + originalFileBytes.length + " bytes");

        Files.write(Path.of("data/decoded/" + metadataFrame.fileName()), originalFileBytes);

        System.out.println("File written to disk successfully at: " + Path.of("data/decoded/" + metadataFrame.fileName()).toAbsolutePath());
    }
}
