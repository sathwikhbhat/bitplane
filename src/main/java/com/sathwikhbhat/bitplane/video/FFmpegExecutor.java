package com.sathwikhbhat.bitplane.video;

import java.io.IOException;
import java.util.StringJoiner;

public class FFmpegExecutor {

    public void execute(ProcessBuilder processBuilder) throws IOException {
        Process process;

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IOException("Failed to start FFmpeg: " + command(processBuilder), e);
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException(
                        "FFmpeg failed with exit code " + exitCode + ": " + command(processBuilder));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFmpeg execution interrupted", e);
        }
    }

    private String command(ProcessBuilder processBuilder) {
        StringJoiner command = new StringJoiner(" ");
        processBuilder.command().forEach(command::add);
        return command.toString();
    }
}
