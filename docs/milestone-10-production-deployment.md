# Milestone 10: Production Deployment

## Overview
Configure production-ready deployment with Docker, Nginx, and proper security practices.

## Implementation

### application-prod.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:ailisting}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      timeout: 2000ms
  
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
    
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000  # 24 hours
  
  ai:
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      model: qwen3.5:0.8b
  
  storage:
    endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
    access-key: ${MINIO_ACCESS_KEY}
    secret-key: ${MINIO_SECRET_KEY}
    bucket: ailisting-images

  rate-limit:
    public:
      limit: 30
      window: 60000
    authenticated:
      limit: 60
      window: 60000
    ai:
      limit: 10
      window: 60000

logging:
  level:
    root: INFO
    com.ailisting: INFO
    org.springframework: WARN
```

### docker-compose.prod.yml
```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
      target: production
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=ailisting
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - OLLAMA_BASE_URL=http://ollama:11434
      - MINIO_ENDPOINT=http://minio:9000
      - MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
      - MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 1G

  postgres:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=ailisting
      - POSTGRES_USER=${DB_USERNAME}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 512M

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 256M

  ollama:
    image: ollama/ollama:latest
    volumes:
      - ollama_data:/root/.ollama
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=${MINIO_ACCESS_KEY}
      - MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY}
    volumes:
      - minio_data:/data
    ports:
      - "9001:9001"

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - app

volumes:
  postgres_data:
  redis_data:
  ollama_data:
  minio_data:
```

### Multi-stage Dockerfile
```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Production
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

# Security: Run as non-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

# Security: Remove unnecessary packages
RUN apk --no-cache add curl && \
    rm -rf /var/cache/apk/*

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Nginx Configuration
```nginx
events {
    worker_connections 1024;
}

http {
    # Rate limiting zones
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=ai:10m rate=2r/s;

    # Upstream
    upstream app {
        server app:8080;
    }

    # Server block
    server {
        listen 80;
        server_name api.ailisting.com;
        
        # Redirect to HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name api.ailisting.com;

        # SSL configuration
        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;

        # API endpoints
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            
            proxy_pass http://app;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Timeout settings
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # AI endpoints (stricter rate limiting)
        location /ai/ {
            limit_req zone=ai burst=5 nodelay;
            
            proxy_pass http://app;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Longer timeout for AI operations
            proxy_connect_timeout 120s;
            proxy_send_timeout 120s;
            proxy_read_timeout 120s;
        }

        # Health check endpoint
        location /health {
            proxy_pass http://app/actuator/health;
            access_log off;
        }

        # Swagger UI (restrict to internal)
        location /swagger-ui/ {
            allow 10.0.0.0/8;
            allow 172.16.0.0/12;
            deny all;
            
            proxy_pass http://app;
        }
    }
}
```

## Architecture Decisions

### Why Multi-stage Build?
- **Security**: Smaller production image without build tools
- **Size**: Reduces image size by ~70%
- **Performance**: Faster deployment and startup

### Why Nginx?
- **SSL Termination**: Handle HTTPS at edge
- **Rate Limiting**: Additional layer of protection
- **Load Balancing**: Ready for horizontal scaling
- **Caching**: Static asset caching (future)

### Environment Variables
All secrets injected via environment:
- **Database**: DB_HOST, DB_USERNAME, DB_PASSWORD
- **Redis**: REDIS_HOST, REDIS_PASSWORD
- **JWT**: JWT_SECRET
- **Storage**: MINIO_ACCESS_KEY, MINIO_SECRET_KEY

## Testing

### Build and Deploy
```bash
# Build images
docker-compose -f docker-compose.prod.yml build

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps
```

### Verify Deployment
```bash
# Health check
curl https://api.ailisting.com/health

# Test API
curl -H "Authorization: Bearer {token}" https://api.ailisting.com/api/listings

# Check logs
docker-compose -f docker-compose.prod.yml logs -f app
```

### Load Testing
```bash
# Install k6
brew install k6

# Run load test
k6 run --vus 10 --duration 30s script.js
```

## Next Steps
- Implement CI/CD pipeline
- Add monitoring (Prometheus/Grafana)
- Configure automated backups
- Implement blue-green deployment