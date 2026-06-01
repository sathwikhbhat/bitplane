package com.sathwikhbhat.codec;

import com.sathwikhbhat.image.generator.ImageGenerator;
import com.sathwikhbhat.protocol.builder.PayloadFrameBuilder;
import com.sathwikhbhat.protocol.frame.MetadataFrame;
import com.sathwikhbhat.protocol.frame.PayloadFrame;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class FileToImageEncoder {

    private final PayloadFrameBuilder payloadFrameBuilder = new PayloadFrameBuilder();
    private final ImageGenerator imageGenerator = new ImageGenerator();

    public ImageFrameSet encode(Path inputFile) throws IOException {
        byte[] fileBytes = Files.readAllBytes(inputFile);

        System.out.println("Encoding file: " + inputFile + " (" + fileBytes.length + " bytes)");

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        List<PayloadFrame> payloadFrames = payloadFrameBuilder.build(fileBytes);
        MetadataFrame metadataFrame = new MetadataFrame(
                inputFile.getFileName().toString(),
                fileBytes.length,
                payloadFrames.size());

        System.out.println("Created " + payloadFrames.size() + " payload frame(s)");

        BufferedImage metadataFrameImage = imageGenerator.metadataToImage(metadataFrame);

        List<BufferedImage> payloadFrameImages = payloadFrames.stream()
                .map(imageGenerator::payloadToImage)
                .toList();

        System.out.println("Rasterized metadata and payload frames");

        return new ImageFrameSet(metadataFrameImage, payloadFrameImages);
    }
}
