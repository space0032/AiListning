# Milestone 4: Listing CRUD Operations

## Goal
Implement complete CRUD operations for product listings with pagination, filtering, sorting, search, status management, and image upload.

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/listings` | Create new listing |
| GET | `/listings` | List all (paginated, sortable) |
| GET | `/listings/{id}` | Get single listing |
| PUT | `/listings/{id}` | Update listing |
| DELETE | `/listings/{id}` | Soft delete + remove image |
| POST | `/listings/{id}/duplicate` | Clone as draft |
| PATCH | `/listings/{id}/status?status=PUBLISHED` | Change status |
| GET | `/listings/platform/{platform}` | Filter by platform |
| GET | `/listings/status/{status}` | Filter by status |
| GET | `/listings/platform/{platform}/status/{status}` | Filter both |
| GET | `/listings/search?keyword=...` | Search listings |
| POST | `/listings/{id}/upload-image` | Upload/replace image |
| GET | `/listings/stats` | Listing counts by status |

## Status State Machine

```
                ┌──────────────────────┐
                │                      │
                ▼                      │
┌────────┐   publish   ┌────────────┐  │   archive   ┌──────────┐
│ DRAFT  │ ──────────> │ PUBLISHED  │ ────────────> │ ARCHIVED │
└────────┘             └────────────┘               └──────────┘
     ▲                                                   │
     └───────────────────────────────────────────────────┘
                         re-activate
```

### Valid Transitions
- DRAFT → PUBLISHED
- DRAFT → ARCHIVED
- PUBLISHED → ARCHIVED
- ARCHIVED → DRAFT

Invalid transitions throw `BadRequestException`.

## Key Features

### Pagination
```
GET /listings?page=0&size=10&sortBy=createdAt&sortDir=desc
```

Response:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

### Sort Validation
Whitelist of allowed sort fields prevents JPA injection:
- `createdAt`, `updatedAt`, `productName`, `platform`, `status`, `id`

Invalid fields default to `createdAt`.

### Search
Searches across 4 fields:
- `productName`
- `productDescription`
- `brand`
- `category`

Case-insensitive with LIKE matching.

### Image Upload
- Stored in MinIO (S3-compatible)
- Old images auto-deleted when uploading new ones
- 5MB max, images only
- UUID-based file names prevent conflicts

### Soft Delete
- `deleted` flag set to true
- Record preserved for analytics
- Image removed from MinIO
- All queries filter out deleted records

## Files Modified/Created
- `ListingService.java` - Added status, stats, AI generation methods
- `ListingServiceImpl.java` - Full implementation with validation
- `ListingController.java` - All endpoints
- `ListingRepository.java` - New queries
- `ListingMapper.java` - Entity→DTO mapping utility
- `ListingResponse.java` - Added originalFileName field
- `StorageServiceImpl.java` - Image lifecycle management
