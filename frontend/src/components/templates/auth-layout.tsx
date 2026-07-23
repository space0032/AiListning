import { Outlet } from 'react-router-dom';
import { Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';

export function AuthLayout() {
  return (
    <div className="min-h-screen flex">
      {/* Left panel - branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-primary relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-primary to-primary/80" />
        <div className="relative z-10 flex flex-col items-center justify-center p-12 text-primary-foreground">
          <Link to="/" className="flex items-center gap-3 mb-8">
            <Sparkles className="h-12 w-12" />
            <span className="text-3xl font-bold">AI Listing</span>
          </Link>
          <h2 className="text-2xl font-semibold text-center mb-4">
            Generate optimized product listings with AI
          </h2>
          <p className="text-center text-primary-foreground/80 max-w-md">
            Create SEO-friendly listings for Amazon, Flipkart, Meesho, and Shopify
            in seconds with our AI-powered platform.
          </p>
        </div>
      </div>

      {/* Right panel - form */}
      <div className="flex-1 flex items-center justify-center p-6 bg-background">
        <div className="w-full max-w-md">
          <Link to="/" className="flex items-center gap-2 mb-8 lg:hidden">
            <Sparkles className="h-6 w-6 text-primary" />
            <span className="font-bold text-xl">AI Listing</span>
          </Link>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
