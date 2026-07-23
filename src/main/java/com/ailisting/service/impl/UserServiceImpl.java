package com.ailisting.service.impl;

import com.ailisting.exception.BadRequestException;
import com.ailisting.model.dto.request.ChangePasswordRequest;
import com.ailisting.model.dto.request.UpdateProfileRequest;
import com.ailisting.model.dto.response.UserResponse;
import com.ailisting.model.entity.User;
import com.ailisting.repository.UserRepository;
import com.ailisting.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(request.getEmail().toLowerCase().trim());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }

        user = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getUsername());

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
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
