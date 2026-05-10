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

        Dimension dimension = new Dimension(image.getWidth(), image.getHeight());

        byte[] extracted = new byte[dimension.height * dimension.width];

        int index = 0;
        for (int y = 0; y < dimension.height; y++) {
            for (int x = 0; x < dimension.width; x++) {
                int value = raster.getSample(x, y, 0);
                extracted[index++] = (byte) value;
            }
        }

        Files.write(Path.of(DECODED_FILE_PATH), extracted);
    }
}
