package com.project.url_shortener.ShortUrl.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/")
    public String home() {
        return "Hello, World!" + port;
    }
    @GetMapping("/api/health")
    public String healthCheck() {
        return "OK";
    }
}
