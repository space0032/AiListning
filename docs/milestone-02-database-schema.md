# Milestone 2: Database Schema & Flyway Migrations

## Goal
Design and implement the complete PostgreSQL database schema with proper normalization, constraints, indexes, and seed data using Flyway migrations.

## Database Schema

### Entity Relationship Diagram

```
┌──────────────┐     ┌──────────────────┐     ┌───────────────────┐
│    users     │     │    listings      │     │  refresh_tokens   │
├──────────────┤     ├──────────────────┤     ├───────────────────┤
│ id (PK)      │◄────│ user_id (FK)     │     │ id (PK)           │
│ username     │     │ id (PK)          │     │ user_id (FK)      │──►users
│ email        │     │ product_name     │     │ token             │
│ password     │     │ product_desc     │     │ expires_at        │
│ full_name    │     │ category         │     │ created_at        │
│ role         │     │ brand            │     └───────────────────┘
│ enabled      │     │ material         │
│ email_verif  │     │ color            │     ┌───────────────────────┐
│ verif_token  │     │ size             │     │   ai_generation_logs  │
│ reset_token  │     │ image_url        │     ├───────────────────────┤
│ created_at   │     │ platform         │     │ id (PK)               │
│ updated_at   │     │ seo_title        │     │ user_id (FK)          │──►users
└──────────────┘     │ bullet_points    │     │ listing_id (FK)       │──►listings
                     │ description      │     │ model_used            │
                     │ tags             │     │ prompt_tokens         │
                     │ keywords         │     │ completion_tokens     │
                     │ meta_desc        │     │ generation_time_ms    │
                     │ platform_format  │     │ platform              │
                     │ status           │     │ status                │
                     │ deleted          │     │ error_message         │
                     │ created_at       │     │ created_at            │
                     │ updated_at       │     └───────────────────────┘
                     └──────────────────┘
```

### Tables

#### 1. users
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Auto-increment ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login identifier |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| full_name | VARCHAR(100) | | Display name |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'ROLE_USER' | ROLE_USER or ROLE_ADMIN |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | Account active flag |
| email_verified | BOOLEAN | NOT NULL, DEFAULT FALSE | Email confirmed flag |
| verification_token | VARCHAR(255) | | Email verification token |
| reset_password_token | VARCHAR(255) | | Password reset token |
| created_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Last update timestamp |

#### 2. listings
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Auto-increment ID |
| user_id | BIGINT | FK → users, NOT NULL | Owner reference |
| product_name | VARCHAR(255) | NOT NULL | Product title |
| product_description | TEXT | | User-provided description |
| category | VARCHAR(100) | | Product category |
| brand | VARCHAR(100) | | Brand name |
| material | VARCHAR(100) | | Material type |
| color | VARCHAR(100) | | Color |
| size | VARCHAR(100) | | Size |
| image_url | VARCHAR(500) | | MinIO image URL |
| original_file_name | VARCHAR(500) | | Original upload filename |
| platform | VARCHAR(20) | NOT NULL | AMAZON/FLIPKART/MEESHO/SHOPIFY |
| seo_title | VARCHAR(500) | | AI-generated SEO title |
| bullet_points | TEXT | | AI-generated bullet points |
| description | TEXT | | AI-generated description |
| tags | TEXT | | AI-generated tags |
| keywords | VARCHAR(500) | | AI-generated keywords |
| meta_description | VARCHAR(300) | | AI-generated meta description |
| platform_formatted_listing | TEXT | | AI-generated formatted listing |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'DRAFT' | DRAFT/PUBLISHED/ARCHIVED |
| deleted | BOOLEAN | NOT NULL, DEFAULT FALSE | Soft delete flag |
| created_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Last update timestamp |

#### 3. refresh_tokens
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Auto-increment ID |
| user_id | BIGINT | FK → users, NOT NULL | Token owner |
| token | VARCHAR(255) | UNIQUE, NOT NULL | JWT refresh token |
| expires_at | TIMESTAMP | NOT NULL | Expiration time |
| created_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Creation timestamp |

#### 4. ai_generation_logs
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Auto-increment ID |
| user_id | BIGINT | FK → users, NOT NULL | Requesting user |
| listing_id | BIGINT | FK → listings, nullable | Associated listing |
| model_used | VARCHAR(100) | NOT NULL | AI model name |
| prompt_tokens | INT | | Input token count |
| completion_tokens | INT | | Output token count |
| total_tokens | INT | | Total token count |
| generation_time_ms | BIGINT | | Generation duration |
| platform | VARCHAR(20) | | Target platform |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'SUCCESS' | SUCCESS/FAILED/TIMEOUT |
| error_message | TEXT | | Error details if failed |
| created_at | TIMESTAMP | NOT NULL, DEFAULT UTC | Creation timestamp |

### Indexes (19 total)

| Table | Index | Columns | Purpose |
|-------|-------|---------|---------|
| users | idx_users_username | username | Login lookup |
| users | idx_users_email | email | Registration check |
| users | idx_users_verification_token | verification_token | Email verification |
| users | idx_users_reset_token | reset_password_token | Password reset |
| listings | idx_listings_user_id | user_id | User's listings |
| listings | idx_listings_deleted | deleted | Filter soft-deleted |
| listings | idx_listings_platform | platform | Platform filter |
| listings | idx_listings_status | status | Status filter |
| listings | idx_listings_user_deleted | user_id, deleted | Composite: user + active |
| listings | idx_listings_user_platform | user_id, platform, deleted | Platform filter per user |
| listings | idx_listings_user_status | user_id, status, deleted | Status filter per user |
| listings | idx_listings_created | created_at DESC | Chronological ordering |
| listings | idx_listings_search | user_id, product_name (WHERE deleted=FALSE) | Search optimization |
| refresh_tokens | idx_refresh_tokens_user | user_id | User's tokens |
| refresh_tokens | idx_refresh_tokens_token | token | Token lookup |
| refresh_tokens | idx_refresh_tokens_expires | expires_at | Cleanup expired |
| ai_generation_logs | idx_ai_logs_user_id | user_id | User's logs |
| ai_generation_logs | idx_ai_logs_listing_id | listing_id | Listing's logs |
| ai_generation_logs | idx_ai_logs_model | model_used | Model analytics |
| ai_generation_logs | idx_ai_logs_created | created_at DESC | Chronological |
| ai_generation_logs | idx_ai_logs_user_date | user_id, created_at DESC | User usage over time |

### Constraints

- **CHECK**: `role IN ('ROLE_USER', 'ROLE_ADMIN')`
- **CHECK**: `platform IN ('AMAZON', 'FLIPKART', 'MEESHO', 'SHOPIFY')`
- **CHECK**: `status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')`
- **CHECK**: `status IN ('SUCCESS', 'FAILED', 'TIMEOUT')` (ai_generation_logs)
- **UNIQUE**: users.username, users.email, refresh_tokens.token
- **CASCADE DELETE**: listings → users, refresh_tokens → users, ai_generation_logs → users
- **SET NULL**: ai_generation_logs.listing_id → listings

### Seed Data
- Default admin user: `admin` / `admin123` (bcrypt hashed)
- For development only

## Flyway Migration File
`src/main/resources/db/migration/V1__init_schema.sql`
