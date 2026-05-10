package com.sathwikhbhat.encoder;

import com.sathwikhbhat.util.ImageDimensionUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileToImageEncoder {
    public void encode(Path inputFile) throws IOException {
        byte[] data = Files.readAllBytes(inputFile);
        System.out.println("File read successfully. Size: " + data.length + " bytes");

        Dimension dimension = ImageDimensionUtil.calculateDimensions(data.length);
        System.out.println("Calculated image dimensions: " + dimension.width + "x" + dimension.height);
    }
}
