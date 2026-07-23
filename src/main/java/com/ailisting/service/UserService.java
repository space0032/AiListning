package com.ailisting.service;

import com.ailisting.model.dto.request.ChangePasswordRequest;
import com.ailisting.model.dto.request.UpdateProfileRequest;
import com.ailisting.model.dto.response.UserResponse;

public interface UserService {

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);
}
