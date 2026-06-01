package com.sathwikhbhat.video;

import java.io.IOException;

public class FFmpegExecutor {

    public void execute(ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0)
                throw new RuntimeException("FFmpeg encoding failed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
