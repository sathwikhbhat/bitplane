package com.sathwikhbhat.bitplane.service;

import com.sathwikhbhat.bitplane.constants.Constants;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Service
public class WorkspaceCleanupService {

    @Scheduled(fixedRate = 300000)
    public void cleanupStaleWorkspaces() throws IOException {
        Path tempDirectory = Constants.TEMP_DIRECTORY;

        if (!Files.exists(tempDirectory)) {
            return;
        }

        Instant cutoff = Instant.now().minusMillis(Constants.MAX_AGE_MILLIS);

        try (var workspaces = Files.list(tempDirectory)) {
            workspaces
                    .filter(Files::isDirectory)
                    .filter(workspace -> isOlderThan(workspace, cutoff))
                    .forEach(this::deleteWorkspace);
        }
    }

    private boolean isOlderThan(Path workspace, Instant cutoff) {
        try {
            return Files.getLastModifiedTime(workspace).toInstant().isBefore(cutoff);
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteWorkspace(Path workspace) {
        try {
            FileSystemUtils.deleteRecursively(workspace);
        } catch (IOException ignored) {
        }
    }
}
