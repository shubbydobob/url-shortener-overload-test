package com.project.url_shortener.ShortUrl.Controller;

import com.project.url_shortener.ShortUrl.Service.ShortUrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;




@RestController
@RequestMapping("/api/url")
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService){
        this.shortUrlService = shortUrlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<Map<String, String>> shortenUrl(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("originalUrl");
        String shortCode = shortUrlService.generateShortUrl(originalUrl);
        return ResponseEntity.ok(Map.of("shortCode", shortCode));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrl = shortUrlService.getOriginalUrl(shortCode);
        return originalUrl.map(url -> ResponseEntity.ok(Map.of("originalUrl", url)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}




