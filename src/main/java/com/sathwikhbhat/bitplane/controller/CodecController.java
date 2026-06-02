package com.sathwikhbhat.bitplane.controller;

import com.sathwikhbhat.bitplane.constants.Constants;
import com.sathwikhbhat.bitplane.service.CodecService;
import io.github.sathwikhbhat.apiexecutiontracker.annotation.TrackExecutionTime;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@TrackExecutionTime
@RestController
@RequestMapping("/api/v1/codec")
public class CodecController {

    private final CodecService codecService;

    public CodecController(CodecService codecService) {
        this.codecService = codecService;
    }

    @PostMapping("/encode")
    public ResponseEntity<Resource> encode(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFileName = getOriginalFileName(file);
        Path jobDirectory = createJobDirectory();
        Path tempFile = jobDirectory.resolve("input.bin");

        file.transferTo(tempFile);

        Path videoPath = codecService.encode(tempFile, originalFileName, jobDirectory);
        Resource resource = new FileSystemResource(videoPath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"output.mp4\"")
                .contentType(MediaType.valueOf("video/mp4"))
                .contentLength(Files.size(videoPath))
                .body(resource);
    }

    @PostMapping("/decode")
    public ResponseEntity<Resource> decode(@RequestParam("video") MultipartFile video) throws IOException {
        Path jobDirectory = createJobDirectory();
        Path tempVideo = jobDirectory.resolve("input.mp4");

        video.transferTo(tempVideo);

        Path decodedFile = codecService.decode(tempVideo, jobDirectory);
        Resource resource = new FileSystemResource(decodedFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + decodedFile.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(decodedFile))
                .body(resource);
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
}
