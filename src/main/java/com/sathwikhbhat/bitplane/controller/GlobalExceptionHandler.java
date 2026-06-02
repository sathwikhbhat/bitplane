package com.sathwikhbhat.bitplane.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFoundException() {
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(Exception e, Model model) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        model.addAttribute("errorMessage", "The file exceeds the maximum allowed upload size of 5GB.");
        return "exception-error";
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        JsonProcessingException.class,
        MultipartException.class
    })
    public String handleCorruptedFileException(Exception e, Model model) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        model.addAttribute(
                "errorMessage",
                "The file you submitted appears to be corrupted or invalid. Please check the file and try again.");
        return "exception-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again in a moment.");
        return "exception-error";
    }
}
