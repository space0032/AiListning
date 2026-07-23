# Milestone 7: Rate Limiting

## Overview
Implement rate limiting to protect the API from abuse, ensure fair usage, and maintain service stability.

## Implementation

### RateLimitFilter with Lua Script
```java
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    
    // Lua script for sliding window rate limiting
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local window = tonumber(ARGV[1])
        local limit = tonumber(ARGV[2])
        local now = tonumber(ARGV[3])
        
        -- Remove expired entries
        redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
        
        -- Count current requests
        local current = redis.call('ZCARD', key)
        
        if current < limit then
            -- Add current request
            redis.call('ZADD', key, now, now .. math.random())
            redis.call('EXPIRE', key, window / 1000)
            return {limit - current - 1, 0}
        else
            -- Get oldest request for retry-after
            local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
            local retryAfter = oldest[2] and (tonumber(oldest[2]) + window - now) / 1000 or 1
            return {0, math.ceil(retryAfter)}
        end
        """;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIdentifier(request);
        RateLimitTier tier = determineTier(request);
        RateLimitConfig.TierConfig tierConfig = rateLimitConfig.getTier(tier);
        
        String key = "rate_limit:" + tier.name() + ":" + clientIp;
        long now = System.currentTimeMillis();
        
        List<Long> result = redisTemplate.execute(
            new DefaultRedisScript<>(LUA_SCRIPT, List.class),
            List.of(key),
            String.valueOf(tierConfig.getWindow()),
            String.valueOf(tierConfig.getLimit()),
            String.valueOf(now)
        );
        
        int remaining = result.get(0).intValue();
        long retryAfter = result.get(1).longValue();
        
        // Set rate limit headers
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(tierConfig.getLimit()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf((now + tierConfig.getWindow()) / 1000));
        
        if (retryAfter > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private RateLimitTier determineTier(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/ai/")) return RateLimitTier.AI;
        if (request.getHeader("Authorization") != null) return RateLimitTier.AUTHENTICATED;
        return RateLimitTier.PUBLIC;
    }
}
```

### Sliding Window Algorithm
The sliding window algorithm provides more accurate rate limiting than fixed windows:

```
Fixed Window Problem:
Request 29 → Request 31 → Request 30 (3 requests in 1 second, but 60 in 2 consecutive windows)

Sliding Window Solution:
Uses weighted average of current and previous window for smoother limiting
```

### Rate Limit Tiers
```yaml
app:
  rate-limit:
    public:
      limit: 30
      window: 60000  # 1 minute in milliseconds
    authenticated:
      limit: 60
      window: 60000
    ai:
      limit: 10
      window: 60000
```

| Tier | Limit/Min | Use Case |
|------|-----------|----------|
| Public | 30 | Unauthenticated endpoints |
| Authenticated | 60 | Standard API operations |
| AI | 10 | AI generation endpoints |

### Client Identification
```java
private String getClientIdentifier(HttpServletRequest request) {
    // Use user ID if authenticated, otherwise IP
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null) {
        try {
            String token = authHeader.substring(7);
            return "user:" + jwtUtil.extractUserId(token);
        } catch (Exception e) {
            // Fall back to IP
        }
    }
    
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return "ip:" + xForwardedFor.split(",")[0].trim();
    }
    
    return "ip:" + request.getRemoteAddr();
}
```

## Architecture Decisions

### Why Sliding Window?
- **Accuracy**: Prevents boundary burst issues
- **Fairness**: Smoother rate limiting across time windows
- **Redis Efficient**: Uses sorted sets with minimal memory

### Why Lua Script?
- **Atomicity**: Single Redis operation prevents race conditions
- **Performance**: Single round-trip to Redis
- **Consistency**: All checks and updates happen atomically

### Tiered Approach
- **Public**: Prevents abuse from anonymous users
- **Authenticated**: Higher limits for verified users
- **AI**: Lowest limits due to high resource consumption

## Testing

### Verify Rate Limiting
```bash
# Test public tier (should hit limit after 30 requests)
for i in {1..35}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/listings
done

# Check rate limit headers
curl -I http://localhost:8080/listings
# X-Rate-Limit-Limit: 30
# X-Rate-Limit-Remaining: 29
```

### Load Testing
```bash
# Using Apache Bench
ab -n 100 -c 10 http://localhost:8080/listings
```

## Next Steps
- Implement rate limit bypass for whitelisted IPs
- Add rate limit metrics to monitoring
- Implement adaptive rate limiting based on server load
- Consider token bucket algorithm for burst allowance