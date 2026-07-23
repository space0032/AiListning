package com.ailisting.service;

import com.ailisting.ai.AiGenerationServiceImpl;
import com.ailisting.ai.AiProvider;
import com.ailisting.ai.AiProviderFactory;
import com.ailisting.exception.BadRequestException;
import com.ailisting.model.dto.request.ListingGenerationRequest;
import com.ailisting.model.dto.response.ListingGenerationResponse;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.AiGenerationLogRepository;
import com.ailisting.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiGenerationServiceImplTest {

    @Mock
    private AiProviderFactory providerFactory;

    @Mock
    private AiGenerationLogRepository generationLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiGenerationServiceImpl aiGenerationService;

    private User testUser;
    private AiProvider mockProvider;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .password("encodedPassword")
                .fullName("User 1")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        mockProvider = mock(AiProvider.class);
    }

    @Test
    void generateListing_ValidRequest_ReturnsResponse() {
        ListingGenerationRequest request = ListingGenerationRequest.builder()
                .productName("Test Product")
                .productDescription("A test product")
                .category("Electronics")
                .brand("TestBrand")
                .platform(Platform.AMAZON)
                .build();

        String jsonResponse = """
                {
                    "seoTitle": "AI Generated Title",
                    "bulletPoints": "Feature 1\\nFeature 2",
                    "description": "AI Generated Description",
                    "tags": "electronics, test",
                    "keywords": "test product electronics",
                    "metaDescription": "AI meta description",
                    "platformFormattedListing": "Formatted for Amazon"
                }
                """;

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerFactory.getDefaultProvider()).thenReturn(mockProvider);
        when(mockProvider.getModelName()).thenReturn("qwen3.5:0.8b");
        when(mockProvider.generateJson(anyString(), isNull())).thenReturn(jsonResponse);

        ListingGenerationResponse result = aiGenerationService.generateListing(request, 1L);

        assertNotNull(result);
        assertEquals("AI Generated Title", result.getSeoTitle());
        assertEquals("AI Generated Description", result.getDescription());
        assertEquals("qwen3.5:0.8b", result.getModelUsed());
        assertNotNull(result.getGenerationTimeMs());
        verify(generationLogRepository).save(any());
    }

    @Test
    void generateListing_UserNotFound_ThrowsBadRequest() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ListingGenerationRequest request = ListingGenerationRequest.builder()
                .productName("Test")
                .platform(Platform.AMAZON)
                .build();

        assertThrows(BadRequestException.class,
                () -> aiGenerationService.generateListing(request, 999L));
    }

    @Test
    void generateListing_ProviderThrows_ThrowsBadRequestAndLogsFailure() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerFactory.getDefaultProvider()).thenReturn(mockProvider);
        when(mockProvider.generateJson(anyString(), isNull()))
                .thenThrow(new com.ailisting.ai.AiProviderException("ollama", "model", "connection refused"));

        ListingGenerationRequest request = ListingGenerationRequest.builder()
                .productName("Test")
                .platform(Platform.AMAZON)
                .build();

        assertThrows(BadRequestException.class,
                () -> aiGenerationService.generateListing(request, 1L));
        verify(generationLogRepository).save(argThat(log ->
                log.getStatus() == com.ailisting.model.entity.AiGenerationLog.GenerationStatus.FAILED));
    }

    @Test
    void generateListing_InvalidJsonResponse_ReturnsRawDescription() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(providerFactory.getDefaultProvider()).thenReturn(mockProvider);
        when(mockProvider.getModelName()).thenReturn("qwen3.5:0.8b");
        when(mockProvider.generateJson(anyString(), isNull())).thenReturn("This is plain text, not JSON");

        ListingGenerationRequest request = ListingGenerationRequest.builder()
                .productName("Test")
                .platform(Platform.AMAZON)
                .build();

        ListingGenerationResponse result = aiGenerationService.generateListing(request, 1L);

        assertNotNull(result);
        assertEquals("This is plain text, not JSON", result.getDescription());
    }

    @Test
    void isAiAvailable_ProviderExistsAndAvailable_ReturnsTrue() {
        when(providerFactory.getDefaultProvider()).thenReturn(mockProvider);
        when(mockProvider.isAvailable()).thenReturn(true);

        assertTrue(aiGenerationService.isAiAvailable());
    }

    @Test
    void isAiAvailable_NoProvider_ReturnsFalse() {
        when(providerFactory.getDefaultProvider()).thenReturn(null);

        assertFalse(aiGenerationService.isAiAvailable());
    }

    @Test
    void isAiAvailable_ProviderThrows_ReturnsFalse() {
        when(providerFactory.getDefaultProvider()).thenThrow(new RuntimeException("Provider unavailable"));

        assertFalse(aiGenerationService.isAiAvailable());
    }

    @Test
    void isAiAvailable_ProviderNotAvailable_ReturnsFalse() {
        when(providerFactory.getDefaultProvider()).thenReturn(mockProvider);
        when(mockProvider.isAvailable()).thenReturn(false);

        assertFalse(aiGenerationService.isAiAvailable());
    }
}