import { format, formatDistanceToNow, parseISO } from 'date-fns';
import { PLATFORM_CONFIG, STATUS_CONFIG } from './constants';
import type { Platform, ListingStatus } from '@/types';

export function formatDate(dateString: string): string {
  return format(parseISO(dateString), 'MMM d, yyyy');
}

export function formatDateTime(dateString: string): string {
  return format(parseISO(dateString), 'MMM d, yyyy HH:mm');
}

export function formatRelativeTime(dateString: string): string {
  return formatDistanceToNow(parseISO(dateString), { addSuffix: true });
}

export function getPlatformConfig(platform: Platform) {
  return PLATFORM_CONFIG[platform] || { name: platform, color: 'bg-gray-100 text-gray-800', icon: '❓' };
}

export function getStatusConfig(status: ListingStatus) {
  return STATUS_CONFIG[status] || { name: status, color: 'bg-gray-100 text-gray-800', icon: '❓' };
}

export function truncate(str: string, length: number): string {
  if (str.length <= length) return str;
  return str.slice(0, length) + '...';
}

export function capitalize(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

export function formatNumber(num: number): string {
  return new Intl.NumberFormat('en-IN').format(num);
}

export function formatDuration(ms: number): string {
  if (ms < 1000) return `${ms}ms`;
  return `${(ms / 1000).toFixed(1)}s`;
}
