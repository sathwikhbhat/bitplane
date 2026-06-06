package com.sathwikhbhat.bitplane.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFoundException() {
        return "error";
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public String handleFileSizeExceededException(Exception e, Model model) {
        log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        String message = """
                Your file exceeds the free tier limit (encode: 100 MB, decode: 300 MB).
                Upgrade to Bitplane Pro for ₹10,000/month only, or just split your file. Your call.
                """;
        model.addAttribute("errorMessage", message);
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

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsableException(AsyncRequestNotUsableException e) {
        log.error("Client disconnected: {}", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        model.addAttribute("errorMessage", "Something went wrong on our end. Please try again in a moment.");
        return "exception-error";
    }
}
