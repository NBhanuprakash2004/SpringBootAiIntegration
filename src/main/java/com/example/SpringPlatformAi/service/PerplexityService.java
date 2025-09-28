package com.example.SpringPlatformAi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class PerplexityService {

    @Value("${perplexity.api.key}")
    private String apiKey;

    @Value("${perplexity.api.base-url}")
    private String baseUrl;
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;
    @Value("${stability.api.key}")
private String stabilityApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String prompt) {
        try {
            String jsonRequest = """
    {
      "model": "sonar-pro",
      "messages": [
        {"role": "user", "content": "%s"}
      ]
    }
    """.formatted(escapeJson(prompt));

    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                    .build();
    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("choices").get(0).path("message").path("content").asText("No answer found");
            } else {
                return "Error: " + response.statusCode() + " " + response.body();
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
    
    public String generateImageDataUri(String prompt) {
        try {
            String jsonRequest = """
                {
                  "text_prompts": [
                    { "text": "%s" }
                  ],
                  "cfg_scale": 7,
                  "height": 1024,
                  "width": 1024,
                  "samples": 1
                }
                """.formatted(escapeJson(prompt));
    
            String engineId = "stable-diffusion-xl-1024-v1-0";
    
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stability.ai/v1/generation/" + engineId + "/text-to-image"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + stabilityApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest, StandardCharsets.UTF_8))
                    .build();
    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() != 200) {
                return "Error: " + response.statusCode() + " " + response.body();
            }
    
            // Parse JSON
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode artifacts = root.path("artifacts");
    
            if (artifacts.isArray() && artifacts.size() > 0) {
                String base64Image = artifacts.get(0).path("base64").asText().trim();
    
                // Add data URI prefix
                if (!base64Image.startsWith("data:image")) {
                    base64Image = "data:image/png;base64," + base64Image;
                }
    
                return base64Image;
            }
    
            return "No image data found";
    
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
    
    
    
    
    public String listStabilityEngines() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stability.ai/v1/engines/list"))
                    .header("Authorization", "Bearer " + stabilityApiKey)
                    .GET()
                    .build();
    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Error: " + response.statusCode() + " " + response.body();
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }
    }
        

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"");
    }
}
