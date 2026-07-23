export type Platform = 'AMAZON' | 'FLIPKART' | 'MEESHO' | 'SHOPIFY';
export type ListingStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface Listing {
  id: number;
  productName: string;
  productDescription: string | null;
  category: string | null;
  brand: string | null;
  material: string | null;
  color: string | null;
  size: string | null;
  imageUrl: string | null;
  originalFileName: string | null;
  platform: Platform;
  seoTitle: string | null;
  bulletPoints: string | null;
  description: string | null;
  tags: string | null;
  keywords: string | null;
  metaDescription: string | null;
  platformFormattedListing: string | null;
  status: ListingStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ListingRequest {
  productName: string;
  productDescription?: string;
  category?: string;
  brand?: string;
  material?: string;
  color?: string;
  size?: string;
  platform: Platform;
}

export interface ListingQueryParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  keyword?: string;
  platform?: Platform;
  status?: ListingStatus;
}

export interface ListingStats {
  totalListings: number;
  draftCount: number;
  publishedCount: number;
  archivedCount: number;
  platformStats: Record<Platform, number>;
}
