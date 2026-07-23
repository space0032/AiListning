import { create } from 'zustand';
import type { Platform, ListingStatus } from '@/types';

interface ListingFilters {
  search: string;
  platform: Platform | null;
  status: ListingStatus | null;
}

interface ListingState {
  filters: ListingFilters;
  viewMode: 'table' | 'grid';
  selectedIds: number[];

  setFilters: (filters: Partial<ListingFilters>) => void;
  resetFilters: () => void;
  setViewMode: (mode: 'table' | 'grid') => void;
  toggleSelection: (id: number) => void;
  selectAll: (ids: number[]) => void;
  clearSelection: () => void;
}

const defaultFilters: ListingFilters = {
  search: '',
  platform: null,
  status: null,
};

export const useListingStore = create<ListingState>((set) => ({
  filters: defaultFilters,
  viewMode: 'table',
  selectedIds: [],

  setFilters: (newFilters) =>
    set((state) => ({ filters: { ...state.filters, ...newFilters } })),

  resetFilters: () => set({ filters: defaultFilters }),

  setViewMode: (mode) => set({ viewMode: mode }),

  toggleSelection: (id) =>
    set((state) => ({
      selectedIds: state.selectedIds.includes(id)
        ? state.selectedIds.filter((i) => i !== id)
        : [...state.selectedIds, id],
    })),

  selectAll: (ids) => set({ selectedIds: ids }),

  clearSelection: () => set({ selectedIds: [] }),
}));
