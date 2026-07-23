# API Reference

## Base URL
- Development: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI Spec: `http://localhost:8080/v3/api-docs`

## Authentication
All protected endpoints require `Authorization: Bearer <access_token>` header.

## Endpoints

### Auth (`/auth`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Register new user |
| POST | `/auth/login` | No | Login |
| POST | `/auth/refresh` | No | Refresh token pair |
| POST | `/auth/logout` | No | Revoke refresh token |
| POST | `/auth/forgot-password` | No | Request password reset |
| POST | `/auth/reset-password` | No | Reset password with token |
| GET | `/auth/verify-email` | No | Verify email with token |

#### POST /auth/register
```json
Request: { "username": "string", "email": "string", "password": "string", "fullName": "string" }
Response 201: { "success": true, "data": { "accessToken": "...", "refreshToken": "...", "user": {...} } }
```

#### POST /auth/login
```json
Request: { "username": "string", "password": "string" }
Response 200: { "success": true, "data": { "accessToken": "...", "refreshToken": "...", "user": {...} } }
```

#### POST /auth/refresh
```json
Request: { "refreshToken": "string" }
Response 200: { "success": true, "data": { "accessToken": "...", "refreshToken": "..." } }
```

#### POST /auth/logout
```json
Request: { "refreshToken": "string" }
Response 200: { "success": true, "message": "Logged out successfully" }
```

#### POST /auth/forgot-password?email=user@example.com
Response 200: { "success": true, "message": "If the email exists, a reset link has been sent" }

#### POST /auth/reset-password
```json
Request: { "token": "string", "newPassword": "string" }
Response 200: { "success": true, "message": "Password reset successfully" }
```

#### GET /auth/verify-email?token=string
Response 200: { "success": true, "message": "Email verified successfully" }

---

### Listings (`/listings`) - Requires Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/listings` | Create listing |
| GET | `/listings` | Get all listings (paginated) |
| GET | `/listings/{id}` | Get listing by ID |
| PUT | `/listings/{id}` | Update listing |
| DELETE | `/listings/{id}` | Soft delete listing |
| POST | `/listings/{id}/duplicate` | Duplicate listing as draft |
| PATCH | `/listings/{id}/status?status=` | Update listing status |
| GET | `/listings/platform/{platform}` | Filter by platform |
| GET | `/listings/status/{status}` | Filter by status |
| GET | `/listings/platform/{platform}/status/{status}` | Filter by both |
| GET | `/listings/search?keyword=` | Search listings |
| POST | `/listings/{id}/upload-image` | Upload product image |
| POST | `/listings/{id}/generate` | Generate AI content |
| GET | `/listings/stats` | Get listing statistics |

#### POST /listings
```json
Request: { "productName": "string", "productDescription": "string", "category": "string", "brand": "string", "material": "string", "color": "string", "size": "string", "platform": "AMAZON|FLIPKART|MEESHO|SHOPIFY" }
Response 201: { "success": true, "data": { "id": 1, "productName": "...", "status": "DRAFT", ... } }
```

#### GET /listings?page=0&size=10&sortBy=createdAt&sortDir=desc
```json
Response 200: { "success": true, "data": { "content": [...], "page": 0, "size": 10, "totalElements": 50, "totalPages": 5 } }
```

#### PATCH /listings/1/status?status=PUBLISHED
Valid transitions: DRAFT->PUBLISHED, DRAFT->ARCHIVED, PUBLISHED->ARCHIVED, ARCHIVED->DRAFT

---

### AI (`/ai`) - Requires Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/ai/generate-listing` | Generate new listing with AI |
| GET | `/ai/health` | Check AI provider availability |

#### POST /ai/generate-listing
```json
Request: { "productName": "string", "productDescription": "string", "category": "string", "brand": "string", "platform": "AMAZON" }
Response 200: { "success": true, "data": { "seoTitle": "...", "bulletPoints": "...", "description": "...", "tags": "...", "keywords": "...", "metaDescription": "...", "modelUsed": "qwen3.5:0.8b", "generationTimeMs": 1234 } }
```

---

### Admin (`/admin`) - Requires ADMIN Role

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | Get all users (paginated) |
| GET | `/admin/users/{id}` | Get user by ID |
| PATCH | `/admin/users/{id}/toggle-status` | Enable/disable user |
| GET | `/admin/analytics/overview` | Get analytics overview |
| GET | `/admin/analytics/generation-stats` | Get AI generation stats |
| GET | `/admin/health/detailed` | Detailed health check |

---

### Cache (`/cache`) - Requires Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/cache/stats` | Get cache statistics |
| DELETE | `/cache` | Clear all caches |
| DELETE | `/cache/{cacheName}` | Clear specific cache |

## Error Responses
```json
{ "success": false, "message": "Error description", "timestamp": "2026-07-22T18:00:00" }
```

## Rate Limits
| Tier | Limit | Window |
|------|-------|--------|
| Public endpoints | 30 req/min | Sliding window |
| Authenticated | 60 req/min | Sliding window |
| AI generation | 10 req/min | Sliding window |

Rate limit headers returned: `X-Rate-Limit-Remaining`, `X-Rate-Limit-Reset`
