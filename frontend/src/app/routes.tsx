import { createBrowserRouter } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { DashboardLayout } from '@/components/templates/dashboard-layout';
import { AuthLayout } from '@/components/templates/auth-layout';
import { PublicLayout } from '@/components/templates/public-layout';
import { ProtectedRoute } from '@/components/organisms/protected-route';
import { AdminRoute } from '@/components/organisms/admin-route';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';

const Landing = lazy(() => import('@/pages/public/landing'));
const Login = lazy(() => import('@/pages/public/login'));
const Register = lazy(() => import('@/pages/public/register'));
const ForgotPassword = lazy(() => import('@/pages/public/forgot-password'));
const ResetPassword = lazy(() => import('@/pages/public/reset-password'));
const VerifyEmail = lazy(() => import('@/pages/public/verify-email'));
const Dashboard = lazy(() => import('@/pages/dashboard/index'));
const Listings = lazy(() => import('@/pages/listings/index'));
const ListingDetail = lazy(() => import('@/pages/listings/[id]/index'));
const CreateListing = lazy(() => import('@/pages/listings/new'));
const EditListing = lazy(() => import('@/pages/listings/[id]/edit'));
const GenerateListing = lazy(() => import('@/pages/listings/[id]/generate'));
const AdminDashboard = lazy(() => import('@/pages/admin/index'));
const AdminUsers = lazy(() => import('@/pages/admin/users'));
const AdminAnalytics = lazy(() => import('@/pages/admin/analytics'));
const Settings = lazy(() => import('@/pages/settings/index'));
const Unauthorized = lazy(() => import('@/pages/public/unauthorized'));
const NotFound = lazy(() => import('@/pages/public/not-found'));

const SuspenseWrapper = ({ children }: { children: React.ReactNode }) => (
  <Suspense fallback={<LoadingSpinner className="h-screen" />}>
    {children}
  </Suspense>
);

export const router = createBrowserRouter([
  {
    path: '/',
    element: <PublicLayout />,
    children: [
      { index: true, element: <SuspenseWrapper><Landing /></SuspenseWrapper> },
    ],
  },
  {
    path: '/',
    element: <AuthLayout />,
    children: [
      { path: 'login', element: <SuspenseWrapper><Login /></SuspenseWrapper> },
      { path: 'register', element: <SuspenseWrapper><Register /></SuspenseWrapper> },
      { path: 'forgot-password', element: <SuspenseWrapper><ForgotPassword /></SuspenseWrapper> },
      { path: 'reset-password', element: <SuspenseWrapper><ResetPassword /></SuspenseWrapper> },
      { path: 'verify-email', element: <SuspenseWrapper><VerifyEmail /></SuspenseWrapper> },
    ],
  },
  {
    path: '/',
    element: <ProtectedRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { path: 'dashboard', element: <SuspenseWrapper><Dashboard /></SuspenseWrapper> },
          { path: 'listings', element: <SuspenseWrapper><Listings /></SuspenseWrapper> },
          { path: 'listings/new', element: <SuspenseWrapper><CreateListing /></SuspenseWrapper> },
          { path: 'listings/:id', element: <SuspenseWrapper><ListingDetail /></SuspenseWrapper> },
          { path: 'listings/:id/edit', element: <SuspenseWrapper><EditListing /></SuspenseWrapper> },
          { path: 'listings/:id/generate', element: <SuspenseWrapper><GenerateListing /></SuspenseWrapper> },
          { path: 'settings', element: <SuspenseWrapper><Settings /></SuspenseWrapper> },
        ],
      },
    ],
  },
  {
    path: '/admin',
    element: <AdminRoute />,
    children: [
      {
        element: <DashboardLayout />,
        children: [
          { index: true, element: <SuspenseWrapper><AdminDashboard /></SuspenseWrapper> },
          { path: 'users', element: <SuspenseWrapper><AdminUsers /></SuspenseWrapper> },
          { path: 'analytics', element: <SuspenseWrapper><AdminAnalytics /></SuspenseWrapper> },
        ],
      },
    ],
  },
  { path: '/unauthorized', element: <SuspenseWrapper><Unauthorized /></SuspenseWrapper> },
  { path: '*', element: <SuspenseWrapper><NotFound /></SuspenseWrapper> },
]);
