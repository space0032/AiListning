import { Outlet } from 'react-router-dom';
import { Sidebar } from '@/components/organisms/sidebar';
import { Header } from '@/components/organisms/header';
import { useUIStore } from '@/stores';
import { cn } from '@/lib/utils';

export function DashboardLayout() {
  const sidebarCollapsed = useUIStore((state) => state.sidebarCollapsed);

  return (
    <div className="min-h-screen bg-background">
      <Sidebar />
      <div
        className={cn(
          'transition-all duration-300',
          sidebarCollapsed ? 'lg:ml-16' : 'lg:ml-64'
        )}
      >
        <Header />
        <main className="p-4 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
