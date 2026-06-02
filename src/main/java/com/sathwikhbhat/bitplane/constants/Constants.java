package com.sathwikhbhat.bitplane.constants;

import java.nio.file.Path;

public final class Constants {
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    public static final int FRAME_BYTE_CAPACITY = WIDTH * HEIGHT;
    public static final String FORMAT = "png";
    public static final Path TEMP_DIRECTORY = Path.of("data/temp");
    public static final long MAX_AGE_MILLIS = 10L * 60L * 1000L;

    private Constants() {
        /* This utility class should not be instantiated */
    }
}
