package com.example.callrouter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class RedisDebugController {
    private final RedisTemplate<String, String> redis;

    @PostMapping("/debug/put")
    public void put(@RequestParam String key, @RequestParam String value) {
        redis.opsForValue().set(key, value, Duration.ofMinutes(30));
    }

    @GetMapping("/debug/get")
    public String get(@RequestParam String key) {
        return redis.opsForValue().get(key);
    }
}

