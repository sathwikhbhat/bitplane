package com.sathwikhbhat.bitplane.service;

import com.sathwikhbhat.bitplane.codec.FileToImageEncoder;
import com.sathwikhbhat.bitplane.codec.ImageFrameSet;
import com.sathwikhbhat.bitplane.codec.ImageToFileDecoder;
import com.sathwikhbhat.bitplane.video.VideoDecoder;
import com.sathwikhbhat.bitplane.video.VideoEncoder;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class CodecService {

    public Path decode(Path videoPath, Path jobDirectory) throws IOException {
        ImageFrameSet imageFrameSet = new VideoDecoder().decode(videoPath, jobDirectory);

        return new ImageToFileDecoder().decode(imageFrameSet, jobDirectory);
    }

    public Path encode(Path inputFile, String originalFileName, Path jobDirectory) throws IOException {
        ImageFrameSet imageFrameSet = new FileToImageEncoder().encode(inputFile, originalFileName);

        return new VideoEncoder().encode(imageFrameSet, jobDirectory);
    }
}
