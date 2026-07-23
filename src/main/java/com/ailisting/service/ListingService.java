package com.ailisting.service;

import com.ailisting.model.dto.request.ListingRequest;
import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.enums.Platform;
import org.springframework.web.multipart.MultipartFile;

public interface ListingService {

    // ===========================
    // CRUD
    // ===========================

    ListingResponse createListing(ListingRequest request, Long userId);

    ListingResponse getListingById(Long id, Long userId);

    ListingResponse updateListing(Long id, ListingRequest request, Long userId);

    void deleteListing(Long id, Long userId);

    ListingResponse duplicateListing(Long id, Long userId);

    // ===========================
    // Status management
    // ===========================

    ListingResponse updateListingStatus(Long id, Listing.ListingStatus status, Long userId);

    // ===========================
    // List & Search
    // ===========================

    PaginatedResponse<ListingResponse> getUserListings(
            Long userId, int page, int size, String sortBy, String sortDir);

    PaginatedResponse<ListingResponse> getUserListingsByPlatform(
            Long userId, Platform platform, int page, int size);

    PaginatedResponse<ListingResponse> getUserListingsByStatus(
            Long userId, Listing.ListingStatus status, int page, int size);

    PaginatedResponse<ListingResponse> getUserListingsByPlatformAndStatus(
            Long userId, Platform platform, Listing.ListingStatus status, int page, int size);

    PaginatedResponse<ListingResponse> searchListings(Long userId, String keyword, int page, int size);

    // ===========================
    // Image
    // ===========================

    ListingResponse uploadImage(Long id, MultipartFile file, Long userId);

    // ===========================
    // AI Generation
    // ===========================

    ListingResponse generateListingContent(Long id, Long userId);

    // ===========================
    // Stats
    // ===========================

    long getUserListingCount(Long userId);

    long getUserListingCountByStatus(Long userId, Listing.ListingStatus status);
}