import { create } from 'zustand';
import type { Platform, ListingStatus } from '@/types';

interface ListingFilters {
  search: string;
  platform: Platform | null;
  status: ListingStatus | null;
}

interface ListingState {
  filters: ListingFilters;

  setFilters: (filters: Partial<ListingFilters>) => void;
  resetFilters: () => void;
}

const defaultFilters: ListingFilters = {
  search: '',
  platform: null,
  status: null,
};

export const useListingStore = create<ListingState>((set) => ({
  filters: defaultFilters,

  setFilters: (newFilters) =>
    set((state) => ({ filters: { ...state.filters, ...newFilters } })),

  resetFilters: () => set({ filters: defaultFilters }),
}));
