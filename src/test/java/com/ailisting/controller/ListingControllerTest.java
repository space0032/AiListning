package com.ailisting.controller;

import com.ailisting.exception.GlobalExceptionHandler;
import com.ailisting.model.dto.request.ListingRequest;
import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ailisting.config.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ListingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ListingService listingService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListingController listingController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private User testUser;
    private ListingResponse listingResponse;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(listingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = createRegularUser();
        listingResponse = ListingResponse.builder()
                .id(1L)
                .productName("Test Product")
                .productDescription("Test Description")
                .category("Electronics")
                .brand("TestBrand")
                .platform(Platform.AMAZON)
                .status(Listing.ListingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createListing_ValidRequest_ReturnsCreated() throws Exception {
        ListingRequest request = new ListingRequest();
        request.setProductName("New Product");
        request.setProductDescription("New Description");
        request.setPlatform(Platform.AMAZON);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.createListing(any(ListingRequest.class), eq(1L)))
                .thenReturn(listingResponse);

        mockMvc.perform(post("/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Test Product"));
    }

    @Test
    void getListing_ValidId_ReturnsOk() throws Exception {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.getListingById(eq(1L), eq(1L))).thenReturn(listingResponse);

        mockMvc.perform(get("/listings/1")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Test Product"));
    }

    @Test
    void getUserListings_ValidRequest_ReturnsPaginated() throws Exception {
        PaginatedResponse<ListingResponse> paginatedResponse = PaginatedResponse.<ListingResponse>builder()
                .content(List.of(listingResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.getUserListings(eq(1L), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/listings")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].productName").value("Test Product"));
    }

    @Test
    void updateListing_ValidRequest_ReturnsOk() throws Exception {
        ListingRequest request = new ListingRequest();
        request.setProductName("Updated Product");
        request.setPlatform(Platform.AMAZON);

        ListingResponse updatedResponse = ListingResponse.builder()
                .id(1L)
                .productName("Updated Product")
                .platform(Platform.AMAZON)
                .status(Listing.ListingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.updateListing(eq(1L), any(ListingRequest.class), eq(1L)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/listings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Updated Product"));
    }

    @Test
    void deleteListing_ValidId_ReturnsOk() throws Exception {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        doNothing().when(listingService).deleteListing(eq(1L), eq(1L));

        mockMvc.perform(delete("/listings/1")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Listing deleted successfully"));
    }

    @Test
    void duplicateListing_ValidId_ReturnsCreated() throws Exception {
        ListingResponse duplicateResponse = ListingResponse.builder()
                .id(2L)
                .productName("Test Product (Copy)")
                .status(Listing.ListingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.duplicateListing(eq(1L), eq(1L))).thenReturn(duplicateResponse);

        mockMvc.perform(post("/listings/1/duplicate")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Test Product (Copy)"));
    }

    @Test
    void updateListingStatus_ValidTransition_ReturnsOk() throws Exception {
        ListingResponse publishedResponse = ListingResponse.builder()
                .id(1L)
                .productName("Test Product")
                .status(Listing.ListingStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.updateListingStatus(eq(1L), any(Listing.ListingStatus.class), eq(1L)))
                .thenReturn(publishedResponse);

        mockMvc.perform(patch("/listings/1/status")
                        .param("status", "PUBLISHED")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchListings_ValidKeyword_ReturnsResults() throws Exception {
        PaginatedResponse<ListingResponse> searchResponse = PaginatedResponse.<ListingResponse>builder()
                .content(List.of(listingResponse))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.searchListings(eq(1L), anyString(), anyInt(), anyInt()))
                .thenReturn(searchResponse);

        mockMvc.perform(get("/listings/search")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getListingStats_ReturnsOk() throws Exception {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(listingService.getUserListingCount(eq(1L))).thenReturn(10L);
        when(listingService.getUserListingCountByStatus(eq(1L), any(Listing.ListingStatus.class)))
                .thenReturn(5L);

        mockMvc.perform(get("/listings/stats")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("user1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getListing_NoPrincipal_ReturnsServerError() throws Exception {
        mockMvc.perform(get("/listings/1"))
                .andExpect(status().is5xxServerError());
    }
}