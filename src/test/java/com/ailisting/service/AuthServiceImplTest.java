package com.ailisting.service;

import com.ailisting.exception.BadRequestException;
import com.ailisting.model.dto.request.LoginRequest;
import com.ailisting.model.dto.request.RefreshTokenRequest;
import com.ailisting.model.dto.request.RegisterRequest;
import com.ailisting.model.dto.request.ResetPasswordRequest;
import com.ailisting.model.dto.response.AuthResponse;
import com.ailisting.model.entity.RefreshToken;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.RefreshTokenRepository;
import com.ailisting.repository.UserRepository;
import com.ailisting.security.JwtTokenProvider;
import com.ailisting.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    private User testUser;

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
    }

    @Test
    void register_ValidRequest_ReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFullName("New User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(anyString())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(tokenProvider.getRefreshExpirationSeconds()).thenReturn(604800L);
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);

        AuthResponse response = authServiceImpl.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authServiceImpl.register(request));
    }

    @Test
    void register_DuplicateEmail_ThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authServiceImpl.register(request));
    }

    @Test
    void login_ValidCredentials_ReturnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user1");
        request.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(anyString())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("refresh-token");
        when(tokenProvider.getRefreshExpirationSeconds()).thenReturn(604800L);
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);

        AuthResponse response = authServiceImpl.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
    }

    @Test
    void login_DisabledUser_ThrowsBadRequest() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest();
        request.setUsername("user1");
        request.setPassword("password123");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(testUser));

        assertThrows(BadRequestException.class, () -> authServiceImpl.login(request));
    }

    @Test
    void refreshToken_ValidToken_ReturnsNewTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token"))
                .thenReturn(Optional.of(refreshToken));
        when(tokenProvider.generateToken(anyString())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(anyString())).thenReturn("new-refresh-token");
        when(tokenProvider.getRefreshExpirationSeconds()).thenReturn(604800L);
        when(tokenProvider.getExpirationMs()).thenReturn(900000L);

        AuthResponse response = authServiceImpl.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void refreshToken_ExpiredToken_ThrowsBadRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(refreshToken));

        assertThrows(BadRequestException.class, () -> authServiceImpl.refreshToken(request));
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void refreshToken_InvalidToken_ThrowsBadRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("nonexistent-token");

        when(refreshTokenRepository.findByToken("nonexistent-token"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authServiceImpl.refreshToken(request));
    }

    @Test
    void logout_ValidToken_DeletesToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("valid-token")
                .user(testUser)
                .build();

        when(refreshTokenRepository.findByToken("valid-token"))
                .thenReturn(Optional.of(refreshToken));

        authServiceImpl.logout("valid-token");

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void logout_InvalidToken_DoesNotThrow() {
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authServiceImpl.logout("invalid-token"));
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void forgotPassword_ValidEmail_CreatesResetToken() {
        when(userRepository.findByEmail("user1@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> authServiceImpl.forgotPassword("user1@example.com"));
        verify(userRepository).save(argThat(user -> user.getResetPasswordToken() != null));
    }

    @Test
    void forgotPassword_NonExistentEmail_DoesNotThrow() {
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authServiceImpl.forgotPassword("nonexistent@example.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_ValidToken_UpdatesPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-reset-token");
        request.setNewPassword("newPassword123");

        when(userRepository.findByResetPasswordToken("valid-reset-token"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        assertDoesNotThrow(() -> authServiceImpl.resetPassword(request));
        verify(userRepository).save(argThat(user -> user.getResetPasswordToken() == null));
    }

    @Test
    void resetPassword_InvalidToken_ThrowsBadRequest() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("invalid-token");
        request.setNewPassword("newPassword123");

        when(userRepository.findByResetPasswordToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authServiceImpl.resetPassword(request));
    }

    @Test
    void verifyEmail_ValidToken_VerifiesEmail() {
        when(userRepository.findByVerificationToken("valid-verification-token"))
                .thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> authServiceImpl.verifyEmail("valid-verification-token"));
        verify(userRepository).save(argThat(User::isEmailVerified));
    }

    @Test
    void verifyEmail_InvalidToken_ThrowsBadRequest() {
        when(userRepository.findByVerificationToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authServiceImpl.verifyEmail("invalid-token"));
    }
}