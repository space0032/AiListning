# Milestone 3: User Authentication & JWT

## Goal
Implement complete JWT-based authentication with refresh tokens, email verification, and password reset flows.

## Auth Flow

```
┌──────────┐                    ┌──────────┐                    ┌──────────┐
│  Client  │                    │  Server  │                    │Database  │
└────┬─────┘                    └────┬─────┘                    └────┬─────┘
     │  POST /auth/register          │                              │
     │  {username, email, password}  │                              │
     │──────────────────────────────>│                              │
     │                               │  Check username/email        │
     │                               │─────────────────────────────>│
     │                               │  Hash password               │
     │                               │  Save user                   │
     │                               │─────────────────────────────>│
     │                               │  Generate access token       │
     │                               │  Generate refresh token      │
     │                               │  Save refresh token          │
     │                               │─────────────────────────────>│
     │  {accessToken, refreshToken}  │                              │
     │<──────────────────────────────│                              │
     │                               │                              │
     │  GET /listings                │                              │
     │  Authorization: Bearer <jwt>  │                              │
     │──────────────────────────────>│                              │
     │                               │  Extract username from JWT   │
     │                               │  Load user from DB           │
     │                               │─────────────────────────────>│
     │                               │  Set SecurityContext         │
     │                               │  Return data                 │
     │  {data}                       │                              │
     │<──────────────────────────────│                              │
     │                               │                              │
     │  POST /auth/refresh           │                              │
     │  {refreshToken}               │                              │
     │──────────────────────────────>│                              │
     │                               │  Validate refresh token      │
     │                               │─────────────────────────────>│
     │                               │  Delete used token           │
     │                               │─────────────────────────────>│
     │                               │  Generate new pair           │
     │                               │  Save new refresh token      │
     │                               │─────────────────────────────>│
     │  {accessToken, refreshToken}  │                              │
     │<──────────────────────────────│                              │
```

## Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/auth/register` | No | Register new user |
| POST | `/auth/login` | No | Login, get tokens |
| POST | `/auth/refresh` | No | Exchange refresh token |
| POST | `/auth/logout` | No | Revoke refresh token |
| POST | `/auth/forgot-password` | No | Request password reset |
| POST | `/auth/reset-password` | No | Reset with token |
| GET | `/auth/verify-email` | No | Verify email address |

## Key Components

### JwtTokenProvider
- Generates access tokens (15 min default)
- Generates refresh tokens (7 days default)
- Validates tokens
- Extracts username from token

### JwtAuthenticationFilter
- Runs before every request
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token and sets SecurityContext

### Refresh Token Flow
1. Client sends refresh token
2. Server validates token exists in DB and not expired
3. Server deletes used token (single-use)
4. Server generates new access + refresh token pair
5. New refresh token stored in DB

### Security Features
- **Single-use refresh tokens**: Each refresh token is deleted after use
- **Password reset revokes all sessions**: After reset, all refresh tokens deleted
- **Email enumeration prevention**: Forgot password always returns success
- **CORS locked to dev origins**: localhost:3000, 5173, 5174
- **Stateless sessions**: No HTTP session, JWT only

## Files Modified/Created
- `AuthService.java` - Added logout, logoutAll, resetPassword
- `AuthServiceImpl.java` - DB-backed refresh tokens
- `AuthController.java` - New endpoints
- `JwtTokenProvider.java` - Added getRefreshExpirationSeconds, getTokenType
- `RefreshTokenRequest.java` - New DTO
- `ResetPasswordRequest.java` - New DTO
- `CorsConfig.java` - CORS configuration
- `SecurityConfig.java` - Added CORS support
- `RefreshTokenRepository.java` - Token DB operations
