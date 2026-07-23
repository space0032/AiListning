import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useListing, useUpdateListing, useUploadImage } from '@/hooks';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { listingSchema, type ListingInput } from '@/lib/validations';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { ArrowLeft, Save, Image as ImageIcon, X, Upload } from 'lucide-react';

export default function EditListingPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: listing, isLoading } = useListing(Number(id));
  const updateMutation = useUpdateListing();
  const uploadImageMutation = useUploadImage();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<ListingInput>({
    resolver: zodResolver(listingSchema),
    values: listing
      ? {
          productName: listing.productName,
          productDescription: listing.productDescription || '',
          category: listing.category || '',
          brand: listing.brand || '',
          material: listing.material || '',
          color: listing.color || '',
          size: listing.size || '',
          platform: listing.platform,
        }
      : undefined,
  });

  const platform = watch('platform');

  const onDrop = useCallback((acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (file) {
      setSelectedFile(file);
      const url = URL.createObjectURL(file);
      setPreviewUrl(url);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpeg', '.jpg', '.png', '.gif', '.webp'],
    },
    maxFiles: 1,
    maxSize: 5 * 1024 * 1024,
  });

  const removeFile = () => {
    setSelectedFile(null);
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl);
    }
    setPreviewUrl(null);
  };

  const onSubmit = (data: ListingInput) => {
    updateMutation.mutate(
      { id: Number(id), data },
      {
        onSuccess: () => {
          if (selectedFile) {
            uploadImageMutation.mutate(
              { id: Number(id), file: selectedFile },
              {
                onSuccess: () => navigate(`/listings/${id}`),
                onError: () => navigate(`/listings/${id}`),
              }
            );
          } else {
            navigate(`/listings/${id}`);
          }
        },
      }
    );
  };

  const isSubmitting = updateMutation.isPending || uploadImageMutation.isPending;

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
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Edit Listing</h1>
          <p className="text-muted-foreground">
            Update product information
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Product Details</CardTitle>
            <CardDescription>
              Update the basic information about your product
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="productName">Product Name *</Label>
              <Input
                id="productName"
                placeholder="e.g., Wireless Bluetooth Headphones"
                {...register('productName')}
              />
              {errors.productName && (
                <p className="text-sm text-destructive">{errors.productName.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="productDescription">Description</Label>
              <Textarea
                id="productDescription"
                placeholder="Describe your product..."
                rows={4}
                {...register('productDescription')}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="category">Category</Label>
                <Input
                  id="category"
                  placeholder="e.g., Electronics"
                  {...register('category')}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="brand">Brand</Label>
                <Input
                  id="brand"
                  placeholder="e.g., Sony"
                  {...register('brand')}
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="material">Material</Label>
                <Input
                  id="material"
                  placeholder="e.g., Plastic"
                  {...register('material')}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="color">Color</Label>
                <Input
                  id="color"
                  placeholder="e.g., Black"
                  {...register('color')}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="size">Size</Label>
                <Input
                  id="size"
                  placeholder="e.g., Medium"
                  {...register('size')}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Platform</CardTitle>
            <CardDescription>
              Select the e-commerce platform for this listing
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Select
              value={platform}
              onValueChange={(value) => setValue('platform', value as any, { shouldValidate: true })}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a platform" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="AMAZON">Amazon</SelectItem>
                <SelectItem value="FLIPKART">Flipkart</SelectItem>
                <SelectItem value="MEESHO">Meesho</SelectItem>
                <SelectItem value="SHOPIFY">Shopify</SelectItem>
              </SelectContent>
            </Select>
            {errors.platform && (
              <p className="text-sm text-destructive mt-2">{errors.platform.message}</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Product Image</CardTitle>
            <CardDescription>
              Update the product image (optional)
            </CardDescription>
          </CardHeader>
          <CardContent>
            {previewUrl ? (
              <div className="relative">
                <img
                  src={previewUrl}
                  alt="Preview"
                  className="w-full h-48 object-cover rounded-lg"
                />
                <Button
                  type="button"
                  variant="destructive"
                  size="icon"
                  className="absolute top-2 right-2"
                  onClick={removeFile}
                >
                  <X className="h-4 w-4" />
                </Button>
                <p className="text-sm text-muted-foreground mt-2">{selectedFile?.name}</p>
              </div>
            ) : listing.imageUrl ? (
              <div className="space-y-4">
                <div className="relative">
                  <img
                    src={listing.imageUrl}
                    alt={listing.productName}
                    className="w-full h-48 object-cover rounded-lg"
                  />
                  <p className="text-sm text-muted-foreground mt-2">Current image</p>
                </div>
                <div
                  {...getRootProps()}
                  className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-colors ${
                    isDragActive ? 'border-primary bg-primary/5' : 'border-muted-foreground/25'
                  }`}
                >
                  <input {...getInputProps()} />
                  <Upload className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
                  <p className="text-sm text-muted-foreground">
                    {isDragActive ? 'Drop new image here' : 'Click to replace with new image'}
                  </p>
                </div>
              </div>
            ) : (
              <div
                {...getRootProps()}
                className={`border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors ${
                  isDragActive ? 'border-primary bg-primary/5' : 'border-muted-foreground/25'
                }`}
              >
                <input {...getInputProps()} />
                <ImageIcon className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                <p className="text-muted-foreground">
                  {isDragActive ? 'Drop the image here' : 'Drag & drop an image here, or click to select'}
                </p>
                <p className="text-sm text-muted-foreground mt-2">
                  JPEG, PNG, GIF, WebP (max 5MB)
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <div className="flex gap-4">
          <Button type="button" variant="outline" onClick={() => navigate(-1)}>
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            <Save className="mr-2 h-4 w-4" />
            {isSubmitting ? 'Saving...' : 'Save Changes'}
          </Button>
        </div>
      </form>
    </div>
  );
}
