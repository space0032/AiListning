import { useParams, useNavigate } from 'react-router-dom';
import { useListing, useListingAiGeneration } from '@/hooks';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { StatusBadge } from '@/components/molecules/status-badge';
import { ArrowLeft, Sparkles, Copy, Check } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';

export default function GenerateListingPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: listing, isLoading: listingLoading } = useListing(Number(id));
  const generateMutation = useListingAiGeneration(Number(id));
  const [copiedField, setCopiedField] = useState<string | null>(null);

  const handleGenerate = () => {
    generateMutation.mutate();
  };

  const copyToClipboard = (text: string, field: string) => {
    navigator.clipboard.writeText(text);
    setCopiedField(field);
    toast.success('Copied to clipboard');
    setTimeout(() => setCopiedField(null), 2000);
  };

  const hasGeneratedContent = listing?.seoTitle != null;

  if (listingLoading) {
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
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Generate AI Content</h1>
          <p className="text-muted-foreground">
            Create optimized listing content for {listing.productName}
          </p>
        </div>
      </div>

      {/* Product Summary */}
      <Card>
        <CardHeader>
          <CardTitle>Product Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-lg bg-muted flex items-center justify-center">
              <Sparkles className="h-8 w-8 text-muted-foreground" />
            </div>
            <div>
              <h3 className="font-semibold text-lg">{listing.productName}</h3>
              <div className="flex items-center gap-2 mt-1">
                <StatusBadge type="platform" value={listing.platform} />
                {listing.category && (
                  <span className="text-sm text-muted-foreground">{listing.category}</span>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Generate Button */}
      {!hasGeneratedContent && (
        <Card>
          <CardContent className="pt-6">
            <div className="text-center py-8">
              <Sparkles className="h-12 w-12 text-primary mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Ready to generate?</h3>
              <p className="text-muted-foreground mb-6">
                Our AI will create SEO-optimized content for your product listing.
              </p>
              <Button
                size="lg"
                onClick={handleGenerate}
                disabled={generateMutation.isPending}
              >
                {generateMutation.isPending ? (
                  <>
                    <LoadingSpinner size="sm" className="mr-2" />
                    Generating...
                  </>
                ) : (
                  <>
                    <Sparkles className="mr-2 h-5 w-5" />
                    Generate Content
                  </>
                )}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Generated Content */}
      {hasGeneratedContent && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">Generated Content</h2>
            <Button variant="outline" onClick={handleGenerate} disabled={generateMutation.isPending}>
              <Sparkles className="mr-2 h-4 w-4" />
              Regenerate
            </Button>
          </div>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>SEO Title</CardTitle>
                <Button variant="ghost" size="sm"
                  onClick={() => copyToClipboard(listing.seoTitle!, 'seoTitle')}>
                  {copiedField === 'seoTitle' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                </Button>
              </div>
            </CardHeader>
            <CardContent><p>{listing.seoTitle}</p></CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Bullet Points</CardTitle>
                <Button variant="ghost" size="sm"
                  onClick={() => copyToClipboard(listing.bulletPoints!, 'bulletPoints')}>
                  {copiedField === 'bulletPoints' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                </Button>
              </div>
            </CardHeader>
            <CardContent><p className="whitespace-pre-line">{listing.bulletPoints}</p></CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Description</CardTitle>
                <Button variant="ghost" size="sm"
                  onClick={() => copyToClipboard(listing.description!, 'description')}>
                  {copiedField === 'description' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                </Button>
              </div>
            </CardHeader>
            <CardContent><p className="whitespace-pre-line">{listing.description}</p></CardContent>
          </Card>

          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Tags</CardTitle>
                  <Button variant="ghost" size="sm"
                    onClick={() => copyToClipboard(listing.tags!, 'tags')}>
                    {copiedField === 'tags' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                  </Button>
                </div>
              </CardHeader>
              <CardContent><p>{listing.tags}</p></CardContent>
            </Card>

            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>Keywords</CardTitle>
                  <Button variant="ghost" size="sm"
                    onClick={() => copyToClipboard(listing.keywords!, 'keywords')}>
                    {copiedField === 'keywords' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                  </Button>
                </div>
              </CardHeader>
              <CardContent><p>{listing.keywords}</p></CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Meta Description</CardTitle>
                <Button variant="ghost" size="sm"
                  onClick={() => copyToClipboard(listing.metaDescription!, 'metaDescription')}>
                  {copiedField === 'metaDescription' ? <Check className="h-4 w-4" /> : <Copy className="h-4 w-4" />}
                </Button>
              </div>
            </CardHeader>
            <CardContent><p>{listing.metaDescription}</p></CardContent>
          </Card>

          {listing.modelUsed && (
            <div className="text-sm text-muted-foreground text-center">
              Generated using {listing.modelUsed} in {listing.generationTimeMs}ms
            </div>
          )}
        </div>
      )}
    </div>
  );
}
