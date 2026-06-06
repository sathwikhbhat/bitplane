package com.sathwikhbhat.bitplane.controller;

import com.sathwikhbhat.bitplane.exception.FileSizeLimitExceededException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/error/file-size")
    public String fileSizeError(@RequestParam String operation) {
        throw new FileSizeLimitExceededException(operation);
    }
}
