package com.sathwikhbhat.video;

final class FrameFileName {
    private static final String FRAME_PATTERN = "frame_%06d.png";

    private FrameFileName() {
        /* This utility class should not be instantiated */
    }

    static String atIndex(int index) {
        return String.format(FRAME_PATTERN, index);
    }

    static String pattern() {
        return FRAME_PATTERN;
    }
}
