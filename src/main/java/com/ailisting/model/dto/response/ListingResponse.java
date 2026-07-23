package com.ailisting.model.dto.response;

import com.ailisting.model.entity.Listing;
import com.ailisting.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingResponse {

    private Long id;
    private String productName;
    private String productDescription;
    private String category;
    private String brand;
    private String material;
    private String color;
    private String size;
    private String imageUrl;
    private String originalFileName;
    private Platform platform;
    private String seoTitle;
    private String bulletPoints;
    private String description;
    private String tags;
    private String keywords;
    private String metaDescription;
    private String platformFormattedListing;
    private Listing.ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}