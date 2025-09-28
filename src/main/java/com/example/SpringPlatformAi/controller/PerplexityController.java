package com.example.SpringPlatformAi.controller;

import com.example.SpringPlatformAi.service.PerplexityService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PerplexityController {

    private final PerplexityService perplexityService;

    public PerplexityController(PerplexityService perplexityService) {
        this.perplexityService = perplexityService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String prompt) {
        return perplexityService.chat(prompt);
    }

    @GetMapping("/generate-image-uri")
    public String generateImage(@RequestParam String prompt) {
        return perplexityService.generateImageDataUri(prompt);
    }
}
