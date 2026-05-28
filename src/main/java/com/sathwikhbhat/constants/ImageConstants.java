package com.sathwikhbhat.constants;

public final class ImageConstants {
    private ImageConstants() {
        /* This utility class should not be instantiated */
    }

    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;

    public static final int FRAME_BYTE_CAPACITY = WIDTH * HEIGHT;

    public static final String FORMAT = "png";

    public static final String INPUT_FILE_NAME = "data/input/sample.txt";
    public static final String ENCODED_IMAGE_PATH = "data/encoded/encoded.png";
}
