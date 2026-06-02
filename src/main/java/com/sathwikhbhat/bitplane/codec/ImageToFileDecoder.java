package com.sathwikhbhat.bitplane.codec;

import com.sathwikhbhat.bitplane.image.raster.RasterCodec;
import com.sathwikhbhat.bitplane.protocol.codec.MetadataFrameCodec;
import com.sathwikhbhat.bitplane.protocol.codec.PayloadFrameCodec;
import com.sathwikhbhat.bitplane.protocol.frame.MetadataFrame;
import com.sathwikhbhat.bitplane.protocol.frame.PayloadFrame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageToFileDecoder {

    private static final Logger log = LoggerFactory.getLogger(ImageToFileDecoder.class);

    private final RasterCodec rasterCodec = new RasterCodec();
    private final MetadataFrameCodec metadataFrameCodec = new MetadataFrameCodec();
    private final PayloadFrameCodec payloadFrameCodec = new PayloadFrameCodec();

    public Path decode(ImageFrameSet imageFrameSet, Path jobDirectory) throws IOException {
        BufferedImage metadataFrameImage = imageFrameSet.metadataImage();
        List<BufferedImage> payloadFrameImages = imageFrameSet.payloadImages();

        byte[] metadataFrameBytes = rasterCodec.deserialize(metadataFrameImage);
        MetadataFrame metadataFrame = metadataFrameCodec.deserialize(metadataFrameBytes);

        validatePayloadFrameCount(metadataFrame, payloadFrameImages.size());

        List<PayloadFrame> payloadFrames = new ArrayList<>();
        for (BufferedImage payloadFrameImage : payloadFrameImages) {
            byte[] payloadFrameBytes = rasterCodec.deserialize(payloadFrameImage);
            PayloadFrame payloadFrame = payloadFrameCodec.deserialize(payloadFrameBytes);
            payloadFrames.add(payloadFrame);
        }

        payloadFrames.sort(Comparator.comparingInt(PayloadFrame::frameIndex));
        validatePayloadFrameOrder(payloadFrames);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (PayloadFrame payloadFrame : payloadFrames) {
            outputStream.write(payloadFrame.payload());
        }

        byte[] originalFileBytes = Arrays.copyOf(outputStream.toByteArray(), (int) metadataFrame.fileSize());

        Files.createDirectories(jobDirectory);
        Path outputPath =
                jobDirectory.resolve(metadataFrame.fileName().replace('/', '_').replace('\\', '_'));
        Files.write(outputPath, originalFileBytes);

        log.debug("Decoded file: {} ({} bytes)", outputPath.toAbsolutePath(), originalFileBytes.length);

        return outputPath;
    }

    private void validatePayloadFrameCount(MetadataFrame metadataFrame, int actualFrames) {
        int expectedFrames = metadataFrame.totalPayloadFrames();

        if (expectedFrames < 0) {
            throw new IllegalStateException("Invalid payload frame count in metadata: " + expectedFrames);
        }

        if (actualFrames != expectedFrames) {
            throw new IllegalStateException(
                    "Payload frame count mismatch. Expected " + expectedFrames + ", found " + actualFrames);
        }
    }

    private void validatePayloadFrameOrder(List<PayloadFrame> payloadFrames) {
        for (int i = 0; i < payloadFrames.size(); i++) {
            int frameIndex = payloadFrames.get(i).frameIndex();

            if (frameIndex != i) {
                throw new IllegalStateException(
                        "Invalid payload frame sequence. Expected frame " + i + ", found " + frameIndex);
            }
        }
    }
}
