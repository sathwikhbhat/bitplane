package com.sathwikhbhat.video;

import com.sathwikhbhat.codec.ImageFrameSet;
import com.sathwikhbhat.constants.PathConstants;
import com.sathwikhbhat.image.io.ImageIOCodec;
import com.sathwikhbhat.io.DirectoryUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VideoEncoder {

    private final FFmpegExecutor ffmpegExecutor = new FFmpegExecutor();
    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public Path encode(ImageFrameSet imageFrameSet) throws IOException {
        BufferedImage metadataFrameImage = imageFrameSet.metadataImage();
        List<BufferedImage> payloadFrameImages = imageFrameSet.payloadImages();

        DirectoryUtil.clear(PathConstants.FRAME_DIRECTORY);
        Files.createDirectories(PathConstants.FRAME_DIRECTORY);
        Files.createDirectories(PathConstants.OUTPUT_VIDEO.getParent());

        System.out.println("Writing " + (payloadFrameImages.size() + 1) + " image frame(s)");

        // Frame 0 = Metadata
        imageIOCodec.write(
                metadataFrameImage,
                PathConstants.FRAME_DIRECTORY.resolve(FrameFileName.atIndex(0)));

        // Frame 1+ = Payload Frames
        for (int i = 0; i < payloadFrameImages.size(); i++) {
            imageIOCodec.write(
                    payloadFrameImages.get(i),
                    PathConstants.FRAME_DIRECTORY.resolve(FrameFileName.atIndex(i + 1)));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-nostdin",
                "-v", "error",
                "-framerate", "30",
                "-i", PathConstants.FRAME_DIRECTORY.resolve(FrameFileName.pattern()).toString(),
                "-c:v", "libx264rgb",
                "-pix_fmt", "rgb24",
                "-crf", "0",
                "-preset", "veryslow",
                PathConstants.OUTPUT_VIDEO.toString());

        ffmpegExecutor.execute(processBuilder);

        System.out.println("Encoded video: " + PathConstants.OUTPUT_VIDEO.toAbsolutePath());

        return PathConstants.OUTPUT_VIDEO;
    }
}
