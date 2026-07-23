package com.ailisting.controller;

import com.ailisting.model.dto.response.ApiResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.dto.response.UserResponse;
import com.ailisting.model.entity.User;
import com.ailisting.repository.AiGenerationLogRepository;
import com.ailisting.repository.ListingRepository;
import com.ailisting.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin Dashboard & Management")
public class AdminController {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final AiGenerationLogRepository generationLogRepository;

    // ===========================
    // User Management
    // ===========================

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Admin: Get paginated list of all users")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        PaginatedResponse<UserResponse> response = PaginatedResponse.<UserResponse>builder()
                .content(users.getContent().stream()
                        .map(this::mapToUserResponse)
                        .toList())
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .first(users.isFirst())
                .last(users.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Users retrieved", response));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(ApiResponse.success("User retrieved", mapToUserResponse(user)));
    }

    @PatchMapping("/users/{id}/toggle-status")
    @Operation(summary = "Toggle user status", description = "Enable/disable a user account")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "User " + (user.isEnabled() ? "enabled" : "disabled"), mapToUserResponse(user)));
    }

    // ===========================
    // Analytics
    // ===========================

    @GetMapping("/analytics/overview")
    @Operation(summary = "Get overview analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalyticsOverview() {
        long totalUsers = userRepository.count();
        long totalListings = listingRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        long aiGenerations = generationLogRepository.countAllSince(LocalDateTime.of(2000, 1, 1, 0, 0));

        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved", Map.of(
                "totalUsers", totalUsers,
                "totalListings", totalListings,
                "activeUsers", activeUsers,
                "aiGenerations", aiGenerations
        )));
    }

    @GetMapping("/analytics/generation-stats")
    @Operation(summary = "Get AI generation stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGenerationStats() {
        LocalDateTime allTime = LocalDateTime.of(2000, 1, 1, 0, 0);

        long totalGenerations = generationLogRepository.countAllSince(allTime);
        long successfulGenerations = generationLogRepository.countSuccessfulAllSince(allTime);
        double successRate = totalGenerations > 0
                ? (double) successfulGenerations / totalGenerations
                : 0;
        double avgGenerationTimeMs = generationLogRepository.avgGenerationTimeAll();

        // Generations by platform
        Map<String, Long> generationsByPlatform = new java.util.HashMap<>();
        List<Object[]> platformCounts = generationLogRepository.countByPlatformAll();
        for (Object[] row : platformCounts) {
            String platform = row[0] != null ? row[0].toString() : "UNKNOWN";
            Long count = (Long) row[1];
            generationsByPlatform.put(platform, count);
        }

        // Generations by day (last 30 days)
        LocalDateTime last30d = LocalDateTime.now().minusDays(30);
        List<Map<String, Object>> generationsByDay = new java.util.ArrayList<>();
        List<Object[]> dailyCounts = generationLogRepository.countByDaySince(last30d);
        for (Object[] row : dailyCounts) {
            Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("date", row[0] != null ? row[0].toString() : "unknown");
            entry.put("count", row[1] != null ? row[1] : 0);
            generationsByDay.add(entry);
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalGenerations", totalGenerations);
        result.put("successRate", successRate);
        result.put("avgGenerationTimeMs", Math.round(avgGenerationTimeMs));
        result.put("generationsByPlatform", generationsByPlatform);
        result.put("generationsByDay", generationsByDay);

        return ResponseEntity.ok(ApiResponse.success("Generation stats retrieved", result));
    }

    // ===========================
    // Health
    // ===========================

    @GetMapping("/health/detailed")
    @Operation(summary = "Detailed health check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedHealth() {
        long userCount = userRepository.count();
        boolean dbHealthy = userCount >= 0; // Simple check

        return ResponseEntity.ok(ApiResponse.success("Health check", Map.of(
                "status", "UP",
                "database", dbHealthy ? "UP" : "DOWN",
                "totalUsers", userCount
        )));
    }

    // ===========================
    // Helper
    // ===========================

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}