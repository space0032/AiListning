import type { Platform } from './listing';

export interface AdminStats {
  totalUsers: number;
  totalListings: number;
  activeUsers: number;
  aiGenerations: number;
}

export interface GenerationStats {
  totalGenerations: number;
  successRate: number;
  avgGenerationTimeMs: number;
  generationsByPlatform: Record<Platform, number>;
  generationsByDay: { date: string; count: number }[];
}

export interface HealthStatus {
  status: string;
  components: Record<string, { status: string; details?: Record<string, unknown> }>;
}
