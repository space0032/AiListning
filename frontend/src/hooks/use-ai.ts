import { useMutation } from '@tanstack/react-query';
import { aiApi } from '@/api';
import type { AiGenerationRequest } from '@/types';
import toast from 'react-hot-toast';

export function useAiGeneration() {
  return useMutation({
    mutationFn: (data: AiGenerationRequest) => aiApi.generateListing(data),
    onSuccess: () => {
      toast.success('Content generated successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to generate content');
    },
  });
}

export function useAiHealthCheck() {
  return useMutation({
    mutationFn: () => aiApi.checkHealth(),
    onError: () => {
      toast.error('AI service is unavailable');
    },
  });
}
