package com.ailisting.model.dto.response;

import com.ailisting.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingGenerationResponse {

    private String seoTitle;
    private String bulletPoints;
    private String description;
    private String tags;
    private String keywords;
    private String metaDescription;
    private String platformFormattedListing;
    private Platform platform;
    private String modelUsed;
    private Long generationTimeMs;
}