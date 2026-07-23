# AI E-commerce Product Listing Generator

A production-grade AI SaaS application that generates SEO-optimized product listings for e-commerce platforms using Ollama AI.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, shadcn/ui |
| **State Management** | TanStack Query (server state), Zustand (client state) |
| **Forms** | React Hook Form, Zod validation |
| **Backend** | Java 26, Spring Boot 3.2, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 16 (Flyway migrations) |
| **Cache** | Redis 7 |
| **Object Storage** | MinIO (S3 Compatible) |
| **AI Provider** | Ollama (qwen3.5:0.8b) - swappable |
| **API Docs** | SpringDoc OpenAPI / Swagger UI |
| **Containerization** | Docker, Docker Compose |

## Project Status

| Milestone | Feature | Status |
|-----------|---------|--------|
| 1 | Project Foundation | Complete |
| 2 | Database Schema & Migrations | Complete |
| 3 | User Authentication & JWT | Complete |
| 4 | Listing CRUD Operations | Complete |
| 5 | AI Integration with Ollama | Complete |
| 6 | Redis Caching | Complete |
| 7 | Rate Limiting | Complete |
| 8 | OpenAPI/Swagger Documentation | Complete |
| 9 | Admin Features | Complete |
| 10 | Production Deployment Config | Complete |
| 11 | Testing (78 tests) | Complete |
| 12 | Frontend Application | Complete |

## Quick Start

### Prerequisites
- Java 26 (JDK)
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose

### Backend Setup

1. Start infrastructure:
```bash
docker-compose up -d
```

2. Pull AI model:
```bash
docker exec ai-listing-ollama ollama pull qwen3.5:0.8b
```

3. Run application:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

4. Verify:
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html
- MinIO: http://localhost:9001 (minioadmin/minioadmin)

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

4. Access frontend:
- Frontend: http://localhost:3000
- The frontend proxies API requests to the backend at port 8080

### Run Tests

**Backend:**
```bash
mvn test                    # 78 unit tests
mvn verify                  # + integration tests (requires Docker)
```

**Frontend:**
```bash
cd frontend
npm run test                # Unit tests
npm run build               # Production build
```

## API Endpoints

### Auth (`/auth`) - Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login |
| POST | `/auth/refresh` | Refresh token pair |
| POST | `/auth/logout` | Revoke refresh token |
| POST | `/auth/forgot-password` | Request password reset |
| POST | `/auth/reset-password` | Reset password |
| GET | `/auth/verify-email` | Verify email |

### Listings (`/listings`) - Authenticated
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/listings` | Create listing |
| GET | `/listings` | Get all (paginated) |
| GET | `/listings/{id}` | Get listing |
| PUT | `/listings/{id}` | Update listing |
| DELETE | `/listings/{id}` | Soft delete |
| POST | `/listings/{id}/duplicate` | Duplicate as draft |
| PATCH | `/listings/{id}/status` | Update status |
| GET | `/listings/search` | Search listings |
| POST | `/listings/{id}/upload-image` | Upload image |
| POST | `/listings/{id}/generate` | Generate AI content |
| GET | `/listings/stats` | Get stats |

### AI (`/ai`) - Authenticated
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/ai/generate-listing` | Generate listing from scratch |
| GET | `/ai/health` | Check AI availability |

### Admin (`/admin`) - ADMIN Role
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | Get all users |
| PATCH | `/admin/users/{id}/toggle-status` | Enable/disable user |
| GET | `/admin/analytics/overview` | Analytics |
| GET | `/admin/analytics/generation-stats` | AI stats |
| GET | `/admin/health/detailed` | Health check |

## Project Structure

```
ai-listing-generator/
├── frontend/                           # React frontend application
│   ├── src/
│   │   ├── api/                        # API client & service layer
│   │   ├── app/                        # Providers, routes
│   │   ├── components/                 # UI components
│   │   │   ├── ui/                     # shadcn/ui components
│   │   │   ├── atoms/                  # Basic building blocks
│   │   │   ├── molecules/              # Composed components
│   │   │   ├── organisms/              # Complex components
│   │   │   └── templates/              # Page layouts
│   │   ├── hooks/                      # Custom React hooks
│   │   ├── lib/                        # Utilities, validations, constants
│   │   ├── pages/                      # Page components
│   │   │   ├── public/                 # Public pages (login, register, etc.)
│   │   │   ├── dashboard/              # Dashboard
│   │   │   ├── listings/               # Listings CRUD
│   │   │   ├── admin/                  # Admin pages
│   │   │   └── settings/               # User settings
│   │   ├── stores/                     # Zustand stores
│   │   └── types/                      # TypeScript types
│   ├── package.json
│   └── vite.config.ts
├── src/main/java/com/ailisting/
│   ├── ai/                          # AI abstraction (Strategy Pattern)
│   │   ├── AiProvider.java          # Provider interface
│   │   ├── AiProviderFactory.java   # Provider registry
│   │   ├── OllamaProvider.java      # Ollama implementation
│   │   ├── AiGenerationService.java # Generation service
│   │   └── prompt/                  # Prompt templates
│   ├── config/                      # Security, Redis, OpenAPI, CORS, Rate Limit
│   ├── controller/                  # REST controllers
│   ├── model/                       # Entities, DTOs, enums
│   ├── repository/                  # JPA repositories
│   ├── security/                    # JWT, auth filter, rate limit filter
│   ├── service/                     # Business logic interfaces
│   │   └── impl/                    # Service implementations
│   ├── exception/                   # Global exception handler
│   └── util/                        # ListingMapper
├── src/test/java/com/ailisting/    # 78 tests
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/                # Flyway SQL migrations
├── docs/                            # 12 milestone docs + API reference
├── nginx/                           # Reverse proxy config
├── docker-compose.yml               # Development
├── docker-compose.prod.yml          # Production
├── Dockerfile                       # Multi-stage build
└── pom.xml
```

## Documentation

### Milestones
- [Milestone 1: Project Foundation](docs/milestone-01-project-foundation.md)
- [Milestone 2: Database Schema](docs/milestone-02-database-schema.md)
- [Milestone 3: Authentication](docs/milestone-03-authentication.md)
- [Milestone 4: Listing CRUD](docs/milestone-04-listing-crud.md)
- [Milestone 5: AI Integration](docs/milestone-05-ai-integration.md)
- [Milestone 6: Redis Caching](docs/milestone-06-redis-caching.md)
- [Milestone 7: Rate Limiting](docs/milestone-07-rate-limiting.md)
- [Milestone 8: OpenAPI/Swagger](docs/milestone-08-openapi-swagger.md)
- [Milestone 9: Admin Features](docs/milestone-09-admin-features.md)
- [Milestone 10: Production Deployment](docs/milestone-10-production-deployment.md)
- [Milestone 11: Testing](docs/milestone-11-testing.md)

### Reference
- [API Reference](docs/api-reference.md)
- [Architecture](docs/architecture.md)
- [Setup Guide](docs/setup-guide.md)

## License

MIT