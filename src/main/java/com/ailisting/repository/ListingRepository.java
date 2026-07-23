package com.ailisting.repository;

import com.ailisting.model.entity.Listing;
import com.ailisting.model.enums.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    // ===========================
    // Basic CRUD
    // ===========================

    Optional<Listing> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    long countByUserIdAndDeletedFalse(Long userId);

    // ===========================
    // List queries (paginated)
    // ===========================

    Page<Listing> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    Page<Listing> findByUserIdAndPlatformAndDeletedFalse(
            Long userId, Platform platform, Pageable pageable);

    Page<Listing> findByUserIdAndStatusAndDeletedFalse(
            Long userId, Listing.ListingStatus status, Pageable pageable);

    Page<Listing> findByUserIdAndPlatformAndStatusAndDeletedFalse(
            Long userId, Platform platform, Listing.ListingStatus status, Pageable pageable);

    // ===========================
    // Search
    // ===========================

    @Query("SELECT l FROM Listing l WHERE l.user.id = :userId AND l.deleted = false AND " +
           "(LOWER(l.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.productDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.category) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Listing> searchByUserIdAndKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ===========================
    // Stats
    // ===========================

    long countByUserIdAndStatusAndDeletedFalse(Long userId, Listing.ListingStatus status);

    long countByUserIdAndPlatformAndDeletedFalse(Long userId, Platform platform);
}