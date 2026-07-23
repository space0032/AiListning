package com.ailisting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    /**
     * Lua script for sliding window rate limiting.
     *
     * WHY LUA?
     * - Atomic operation: get + increment + expire happens in one step
     * - Prevents race conditions between multiple requests
     * - Redis executes Lua scripts atomically
     */
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(getLuaScript());
        script.setResultType(Long.class);
        return script;
    }

    private String getLuaScript() {
        return """
                local key = KEYS[1]
                local limit = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local current = tonumber(redis.call('GET', key) or '0')
                
                if current >= limit then
                    return 0
                else
                    current = redis.call('INCR', key)
                    if current == 1 then
                        redis.call('EXPIRE', key, window)
                    end
                    return 1
                end
                """;
    }
}