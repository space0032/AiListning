-- =============================================================================
-- V1: Initial Schema - AI Listing Generator
-- =============================================================================
-- Tables: users, listings, refresh_tokens, ai_generation_logs
-- All timestamps use UTC. Soft deletes on listings.
-- =============================================================================

-- ===========================
-- 1. USERS TABLE
-- ===========================
-- Stores user accounts, auth credentials, and verification state.
-- username/email are unique. role controls access (USER/ADMIN).
-- Tokens for email verification and password reset are stored here
-- (will be moved to separate table in future if needed).
-- ===========================
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(100),
    role            VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN         NOT NULL DEFAULT FALSE,
    verification_token    VARCHAR(255),
    reset_password_token  VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at      TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),

    CONSTRAINT uk_users_username  UNIQUE (username),
    CONSTRAINT uk_users_email     UNIQUE (email),
    CONSTRAINT chk_users_role     CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

-- Performance indexes for users
-- These speed up the most common lookup patterns:
--   - Login by username (authentication flow)
--   - Registration duplicate check by email
--   - Password reset token lookup
--   - Email verification token lookup
CREATE INDEX idx_users_username         ON users (username);
CREATE INDEX idx_users_email            ON users (email);
CREATE INDEX idx_users_verification_token ON users (verification_token) WHERE verification_token IS NOT NULL;
CREATE INDEX idx_users_reset_token      ON users (reset_password_token) WHERE reset_password_token IS NOT NULL;

COMMENT ON TABLE users IS 'User accounts for AI Listing Generator SaaS';
COMMENT ON COLUMN users.role IS 'ROLE_USER or ROLE_ADMIN - controls access level';
COMMENT ON COLUMN users.email_verified IS 'Set to true after user confirms email via verification link';

-- ===========================
-- 2. LISTINGS TABLE
-- ===========================
-- Core business table. Each listing belongs to one user.
-- Stores both input (product details) and output (AI-generated content).
-- Uses soft delete (deleted flag) to preserve data for analytics.
-- Status tracks the listing lifecycle: DRAFT -> PUBLISHED -> ARCHIVED
-- ===========================
CREATE TABLE listings (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,

    -- Product input fields (user-provided)
    product_name        VARCHAR(255)    NOT NULL,
    product_description TEXT,
    category            VARCHAR(100),
    brand               VARCHAR(100),
    material            VARCHAR(100),
    color               VARCHAR(100),
    size                VARCHAR(100),

    -- Image storage
    image_url           VARCHAR(500),
    original_file_name  VARCHAR(500),

    -- Platform target
    platform            VARCHAR(20)     NOT NULL,

    -- AI-generated content fields
    seo_title                   VARCHAR(500),
    bullet_points               TEXT,
    description                 TEXT,
    tags                        TEXT,
    keywords                    VARCHAR(500),
    meta_description            VARCHAR(300),
    platform_formatted_listing  TEXT,

    -- Metadata
    status      VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    deleted     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at  TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),

    CONSTRAINT fk_listings_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_listings_platform CHECK (platform IN ('AMAZON', 'FLIPKART', 'MEESHO', 'SHOPIFY')),
    CONSTRAINT chk_listings_status    CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- Performance indexes for listings
-- user_id: Most queries filter by user (their own listings)
-- deleted: Nearly every query filters out soft-deleted records
-- platform: Platform-specific listing views
-- status: Filter by draft/published/archived
-- product_name GIN: Full-text search on product names
CREATE INDEX idx_listings_user_id     ON listings (user_id);
CREATE INDEX idx_listings_deleted     ON listings (deleted);
CREATE INDEX idx_listings_platform    ON listings (platform);
CREATE INDEX idx_listings_status      ON listings (status);
CREATE INDEX idx_listings_user_deleted ON listings (user_id, deleted);
CREATE INDEX idx_listings_user_platform ON listings (user_id, platform, deleted);
CREATE INDEX idx_listings_user_status ON listings (user_id, status, deleted);
CREATE INDEX idx_listings_created     ON listings (created_at DESC);

-- Partial index: Only index non-deleted listings for faster queries
CREATE INDEX idx_listings_search ON listings (user_id, product_name)
    WHERE deleted = FALSE;

COMMENT ON TABLE listings IS 'Product listings with AI-generated SEO content';
COMMENT ON COLUMN listings.deleted IS 'Soft delete flag - records preserved for analytics';
COMMENT ON COLUMN listings.status IS 'Lifecycle: DRAFT (editing) -> PUBLISHED -> ARCHIVED';

-- ===========================
-- 3. REFRESH TOKENS TABLE
-- ===========================
-- Separate table for refresh tokens (not embedded in JWT).
-- Why separate? Revocation: delete row to instantly invalidate.
-- Supports multiple devices per user. Each token is single-use.
-- ===========================
CREATE TABLE refresh_tokens (
    id          BIGSERIAL       PRIMARY KEY,
    user_id     BIGINT          NOT NULL,
    token       VARCHAR(255)    NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);

-- Speed up token lookup during refresh flow
CREATE INDEX idx_refresh_tokens_user   ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token  ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens - supports revocation and multi-device';

-- ===========================
-- 4. AI GENERATION LOGS TABLE
-- ===========================
-- Audit trail for every AI generation request.
-- Tracks model used, token counts, timing, and errors.
-- Critical for: billing (future), debugging, analytics, rate limiting.
-- ===========================
CREATE TABLE ai_generation_logs (
    id                  BIGSERIAL       PRIMARY KEY,
    user_id             BIGINT          NOT NULL,
    listing_id          BIGINT,

    -- AI metrics
    model_used          VARCHAR(100)    NOT NULL,
    prompt_tokens       INT,
    completion_tokens   INT,
    total_tokens        INT,
    generation_time_ms  BIGINT,

    -- Request/Response tracking
    platform            VARCHAR(20),
    status              VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS',
    error_message       TEXT,

    created_at          TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),

    CONSTRAINT fk_ai_logs_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_logs_listing FOREIGN KEY (listing_id)
        REFERENCES listings (id) ON DELETE SET NULL,
    CONSTRAINT chk_ai_logs_status CHECK (status IN ('SUCCESS', 'FAILED', 'TIMEOUT'))
);

-- Analytics queries: group by user, date, model
CREATE INDEX idx_ai_logs_user_id    ON ai_generation_logs (user_id);
CREATE INDEX idx_ai_logs_listing_id ON ai_generation_logs (listing_id);
CREATE INDEX idx_ai_logs_model      ON ai_generation_logs (model_used);
CREATE INDEX idx_ai_logs_created    ON ai_generation_logs (created_at DESC);
CREATE INDEX idx_ai_logs_user_date  ON ai_generation_logs (user_id, created_at DESC);

COMMENT ON TABLE ai_generation_logs IS 'Audit trail for AI generation requests - for billing, analytics, debugging';

-- ===========================
-- SEED DATA (Development Only)
-- ===========================
-- Default admin user (password: admin123 - bcrypt hashed)
-- Only for development. Remove in production.
INSERT INTO users (username, email, password, full_name, role, enabled, email_verified)
VALUES (
    'admin',
    'admin@ailisting.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin User',
    'ROLE_ADMIN',
    TRUE,
    TRUE
);