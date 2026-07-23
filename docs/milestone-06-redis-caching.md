# Milestone 6: Redis Caching

## Overview
Implement Redis caching layer to reduce database load and improve response times for frequently accessed listings.

## Implementation

### CacheManager Configuration
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("listings", config.entryTtl(Duration.ofMinutes(5)));      // Listing details
        cacheConfigs.put("userListings", config.entryTtl(Duration.ofMinutes(3)));  // User's listings
        cacheConfigs.put("searchResults", config.entryTtl(Duration.ofMinutes(2))); // Search queries
        cacheConfigs.put("stats", config.entryTtl(Duration.ofMinutes(15)));        // Statistics
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
```

### @Cacheable/@CacheEvict Usage
```java
@Service
@CacheConfig(cacheNames = "listings")
public class ListingServiceImpl implements ListingService {
    
    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public ListingResponse getListingById(Long id) {
        return listingRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }
    
    @Override
    @CacheEvict(key = "#id")
    public ListingResponse updateListing(Long id, ListingRequest request) {
        // Update logic
    }
    
    @Override
    @CacheEvict(key = "#id")
    public void deleteListing(Long id) {
        // Delete logic
    }
    
    @Override
    @Cacheable(value = "userListings", key = "#userId + ':' + #page + ':' + #size")
    public Page<ListingResponse> getUserListings(Long userId, int page, int size) {
        return listingRepository.findByUserId(userId, PageRequest.of(page, size))
            .map(this::toResponse);
    }
    
    @Override
    @CacheEvict(value = "userListings", allEntries = true)
    public void clearUserListingsCache(Long userId) {
        // Manual cache clear for user
    }
}
```

### CacheController (Admin-Only)
```java
@RestController
@RequestMapping("/admin/cache")
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {
    
    private final CacheManager cacheManager;
    private final RedisConnectionFactory redisConnectionFactory;
    
    @GetMapping("/stats")
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        for (Cache cache : cacheManager.getCacheNames()) {
            stats.put(cache.getName(), getCacheInfo(cache));
        }
        return stats;
    }
    
    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache cleared: " + cacheName);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
        return ResponseEntity.ok("All caches cleared");
    }
}
```

### Key Patterns
| Pattern | TTL | Description |
|---------|-----|-------------|
| `listings:{id}` | 5 min | Individual listing cache |
| `user:listings:{userId}:{page}:{size}` | 3 min | User's paginated listings |
| `searchResults:{keyword}:{page}` | 2 min | Search result cache |
| `stats:{type}` | 15 min | Statistics cache |

## Architecture Decisions

### Why Redis?
- **Performance**: Sub-millisecond response times
- **Scalability**: Can be shared across multiple app instances
- **Data Structures**: Supports complex caching patterns
- **TTL Support**: Native expiration handling

### Cache Invalidation Strategy
- **Time-based**: TTL handles stale data (short TTL for frequently changing data)
- **Event-based**: @CacheEvict on writes ensures consistency
- **Manual**: Admin endpoint for forced refresh

### TTL Rationale
- **Listings (5 min)**: Balance between freshness and performance
- **User Listings (3 min)**: User's own data changes more frequently
- **Search (2 min)**: Search results can become stale quickly
- **Stats (15 min)**: Statistics are relatively static

## Testing

### Verify Cache Operations
```bash
# Check Redis keys
redis-cli KEYS "*listings*"

# Monitor cache hits
redis-cli MONITOR

# Clear specific cache
curl -X POST http://localhost:8080/admin/cache/clear/listings -H "Authorization: Bearer {token}"
```

### Performance Testing
```bash
# First request (cache miss)
time curl http://localhost:8080/listings/1

# Second request (cache hit)
time curl http://localhost:8080/listings/1
```

## Next Steps
- Enable distributed caching for multi-instance deployment
- Implement cache warming strategies
- Add cache metrics to monitoring system
- Consider cache-aside pattern for complex queries