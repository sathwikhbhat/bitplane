package com.sathwikhbhat.encoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwikhbhat.protocol.Metadata;
import com.sathwikhbhat.util.ByteUtil;
import com.sathwikhbhat.util.ImageDimensionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sathwikhbhat.constants.ImageConstants.ENCODED_IMAGE_PATH;


public class FileToImageEncoder {
    public void encode(Path inputFile) throws IOException {
        byte[] fileBytes = Files.readAllBytes(inputFile);
        byte[] fileLengthBytes = ByteUtil.longToBytes(fileBytes.length);

        System.out.println("File read successfully. Size: " + fileBytes.length + " bytes");

        if (fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        Metadata metadata = new Metadata(inputFile.getFileName().toString(), fileBytes.length);

        System.out.println("Metadata created: " + metadata);

        ObjectMapper mapper = new ObjectMapper();
        String metadataJson = mapper.writeValueAsString(metadata);

        byte[] metadataBytes = metadataJson.getBytes(StandardCharsets.UTF_8);
        byte[] metadataLengthBytes = ByteUtil.longToBytes(metadataBytes.length);

        int totalPayloadSize =
                metadataLengthBytes.length
                        + fileLengthBytes.length
                        + metadataBytes.length
                        + fileBytes.length;

        Dimension dimension = ImageDimensionUtil.calculateDimensions(totalPayloadSize);
        System.out.println("Calculated image dimensions: " + dimension.width + "x" + dimension.height);

        byte[] payloadBytes = new byte[totalPayloadSize];
        int offset = 0;

        System.arraycopy(metadataLengthBytes, 0, payloadBytes, offset, metadataLengthBytes.length);
        offset += metadataLengthBytes.length;

        System.arraycopy(fileLengthBytes, 0, payloadBytes, offset, fileLengthBytes.length);
        offset += fileLengthBytes.length;

        System.arraycopy(metadataBytes, 0, payloadBytes, offset, metadataBytes.length);
        offset += metadataBytes.length;

        System.arraycopy(fileBytes, 0, payloadBytes, offset, fileBytes.length);

        BufferedImage image = new BufferedImage(
                dimension.width,
                dimension.height,
                BufferedImage.TYPE_BYTE_GRAY
        );
        WritableRaster raster = image.getRaster();

        int index = 0;
        for (int y = 0; y < dimension.height && index < payloadBytes.length; y++) {
            for (int x = 0; x < dimension.width && index < payloadBytes.length; x++, index++) {
                int value = payloadBytes[index] & 0xFF;
                raster.setSample(x, y, 0, value);
            }
        }

        Path outputFile = Path.of(ENCODED_IMAGE_PATH);
        ImageIO.write(image, "png", outputFile.toFile());

        System.out.println("Image saved successfully to " + outputFile.toAbsolutePath());
    }
}
