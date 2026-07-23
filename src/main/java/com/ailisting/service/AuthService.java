package com.ailisting.service;

import com.ailisting.model.dto.request.LoginRequest;
import com.ailisting.model.dto.request.RefreshTokenRequest;
import com.ailisting.model.dto.request.RegisterRequest;
import com.ailisting.model.dto.request.ResetPasswordRequest;
import com.ailisting.model.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void logoutAll(Long userId);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);
}