package com.sathwikhbhat.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwikhbhat.protocol.Metadata;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageToFileDecoder {
    public void decode(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        Raster raster = image.getRaster();
        System.out.println("Image read successfully");

        Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
        System.out.println("Image dimensions: " + dimension.width + "x" + dimension.height);

        byte[] protocolBytes = new byte[dimension.height * dimension.width];

        int index = 0;
        for (int y = 0; y < dimension.height; y++) {
            for (int x = 0; x < dimension.width; x++) {
                int value = raster.getSample(x, y, 0);
                protocolBytes[index++] = (byte) value;
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap(protocolBytes);
        long metadataLength = buffer.getLong();
        long dataLength = buffer.getLong();
        System.out.println("Metadata Length = " + metadataLength + "\nData Length = " + dataLength);

        byte[] metadataBytes = new byte[(int) metadataLength];
        buffer.get(metadataBytes);

        ObjectMapper mapper = new ObjectMapper();
        Metadata metadata = mapper.readValue(metadataBytes, Metadata.class);
        System.out.println("Metadata extracted: " + metadata);

        byte[] dataBytes = new byte[(int) dataLength];
        buffer.get(dataBytes);

        Path outputFile = Path.of("data/decoded/" + metadata.fileName());
        Files.write(outputFile, dataBytes);
        System.out.println("File saved successfully to " + outputFile.toAbsolutePath());
    }
}
