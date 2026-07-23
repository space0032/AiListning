package com.ailisting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter using sliding window algorithm.
 *
 * WHY THIS APPROACH?
 * - Per-IP rate limiting for public endpoints
 * - Per-user rate limiting for authenticated endpoints
 * - Uses Redis for distributed rate limiting (works across multiple instances)
 * - Lua script ensures atomic operations (no race conditions)
 *
 * RATE LIMIT TIERS:
 * - Public: 30 requests/minute
 * - Authenticated: 60 requests/minute
 * - AI Generation: 10 requests/minute (expensive operation)
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Value("${app.rate-limit.public:30}")
    private int publicRateLimit;

    @Value("${app.rate-limit.authenticated:60}")
    private int authenticatedRateLimit;

    @Value("${app.rate-limit.ai-generation:10}")
    private int aiGenerationRateLimit;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // Determine rate limit based on endpoint
        int limit = getRateLimit(requestUri, request);

        // Generate rate limit key
        String key = "rate_limit:" + getClientIdentifier(request, requestUri);

        // Check rate limit
        Long allowed = redisTemplate.execute(
                rateLimitScript,
                java.util.List.of(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds));

        if (allowed != null && allowed == 0) {
            log.warn("Rate limit exceeded for {}: {}", clientIp, requestUri);
            sendRateLimitExceeded(response, limit);
            return;
        }

        // Add rate limit headers
        addRateLimitHeaders(response, limit, key);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Don't rate limit these endpoints
        return path.startsWith("/actuator/")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register");
    }

    private int getRateLimit(String uri, HttpServletRequest request) {
        if (uri.contains("/ai/")) {
            return aiGenerationRateLimit;
        }
        if (request.getHeader("Authorization") != null) {
            return authenticatedRateLimit;
        }
        return publicRateLimit;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getClientIdentifier(HttpServletRequest request, String uri) {
        if (request.getHeader("Authorization") != null) {
            // For authenticated users, use user identifier
            // In production, extract username from JWT
            return "user:" + getClientIp(request);
        }
        return "ip:" + getClientIp(request);
    }

    private void addRateLimitHeaders(HttpServletResponse response, int limit, String key) {
        // Get current count for headers
        Object count = redisTemplate.opsForValue().get(key);
        int remaining = count != null ? Math.max(0, limit - (int) count) : limit;

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(windowSeconds));
    }

    private void sendRateLimitExceeded(HttpServletResponse response, int limit) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", "0");

        Map<String, Object> body = Map.of(
                "success", false,
                "message", "Rate limit exceeded. Please try again later.",
                "status", 429,
                "retryAfter", windowSeconds
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}