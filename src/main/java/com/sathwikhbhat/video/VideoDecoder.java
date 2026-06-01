package com.sathwikhbhat.video;

import com.sathwikhbhat.codec.ImageFrameSet;
import com.sathwikhbhat.constants.ImageConstants;
import com.sathwikhbhat.image.io.ImageIOCodec;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VideoDecoder {

    private final FFmpegExecutor fFmpegExecutor = new FFmpegExecutor();
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public ImageFrameSet decode(Path videoPath) throws IOException {
        Files.createDirectories(ImageConstants.EXTRACTED_FRAME_DIRECTORY);

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-nostdin",
                "-i",
                videoPath.toString(),
                "-start_number",
                "0",
                ImageConstants.EXTRACTED_FRAME_DIRECTORY
                        .resolve("frame_%06d.png")
                        .toString());

        fFmpegExecutor.execute(processBuilder);

        List<Path> framePaths;

        try (var paths = Files.list(ImageConstants.EXTRACTED_FRAME_DIRECTORY)) {
            framePaths = paths
                    .sorted()
                    .toList();
        }

        if (framePaths.isEmpty())
            throw new RuntimeException("No frames extracted from video");

        BufferedImage metadataImage = imageIOCodec.read(framePaths.get(0));

        List<BufferedImage> payloadImages = new ArrayList<>();
        for (int i = 1; i < framePaths.size(); i++) {
            payloadImages.add(imageIOCodec.read(framePaths.get(i)));
        }

        return new ImageFrameSet(metadataImage, payloadImages);
    }

}
