package com.ailisting.controller;

import com.ailisting.model.dto.request.ChangePasswordRequest;
import com.ailisting.model.dto.request.UpdateProfileRequest;
import com.ailisting.model.dto.response.ApiResponse;
import com.ailisting.model.dto.response.UserResponse;
import com.ailisting.model.entity.User;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.ListingService;
import com.ailisting.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final ListingService listingService;
    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update the authenticated user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = getUserFromAuthentication(authentication);
        UserResponse response = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password", description = "Change the authenticated user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = getUserFromAuthentication(authentication);
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/me/stats")
    @Operation(summary = "Get user statistics", description = "Retrieves statistics for the authenticated user including listing counts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        long listingCount = listingService.getUserListingCount(user.getId());

        Map<String, Object> stats = Map.of(
                "totalListings", listingCount,
                "userId", user.getId()
        );
        return ResponseEntity.ok(ApiResponse.success("User stats retrieved", stats));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
