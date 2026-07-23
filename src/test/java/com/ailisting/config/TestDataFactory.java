package com.ailisting.config;

import com.ailisting.model.entity.Listing;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Platform;
import com.ailisting.model.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestDataFactory {

    public static User createUser(Long id, String username, String email, Role role) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("encodedPassword")
                .fullName(username + " User")
                .role(role)
                .enabled(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User createAdminUser() {
        return createUser(1L, "admin", "admin@example.com", Role.ROLE_ADMIN);
    }

    public static User createRegularUser() {
        return createUser(1L, "user1", "user1@example.com", Role.ROLE_USER);
    }

    public static User createRegularUser(Long id) {
        return createUser(id, "user" + id, "user" + id + "@example.com", Role.ROLE_USER);
    }

    public static Listing createListing(User user, String productName) {
        return Listing.builder()
                .id(1L)
                .user(user)
                .productName(productName)
                .productDescription("Description for " + productName)
                .category("Electronics")
                .brand("TestBrand")
                .platform(Platform.AMAZON)
                .status(Listing.ListingStatus.DRAFT)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Listing createPublishedListing(User user, String productName) {
        Listing listing = createListing(user, productName);
        listing.setStatus(Listing.ListingStatus.PUBLISHED);
        return listing;
    }

    public static Listing createListingWithId(Long id, User user, String productName) {
        Listing listing = createListing(user, productName);
        listing.setId(id);
        return listing;
    }
}