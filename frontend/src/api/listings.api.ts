import { apiClient } from './client';
import type {
  Listing,
  ListingRequest,
  ListingQueryParams,
  ListingStats,
  ListingStatus,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

export const listingsApi = {
  getAll: (params?: ListingQueryParams) =>
    apiClient.get<any, ApiResponse<PaginatedResponse<Listing>>>('/listings', { params }),

  getById: (id: number) =>
    apiClient.get<any, ApiResponse<Listing>>(`/listings/${id}`),

  create: (data: ListingRequest) =>
    apiClient.post<any, ApiResponse<Listing>>('/listings', data),

  update: (id: number, data: ListingRequest) =>
    apiClient.put<any, ApiResponse<Listing>>(`/listings/${id}`, data),

  delete: (id: number) =>
    apiClient.delete(`/listings/${id}`),

  updateStatus: (id: number, status: ListingStatus) =>
    apiClient.patch(`/listings/${id}/status`, null, { params: { status } }),

  duplicate: (id: number) =>
    apiClient.post<any, ApiResponse<Listing>>(`/listings/${id}/duplicate`),

  uploadImage: (id: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient.post<any, ApiResponse<Listing>>(`/listings/${id}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  generateAi: (id: number) =>
    apiClient.post(`/listings/${id}/generate`),

  search: (keyword: string, params?: ListingQueryParams) =>
    apiClient.get('/listings/search', { params: { keyword, ...params } }),

  getStats: () =>
    apiClient.get<any, ApiResponse<ListingStats>>('/listings/stats'),
};
