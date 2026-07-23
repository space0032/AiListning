package com.ailisting.controller;

import com.ailisting.model.dto.request.ListingRequest;
import com.ailisting.model.dto.response.ApiResponse;
import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
@Tag(name = "Listings", description = "Product Listing CRUD Operations")
public class ListingController {

    private final ListingService listingService;
    private final UserRepository userRepository;

    // ===========================
    // CRUD
    // ===========================

    @PostMapping
    @Operation(summary = "Create listing", description = "Create a new product listing")
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.createListing(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Listing created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get listing", description = "Get a single listing by ID")
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.getListingById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all listings", description = "Get paginated list of user's listings")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingResponse>>> getUserListings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        PaginatedResponse<ListingResponse> response =
                listingService.getUserListings(userId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update listing", description = "Update an existing listing")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            @Valid @RequestBody ListingRequest request,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.updateListing(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete listing", description = "Soft delete a listing")
    public ResponseEntity<ApiResponse<Void>> deleteListing(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        listingService.deleteListing(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing deleted successfully", null));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate listing", description = "Clone a listing as draft")
    public ResponseEntity<ApiResponse<ListingResponse>> duplicateListing(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.duplicateListing(id, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Listing duplicated successfully", response));
    }

    // ===========================
    // Status Management
    // ===========================

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Change listing status (DRAFT/PUBLISHED/ARCHIVED)")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListingStatus(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam Listing.ListingStatus status,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.updateListingStatus(id, status, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing status updated", response));
    }

    // ===========================
    // Filter by Platform
    // ===========================

    @GetMapping("/platform/{platform}")
    @Operation(summary = "Filter by platform", description = "Get listings filtered by e-commerce platform")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingResponse>>> getListingsByPlatform(
            @Parameter(description = "Platform") @PathVariable Platform platform,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        PaginatedResponse<ListingResponse> response =
                listingService.getUserListingsByPlatform(userId, platform, page, size);
        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", response));
    }

    // ===========================
    // Filter by Status
    // ===========================

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter by status", description = "Get listings filtered by status")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingResponse>>> getListingsByStatus(
            @Parameter(description = "Status") @PathVariable Listing.ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        PaginatedResponse<ListingResponse> response =
                listingService.getUserListingsByStatus(userId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", response));
    }

    // ===========================
    // Filter by Platform + Status
    // ===========================

    @GetMapping("/platform/{platform}/status/{status}")
    @Operation(summary = "Filter by platform and status")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingResponse>>> getListingsByPlatformAndStatus(
            @Parameter(description = "Platform") @PathVariable Platform platform,
            @Parameter(description = "Status") @PathVariable Listing.ListingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        PaginatedResponse<ListingResponse> response =
                listingService.getUserListingsByPlatformAndStatus(userId, platform, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Listings retrieved successfully", response));
    }

    // ===========================
    // Search
    // ===========================

    @GetMapping("/search")
    @Operation(summary = "Search listings", description = "Search across product name, description, brand, category")
    public ResponseEntity<ApiResponse<PaginatedResponse<ListingResponse>>> searchListings(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        PaginatedResponse<ListingResponse> response =
                listingService.searchListings(userId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", response));
    }

    // ===========================
    // Image Upload
    // ===========================

    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Upload image", description = "Upload product image (max 5MB, images only)")
    public ResponseEntity<ApiResponse<ListingResponse>> uploadImage(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            @Parameter(description = "Image file") @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.uploadImage(id, file, userId);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", response));
    }

    // ===========================
    // AI Generate for Existing Listing
    // ===========================

    @PostMapping("/{id}/generate")
    @Operation(summary = "Generate AI content", description = "Generate SEO content for existing listing using AI")
    public ResponseEntity<ApiResponse<ListingResponse>> generateListingContent(
            @Parameter(description = "Listing ID") @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserId(authentication);
        ListingResponse response = listingService.generateListingContent(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Listing content generated successfully", response));
    }

    // ===========================
    // Stats
    // ===========================

    @GetMapping("/stats")
    @Operation(summary = "Get stats", description = "Get listing counts by status")
    public ResponseEntity<ApiResponse<Object>> getListingStats(Authentication authentication) {
        Long userId = getUserId(authentication);

        long total = listingService.getUserListingCount(userId);
        long drafts = listingService.getUserListingCountByStatus(userId, Listing.ListingStatus.DRAFT);
        long published = listingService.getUserListingCountByStatus(userId, Listing.ListingStatus.PUBLISHED);
        long archived = listingService.getUserListingCountByStatus(userId, Listing.ListingStatus.ARCHIVED);

        return ResponseEntity.ok(ApiResponse.success("Stats retrieved", new Object() {
            public final long totalListings = total;
            public final long draftListings = drafts;
            public final long publishedListings = published;
            public final long archivedListings = archived;
        }));
    }

    // ===========================
    // Helper
    // ===========================

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}