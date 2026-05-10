package com.sathwikhbhat;

import com.sathwikhbhat.encoder.FileToImageEncoder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path inputFile = Paths.get("data/input/sample.txt");
        new FileToImageEncoder().encode(inputFile);
    }
}
