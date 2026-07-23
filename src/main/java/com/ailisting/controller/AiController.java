package com.ailisting.controller;

import com.ailisting.ai.AiGenerationService;
import com.ailisting.model.dto.request.ListingGenerationRequest;
import com.ailisting.model.dto.response.ApiResponse;
import com.ailisting.model.dto.response.ListingGenerationResponse;
import com.ailisting.model.entity.User;
import com.ailisting.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI Generation")
public class AiController {

    private final AiGenerationService aiGenerationService;
    private final UserRepository userRepository;

    @PostMapping("/generate-listing")
    @Operation(summary = "Generate listing", description = "Generate a complete product listing using AI")
    public ResponseEntity<ApiResponse<ListingGenerationResponse>> generateListing(
            @Valid @RequestBody ListingGenerationRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingGenerationResponse response = aiGenerationService.generateListing(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing generated successfully", response));
    }

    @GetMapping("/health")
    @Operation(summary = "AI health check", description = "Check if AI provider is available")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiHealth() {
        boolean available = aiGenerationService.isAiAvailable();
        return ResponseEntity.ok(ApiResponse.success(
                "AI health check",
                Map.of("available", available)));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}