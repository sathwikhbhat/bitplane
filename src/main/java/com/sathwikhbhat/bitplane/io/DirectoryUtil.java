package com.sathwikhbhat.bitplane.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class DirectoryUtil {
    private DirectoryUtil() {
        /* This utility class should not be instantiated */
    }

    public static void clear(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths
                    .sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(directory))
                    .forEach(DirectoryUtil::delete);
        }
    }

    private static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete stale file: " + path, e);
        }
    }
}
