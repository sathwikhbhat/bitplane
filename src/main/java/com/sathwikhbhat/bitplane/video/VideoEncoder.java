package com.sathwikhbhat.bitplane.video;

import com.sathwikhbhat.bitplane.codec.ImageFrameSet;
import com.sathwikhbhat.bitplane.image.io.ImageIOCodec;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoEncoder {

    private static final Logger log = LoggerFactory.getLogger(VideoEncoder.class);

    private final FFmpegExecutor ffmpegExecutor = new FFmpegExecutor();
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public Path encode(ImageFrameSet imageFrameSet, Path jobDirectory) throws IOException {
        BufferedImage metadataFrameImage = imageFrameSet.metadataImage();
        List<BufferedImage> payloadFrameImages = imageFrameSet.payloadImages();

        Path frameDirectory = jobDirectory.resolve("frames");
        Path outputVideo = jobDirectory.resolve("output.mp4");

        Files.createDirectories(frameDirectory);

        // Frame 0 = Metadata
        imageIOCodec.write(metadataFrameImage, frameDirectory.resolve(FrameFileName.atIndex(0)));

        // Frame 1+ = Payload Frames
        for (int i = 0; i < payloadFrameImages.size(); i++) {
            imageIOCodec.write(payloadFrameImages.get(i), frameDirectory.resolve(FrameFileName.atIndex(i + 1)));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-nostdin",
                "-v",
                "error",
                "-framerate",
                "30",
                "-i",
                frameDirectory.resolve(FrameFileName.pattern()).toString(),
                "-c:v",
                "libx264rgb",
                "-pix_fmt",
                "rgb24",
                "-crf",
                "0",
                "-preset",
                "veryslow",
                outputVideo.toString());

        ffmpegExecutor.execute(processBuilder);

        log.debug("Encoded video: {}", outputVideo.toAbsolutePath());

        return outputVideo;
    }
}
