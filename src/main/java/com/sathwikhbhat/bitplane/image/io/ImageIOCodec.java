package com.sathwikhbhat.bitplane.image.io;

import com.sathwikhbhat.bitplane.constants.ImageConstants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImageIOCodec {

    public void write(BufferedImage image, Path outputPath) throws IOException {
        ImageIO.write(image, ImageConstants.FORMAT, outputPath.toFile());
    }

    public BufferedImage read(Path imagePath) throws IOException {
        return ImageIO.read(imagePath.toFile());
    }
}
