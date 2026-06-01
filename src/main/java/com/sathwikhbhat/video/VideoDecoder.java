package com.sathwikhbhat.video;

import com.sathwikhbhat.codec.ImageFrameSet;
import com.sathwikhbhat.constants.PathConstants;
import com.sathwikhbhat.image.io.ImageIOCodec;
import com.sathwikhbhat.io.DirectoryUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VideoDecoder {

    private final FFmpegExecutor ffmpegExecutor = new FFmpegExecutor();
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public ImageFrameSet decode(Path videoPath) throws IOException {
        DirectoryUtil.clear(PathConstants.EXTRACTED_FRAME_DIRECTORY);
        Files.createDirectories(PathConstants.EXTRACTED_FRAME_DIRECTORY);

        System.out.println("Extracting frames from video: " + videoPath);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-nostdin",
                "-i",
                videoPath.toString(),
                "-start_number",
                "0",
                PathConstants.EXTRACTED_FRAME_DIRECTORY
                        .resolve(FrameFileName.pattern())
                        .toString());

        ffmpegExecutor.execute(processBuilder);

        List<Path> framePaths;

        try (var paths = Files.list(PathConstants.EXTRACTED_FRAME_DIRECTORY)) {
            framePaths = paths
                    .sorted()
                    .toList();
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
