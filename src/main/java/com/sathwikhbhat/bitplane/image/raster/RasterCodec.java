package com.sathwikhbhat.bitplane.image.raster;

import com.sathwikhbhat.bitplane.constants.Constants;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class RasterCodec {

    public BufferedImage serialize(byte[] data) {
        BufferedImage image = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();

        int index = 0;
        for (int y = 0; y < Constants.HEIGHT && index < data.length; y++) {
            for (int x = 0; x < Constants.WIDTH && index < data.length; x++, index++) {
                int value = data[index] & 0xFF;
                raster.setSample(x, y, 0, value);
            }
        }

        return image;
    }

    public byte[] deserialize(BufferedImage image) {
        Raster raster = image.getRaster();
        byte[] data = new byte[image.getWidth() * image.getHeight()];

        int index = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int value = raster.getSample(x, y, 0);
                data[index++] = (byte) value;
            }
        }

        return data;
    }
}
