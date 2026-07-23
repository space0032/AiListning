# Milestone 1: Project Foundation & Architecture Setup

## Goal
Set up the complete project structure with Spring Boot 3, Java 26, and all necessary dependencies. Create the layered architecture with proper package organization.

## System Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Client (React)                     │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP/HTTPS
                       ▼
┌─────────────────────────────────────────────────────┐
│              Spring Boot Application                 │
│  ┌─────────────────────────────────────────────┐   │
│  │           Controller Layer (REST API)        │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │             Service Layer (Business)         │   │
│  └──────────────────┬──────────────────────────┘   │
│                     │                               │
│  ┌──────────────────▼──────────────────────────┐   │
│  │          Repository Layer (Data Access)      │   │
│  └─────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────┘
                       │
       ┌───────────────┼───────────────┐
       ▼               ▼               ▼
  ┌─────────┐    ┌──────────┐    ┌──────────┐
  │PostgreSQL│    │  Redis   │    │  MinIO   │
  └─────────┘    └──────────┘    └──────────┘
```

## Tech Stack
- **Java 26** (runtime)
- **Spring Boot 3.2.0** (framework)
- **Spring Security** (authentication)
- **Spring Data JPA** (ORM)
- **PostgreSQL 16** (database)
- **Redis 7** (cache)
- **MinIO** (object storage)
- **Ollama** (local AI)
- **Docker Compose** (infrastructure)

## Packages Created

| Package | Purpose |
|---------|---------|
| `com.ailisting` | Root package, main application class |
| `com.ailisting.config` | Spring configuration classes |
| `com.ailisting.controller` | REST controllers |
| `com.ailisting.service` | Business logic interfaces |
| `com.ailisting.service.impl` | Business logic implementations |
| `com.ailisting.repository` | Data access (JPA repositories) |
| `com.ailisting.model.entity` | JPA entities |
| `com.ailisting.model.dto.request` | API request DTOs |
| `com.ailisting.model.dto.response` | API response DTOs |
| `com.ailisting.model.enums` | Enumeration types |
| `com.ailisting.exception` | Custom exceptions + global handler |
| `com.ailisting.security` | JWT token provider + filter |

## Key Files Created

```
pom.xml
docker-compose.yml
Dockerfile
.gitignore
src/main/java/com/ailisting/
  ├── AiListingApplication.java
  ├── config/
  │   ├── SecurityConfig.java
  │   ├── RedisConfig.java
  │   ├── MinioConfig.java
  │   └── OllamaConfig.java
  ├── security/
  │   ├── JwtTokenProvider.java
  │   ├── JwtAuthenticationFilter.java
  │   └── CustomUserDetailsService.java
  ├── model/
  │   ├── entity/User.java, Listing.java
  │   ├── dto/request/RegisterRequest.java, LoginRequest.java, ListingRequest.java
  │   ├── dto/response/AuthResponse.java, UserResponse.java, ListingResponse.java, PaginatedResponse.java, ApiResponse.java
  │   └── enums/Role.java, Platform.java
  ├── repository/UserRepository.java, ListingRepository.java
  ├── service/AuthService.java, ListingService.java, StorageService.java
  ├── controller/AuthController.java, ListingController.java, UserController.java
  └── exception/GlobalExceptionHandler.java, ResourceNotFoundException.java, BadRequestException.java, ErrorResponse.java, ValidationErrorResponse.java

src/main/resources/
  ├── application.yml
  ├── application-dev.yml
  └── db/migration/V1__init_schema.sql
```

## Production Considerations
- Connection pooling configured (HikariCP: max 20, min idle 5)
- Graceful shutdown enabled
- Actuator health checks exposed
- Structured logging with file output
- Environment-specific profiles (dev/prod)

## Scalability Considerations
- Stateless JWT authentication (horizontal scaling)
- Redis cache layer ready
- MinIO for distributed file storage
- PostgreSQL with proper connection pooling

## Security Considerations
- CORS configured for specific origins only
- CSRF disabled (stateless API)
- Password encoding with BCrypt
- JWT secret key in config (use env vars in production)
- No JPA entities exposed through API
