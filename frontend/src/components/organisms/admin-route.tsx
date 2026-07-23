import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/stores';

export function AdminRoute() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const user = useAuthStore((state) => state.user);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
}
