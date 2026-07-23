import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Sparkles, Zap, Globe, Shield, ArrowRight } from 'lucide-react';

const features = [
  {
    icon: <Sparkles className="h-6 w-6" />,
    title: 'AI-Powered Content',
    description: 'Generate SEO-optimized product titles, descriptions, and bullet points with advanced AI.',
  },
  {
    icon: <Globe className="h-6 w-6" />,
    title: 'Multi-Platform',
    description: 'Create listings for Amazon, Flipkart, Meesho, and Shopify from a single dashboard.',
  },
  {
    icon: <Zap className="h-6 w-6" />,
    title: 'Instant Generation',
    description: 'Get professional product listings in seconds, not hours. Save time and boost sales.',
  },
  {
    icon: <Shield className="h-6 w-6" />,
    title: 'Secure & Private',
    description: 'Your product data is encrypted and secure. We never share your information.',
  },
];

export default function LandingPage() {
  return (
    <div className="flex flex-col">
      {/* Hero Section */}
      <section className="py-20 px-4 bg-gradient-to-b from-background to-muted/50">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold tracking-tight mb-6">
            Generate <span className="text-primary">AI-Powered</span> Product Listings
          </h1>
          <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
            Create optimized, SEO-friendly product listings for Amazon, Flipkart, Meesho, and Shopify
            in seconds with our advanced AI platform.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button size="lg" asChild>
              <Link to="/register">
                Get Started Free
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" asChild>
              <Link to="/login">Sign In</Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 px-4">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-12">
            Everything you need to create better listings
          </h2>
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feature, index) => (
              <Card key={index} className="border-0 shadow-md hover:shadow-lg transition-shadow">
                <CardContent className="pt-6">
                  <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center text-primary mb-4">
                    {feature.icon}
                  </div>
                  <h3 className="font-semibold text-lg mb-2">{feature.title}</h3>
                  <p className="text-muted-foreground text-sm">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4 bg-primary text-primary-foreground">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl font-bold mb-4">
            Ready to transform your product listings?
          </h2>
          <p className="text-primary-foreground/80 mb-8 text-lg">
            Join thousands of sellers who are already using AI to boost their sales.
          </p>
          <Button size="lg" variant="secondary" asChild>
            <Link to="/register">
              Start for Free
              <ArrowRight className="ml-2 h-5 w-5" />
            </Link>
          </Button>
        </div>
      </section>
    </div>
  );
}
