import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuthStore } from '@/stores';
import { useUpdateProfile, useChangePassword } from '@/hooks';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { formatDate } from '@/lib/formatters';
import { User, Mail, Shield, Calendar, Save, Lock } from 'lucide-react';

const profileSchema = z.object({
  fullName: z.string().min(2, 'Full name must be at least 2 characters').max(100),
  email: z.string().email('Invalid email address'),
});

const passwordSchema = z.object({
  currentPassword: z.string().min(1, 'Current password is required'),
  newPassword: z.string().min(8, 'Password must be at least 8 characters').max(128),
  confirmPassword: z.string().min(1, 'Please confirm your password'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type ProfileInput = z.infer<typeof profileSchema>;
type PasswordInput = z.infer<typeof passwordSchema>;

export default function SettingsPage() {
  const user = useAuthStore((state) => state.user);
  const updateUser = useAuthStore((state) => state.updateUser);
  const updateProfileMutation = useUpdateProfile();
  const changePasswordMutation = useChangePassword();

  const {
    register: registerProfile,
    handleSubmit: handleProfileSubmit,
    formState: { errors: profileErrors },
  } = useForm<ProfileInput>({
    resolver: zodResolver(profileSchema),
    values: {
      fullName: user?.fullName || '',
      email: user?.email || '',
    },
  });

  const {
    register: registerPassword,
    handleSubmit: handlePasswordSubmit,
    reset: resetPasswordForm,
    formState: { errors: passwordErrors },
  } = useForm<PasswordInput>({
    resolver: zodResolver(passwordSchema),
  });

  const onProfileSubmit = (data: ProfileInput) => {
    updateProfileMutation.mutate(data, {
      onSuccess: (response) => {
        if (response.data) {
          updateUser(response.data);
        }
      },
    });
  };

  const onPasswordSubmit = (data: PasswordInput) => {
    changePasswordMutation.mutate(
      {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      },
      {
        onSuccess: () => resetPasswordForm(),
      }
    );
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Settings</h1>
        <p className="text-muted-foreground">
          Manage your account settings
        </p>
      </div>

      {/* Profile Form */}
      <Card>
        <CardHeader>
          <CardTitle>Profile Information</CardTitle>
          <CardDescription>Update your name and email address</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleProfileSubmit(onProfileSubmit)} className="space-y-4">
            <div className="flex items-center gap-4 mb-6">
              <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center">
                <User className="h-8 w-8 text-muted-foreground" />
              </div>
              <div>
                <h3 className="text-lg font-semibold">{user?.fullName || user?.username}</h3>
                <p className="text-muted-foreground">{user?.email}</p>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="fullName">Full Name</Label>
              <Input
                id="fullName"
                placeholder="Your full name"
                {...registerProfile('fullName')}
              />
              {profileErrors.fullName && (
                <p className="text-sm text-destructive">{profileErrors.fullName.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="Your email address"
                {...registerProfile('email')}
              />
              {profileErrors.email && (
                <p className="text-sm text-destructive">{profileErrors.email.message}</p>
              )}
            </div>

            <Button type="submit" disabled={updateProfileMutation.isPending}>
              <Save className="mr-2 h-4 w-4" />
              {updateProfileMutation.isPending ? 'Saving...' : 'Save Changes'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* Change Password Form */}
      <Card>
        <CardHeader>
          <CardTitle>Change Password</CardTitle>
          <CardDescription>Update your password to keep your account secure</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handlePasswordSubmit(onPasswordSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Current Password</Label>
              <Input
                id="currentPassword"
                type="password"
                placeholder="Enter current password"
                {...registerPassword('currentPassword')}
              />
              {passwordErrors.currentPassword && (
                <p className="text-sm text-destructive">{passwordErrors.currentPassword.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="newPassword">New Password</Label>
              <Input
                id="newPassword"
                type="password"
                placeholder="Enter new password"
                {...registerPassword('newPassword')}
              />
              {passwordErrors.newPassword && (
                <p className="text-sm text-destructive">{passwordErrors.newPassword.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirm New Password</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="Confirm new password"
                {...registerPassword('confirmPassword')}
              />
              {passwordErrors.confirmPassword && (
                <p className="text-sm text-destructive">{passwordErrors.confirmPassword.message}</p>
              )}
            </div>

            <Button type="submit" disabled={changePasswordMutation.isPending}>
              <Lock className="mr-2 h-4 w-4" />
              {changePasswordMutation.isPending ? 'Changing...' : 'Change Password'}
            </Button>
          </form>
        </CardContent>
      </Card>

      {/* Account Status */}
      <Card>
        <CardHeader>
          <CardTitle>Account Status</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Mail className="h-4 w-4 text-muted-foreground" />
              <span>Email Verified</span>
            </div>
            <Badge variant={user?.emailVerified ? 'default' : 'destructive'}>
              {user?.emailVerified ? 'Verified' : 'Not Verified'}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Shield className="h-4 w-4 text-muted-foreground" />
              <span>Account Status</span>
            </div>
            <Badge variant={user?.enabled ? 'default' : 'destructive'}>
              {user?.enabled ? 'Active' : 'Disabled'}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span>Member Since</span>
            </div>
            <span className="text-sm text-muted-foreground">
              {user?.createdAt ? formatDate(user.createdAt) : '-'}
            </span>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
