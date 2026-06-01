package com.sathwikhbhat;

import com.sathwikhbhat.codec.FileToImageEncoder;
import com.sathwikhbhat.codec.ImageToFileDecoder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.sathwikhbhat.constants.ImageConstants.INPUT_FILE_NAME;

public class Main {
    public static void main(String[] args) throws IOException {
//        Path inputFile = Paths.get(INPUT_FILE_NAME);
//        new FileToImageEncoder().encode(inputFile);
//
//        System.out.println();
//
//        Path encodedFile = Paths.get(ENCODED_IMAGE_PATH);
//        new ImageToFileDecoder().decode(encodedFile);

        Path inputFile = Paths.get(INPUT_FILE_NAME);
        new ImageToFileDecoder().decode(new FileToImageEncoder().encode(inputFile));
    }
}
