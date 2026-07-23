package com.ailisting.ai;

import com.ailisting.ai.prompt.PromptTemplates;
import com.ailisting.exception.BadRequestException;
import com.ailisting.model.dto.request.ListingGenerationRequest;
import com.ailisting.model.dto.response.ListingGenerationResponse;
import com.ailisting.model.entity.AiGenerationLog;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.repository.AiGenerationLogRepository;
import com.ailisting.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final AiProviderFactory providerFactory;
    private final AiGenerationLogRepository generationLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ListingGenerationResponse generateListing(ListingGenerationRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Build the prompt
        String prompt = PromptTemplates.buildListingPrompt(
                request.getPlatform().name(),
                request.getProductName(),
                request.getCategory(),
                request.getBrand(),
                request.getMaterial(),
                request.getColor(),
                request.getSize(),
                request.getProductDescription());

        // Get the AI provider
        AiProvider provider = providerFactory.getDefaultProvider();

        // Generate
        long startTime = System.currentTimeMillis();
        String jsonResponse;
        String modelUsed = provider.getModelName();

        try {
            jsonResponse = provider.generateJson(prompt, null);
        } catch (AiProviderException e) {
            log.error("AI generation failed for user {}: {}", userId, e.getMessage());
            saveLog(user, null, modelUsed, request.getPlatform(),
                    System.currentTimeMillis() - startTime, "FAILED", e.getMessage());
            throw new BadRequestException("AI generation failed: " + e.getMessage());
        }

        long generationTimeMs = System.currentTimeMillis() - startTime;
        log.info("AI generation completed: model={}, time={}ms, user={}", modelUsed, generationTimeMs, userId);

        // Parse the response
        ListingGenerationResponse response = parseJsonResponse(jsonResponse, request.getPlatform());
        response.setModelUsed(modelUsed);
        response.setGenerationTimeMs(generationTimeMs);

        // Log the generation
        saveLog(user, null, modelUsed, request.getPlatform(), generationTimeMs, "SUCCESS", null);

        return response;
    }

    @Override
    public boolean isAiAvailable() {
        try {
            return providerFactory.getDefaultProvider().isAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    // ===========================
    // Private helpers
    // ===========================

    private ListingGenerationResponse parseJsonResponse(String jsonResponse, Platform platform) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonResponse);

            return ListingGenerationResponse.builder()
                    .seoTitle(getJsonText(json, "seoTitle"))
                    .bulletPoints(getJsonText(json, "bulletPoints"))
                    .description(getJsonText(json, "description"))
                    .tags(getJsonText(json, "tags"))
                    .keywords(getJsonText(json, "keywords"))
                    .metaDescription(getJsonText(json, "metaDescription"))
                    .platformFormattedListing(getJsonText(json, "platformFormattedListing"))
                    .platform(platform)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, using raw text: {}", e.getMessage());
            return ListingGenerationResponse.builder()
                    .description(jsonResponse)
                    .platform(platform)
                    .build();
        }
    }

    private String getJsonText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private void saveLog(User user, Listing listing, String modelUsed, Platform platform,
                         long generationTimeMs, String status, String errorMessage) {
        try {
            AiGenerationLog log = AiGenerationLog.builder()
                    .user(user)
                    .listing(listing)
                    .modelUsed(modelUsed)
                    .platform(platform)
                    .generationTimeMs(generationTimeMs)
                    .status(AiGenerationLog.GenerationStatus.valueOf(status))
                    .errorMessage(errorMessage)
                    .build();
            generationLogRepository.save(log);
        } catch (Exception e) {
            log.warn("Failed to save generation log: {}", e.getMessage());
        }
    }
}