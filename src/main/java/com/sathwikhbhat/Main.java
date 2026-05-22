package com.sathwikhbhat;

import com.sathwikhbhat.codec.decoder.ImageToFileDecoder;
import com.sathwikhbhat.codec.encoder.FileToImageEncoder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sathwikhbhat.constants.ImageConstants.ENCODED_IMAGE_PATH;
import static com.sathwikhbhat.constants.ImageConstants.INPUT_FILE_NAME;

public class Main {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get(INPUT_FILE_NAME);
        new FileToImageEncoder().encode(inputFile);

        System.out.println();

        Path encodedFile = Paths.get(ENCODED_IMAGE_PATH);
        new ImageToFileDecoder().decode(encodedFile);
    }
}
