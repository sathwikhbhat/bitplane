package com.sathwikhbhat;

import com.sathwikhbhat.codec.FileToImageEncoder;
import com.sathwikhbhat.codec.ImageFrameSet;
import com.sathwikhbhat.codec.ImageToFileDecoder;
import com.sathwikhbhat.constants.ImageConstants;
import com.sathwikhbhat.video.VideoDecoder;
import com.sathwikhbhat.video.VideoEncoder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get(ImageConstants.INPUT_FILE_NAME);
        ImageFrameSet imageFrameSet = new FileToImageEncoder().encode(inputFile);
        Path videoPath = new VideoEncoder().encode(imageFrameSet);
        ImageFrameSet decodedImageFrameSet = new VideoDecoder().decode(videoPath);
        new ImageToFileDecoder().decode(decodedImageFrameSet);
    }
}
