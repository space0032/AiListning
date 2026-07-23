package com.ailisting.service.impl;

import com.ailisting.ai.AiGenerationService;
import com.ailisting.exception.BadRequestException;
import com.ailisting.exception.ResourceNotFoundException;
import com.ailisting.model.dto.request.ListingGenerationRequest;
import com.ailisting.model.dto.request.ListingRequest;
import com.ailisting.model.dto.response.ListingGenerationResponse;
import com.ailisting.model.dto.response.ListingResponse;
import com.ailisting.model.dto.response.PaginatedResponse;
import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.repository.ListingRepository;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.ListingService;
import com.ailisting.service.StorageService;
import com.ailisting.util.ListingMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingServiceImpl.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "productName", "platform", "status", "id");

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final AiGenerationService aiGenerationService;

    // ===========================
    // CRUD Operations
    // ===========================

    @Override
    @Transactional
    @CacheEvict(value = "userListings", key = "#userId")
    public ListingResponse createListing(ListingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Listing listing = Listing.builder()
                .user(user)
                .productName(request.getProductName().trim())
                .productDescription(trimOrNull(request.getProductDescription()))
                .category(trimOrNull(request.getCategory()))
                .brand(trimOrNull(request.getBrand()))
                .material(trimOrNull(request.getMaterial()))
                .color(trimOrNull(request.getColor()))
                .size(trimOrNull(request.getSize()))
                .platform(request.getPlatform())
                .status(Listing.ListingStatus.DRAFT)
                .deleted(false)
                .build();

        listing = listingRepository.save(listing);
        log.info("Listing created: id={}, user={}", listing.getId(), userId);

        return ListingMapper.toResponse(listing);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "listings", key = "#id + ':' + #userId")
    public ListingResponse getListingById(Long id, Long userId) {
        Listing listing = findListingOrThrow(id, userId);
        return ListingMapper.toResponse(listing);
    }

    @Override
    @Transactional
    @CachePut(value = "listings", key = "#id + ':' + #userId")
    @CacheEvict(value = "userListings", key = "#userId")
    public ListingResponse updateListing(Long id, ListingRequest request, Long userId) {
        Listing listing = findListingOrThrow(id, userId);

        listing.setProductName(request.getProductName().trim());
        listing.setProductDescription(trimOrNull(request.getProductDescription()));
        listing.setCategory(trimOrNull(request.getCategory()));
        listing.setBrand(trimOrNull(request.getBrand()));
        listing.setMaterial(trimOrNull(request.getMaterial()));
        listing.setColor(trimOrNull(request.getColor()));
        listing.setSize(trimOrNull(request.getSize()));
        listing.setPlatform(request.getPlatform());

        listing = listingRepository.save(listing);
        log.info("Listing updated: id={}", id);

        return ListingMapper.toResponse(listing);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"listings", "userListings", "listingStats"}, key = "#userId")
    public void deleteListing(Long id, Long userId) {
        Listing listing = findListingOrThrow(id, userId);

        if (listing.getImageUrl() != null) {
            storageService.deleteFile(listing.getImageUrl());
        }

        listing.setDeleted(true);
        listingRepository.save(listing);
        log.info("Listing soft-deleted: id={}, user={}", id, userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userListings", key = "#userId")
    public ListingResponse duplicateListing(Long id, Long userId) {
        Listing original = findListingOrThrow(id, userId);

        Listing duplicate = Listing.builder()
                .user(original.getUser())
                .productName(original.getProductName() + " (Copy)")
                .productDescription(original.getProductDescription())
                .category(original.getCategory())
                .brand(original.getBrand())
                .material(original.getMaterial())
                .color(original.getColor())
                .size(original.getSize())
                .platform(original.getPlatform())
                .status(Listing.ListingStatus.DRAFT)
                .deleted(false)
                .build();

        duplicate = listingRepository.save(duplicate);
        log.info("Listing duplicated: original={}, new={}", id, duplicate.getId());

        return ListingMapper.toResponse(duplicate);
    }

    // ===========================
    // Status Management
    // ===========================

    @Override
    @Transactional
    @CachePut(value = "listings", key = "#id + ':' + #userId")
    @CacheEvict(value = "userListings", key = "#userId")
    public ListingResponse updateListingStatus(Long id, Listing.ListingStatus status, Long userId) {
        Listing listing = findListingOrThrow(id, userId);

        validateStatusTransition(listing.getStatus(), status);

        listing.setStatus(status);
        listing = listingRepository.save(listing);
        log.info("Listing status updated: id={}, {} -> {}", id, listing.getStatus(), status);

        return ListingMapper.toResponse(listing);
    }

    // ===========================
    // List & Search (not cached - paginated data changes frequently)
    // ===========================

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingResponse> getUserListings(
            Long userId, int page, int size, String sortBy, String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<Listing> listings = listingRepository.findByUserIdAndDeletedFalse(userId, pageable);
        return buildPaginatedResponse(listings);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingResponse> getUserListingsByPlatform(
            Long userId, Platform platform, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Listing> listings = listingRepository
                .findByUserIdAndPlatformAndDeletedFalse(userId, platform, pageable);
        return buildPaginatedResponse(listings);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingResponse> getUserListingsByStatus(
            Long userId, Listing.ListingStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Listing> listings = listingRepository
                .findByUserIdAndStatusAndDeletedFalse(userId, status, pageable);
        return buildPaginatedResponse(listings);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingResponse> getUserListingsByPlatformAndStatus(
            Long userId, Platform platform, Listing.ListingStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Listing> listings = listingRepository
                .findByUserIdAndPlatformAndStatusAndDeletedFalse(userId, platform, status, pageable);
        return buildPaginatedResponse(listings);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ListingResponse> searchListings(
            Long userId, String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Listing> listings = listingRepository
                .searchByUserIdAndKeyword(userId, keyword.trim(), pageable);
        return buildPaginatedResponse(listings);
    }

    // ===========================
    // Image Upload
    // ===========================

    @Override
    @Transactional
    @CachePut(value = "listings", key = "#id + ':' + #userId")
    public ListingResponse uploadImage(Long id, MultipartFile file, Long userId) {
        Listing listing = findListingOrThrow(id, userId);

        if (listing.getImageUrl() != null) {
            storageService.deleteFile(listing.getImageUrl());
        }

        String imageUrl = storageService.uploadFile(file);
        listing.setImageUrl(imageUrl);
        listing.setOriginalFileName(file.getOriginalFilename());

        listing = listingRepository.save(listing);
        log.info("Image uploaded for listing: id={}, url={}", id, imageUrl);

        return ListingMapper.toResponse(listing);
    }

    // ===========================
    // Stats (cached)
    // ===========================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "listingStats", key = "#userId")
    public long getUserListingCount(Long userId) {
        return listingRepository.countByUserIdAndDeletedFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "listingStats", key = "#userId + ':' + #status")
    public long getUserListingCountByStatus(Long userId, Listing.ListingStatus status) {
        return listingRepository.countByUserIdAndStatusAndDeletedFalse(userId, status);
    }

    // ===========================
    // AI Generation
    // ===========================

    @Override
    @Transactional
    @CachePut(value = "listings", key = "#id + ':' + #userId")
    public ListingResponse generateListingContent(Long id, Long userId) {
        Listing listing = findListingOrThrow(id, userId);

        ListingGenerationRequest request = ListingGenerationRequest.builder()
                .productName(listing.getProductName())
                .productDescription(listing.getProductDescription())
                .category(listing.getCategory())
                .brand(listing.getBrand())
                .material(listing.getMaterial())
                .color(listing.getColor())
                .size(listing.getSize())
                .platform(listing.getPlatform())
                .build();

        ListingGenerationResponse aiResponse = aiGenerationService.generateListing(request, userId, id);

        listing.setSeoTitle(aiResponse.getSeoTitle());
        listing.setBulletPoints(aiResponse.getBulletPoints());
        listing.setDescription(aiResponse.getDescription());
        listing.setTags(aiResponse.getTags());
        listing.setKeywords(aiResponse.getKeywords());
        listing.setMetaDescription(aiResponse.getMetaDescription());
        listing.setPlatformFormattedListing(aiResponse.getPlatformFormattedListing());
        listing.setModelUsed(aiResponse.getModelUsed());
        listing.setGenerationTimeMs(aiResponse.getGenerationTimeMs());

        listing = listingRepository.save(listing);
        log.info("AI content applied to listing: id={}, model={}, time={}ms",
                id, aiResponse.getModelUsed(), aiResponse.getGenerationTimeMs());

        return ListingMapper.toResponse(listing);
    }

    // ===========================
    // Private helpers
    // ===========================

    private Listing findListingOrThrow(Long id, Long userId) {
        return listingRepository.findByIdAndUserIdAndDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", "id", id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

    private PaginatedResponse<ListingResponse> buildPaginatedResponse(Page<Listing> page) {
        return PaginatedResponse.<ListingResponse>builder()
                .content(page.getContent().stream()
                        .map(ListingMapper::toResponse)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private void validateStatusTransition(Listing.ListingStatus from, Listing.ListingStatus to) {
        boolean valid = switch (from) {
            case DRAFT -> to == Listing.ListingStatus.PUBLISHED || to == Listing.ListingStatus.ARCHIVED;
            case PUBLISHED -> to == Listing.ListingStatus.ARCHIVED;
            case ARCHIVED -> to == Listing.ListingStatus.DRAFT;
        };

        if (!valid) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", from, to));
        }
    }

    private String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }
}