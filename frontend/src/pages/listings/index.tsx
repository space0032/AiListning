import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useListings, useDeleteListing, useUpdateListingStatus, useDuplicateListing } from '@/hooks';
import { useListingStore } from '@/stores';
import { useDebounce } from '@/hooks';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { SearchInput } from '@/components/molecules/search-input';
import { StatusBadge } from '@/components/molecules/status-badge';
import { EmptyState } from '@/components/molecules/empty-state';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Plus, MoreHorizontal, Pencil, Trash2, Copy, Eye, Sparkles, Package } from 'lucide-react';
import { formatDate } from '@/lib/formatters';
import type { Platform, ListingStatus } from '@/types';

export default function ListingsPage() {
  const navigate = useNavigate();
  const { filters, setFilters } = useListingStore();
  const [page, setPage] = useState(0);
  const [size] = useState(10);
  const debouncedSearch = useDebounce(filters.search, 300);

  const { data, isLoading } = useListings({
    page,
    size,
    keyword: debouncedSearch || undefined,
    platform: filters.platform || undefined,
    status: filters.status || undefined,
    sortBy: 'createdAt',
    sortDir: 'desc',
  });

  const deleteMutation = useDeleteListing();
  const updateStatusMutation = useUpdateListingStatus();
  const duplicateMutation = useDuplicateListing();

  const handleDelete = (id: number, name: string) => {
    if (window.confirm(`Are you sure you want to delete "${name}"?`)) {
      deleteMutation.mutate(id);
    }
  };

  const handleStatusChange = (id: number, status: ListingStatus) => {
    updateStatusMutation.mutate({ id, status });
  };

  const handleDuplicate = (id: number) => {
    duplicateMutation.mutate(id);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Listings</h1>
          <p className="text-muted-foreground">
            Manage your product listings
          </p>
        </div>
        <Button asChild>
          <Link to="/listings/new">
            <Plus className="mr-2 h-4 w-4" />
            New Listing
          </Link>
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <SearchInput
              value={filters.search}
              onChange={(value) => setFilters({ search: value })}
              placeholder="Search listings..."
              className="flex-1"
            />
            <Select
              value={filters.platform || 'all'}
              onValueChange={(value) =>
                setFilters({ platform: value === 'all' ? null : value as Platform })
              }
            >
              <SelectTrigger className="w-full sm:w-[180px]">
                <SelectValue placeholder="All Platforms" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Platforms</SelectItem>
                <SelectItem value="AMAZON">Amazon</SelectItem>
                <SelectItem value="FLIPKART">Flipkart</SelectItem>
                <SelectItem value="MEESHO">Meesho</SelectItem>
                <SelectItem value="SHOPIFY">Shopify</SelectItem>
              </SelectContent>
            </Select>
            <Select
              value={filters.status || 'all'}
              onValueChange={(value) =>
                setFilters({ status: value === 'all' ? null : value as ListingStatus })
              }
            >
              <SelectTrigger className="w-full sm:w-[180px]">
                <SelectValue placeholder="All Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="DRAFT">Draft</SelectItem>
                <SelectItem value="PUBLISHED">Published</SelectItem>
                <SelectItem value="ARCHIVED">Archived</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Listings Table */}
      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <LoadingSpinner className="py-12" />
          ) : data?.content && data.content.length > 0 ? (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Product</TableHead>
                    <TableHead>Platform</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data.content.map((listing) => (
                    <TableRow key={listing.id}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded bg-muted flex items-center justify-center flex-shrink-0">
                            <Package className="h-5 w-5 text-muted-foreground" />
                          </div>
                          <div>
                            <p className="font-medium line-clamp-1">{listing.productName}</p>
                            {listing.brand && (
                              <p className="text-sm text-muted-foreground">{listing.brand}</p>
                            )}
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <StatusBadge type="platform" value={listing.platform} />
                      </TableCell>
                      <TableCell>
                        <StatusBadge type="status" value={listing.status} />
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formatDate(listing.createdAt)}
                      </TableCell>
                      <TableCell className="text-right">
                        <DropdownMenu>
                          <DropdownMenuTrigger className="inline-flex items-center justify-center h-8 w-8 rounded-md text-sm font-medium hover:bg-muted hover:text-foreground outline-none">
                            <MoreHorizontal className="h-4 w-4" />
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem
                              onClick={() => navigate(`/listings/${listing.id}`)}
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              View
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => navigate(`/listings/${listing.id}/edit`)}
                            >
                              <Pencil className="mr-2 h-4 w-4" />
                              Edit
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => navigate(`/listings/${listing.id}/generate`)}
                            >
                              <Sparkles className="mr-2 h-4 w-4" />
                              Generate AI
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleDuplicate(listing.id)}>
                              <Copy className="mr-2 h-4 w-4" />
                              Duplicate
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            {listing.status === 'DRAFT' && (
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(listing.id, 'PUBLISHED')}
                              >
                                Publish
                              </DropdownMenuItem>
                            )}
                            {listing.status === 'PUBLISHED' && (
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(listing.id, 'ARCHIVED')}
                              >
                                Archive
                              </DropdownMenuItem>
                            )}
                            {listing.status === 'ARCHIVED' && (
                              <DropdownMenuItem
                                onClick={() => handleStatusChange(listing.id, 'DRAFT')}
                              >
                                Move to Draft
                              </DropdownMenuItem>
                            )}
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              onClick={() => handleDelete(listing.id, listing.productName)}
                              className="text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>

              {/* Pagination */}
              {data.totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t">
                  <p className="text-sm text-muted-foreground">
                    Showing {page * size + 1} to {Math.min((page + 1) * size, data.totalElements)} of{' '}
                    {data.totalElements} listings
                  </p>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(page - 1)}
                      disabled={page === 0}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage(page + 1)}
                      disabled={page >= data.totalPages - 1}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </>
          ) : (
            <EmptyState
              title="No listings found"
              description="Create your first product listing to get started."
              action={{
                label: 'Create Listing',
                onClick: () => navigate('/listings/new'),
              }}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
