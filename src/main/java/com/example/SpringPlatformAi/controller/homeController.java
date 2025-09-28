package com.example.SpringPlatformAi.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class homeController {

    @GetMapping("/")
    public String home() {
        // Returns the Thymeleaf template "index.html"
        return "index";
    }
}
