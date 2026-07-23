# Setup Guide

## Prerequisites
- Java 26 (JDK)
- Maven 3.9+
- Docker & Docker Compose
- Git

## Quick Start (Development)

### 1. Clone & Start Infrastructure
```bash
git clone <repo-url>
cd AIListning
docker-compose up -d
```
This starts: PostgreSQL (5432), Redis (6379), Ollama (11434), MinIO (9000/9001)

### 2. Pull AI Model
```bash
docker exec ai-listing-ollama ollama pull qwen3.5:0.8b
```

### 3. Run Application
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verify
- API: http://localhost:8080/auth/login
- Swagger: http://localhost:8080/swagger-ui/index.html
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)

## Run Tests
```bash
# Unit tests
mvn test

# Integration tests (requires Docker)
mvn verify
```

## Production Deployment

### Docker Compose (Production)
```bash
# Set environment variables
export DB_PASSWORD=secure-password
export JWT_SECRET=your-256-bit-secret-key-here-change-in-production
export REDIS_PASSWORD=secure-redis-password

# Start all services
docker-compose -f docker-compose.prod.yml up -d
```

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| DB_HOST | postgres | Database host |
| DB_NAME | ai_listing | Database name |
| DB_USERNAME | postgres | Database user |
| DB_PASSWORD | postgres | Database password |
| REDIS_HOST | redis | Redis host |
| REDIS_PASSWORD | (empty) | Redis password |
| JWT_SECRET | (required) | 256-bit JWT signing key |
| OLLAMA_BASE_URL | http://ollama:11434 | Ollama URL |
| OLLAMA_MODEL | qwen3.5:0.8b | AI model |
| MINIO_ENDPOINT | http://minio:9000 | MinIO URL |
| SERVER_PORT | 8080 | Application port |

## IDE Setup
### IntelliJ IDEA
1. Open project
2. File > Project Structure > SDKs > Add Java 26
3. Maven > Reimport
4. Run Configuration: Spring Boot, Profile: dev

### VS Code
1. Install Extension Pack for Java
2. Install Spring Boot Extension Pack
3. Open terminal: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

## API Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"pass123","fullName":"John Doe"}'
```

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123"}'
```

### Create Listing
```bash
curl -X POST http://localhost:8080/listings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"productName":"Wireless Mouse","category":"Electronics","brand":"Logitech","platform":"AMAZON"}'
```

### Generate AI Content
```bash
curl -X POST http://localhost:8080/listings/1/generate \
  -H "Authorization: Bearer <token>"
```
