package com.sathwikhbhat.bitplane.image;

import com.sathwikhbhat.bitplane.constants.Constants;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public class ImageIOCodec {

    public void write(BufferedImage image, Path outputPath) throws IOException {
        ImageIO.write(image, Constants.FORMAT, outputPath.toFile());
    }

    public BufferedImage read(Path imagePath) throws IOException {
        return ImageIO.read(imagePath.toFile());
    }
}
