package com.ailisting.service.impl;

import com.ailisting.exception.BadRequestException;
import com.ailisting.model.dto.request.LoginRequest;
import com.ailisting.model.dto.request.RegisterRequest;
import com.ailisting.model.dto.request.RefreshTokenRequest;
import com.ailisting.model.dto.request.ResetPasswordRequest;
import com.ailisting.model.dto.response.AuthResponse;
import com.ailisting.model.dto.response.UserResponse;
import com.ailisting.model.entity.RefreshToken;
import com.ailisting.model.entity.User;
import com.ailisting.model.enums.Role;
import com.ailisting.repository.RefreshTokenRepository;
import com.ailisting.repository.UserRepository;
import com.ailisting.security.JwtTokenProvider;
import com.ailisting.service.AuthService;
import com.ailisting.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userListings", "listingStats"}, allEntries = true)
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        emailService.sendEmailVerification(user.getEmail(), user.getVerificationToken());
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().trim(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is disabled");
        }

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        log.info("User logged in successfully: {}", user.getUsername());

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new BadRequestException("Refresh token has expired");
        }

        User user = storedToken.getUser();

        // Delete the used refresh token (single-use)
        refreshTokenRepository.delete(storedToken);

        // Generate new token pair
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
        log.info("User logged out, refresh token revoked");
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.deleteAllExceptCurrent(userId, "NONE");
        log.info("All sessions revoked for user: {}", userId);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase().trim())
                .ifPresent(user -> {
                    user.setResetPasswordToken(UUID.randomUUID().toString());
                    user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
                    userRepository.save(user);
                    emailService.sendPasswordResetEmail(user.getEmail(), user.getResetPasswordToken());
                    log.info("Password reset email sent for user: {}", user.getUsername());
                });
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetPasswordTokenExpiry() != null && user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

        // Revoke all existing refresh tokens on password reset
        refreshTokenRepository.deleteAllExceptCurrent(user.getId(), "NONE");

        log.info("Password reset successfully for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getUsername());
    }

    // ===========================
    // Private helper methods
    // ===========================

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = tokenProvider.generateToken(user.getUsername());
        String refreshTokenValue = tokenProvider.generateRefreshToken(user.getUsername());

        // Store refresh token in database
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(
                        tokenProvider.getRefreshExpirationSeconds()))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs())
                .user(mapToUserResponse(user))
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}