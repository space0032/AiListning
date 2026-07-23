package com.ailisting.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long-for-hs256");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpiration", 604800000L);
        jwtTokenProvider.init();
    }

    @Test
    void generateToken_ValidUsername_ReturnsToken() {
        String token = jwtTokenProvider.generateToken("user1");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_ValidUsername_ReturnsToken() {
        String token = jwtTokenProvider.generateRefreshToken("user1");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_ValidAccessToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken("user1");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_ValidRefreshToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateRefreshToken("user1");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1L);
        String token = jwtTokenProvider.generateToken("user1");
        assertTrue(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    void getUsernameFromToken_ValidToken_ReturnsUsername() {
        String token = jwtTokenProvider.generateToken("user1");
        assertEquals("user1", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void getTokenType_AccessToken_ReturnsAccess() {
        String token = jwtTokenProvider.generateToken("user1");
        assertEquals("access", jwtTokenProvider.getTokenType(token));
    }

    @Test
    void getTokenType_RefreshToken_ReturnsRefresh() {
        String token = jwtTokenProvider.generateRefreshToken("user1");
        assertEquals("refresh", jwtTokenProvider.getTokenType(token));
    }

    @Test
    void getExpirationMs_ReturnsCorrectValue() {
        assertEquals(900000L, jwtTokenProvider.getExpirationMs());
    }

    @Test
    void getRefreshExpirationSeconds_ReturnsCorrectValue() {
        assertEquals(604800L, jwtTokenProvider.getRefreshExpirationSeconds());
    }

    @Test
    void isTokenExpired_FreshToken_ReturnsFalse() {
        String token = jwtTokenProvider.generateToken("user1");
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }
}