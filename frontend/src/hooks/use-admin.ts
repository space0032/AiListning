import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/api';
import toast from 'react-hot-toast';

export function useAdminUsers(params?: { page?: number; size?: number }) {
  return useQuery({
    queryKey: ['adminUsers', params],
    queryFn: () => adminApi.getUsers(params),
    select: (data) => data.data,
  });
}

export function useToggleUserStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => adminApi.toggleUserStatus(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminUsers'] });
      toast.success('User status updated');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to update user status');
    },
  });
}

export function useAdminOverview() {
  return useQuery({
    queryKey: ['adminOverview'],
    queryFn: adminApi.getOverview,
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000,
  });
}

export function useGenerationStats() {
  return useQuery({
    queryKey: ['generationStats'],
    queryFn: adminApi.getGenerationStats,
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000,
  });
}

export function useHealthCheck() {
  return useQuery({
    queryKey: ['healthCheck'],
    queryFn: adminApi.healthCheck,
    select: (data) => data.data,
    refetchInterval: 30000,
  });
}
