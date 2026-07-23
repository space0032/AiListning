import { apiClient } from './client';
import type { AuthResponse, LoginRequest, RegisterRequest, ApiResponse } from '@/types';

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<any, ApiResponse<AuthResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<any, ApiResponse<AuthResponse>>('/auth/register', data),

  refresh: (refreshToken: string) =>
    apiClient.post<any, ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken }),

  logout: (refreshToken: string) =>
    apiClient.post<any, ApiResponse<void>>('/auth/logout', { refreshToken }),

  forgotPassword: (email: string) =>
    apiClient.post<any, ApiResponse<void>>('/auth/forgot-password', null, {
      params: { email },
    }),

  resetPassword: (token: string, newPassword: string) =>
    apiClient.post<any, ApiResponse<void>>('/auth/reset-password', {
      token,
      newPassword,
    }),

  verifyEmail: (token: string) =>
    apiClient.get<any, ApiResponse<void>>('/auth/verify-email', {
      params: { token },
    }),
};
