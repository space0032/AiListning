package com.ailisting.util;

import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.entity.Listing;

/**
 * Maps between Listing entity and ListingResponse DTO.
 *
 * Why a mapper class instead of MapStruct?
 * For MVP, a simple static method avoids annotation processing complexity.
 * When the project grows, migrate to MapStruct for compile-time safety.
 */
public final class ListingMapper {

    private ListingMapper() {
        // Utility class
    }

    public static ListingResponse toResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        return ListingResponse.builder()
                .id(listing.getId())
                .productName(listing.getProductName())
                .productDescription(listing.getProductDescription())
                .category(listing.getCategory())
                .brand(listing.getBrand())
                .material(listing.getMaterial())
                .color(listing.getColor())
                .size(listing.getSize())
                .imageUrl(listing.getImageUrl())
                .originalFileName(listing.getOriginalFileName())
                .platform(listing.getPlatform())
                .seoTitle(listing.getSeoTitle())
                .bulletPoints(listing.getBulletPoints())
                .description(listing.getDescription())
                .tags(listing.getTags())
                .keywords(listing.getKeywords())
                .metaDescription(listing.getMetaDescription())
                .platformFormattedListing(listing.getPlatformFormattedListing())
                .status(listing.getStatus())
                .modelUsed(listing.getModelUsed())
                .generationTimeMs(listing.getGenerationTimeMs())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }
}