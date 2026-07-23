package com.ailisting.service;

import com.ailisting.exception.BadRequestException;
import com.ailisting.exception.ResourceNotFoundException;
import com.ailisting.model.dto.request.ListingRequest;
import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.ListingRepository;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.impl.ListingServiceImpl;
import com.ailisting.util.ListingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private com.ailisting.ai.AiGenerationService aiGenerationService;

    @InjectMocks
    private ListingServiceImpl listingServiceImpl;

    private User testUser;
    private Listing testListing;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .fullName("User 1")
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        testListing = Listing.builder()
                .id(1L)
                .user(testUser)
                .productName("Test Product")
                .productDescription("A test product description")
                .category("Electronics")
                .brand("TestBrand")
                .platform(Platform.AMAZON)
                .status(Listing.ListingStatus.DRAFT)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createListing_ValidRequest_ReturnsCreatedListing() {
        ListingRequest request = new ListingRequest();
        request.setProductName("New Product");
        request.setProductDescription("New Description");
        request.setCategory("Clothing");
        request.setPlatform(Platform.AMAZON);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> {
            Listing listing = invocation.getArgument(0);
            listing.setId(2L);
            return listing;
        });

        ListingResponse result = listingServiceImpl.createListing(request, 1L);

        assertNotNull(result);
        assertEquals("New Product", result.getProductName());
        assertEquals(Listing.ListingStatus.DRAFT, result.getStatus());
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void createListing_InvalidUserId_ThrowsResourceNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ListingRequest request = new ListingRequest();
        request.setProductName("Product");

        assertThrows(ResourceNotFoundException.class,
                () -> listingServiceImpl.createListing(request, 999L));
    }

    @Test
    void getListingById_ValidId_ReturnsListing() {
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));

        ListingResponse result = listingServiceImpl.getListingById(1L, 1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void getListingById_InvalidId_ThrowsResourceNotFound() {
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(999L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> listingServiceImpl.getListingById(999L, 1L));
    }

    @Test
    void updateListing_ValidRequest_ReturnsUpdatedListing() {
        ListingRequest request = new ListingRequest();
        request.setProductName("Updated Product");
        request.setProductDescription("Updated Description");
        request.setPlatform(Platform.AMAZON);

        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse result = listingServiceImpl.updateListing(1L, request, 1L);

        assertNotNull(result);
        assertEquals("Updated Product", result.getProductName());
    }

    @Test
    void deleteListing_DraftListing_SoftDeletes() {
        testListing.setStatus(Listing.ListingStatus.DRAFT);
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> listingServiceImpl.deleteListing(1L, 1L));
        verify(listingRepository).save(argThat(Listing::isDeleted));
    }

    @Test
    void deleteListing_WithImage_DeletesImageFirst() {
        testListing.setImageUrl("https://example.com/image.jpg");
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        listingServiceImpl.deleteListing(1L, 1L);

        verify(storageService).deleteFile("https://example.com/image.jpg");
        verify(listingRepository).save(argThat(Listing::isDeleted));
    }

    @Test
    void duplicateListing_ValidListing_CreatesCopy() {
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> {
            Listing listing = invocation.getArgument(0);
            listing.setId(3L);
            return listing;
        });

        ListingResponse result = listingServiceImpl.duplicateListing(1L, 1L);

        assertNotNull(result);
        assertTrue(result.getProductName().contains("Copy"));
        assertEquals(Listing.ListingStatus.DRAFT, result.getStatus());
    }

    @Test
    void updateListingStatus_ValidTransition_UpdatesStatus() {
        testListing.setStatus(Listing.ListingStatus.DRAFT);
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse result = listingServiceImpl.updateListingStatus(
                1L, Listing.ListingStatus.PUBLISHED, 1L);

        assertEquals(Listing.ListingStatus.PUBLISHED, result.getStatus());
    }

    @Test
    void updateListingStatus_InvalidTransition_ThrowsBadRequest() {
        testListing.setStatus(Listing.ListingStatus.ARCHIVED);
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));

        assertThrows(BadRequestException.class,
                () -> listingServiceImpl.updateListingStatus(1L, Listing.ListingStatus.PUBLISHED, 1L));
    }

    @Test
    void updateListingStatus_PublishedToDraft_ThrowsBadRequest() {
        testListing.setStatus(Listing.ListingStatus.PUBLISHED);
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));

        assertThrows(BadRequestException.class,
                () -> listingServiceImpl.updateListingStatus(1L, Listing.ListingStatus.DRAFT, 1L));
    }

    @Test
    void getUserListings_ValidUser_ReturnsPaginatedListings() {
        Page<Listing> page = new PageImpl<>(List.of(testListing));
        when(listingRepository.findByUserIdAndDeletedFalse(eq(1L), any(PageRequest.class)))
                .thenReturn(page);

        PaginatedResponse<ListingResponse> result =
                listingServiceImpl.getUserListings(1L, 0, 10, "createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().getFirst().getProductName());
    }

    @Test
    void getUserListingsByPlatform_FiltersCorrectly() {
        Page<Listing> page = new PageImpl<>(List.of(testListing));
        when(listingRepository.findByUserIdAndPlatformAndDeletedFalse(eq(1L), eq(Platform.AMAZON), any(PageRequest.class)))
                .thenReturn(page);

        PaginatedResponse<ListingResponse> result =
                listingServiceImpl.getUserListingsByPlatform(1L, Platform.AMAZON, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserListingsByStatus_FiltersCorrectly() {
        Page<Listing> page = new PageImpl<>(List.of(testListing));
        when(listingRepository.findByUserIdAndStatusAndDeletedFalse(
                eq(1L), eq(Listing.ListingStatus.DRAFT), any(PageRequest.class)))
                .thenReturn(page);

        PaginatedResponse<ListingResponse> result =
                listingServiceImpl.getUserListingsByStatus(1L, Listing.ListingStatus.DRAFT, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void searchListings_ValidQuery_ReturnsResults() {
        Page<Listing> page = new PageImpl<>(List.of(testListing));
        when(listingRepository.searchByUserIdAndKeyword(eq(1L), eq("test"), any(PageRequest.class)))
                .thenReturn(page);

        PaginatedResponse<ListingResponse> result =
                listingServiceImpl.searchListings(1L, "test", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUserListingCount_ReturnsCorrectCount() {
        when(listingRepository.countByUserIdAndDeletedFalse(1L)).thenReturn(10L);

        long count = listingServiceImpl.getUserListingCount(1L);

        assertEquals(10L, count);
    }

    @Test
    void getUserListingCountByStatus_ReturnsCorrectCount() {
        when(listingRepository.countByUserIdAndStatusAndDeletedFalse(1L, Listing.ListingStatus.DRAFT))
                .thenReturn(3L);

        long count = listingServiceImpl.getUserListingCountByStatus(1L, Listing.ListingStatus.DRAFT);

        assertEquals(3L, count);
    }

    @Test
    void generateListingContent_ValidListing_CallsAiService() {
        when(listingRepository.findByIdAndUserIdAndDeletedFalse(1L, 1L))
                .thenReturn(Optional.of(testListing));

        var aiResponse = com.ailisting.model.dto.response.ListingGenerationResponse.builder()
                .seoTitle("AI Generated Title")
                .description("AI Generated Description")
                .platform(Platform.AMAZON)
                .build();

        when(aiGenerationService.generateListing(any(), eq(1L), eq(1L))).thenReturn(aiResponse);
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse result = listingServiceImpl.generateListingContent(1L, 1L);

        assertNotNull(result);
        verify(aiGenerationService).generateListing(any(), eq(1L), eq(1L));
    }
}