import { useParams, useNavigate } from 'react-router-dom';
import { useListing, useDeleteListing, useUploadImage } from '@/hooks';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/molecules/status-badge';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { formatDateTime } from '@/lib/formatters';
import {
  ArrowLeft,
  Pencil,
  Trash2,
  Sparkles,
  Upload,
} from 'lucide-react';
import { useCallback, useRef } from 'react';

export default function ListingDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: listing, isLoading } = useListing(Number(id));
  const deleteMutation = useDeleteListing();
  const uploadMutation = useUploadImage();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDelete = () => {
    if (window.confirm('Are you sure you want to delete this listing?')) {
      deleteMutation.mutate(Number(id), {
        onSuccess: () => navigate('/listings'),
      });
    }
  };

  const handleImageUpload = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
        uploadMutation.mutate({ id: Number(id), file });
      }
    },
    [id, uploadMutation]
  );

  if (isLoading) {
    return <LoadingSpinner className="py-24" />;
  }

  if (!listing) {
    return (
      <div className="text-center py-24">
        <p className="text-muted-foreground">Listing not found</p>
        <Button variant="link" onClick={() => navigate('/listings')}>
          Back to listings
        </Button>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold">{listing.productName}</h1>
            <div className="flex items-center gap-2 mt-1">
              <StatusBadge type="platform" value={listing.platform} />
              <StatusBadge type="status" value={listing.status} />
            </div>
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate(`/listings/${id}/edit`)}>
            <Pencil className="mr-2 h-4 w-4" />
            Edit
          </Button>
          <Button onClick={() => navigate(`/listings/${id}/generate`)}>
            <Sparkles className="mr-2 h-4 w-4" />
            Generate AI
          </Button>
        </div>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Product Details */}
        <Card>
          <CardHeader>
            <CardTitle>Product Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground">Description</p>
              <p className="mt-1">{listing.productDescription || 'No description'}</p>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Category</p>
                <p className="mt-1">{listing.category || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Brand</p>
                <p className="mt-1">{listing.brand || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Material</p>
                <p className="mt-1">{listing.material || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Color</p>
                <p className="mt-1">{listing.color || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Size</p>
                <p className="mt-1">{listing.size || '-'}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Image */}
        <Card>
          <CardHeader>
            <CardTitle>Product Image</CardTitle>
          </CardHeader>
          <CardContent>
            {listing.imageUrl ? (
              <div className="relative">
                <img
                  src={listing.imageUrl}
                  alt={listing.productName}
                  className="w-full h-64 object-cover rounded-lg"
                />
                <Button
                  variant="secondary"
                  size="sm"
                  className="absolute top-2 right-2"
                  onClick={() => fileInputRef.current?.click()}
                >
                  <Upload className="mr-2 h-4 w-4" />
                  Change
                </Button>
              </div>
            ) : (
              <div
                className="w-full h-64 border-2 border-dashed rounded-lg flex flex-col items-center justify-center cursor-pointer hover:bg-muted/50 transition-colors"
                onClick={() => fileInputRef.current?.click()}
              >
                <Upload className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground">Click to upload image</p>
                <p className="text-sm text-muted-foreground">PNG, JPG up to 5MB</p>
              </div>
            )}
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleImageUpload}
            />
          </CardContent>
        </Card>

        {/* AI Content */}
        <Card className="md:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>AI Generated Content</CardTitle>
              {!listing.seoTitle && (
                <Button size="sm" onClick={() => navigate(`/listings/${id}/generate`)}>
                  <Sparkles className="mr-2 h-4 w-4" />
                  Generate Content
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            {listing.seoTitle ? (
              <>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">SEO Title</p>
                  <p className="mt-1">{listing.seoTitle}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Bullet Points</p>
                  <p className="mt-1 whitespace-pre-line">{listing.bulletPoints}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Description</p>
                  <p className="mt-1 whitespace-pre-line">{listing.description}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Tags</p>
                    <p className="mt-1">{listing.tags}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Keywords</p>
                    <p className="mt-1">{listing.keywords}</p>
                  </div>
                </div>
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Meta Description</p>
                  <p className="mt-1">{listing.metaDescription}</p>
                </div>
              </>
            ) : (
              <p className="text-muted-foreground text-center py-8">
                No AI content generated yet. Click "Generate Content" to create optimized listing content.
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Metadata */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between text-sm text-muted-foreground">
            <span>Created: {formatDateTime(listing.createdAt)}</span>
            <span>Updated: {formatDateTime(listing.updatedAt)}</span>
          </div>
        </CardContent>
      </Card>

      {/* Actions */}
      <div className="flex justify-end gap-2">
        <Button variant="outline" onClick={handleDelete} className="text-destructive hover:text-destructive">
          <Trash2 className="mr-2 h-4 w-4" />
          Delete
        </Button>
      </div>
    </div>
  );
}
