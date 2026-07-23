import { apiClient } from './client';
import type { AiGenerationRequest, AiGenerationResponse, ApiResponse } from '@/types';

export const aiApi = {
  generateListing: (data: AiGenerationRequest) =>
    apiClient.post<any, ApiResponse<AiGenerationResponse>>('/ai/generate-listing', data),

  checkHealth: () =>
    apiClient.get<any, ApiResponse<{ status: string; provider: string }>>('/ai/health'),
};
