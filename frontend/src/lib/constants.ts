export const PLATFORM_CONFIG = {
  AMAZON: { name: 'Amazon', color: 'bg-orange-100 text-orange-800', icon: '📦' },
  FLIPKART: { name: 'Flipkart', color: 'bg-blue-100 text-blue-800', icon: '🛒' },
  MEESHO: { name: 'Meesho', color: 'bg-pink-100 text-pink-800', icon: '🛍️' },
  SHOPIFY: { name: 'Shopify', color: 'bg-green-100 text-green-800', icon: '🏪' },
} as const;

export const STATUS_CONFIG = {
  DRAFT: { name: 'Draft', color: 'bg-yellow-100 text-yellow-800', icon: '📝' },
  PUBLISHED: { name: 'Published', color: 'bg-green-100 text-green-800', icon: '✅' },
  ARCHIVED: { name: 'Archived', color: 'bg-gray-100 text-gray-800', icon: '📁' },
} as const;

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const APP_NAME = 'AI Listing Generator';

export const DEBOUNCE_DELAY = 300;

export const PAGE_SIZES = [10, 25, 50, 100];
export const DEFAULT_PAGE_SIZE = 10;
