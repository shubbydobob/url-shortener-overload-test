package com.project.url_shortener.ShortUrl.Service;

import com.project.url_shortener.ShortUrl.Domain.ShortUrl;
import com.project.url_shortener.ShortUrl.Repository.ShortUrlRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String CHAR_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final long CACHE_TTL = 3600; // 캐시 유지 시간 (1시간)

    public ShortUrlService(ShortUrlRepository shortUrlRepository, RedisTemplate<String, String> redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.redisTemplate = redisTemplate;
    }

    public String generateShortUrl(String originalUrl) {
        String shortCode;
        do {
            shortCode = generateRandomCode();
        } while (shortUrlRepository.findByShortCode(shortCode).isPresent());

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(originalUrl);
        shortUrlRepository.save(shortUrl);

        // Redis에 저장 ( 캐시 적용)
        redisTemplate.opsForValue().set(shortCode, originalUrl, CACHE_TTL, TimeUnit.SECONDS);
        System.out.println("단축 URL 저장 & Redis 캐싱: " + shortCode + " -> " + originalUrl);
        return shortCode;
    }

    public Optional<String> getOriginalUrl(String shortCode) {
        // Redis에서 먼저 조회
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);
        if (cachedUrl != null) {
            System.out.println("✅ 캐싱된 데이터 사용: " + cachedUrl);
            return Optional.of(cachedUrl);
        }

        // Redis에 없으면 MySQL에서 조회하고 Redis에 저장
        return shortUrlRepository.findByShortCode(shortCode).map(url -> {
            redisTemplate.opsForValue().set(shortCode, url.getOriginalUrl(), CACHE_TTL, TimeUnit.SECONDS);
            System.out.println("🆕 캐싱 저장: " + shortCode + " -> " + url.getOriginalUrl());
            return url.getOriginalUrl();
        });
    }


    private String generateRandomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHAR_SET.charAt(random.nextInt(CHAR_SET.length())));
        }
        return sb.toString();
    }
}
