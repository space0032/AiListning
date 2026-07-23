import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
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
import { useCreateListing } from '@/hooks';
import { ArrowLeft, Save } from 'lucide-react';

export default function CreateListingPage() {
  const navigate = useNavigate();
  const createMutation = useCreateListing();

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<ListingInput>({
    resolver: zodResolver(listingSchema),
    defaultValues: {
      platform: undefined,
    },
  });

  const platform = watch('platform');

  const onSubmit = (data: ListingInput) => {
    createMutation.mutate(data, {
      onSuccess: (response) => {
        navigate(`/listings/${response.data.id}`);
      },
    });
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Create Listing</h1>
          <p className="text-muted-foreground">
            Add a new product listing
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Product Details</CardTitle>
            <CardDescription>
              Enter the basic information about your product
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

        <div className="flex gap-4">
          <Button type="button" variant="outline" onClick={() => navigate(-1)}>
            Cancel
          </Button>
          <Button type="submit" disabled={createMutation.isPending}>
            <Save className="mr-2 h-4 w-4" />
            {createMutation.isPending ? 'Creating...' : 'Create Listing'}
          </Button>
        </div>
      </form>
    </div>
  );
}
