import type { Platform } from './listing';

export interface AiGenerationRequest {
  productName: string;
  productDescription?: string;
  category?: string;
  brand?: string;
  material?: string;
  color?: string;
  size?: string;
  platform: Platform;
}

export interface AiGenerationResponse {
  seoTitle: string;
  bulletPoints: string;
  description: string;
  tags: string;
  keywords: string;
  metaDescription: string;
  modelUsed: string;
  generationTimeMs: number;
}
