package com.sathwikhbhat.bitplane.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/exception-error-page")
    public String exceptionErrorPage() {
        return "exception-error";
    }
}
