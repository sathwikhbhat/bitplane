package com.sathwikhbhat.bitplane.video;

import com.sathwikhbhat.bitplane.codec.ImageFrameSet;
import com.sathwikhbhat.bitplane.image.io.ImageIOCodec;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VideoDecoder {

    private final FFmpegExecutor ffmpegExecutor = new FFmpegExecutor();
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public ImageFrameSet decode(Path videoPath, Path jobDirectory) throws IOException {
        if (!Files.exists(videoPath)) {
            throw new IOException("Video file does not exist: " + videoPath);
        }

        Path extractedFrameDirectory = jobDirectory.resolve("extracted_frames");
        Files.createDirectories(extractedFrameDirectory);

        System.out.println("Extracting frames from video: " + videoPath);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-nostdin",
                "-v", "error",
                "-i",
                videoPath.toString(),
                "-start_number",
                "0",
                extractedFrameDirectory
                        .resolve(FrameFileName.pattern())
                        .toString());

        ffmpegExecutor.execute(processBuilder);

        List<Path> framePaths;

        try (var paths = Files.list(extractedFrameDirectory)) {
            framePaths = paths.sorted().toList();
        }

        if (framePaths.isEmpty()) {
            throw new RuntimeException("No frames extracted from video");
        }

        System.out.println("Extracted " + framePaths.size() + " image frame(s)");

        BufferedImage metadataImage = imageIOCodec.read(framePaths.getFirst());

        List<BufferedImage> payloadImages = new ArrayList<>();
        for (int i = 1; i < framePaths.size(); i++) {
            payloadImages.add(imageIOCodec.read(framePaths.get(i)));
        }

        return new ImageFrameSet(metadataImage, payloadImages);
    }

}
