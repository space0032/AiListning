# Architecture

## System Overview
AI E-commerce Product Listing Generator - A full-stack SaaS that generates optimized product listings for multiple e-commerce platforms using AI.

## Tech Stack

### Frontend
- **Framework**: React 18, TypeScript 5.5, Vite 5.4
- **Styling**: Tailwind CSS 4, shadcn/ui (Radix UI)
- **State**: TanStack Query (server state), Zustand (client state)
- **Forms**: React Hook Form + Zod validation
- **HTTP**: Axios with interceptors

### Backend
- **Runtime**: Java 26, Spring Boot 3.2.0
- **Database**: PostgreSQL 16 (Flyway migrations)
- **Cache**: Redis 7
- **AI**: Ollama (qwen3.5:0.8b) - swappable provider
- **Storage**: MinIO (S3-compatible)
- **API Docs**: SpringDoc OpenAPI / Swagger UI
- **Security**: JWT (access + refresh tokens), BCrypt

## Package Structure
```
com.ailisting
├── config/           # SecurityConfig, RedisConfig, OpenApiConfig, CorsConfig, RateLimitConfig
├── security/         # JwtTokenProvider, JwtAuthenticationFilter, RateLimitFilter
├── controller/       # AuthController, ListingController, AiController, AdminController, CacheController
├── service/          # AuthService, ListingService (interfaces)
│   └── impl/         # AuthServiceImpl, ListingServiceImpl
├── repository/       # JPA repositories (User, Listing, RefreshToken, AiGenerationLog)
├── model/
│   ├── entity/       # User, Listing, RefreshToken, AiGenerationLog
│   ├── dto/request/  # RegisterRequest, LoginRequest, ListingRequest, etc.
│   └── dto/response/ # AuthResponse, ListingResponse, ApiResponse, PaginatedResponse
├── enums/            # Role, Platform
├── exception/        # GlobalExceptionHandler, BadRequestException, ResourceNotFoundException
├── ai/               # AiProvider (interface), OllamaProvider, AiProviderFactory, AiGenerationService
│   └── prompt/       # PromptTemplates
└── util/             # ListingMapper
```

## Design Patterns
1. **Strategy Pattern** - AiProvider interface allows swapping AI backends (Ollama, OpenAI, Claude)
2. **Factory Pattern** - AiProviderFactory manages multiple AI providers
3. **Builder Pattern** - All entities and DTOs use Lombok @Builder
4. **Template Method** - PromptTemplates separates prompt logic from Java code
5. **Repository Pattern** - Spring Data JPA repositories abstract persistence

## Security Architecture
```
Request -> RateLimitFilter -> JwtAuthenticationFilter -> SecurityFilterChain -> Controller
                                    |
                            Extracts JWT from Authorization header
                            Validates token, sets SecurityContext
```

- **Access Token**: 15 min expiry, contains username
- **Refresh Token**: 7 days, stored in DB (single-use, revoked on refresh)
- **Rate Limiting**: Redis-based sliding window with Lua script (atomic operations)

## Caching Strategy
| Cache Name | TTL | Eviction |
|------------|-----|----------|
| listings | 15 min | @CachePut on update, @CacheEvict on delete |
| userListings | 10 min | @CacheEvict on create/update/delete |
| listingStats | 5 min | @CacheEvict on status change |
| users | 2 hours | @CacheEvict on role/status change |

## Data Flow: AI Generation
```
User Request -> ListingController -> ListingService.generateListingContent()
    -> AiGenerationService.generateListing()
        -> PromptTemplates.buildListingPrompt()  (template)
        -> AiProviderFactory.getDefaultProvider() (strategy)
        -> OllamaProvider.generateJson()          (HTTP call)
        -> Parse JSON response
        -> Save AiGenerationLog
    -> Update Listing entity with AI content
    -> Return ListingResponse
```

## Database Schema
4 tables: `users`, `listings`, `refresh_tokens`, `ai_generation_logs`
- Soft delete on listings (deleted flag)
- Single-use refresh tokens (revoked on use)
- All timestamps in UTC

## Deployment
- Multi-stage Dockerfile (builder + runtime)
- Docker Compose with PostgreSQL, Redis, Ollama, MinIO, Nginx
- Nginx reverse proxy with rate limiting and security headers
- All secrets via environment variables

## Frontend Architecture

### State Management Split
- **TanStack Query**: API data caching, background refetching, optimistic updates
- **Zustand**: Auth tokens (in-memory), UI preferences, listing filters

### Authentication Flow (Frontend)
```
Login -> Store accessToken in Zustand (memory)
      -> Store refreshToken in HttpOnly cookie
      -> Store user profile in Zustand (persisted)

API Request -> Axios interceptor attaches Bearer token
           -> 401 response -> Try refresh token
           -> Refresh success -> Retry request
           -> Refresh failure -> Force logout
```

### Route Protection
```
/ -> PublicLayout (Landing, Login, Register)
/dashboard -> ProtectedRoute -> DashboardLayout
/admin -> AdminRoute -> DashboardLayout
```

### Component Hierarchy
```
Templates (page layouts)
  └── Organisms (complex: Sidebar, Header, DataTable)
      └── Molecules (composed: SearchInput, StatusBadge)
          └── Atoms (basic: Badge, LoadingSpinner)
              └── UI (shadcn/ui primitives)
```

For detailed frontend architecture, see [frontend-architecture.md](frontend-architecture.md) and [milestone-13-frontend.md](milestone-13-frontend.md).
