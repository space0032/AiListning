package com.ailisting.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Redis cache management (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class CacheController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Returns Redis cache key count and DB size (Admin only)")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Set<String> keys = redisTemplate.keys("*");
        Long dbSize = redisTemplate.execute(
                (RedisCallback<Long>) conn -> conn.commands().dbSize());

        return ResponseEntity.ok(Map.of(
                "totalKeys", keys != null ? keys.size() : 0,
                "dbSize", dbSize != null ? dbSize : 0L
        ));
    }

    @PostMapping("/clear")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear all caches", description = "Evicts all Redis cache entries (Admin only)")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return ResponseEntity.ok(Map.of("message", "All caches cleared"));
    }
}
