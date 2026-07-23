package com.ailisting.ai;

import com.ailisting.model.dto.request.ListingGenerationRequest;
import com.ailisting.model.dto.response.ListingGenerationResponse;

public interface AiGenerationService {

    /**
     * Generate a complete product listing using AI.
     *
     * @param request Product details for generation
     * @param userId User ID for logging/analytics
     * @return Generated listing content
     */
    ListingGenerationResponse generateListing(ListingGenerationRequest request, Long userId);

    /**
     * Check if the AI provider is available.
     */
    boolean isAiAvailable();
}
