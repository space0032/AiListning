import { useSearchParams, Link } from 'react-router-dom';
import { useEffect } from 'react';
import { useVerifyEmail } from '@/hooks';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/atoms/loading-spinner';
import { CheckCircle, XCircle } from 'lucide-react';

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const { mutate, isPending, isSuccess } = useVerifyEmail();

  useEffect(() => {
    if (token) {
      mutate(token);
    }
  }, [token, mutate]);

  if (!token) {
    return (
      <Card>
        <CardContent className="pt-6 text-center">
          <XCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">Invalid Verification Link</h2>
          <p className="text-muted-foreground mb-4">
            The verification link is invalid or missing.
          </p>
          <Button asChild>
            <Link to="/login">Go to Login</Link>
          </Button>
        </CardContent>
      </Card>
    );
  }

  if (isPending) {
    return (
      <Card>
        <CardContent className="pt-6 text-center">
          <LoadingSpinner className="mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">Verifying your email...</h2>
          <p className="text-muted-foreground">
            Please wait while we verify your email address.
          </p>
        </CardContent>
      </Card>
    );
  }

  if (isSuccess) {
    return (
      <Card>
        <CardContent className="pt-6 text-center">
          <CheckCircle className="h-12 w-12 text-green-600 mx-auto mb-4" />
          <h2 className="text-xl font-semibold mb-2">Email Verified!</h2>
          <p className="text-muted-foreground mb-4">
            Your email has been successfully verified.
          </p>
          <Button asChild>
            <Link to="/dashboard">Go to Dashboard</Link>
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent className="pt-6 text-center">
        <XCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
        <h2 className="text-xl font-semibold mb-2">Verification Failed</h2>
        <p className="text-muted-foreground mb-4">
          The verification link may have expired or is invalid.
        </p>
        <Button asChild>
          <Link to="/login">Go to Login</Link>
        </Button>
      </CardContent>
    </Card>
  );
}
