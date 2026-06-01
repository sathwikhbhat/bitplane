package com.sathwikhbhat.bitplane.controller;

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
import java.nio.file.InvalidPathException;

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
        Path tempFile = Files.createTempFile("upload", originalFileName);
        tempFile.toFile().deleteOnExit();

        file.transferTo(tempFile);

        Path videoPath = codecService.encode(tempFile, originalFileName);
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
        Path tempVideo = Files.createTempFile("video", ".mp4");
        tempVideo.toFile().deleteOnExit();

        video.transferTo(tempVideo);

        Path decodedFile = codecService.decode(tempVideo);
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

        if (!StringUtils.hasText(originalFileName)) {
            return "uploaded-file";
        }

        try {
            Path fileName = Path.of(originalFileName.replace('\\', '/')).getFileName();

            if (fileName != null && StringUtils.hasText(fileName.toString())) {
                return fileName.toString();
            }
        } catch (InvalidPathException ignored) {
        }

        return "uploaded-file";
    }
}
