export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  enabled: boolean;
  emailVerified: boolean;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}
