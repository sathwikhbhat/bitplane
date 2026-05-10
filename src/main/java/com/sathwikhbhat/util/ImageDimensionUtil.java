package com.sathwikhbhat.util;

import java.awt.*;

public class ImageDimensionUtil {
    private ImageDimensionUtil() {
        /* This utility class should not be instantiated */
    }

    public static Dimension calculateDimensions(int totalBytes) {
        int width = (int) Math.ceil(Math.sqrt(totalBytes));
        int height = (int) Math.ceil((double) totalBytes / width);

        return new Dimension(width, height);
    }
}
