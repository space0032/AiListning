import { cn } from '@/lib/utils';
import type { Platform, ListingStatus } from '@/types';

const platformVariants: Record<string, string> = {
  AMAZON: 'bg-orange-100 text-orange-800',
  FLIPKART: 'bg-blue-100 text-blue-800',
  MEESHO: 'bg-pink-100 text-pink-800',
  SHOPIFY: 'bg-green-100 text-green-800',
};

const statusVariants: Record<string, string> = {
  DRAFT: 'bg-yellow-100 text-yellow-800',
  PUBLISHED: 'bg-green-100 text-green-800',
  ARCHIVED: 'bg-gray-100 text-gray-800',
};

interface StatusBadgeProps {
  type: 'platform' | 'status';
  value: Platform | ListingStatus;
  className?: string;
}

export function StatusBadge({ type, value, className }: StatusBadgeProps) {
  const variants = type === 'platform' ? platformVariants : statusVariants;
  const label = value.charAt(0) + value.slice(1).toLowerCase();

  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
        variants[value],
        className
      )}
    >
      {label}
    </span>
  );
}
