package com.sathwikhbhat.bitplane.controller;

import com.sathwikhbhat.bitplane.constants.Constants;
import com.sathwikhbhat.bitplane.service.CodecService;
import io.github.sathwikhbhat.apiexecutiontracker.annotation.TrackExecutionTime;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@TrackExecutionTime
@RestController
@RequestMapping("/api/v1/codec")
public class CodecController {

    private final CodecService codecService;

    public CodecController(CodecService codecService) {
        this.codecService = codecService;
    }

    @PostMapping("/encode")
    public ResponseEntity<StreamingResponseBody> encode(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFileName = getOriginalFileName(file);
        Path jobDirectory = createJobDirectory();
        Path tempFile = jobDirectory.resolve("input.bin");
        try {
            file.transferTo(tempFile);

            Path videoPath = codecService.encode(tempFile, originalFileName, jobDirectory);

            StreamingResponseBody body = outputStream -> {
                try (var inputStream = Files.newInputStream(videoPath)) {
                    inputStream.transferTo(outputStream);
                } finally {
                    deleteWorkspace(jobDirectory);
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"output.mp4\"")
                    .contentType(MediaType.valueOf("video/mp4"))
                    .contentLength(Files.size(videoPath))
                    .body(body);
        } catch (IOException | RuntimeException e) {
            deleteWorkspace(jobDirectory);
            throw e;
        }
    }

    @PostMapping("/decode")
    public ResponseEntity<StreamingResponseBody> decode(@RequestParam("video") MultipartFile video) throws IOException {
        Path jobDirectory = createJobDirectory();
        Path tempVideo = jobDirectory.resolve("input.mp4");
        try {
            video.transferTo(tempVideo);

            Path decodedFile = codecService.decode(tempVideo, jobDirectory);

            StreamingResponseBody body = outputStream -> {
                try (var inputStream = Files.newInputStream(decodedFile)) {
                    inputStream.transferTo(outputStream);
                } finally {
                    deleteWorkspace(jobDirectory);
                }
            };

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + decodedFile.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(Files.size(decodedFile))
                    .body(body);
        } catch (IOException | RuntimeException e) {
            deleteWorkspace(jobDirectory);
            throw e;
        }
    }

    private String getOriginalFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        return StringUtils.hasText(originalFileName) ? originalFileName : "uploaded-file";
    }

    private Path createJobDirectory() throws IOException {
        Path jobDirectory = Constants.TEMP_DIRECTORY.resolve(UUID.randomUUID().toString());
        Files.createDirectories(jobDirectory);
        return jobDirectory;
    }

    private void deleteWorkspace(Path workspace) {
        try {
            FileSystemUtils.deleteRecursively(workspace);
        } catch (IOException ignored) {
        }
    }
}
