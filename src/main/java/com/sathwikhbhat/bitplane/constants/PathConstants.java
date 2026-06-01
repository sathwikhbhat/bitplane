package com.sathwikhbhat.bitplane.constants;

import java.nio.file.Path;

public final class PathConstants {
    private PathConstants() {
        /* This utility class should not be instantiated */
    }

    public static final Path INPUT_FILE = Path.of("data/input/sample.txt");

    public static final Path FRAME_DIRECTORY = Path.of("data/temp/frames");
    public static final Path EXTRACTED_FRAME_DIRECTORY = Path.of("data/temp/extracted");

    public static final Path OUTPUT_VIDEO = Path.of("data/output/output.mp4");
    public static final Path DECODED_DIRECTORY = Path.of("data/decoded");
}
