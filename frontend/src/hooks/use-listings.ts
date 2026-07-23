import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listingsApi } from '@/api';
import type { ListingQueryParams, ListingRequest, ListingStatus } from '@/types';
import toast from 'react-hot-toast';

export function useListings(params?: ListingQueryParams) {
  return useQuery({
    queryKey: ['listings', params],
    queryFn: () => listingsApi.getAll(params),
    select: (data) => data.data,
  });
}

export function useListing(id: number) {
  return useQuery({
    queryKey: ['listing', id],
    queryFn: () => listingsApi.getById(id),
    select: (data) => data.data,
    enabled: !!id,
  });
}

export function useCreateListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: ListingRequest) => listingsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      toast.success('Listing created successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to create listing');
    },
  });
}

export function useUpdateListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: ListingRequest }) =>
      listingsApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      queryClient.invalidateQueries({ queryKey: ['listing', id] });
      toast.success('Listing updated successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to update listing');
    },
  });
}

export function useDeleteListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => listingsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      toast.success('Listing deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to delete listing');
    },
  });
}

export function useUpdateListingStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: { id: number; status: ListingStatus }) =>
      listingsApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      toast.success('Status updated successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to update status');
    },
  });
}

export function useDuplicateListing() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => listingsApi.duplicate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      toast.success('Listing duplicated successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to duplicate listing');
    },
  });
}

export function useUploadImage() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, file }: { id: number; file: File }) =>
      listingsApi.uploadImage(id, file),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      queryClient.invalidateQueries({ queryKey: ['listing', id] });
      toast.success('Image uploaded successfully');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to upload image');
    },
  });
}

export function useListingStats() {
  return useQuery({
    queryKey: ['listingStats'],
    queryFn: listingsApi.getStats,
    select: (data) => data.data,
    staleTime: 5 * 60 * 1000,
  });
}

export function useListingAiGeneration(id: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => listingsApi.generateAi(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['listing', id] });
      queryClient.invalidateQueries({ queryKey: ['listings'] });
      toast.success('AI content generated and saved');
    },
    onError: (error: any) => {
      toast.error(error?.response?.data?.message || 'Failed to generate content');
    },
  });
}
