package com.sathwikhbhat.bitplane.service;

import com.sathwikhbhat.bitplane.codec.FileToImageEncoder;
import com.sathwikhbhat.bitplane.codec.ImageFrameSet;
import com.sathwikhbhat.bitplane.codec.ImageToFileDecoder;
import com.sathwikhbhat.bitplane.video.VideoDecoder;
import com.sathwikhbhat.bitplane.video.VideoEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CodecService {

    public Path decode(Path videoPath) throws IOException {
        ImageFrameSet imageFrameSet = new VideoDecoder().decode(videoPath);

        return new ImageToFileDecoder().decode(imageFrameSet);
    }

    public Path encode(Path inputFile) throws IOException {
        ImageFrameSet imageFrameSet = new FileToImageEncoder().encode(inputFile);

        return new VideoEncoder().encode(imageFrameSet);
    }

    public Path encode(Path inputFile, String originalFileName) throws IOException {
        ImageFrameSet imageFrameSet = new FileToImageEncoder().encode(inputFile, originalFileName);

        return new VideoEncoder().encode(imageFrameSet);
    }
}
