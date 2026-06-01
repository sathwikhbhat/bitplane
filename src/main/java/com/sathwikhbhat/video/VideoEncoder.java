package com.sathwikhbhat.video;

import com.sathwikhbhat.codec.ImageFrameSet;
import com.sathwikhbhat.constants.ImageConstants;
import com.sathwikhbhat.image.io.ImageIOCodec;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VideoEncoder {

    private final ImageIOCodec imageIOCodec = new ImageIOCodec();

    public Path encode(ImageFrameSet imageFrameSet) throws IOException {
        BufferedImage metadataFrameImage = imageFrameSet.metadataImage();
        List<BufferedImage> payloadFrameImages = imageFrameSet.payloadImages();

        Files.createDirectories(ImageConstants.FRAME_DIRECTORY);

        // Frame 0 = Metadata
        imageIOCodec.write(
                metadataFrameImage,
                ImageConstants.FRAME_DIRECTORY.resolve("frame_000000.png")
        );

        // Frame 1+ = Payload Frames
        for (int i = 0; i < payloadFrameImages.size(); i++) {
            imageIOCodec.write(
                    payloadFrameImages.get(i),
                    ImageConstants.FRAME_DIRECTORY.resolve(
                            String.format("frame_%06d.png", i + 1))
            );
        }

        return ImageConstants.OUTPUT_VIDEO;
    }
}
