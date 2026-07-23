import { apiClient } from './client';
import type { User, ApiResponse } from '@/types';

export interface UpdateProfileRequest {
  fullName?: string;
  email?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const userApi = {
  getProfile: () =>
    apiClient.get<any, ApiResponse<User>>('/users/me'),

  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<any, ApiResponse<User>>('/users/me', data),

  changePassword: (data: ChangePasswordRequest) =>
    apiClient.put<any, ApiResponse<void>>('/users/me/password', data),

  getStats: () =>
    apiClient.get<any, ApiResponse<{ totalListings: number; userId: number }>>('/users/me/stats'),
};
