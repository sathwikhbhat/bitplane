package com.sathwikhbhat.encoder;

import com.sathwikhbhat.util.ImageDimensionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.sathwikhbhat.constants.ImageConstants.ENCODED_IMAGE_PATH;


public class FileToImageEncoder {
    public void encode(Path inputFile) throws IOException {
        byte[] data = Files.readAllBytes(inputFile);
        System.out.println("File read successfully. Size: " + data.length + " bytes");

        if (data.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        Dimension dimension = ImageDimensionUtil.calculateDimensions(data.length);
        System.out.println("Calculated image dimensions: " + dimension.width + "x" + dimension.height);

        BufferedImage image = new BufferedImage(
                dimension.width,
                dimension.height,
                BufferedImage.TYPE_BYTE_GRAY
        );
        WritableRaster raster = image.getRaster();

        int index = 0;
        for (int y = 0; y < dimension.height && index < data.length; y++) {
            for (int x = 0; x < dimension.width && index < data.length; x++, index++) {
                int value = data[index] & 0xFF;
                raster.setSample(x, y, 0, value);
            }
        }

        Path outputFile = Path.of(ENCODED_IMAGE_PATH);
        ImageIO.write(image, "png", outputFile.toFile());

        System.out.println("Image saved successfully to " + outputFile.toAbsolutePath());
    }
}
