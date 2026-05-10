package com.sathwikhbhat.decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sathwikhbhat.constants.ImageConstants.DECODED_FILE_PATH;

public class ImageToFileDecoder {
    public void decode(Path imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath.toFile());
        Raster raster = image.getRaster();
        System.out.println("Image read successfully");

        Dimension dimension = new Dimension(image.getWidth(), image.getHeight());
        System.out.println("Image dimensions: " + dimension.width + "x" + dimension.height);

        byte[] extracted = new byte[dimension.height * dimension.width];

        int index = 0;
        for (int y = 0; y < dimension.height; y++) {
            for (int x = 0; x < dimension.width; x++) {
                int value = raster.getSample(x, y, 0);
                extracted[index++] = (byte) value;
            }
        }

        Path outputFile = Path.of(DECODED_FILE_PATH);
        Files.write(outputFile, extracted);
        System.out.println("File saved successfully to " + outputFile.toAbsolutePath());
    }
}
