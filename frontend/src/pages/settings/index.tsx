import { useAuthStore } from '@/stores';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { formatDate } from '@/lib/formatters';
import { User, Mail, Shield, Calendar } from 'lucide-react';

export default function SettingsPage() {
  const user = useAuthStore((state) => state.user);

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Settings</h1>
        <p className="text-muted-foreground">
          Manage your account settings
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Profile Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center">
              <User className="h-8 w-8 text-muted-foreground" />
            </div>
            <div>
              <h3 className="text-lg font-semibold">{user?.fullName || user?.username}</h3>
              <p className="text-muted-foreground">{user?.email}</p>
            </div>
          </div>

          <div className="grid gap-4 pt-4 border-t">
            <div className="flex items-center gap-3">
              <Mail className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Email</p>
                <p>{user?.email}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Shield className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Role</p>
                <Badge variant={user?.role === 'ROLE_ADMIN' ? 'default' : 'secondary'}>
                  {user?.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}
                </Badge>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Member Since</p>
                <p>{user?.createdAt ? formatDate(user.createdAt) : '-'}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Account Status</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <span>Email Verified</span>
            <Badge variant={user?.emailVerified ? 'default' : 'destructive'}>
              {user?.emailVerified ? 'Verified' : 'Not Verified'}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <span>Account Status</span>
            <Badge variant={user?.enabled ? 'default' : 'destructive'}>
              {user?.enabled ? 'Active' : 'Disabled'}
            </Badge>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
