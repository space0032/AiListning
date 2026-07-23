import { useGenerationStats } from '@/hooks';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { formatNumber, formatDuration } from '@/lib/formatters';
import { BarChart3, Clock, CheckCircle, TrendingUp } from 'lucide-react';

export default function AdminAnalyticsPage() {
  const stats = useGenerationStats();

  if (stats.isLoading) {
    return <LoadingSpinner className="py-24" />;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Analytics</h1>
        <p className="text-muted-foreground">
          AI generation statistics and insights
        </p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Generations</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatNumber(stats.data?.totalGenerations || 0)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Success Rate</CardTitle>
            <CheckCircle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {((stats.data?.successRate || 0) * 100).toFixed(1)}%
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg. Generation Time</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatDuration(stats.data?.avgGenerationTimeMs || 0)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Platforms Used</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats.data?.generationsByPlatform
                ? Object.keys(stats.data.generationsByPlatform).length
                : 0}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Platform Breakdown */}
      <Card>
        <CardHeader>
          <CardTitle>Generations by Platform</CardTitle>
        </CardHeader>
        <CardContent>
          {stats.data?.generationsByPlatform ? (
            <div className="space-y-4">
              {Object.entries(stats.data.generationsByPlatform).map(([platform, count]) => (
                <div key={platform} className="flex items-center">
                  <span className="w-24 font-medium">{platform}</span>
                  <div className="flex-1 mx-4">
                    <div className="h-4 bg-muted rounded-full overflow-hidden">
                      <div
                        className="h-full bg-primary"
                        style={{
                          width: `${
                            (count / Math.max(...Object.values(stats.data!.generationsByPlatform))) *
                            100
                          }%`,
                        }}
                      />
                    </div>
                  </div>
                  <span className="w-16 text-right text-sm text-muted-foreground">
                    {formatNumber(count)}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">No data available</p>
          )}
        </CardContent>
      </Card>

      {/* Daily Generations */}
      <Card>
        <CardHeader>
          <CardTitle>Generations Over Time</CardTitle>
        </CardHeader>
        <CardContent>
          {stats.data?.generationsByDay && stats.data.generationsByDay.length > 0 ? (
            <div className="h-64 flex items-end gap-1">
              {stats.data.generationsByDay.slice(-14).map((day, index) => {
                const maxCount = Math.max(...stats.data!.generationsByDay.map((d) => d.count));
                const height = maxCount > 0 ? (day.count / maxCount) * 100 : 0;
                return (
                  <div
                    key={index}
                    className="flex-1 flex flex-col items-center"
                  >
                    <div
                      className="w-full bg-primary rounded-t"
                      style={{ height: `${height}%`, minHeight: day.count > 0 ? '4px' : '0' }}
                    />
                    <span className="text-xs text-muted-foreground mt-2">
                      {new Date(day.date).getDate()}
                    </span>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">No data available</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
