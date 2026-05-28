package com.sathwikhbhat.image.raster;

import com.sathwikhbhat.constants.ImageConstants;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class RasterCodec {

    public BufferedImage serialize(byte[] data) {
        BufferedImage image = new BufferedImage(ImageConstants.WIDTH, ImageConstants.HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();

        int index = 0;
        for (int y = 0; y < ImageConstants.HEIGHT && index < data.length; y++) {
            for (int x = 0; x < ImageConstants.WIDTH && index < data.length; x++, index++) {
                int value = data[index] & 0xFF;
                raster.setSample(x, y, 0, value);
            }
        }

        return image;
    }

    public byte[] deserialize(BufferedImage image, int length) {
        Raster raster = image.getRaster();
        byte[] data = new byte[length];

        int index = 0;
        for (int y = 0; y < image.getHeight() && index < length; y++) {
            for (int x = 0; x < image.getWidth() && index < length; x++) {
                int value = raster.getSample(x, y, 0);
                data[index++] = (byte) value;
            }
        }

        return data;
    }
}
