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

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        LocalDateTime last7d = LocalDateTime.now().minusDays(7);
        LocalDateTime last30d = LocalDateTime.now().minusDays(30);

        long generationsLast24h = generationLogRepository.countByUserIdSince(0L, last24h);
        long generationsLast7d = generationLogRepository.countByUserIdSince(0L, last7d);
        long generationsLast30d = generationLogRepository.countByUserIdSince(0L, last30d);

        long tokensUsed = generationLogRepository.sumTokensByUserIdSince(0L, last30d);

        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved", Map.of(
                "totalUsers", totalUsers,
                "totalListings", totalListings,
                "generationsLast24h", generationsLast24h,
                "generationsLast7d", generationsLast7d,
                "generationsLast30d", generationsLast30d,
                "tokensUsedLast30d", tokensUsed
        )));
    }

    @GetMapping("/analytics/generation-stats")
    @Operation(summary = "Get AI generation stats")
    public ResponseEntity<ApiResponse<Object>> getGenerationStats() {
        LocalDateTime last7d = LocalDateTime.now().minusDays(7);
        LocalDateTime last30d = LocalDateTime.now().minusDays(30);

        long successLast7d = generationLogRepository.countSuccessfulByUserIdSince(0L, last7d);
        long successLast30d = generationLogRepository.countSuccessfulByUserIdSince(0L, last30d);
        long totalLast7d = generationLogRepository.countByUserIdSince(0L, last7d);
        long totalLast30d = generationLogRepository.countByUserIdSince(0L, last30d);

        double successRate7d = totalLast7d > 0 ? (double) successLast7d / totalLast7d * 100 : 0;
        double successRate30d = totalLast30d > 0 ? (double) successLast30d / totalLast30d * 100 : 0;

        return ResponseEntity.ok(ApiResponse.success("Generation stats retrieved", new Object() {
            public final long totalGenerations7d = totalLast7d;
            public final long successfulGenerations7d = successLast7d;
            public final double successRate7dPercent = Math.round(successRate7d * 100.0) / 100.0;
            public final long totalGenerations30d = totalLast30d;
            public final long successfulGenerations30d = successLast30d;
            public final double successRate30dPercent = Math.round(successRate30d * 100.0) / 100.0;
        }));
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