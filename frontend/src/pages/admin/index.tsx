import { useAdminOverview, useHealthCheck } from '@/hooks';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { Users, Package, Activity, Sparkles, Heart, AlertCircle } from 'lucide-react';
import { formatNumber } from '@/lib/formatters';

export default function AdminDashboardPage() {
  const stats = useAdminOverview();
  const health = useHealthCheck();

  const statCards = [
    {
      title: 'Total Users',
      value: stats.data?.totalUsers || 0,
      icon: <Users className="h-5 w-5" />,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
    },
    {
      title: 'Total Listings',
      value: stats.data?.totalListings || 0,
      icon: <Package className="h-5 w-5" />,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
    },
    {
      title: 'Active Users',
      value: stats.data?.activeUsers || 0,
      icon: <Activity className="h-5 w-5" />,
      color: 'text-purple-600',
      bgColor: 'bg-purple-100',
    },
    {
      title: 'AI Generations',
      value: stats.data?.aiGenerations || 0,
      icon: <Sparkles className="h-5 w-5" />,
      color: 'text-orange-600',
      bgColor: 'bg-orange-100',
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Admin Dashboard</h1>
        <p className="text-muted-foreground">
          System overview and management
        </p>
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

      {/* System Health */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            {health.data?.status === 'UP' ? (
              <Heart className="h-5 w-5 text-green-600" />
            ) : (
              <AlertCircle className="h-5 w-5 text-red-600" />
            )}
            System Health
          </CardTitle>
        </CardHeader>
        <CardContent>
          {health.isLoading ? (
            <LoadingSpinner />
          ) : health.data ? (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="font-medium">Status:</span>
                <span
                  className={`px-2 py-1 rounded text-sm ${
                    health.data.status === 'UP'
                      ? 'bg-green-100 text-green-800'
                      : 'bg-red-100 text-red-800'
                  }`}
                >
                  {health.data.status}
                </span>
              </div>
              {health.data.components && (
                <div className="grid gap-2">
                  {Object.entries(health.data.components).map(([name, component]) => (
                    <div key={name} className="flex items-center justify-between p-2 border rounded">
                      <span className="capitalize">{name}</span>
                      <span
                        className={`px-2 py-1 rounded text-xs ${
                          component.status === 'UP'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-red-100 text-red-800'
                        }`}
                      >
                        {component.status}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <p className="text-muted-foreground">Unable to fetch health status</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
