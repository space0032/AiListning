import { useListingStats, useListings } from '@/hooks';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, FileText, CheckCircle, Archive, Sparkles } from 'lucide-react';
import { formatNumber } from '@/lib/formatters';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function DashboardPage() {
  const stats = useListingStats();
  const recentListings = useListings({ page: 0, size: 5, sortBy: 'createdAt', sortDir: 'desc' });

  const statCards = [
    {
      title: 'Total Listings',
      value: stats.data?.totalListings || 0,
      icon: <Package className="h-5 w-5" />,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
    },
    {
      title: 'Drafts',
      value: stats.data?.draftCount || 0,
      icon: <FileText className="h-5 w-5" />,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-100',
    },
    {
      title: 'Published',
      value: stats.data?.publishedCount || 0,
      icon: <CheckCircle className="h-5 w-5" />,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
    },
    {
      title: 'Archived',
      value: stats.data?.archivedCount || 0,
      icon: <Archive className="h-5 w-5" />,
      color: 'text-gray-600',
      bgColor: 'bg-gray-100',
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-muted-foreground">
            Welcome back! Here's an overview of your listings.
          </p>
        </div>
        <Button asChild>
          <Link to="/listings/new">
            <Sparkles className="mr-2 h-4 w-4" />
            New Listing
          </Link>
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((stat, index) => (
          <Card key={index}>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">{stat.title}</CardTitle>
              <div className={`${stat.bgColor} ${stat.color} p-2 rounded-md`}>
                {stat.icon}
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{formatNumber(stat.value)}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Listings */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Recent Listings</CardTitle>
            <Button variant="ghost" asChild>
              <Link to="/listings">View all</Link>
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {recentListings.data?.content && recentListings.data.content.length > 0 ? (
            <div className="space-y-4">
              {recentListings.data.content.map((listing) => (
                <div
                  key={listing.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded bg-muted flex items-center justify-center">
                      <Package className="h-5 w-5 text-muted-foreground" />
                    </div>
                    <div>
                      <p className="font-medium">{listing.productName}</p>
                      <p className="text-sm text-muted-foreground">
                        {listing.platform} • {listing.status}
                      </p>
                    </div>
                  </div>
                  <Button variant="ghost" size="sm" asChild>
                    <Link to={`/listings/${listing.id}`}>View</Link>
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8">
              <p className="text-muted-foreground mb-4">No listings yet</p>
              <Button asChild>
                <Link to="/listings/new">
                  <Sparkles className="mr-2 h-4 w-4" />
                  Create your first listing
                </Link>
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
