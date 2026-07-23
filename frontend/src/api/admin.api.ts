import { apiClient } from './client';
import type { User, AdminStats, GenerationStats, HealthStatus, ApiResponse, PaginatedResponse } from '@/types';

export const adminApi = {
  getUsers: (params?: { page?: number; size?: number }) =>
    apiClient.get<any, ApiResponse<PaginatedResponse<User>>>('/admin/users', { params }),

  getUserById: (id: number) =>
    apiClient.get<any, ApiResponse<User>>(`/admin/users/${id}`),

  toggleUserStatus: (id: number) =>
    apiClient.patch<any, ApiResponse<User>>(`/admin/users/${id}/toggle-status`),

  getOverview: () =>
    apiClient.get<any, ApiResponse<AdminStats>>('/admin/analytics/overview'),

  getGenerationStats: () =>
    apiClient.get<any, ApiResponse<GenerationStats>>('/admin/analytics/generation-stats'),

  healthCheck: () =>
    apiClient.get<any, ApiResponse<HealthStatus>>('/admin/health/detailed'),
};
