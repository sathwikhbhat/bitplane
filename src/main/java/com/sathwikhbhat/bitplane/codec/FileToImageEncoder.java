package com.sathwikhbhat.bitplane.codec;

import com.sathwikhbhat.bitplane.image.generator.ImageGenerator;
import com.sathwikhbhat.bitplane.protocol.builder.PayloadFrameBuilder;
import com.sathwikhbhat.bitplane.protocol.frame.MetadataFrame;
import com.sathwikhbhat.bitplane.protocol.frame.PayloadFrame;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileToImageEncoder {

    private static final Logger log = LoggerFactory.getLogger(FileToImageEncoder.class);

    private final PayloadFrameBuilder payloadFrameBuilder = new PayloadFrameBuilder();
    private final ImageGenerator imageGenerator = new ImageGenerator();

    public ImageFrameSet encode(Path inputFile, String originalFileName) throws IOException {
        byte[] fileBytes = Files.readAllBytes(inputFile);

        log.debug("Encoding file: {} ({} bytes)", originalFileName, fileBytes.length);

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        List<PayloadFrame> payloadFrames = payloadFrameBuilder.build(fileBytes);
        MetadataFrame metadataFrame = new MetadataFrame(originalFileName, fileBytes.length, payloadFrames.size());

        BufferedImage metadataFrameImage = imageGenerator.metadataToImage(metadataFrame);

        List<BufferedImage> payloadFrameImages =
                payloadFrames.stream().map(imageGenerator::payloadToImage).toList();

        return new ImageFrameSet(metadataFrameImage, payloadFrameImages);
    }
}
