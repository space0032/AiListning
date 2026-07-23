import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/api';
import type { UpdateProfileRequest, ChangePasswordRequest } from '@/api/user.api';
import toast from 'react-hot-toast';

export function useUserProfile() {
  return useQuery({
    queryKey: ['userProfile'],
    queryFn: userApi.getProfile,
    select: (data) => data.data,
  });
}

export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => userApi.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
      toast.success('Profile updated successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to update profile');
    },
  });
}

export function useChangePassword() {
  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => userApi.changePassword(data),
    onSuccess: () => {
      toast.success('Password changed successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to change password');
    },
  });
}
