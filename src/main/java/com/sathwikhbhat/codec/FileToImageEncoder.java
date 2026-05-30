package com.sathwikhbhat.codec;

import com.sathwikhbhat.constants.ImageConstants;
import com.sathwikhbhat.image.generator.ImageGenerator;
import com.sathwikhbhat.image.io.ImageIOCodec;
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
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public void encode(Path inputFile) throws IOException {
        byte[] fileBytes = Files.readAllBytes(inputFile);

        System.out.println("File read successfully. Size: " + fileBytes.length + " bytes");

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        List<PayloadFrame> payloadFrames = payloadFrameBuilder.build(fileBytes);
        MetadataFrame metadataFrame = new MetadataFrame(
                inputFile.getFileName().toString(),
                fileBytes.length,
                payloadFrames.size());

        System.out.println("Metadata created: " + metadataFrame);

        System.out.println("=======================================================");

        System.out.println("Initialising Rasterization...");

        BufferedImage metadataFrameImage = imageGenerator.metadataToImage(metadataFrame);

        List<BufferedImage> payloadFrameImages = payloadFrames.stream()
                .map(imageGenerator::payloadToImage)
                .toList();

        System.out.println("Rasterization completed");
        System.out.println("Total Payload Frames = " + payloadFrameImages.size());

        imageIOCodec.write(metadataFrameImage,
                Path.of(ImageConstants.ENCODED_IMAGE_PATH
                        .replace(".png", "_metadata.png")));

        for (int i = 0; i < payloadFrameImages.size(); i++) {
            imageIOCodec.write(payloadFrameImages.get(i),
                    Path.of(ImageConstants.ENCODED_IMAGE_PATH
                            .replace(".png", "_payload_" + i + ".png")));
        }
    }
}
