# Milestone 9: Admin Features

## Overview
Implement administrative endpoints for user management, analytics, and system health monitoring.

## Implementation

### AdminController
```java
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final ListingService listingService;
    private final AiGenerationService aiGenerationService;
    
    // User Management
    @GetMapping("/users")
    @Operation(summary = "List all users with pagination")
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.getAllUsers(PageRequest.of(page, size)));
    }
    
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user details")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
    
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Toggle user active status")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }
    
    // Analytics
    @GetMapping("/analytics/overview")
    @Operation(summary = "Get system overview")
    public ResponseEntity<AnalyticsOverview> getOverview() {
        AnalyticsOverview overview = new AnalyticsOverview();
        overview.setTotalUsers(userService.getUserCount());
        overview.setTotalListings(listingService.getListingCount());
        overview.setActiveListings(listingService.getListingCountByStatus(ListingStatus.PUBLISHED));
        overview.setAiGenerations(aiGenerationService.getGenerationCount());
        return ResponseEntity.ok(overview);
    }
    
    @GetMapping("/analytics/generations")
    @Operation(summary = "Get AI generation statistics")
    public ResponseEntity<GenerationStats> getGenerationStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(aiGenerationService.getStats(from, to));
    }
    
    // Health Check
    @GetMapping("/health/detailed")
    @Operation(summary = "Detailed health check with component status")
    public ResponseEntity<DetailedHealth> getDetailedHealth() {
        DetailedHealth health = new DetailedHealth();
        health.setStatus("UP");
        health.setDatabase(checkDatabaseHealth());
        health.setRedis(checkRedisHealth());
        health.setAiProvider(checkAiProviderHealth());
        health.setStorage(checkStorageHealth());
        health.setTimestamp(Instant.now());
        return ResponseEntity.ok(health);
    }
}
```

### User Management Service
```java
@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        user.setActive(!user.isActive());
        user.setUpdatedAt(Instant.now());
        
        User updated = userRepository.save(user);
        return mapToResponse(updated);
    }
    
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::mapToResponse);
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
}
```

### Analytics DTOs
```java
@Data
public class AnalyticsOverview {
    private long totalUsers;
    private long totalListings;
    private long activeListings;
    private long aiGenerations;
    private Map<String, Long> listingsByPlatform;
    private Map<String, Long> usersByRole;
}

@Data
public class GenerationStats {
    private long totalGenerations;
    private long successfulGenerations;
    private long failedGenerations;
    private double averageGenerationTime;
    private Map<String, Long> generationsByModel;
    private List<DailyStats> dailyStats;
}

@Data
public class DetailedHealth {
    private String status;
    private ComponentHealth database;
    private ComponentHealth redis;
    private ComponentHealth aiProvider;
    private ComponentHealth storage;
    private Instant timestamp;
}

@Data
public class ComponentHealth {
    private String status;
    private long responseTimeMs;
    private String message;
    private Map<String, Object> details;
}
```

### Health Check Implementation
```java
private ComponentHealth checkDatabaseHealth() {
    long start = System.currentTimeMillis();
    try {
        jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return new ComponentHealth("UP", System.currentTimeMillis() - start, "Database accessible");
    } catch (Exception e) {
        return new ComponentHealth("DOWN", System.currentTimeMillis() - start, e.getMessage());
    }
}

private ComponentHealth checkRedisHealth() {
    long start = System.currentTimeMillis();
    try {
        redisTemplate.getConnectionFactory().getConnection().ping();
        return new ComponentHealth("UP", System.currentTimeMillis() - start, "Redis accessible");
    } catch (Exception e) {
        return new ComponentHealth("DOWN", System.currentTimeMillis() - start, e.getMessage());
    }
}

private ComponentHealth checkAiProviderHealth() {
    long start = System.currentTimeMillis();
    try {
        boolean available = aiGenerationService.isAiAvailable();
        return new ComponentHealth(
            available ? "UP" : "DEGRADED", 
            System.currentTimeMillis() - start,
            available ? "AI provider available" : "AI provider unavailable"
        );
    } catch (Exception e) {
        return new ComponentHealth("DOWN", System.currentTimeMillis() - start, e.getMessage());
    }
}
```

## Architecture Decisions

### Why Separate Admin Endpoints?
- **Security**: Isolate admin operations from regular API
- **Organization**: Clear separation of concerns
- **Documentation**: Swagger groups admin endpoints separately

### Role-Based Access
- **@PreAuthorize**: Method-level security check
- **Role Hierarchy**: ADMIN > USER
- **Auditing**: All admin actions logged

### Health Check Strategy
- **Component-Based**: Individual checks for each dependency
- **Response Time**: Track latency for each component
- **Graceful Degradation**: Report partial failures

## Testing

### Test Admin Access
```bash
# List users (as admin)
curl -H "Authorization: Bearer {admin_token}" http://localhost:8080/admin/users

# Get overview
curl -H "Authorization: Bearer {admin_token}" http://localhost:8080/admin/analytics/overview

# Detailed health
curl -H "Authorization: Bearer {admin_token}" http://localhost:8080/admin/health/detailed
```

### Verify Security
```bash
# Should return 403 for non-admin
curl -H "Authorization: Bearer {user_token}" http://localhost:8080/admin/users

# Should return 401 for no token
curl http://localhost:8080/admin/users
```

### Check Response Format
```json
{
  "status": "UP",
  "database": {
    "status": "UP",
    "responseTimeMs": 12,
    "message": "Database accessible"
  },
  "redis": {
    "status": "UP",
    "responseTimeMs": 3,
    "message": "Redis accessible"
  },
  "aiProvider": {
    "status": "UP",
    "responseTimeMs": 150,
    "message": "AI provider available"
  }
}
```

## Next Steps
- Add audit logging for admin actions
- Implement user impersonation for support
- Add system configuration management
- Implement automated backup endpoints