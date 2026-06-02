package com.sathwikhbhat.bitplane.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFoundException() {
        return "error";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(Model model) {
        model.addAttribute("errorMessage", "The file exceeds the maximum allowed upload size of 5GB.");
        return "exception-error";
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        JsonProcessingException.class,
        MultipartException.class
    })
    public String handleCorruptedFileException(Model model) {
        model.addAttribute(
                "errorMessage",
                "The file you submitted appears to be corrupted or invalid. Please check the file and try again.");
        return "exception-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Model model) {
        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again in a moment.");
        return "exception-error";
    }
}
