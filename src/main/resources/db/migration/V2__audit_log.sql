-- =============================================================================
-- V2: Audit Logs Table
-- =============================================================================
-- Stores audit trail for all API operations.
-- Tracks who did what, when, from where, and the result.
-- =============================================================================

CREATE TABLE audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,
    username        VARCHAR(100),
    action          VARCHAR(50)     NOT NULL,
    entity_type     VARCHAR(50),
    entity_id       BIGINT,
    method          VARCHAR(10),
    endpoint        VARCHAR(500),
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    status_code     INT,
    success         BOOLEAN         NOT NULL DEFAULT TRUE,
    error_message   TEXT,
    duration_ms     BIGINT,
    created_at      TIMESTAMP       NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),

    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL
);

-- Performance indexes for audit logs
CREATE INDEX idx_audit_logs_user_id    ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action     ON audit_logs (action);
CREATE INDEX idx_audit_logs_entity     ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created    ON audit_logs (created_at DESC);
CREATE INDEX idx_audit_logs_user_date  ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_logs_status     ON audit_logs (success);

COMMENT ON TABLE audit_logs IS 'Audit trail for all API operations - security, debugging, compliance';
COMMENT ON COLUMN audit_logs.action IS 'Action performed: LOGIN, REGISTER, CREATE_LISTING, UPDATE_LISTING, DELETE_LISTING, AI_GENERATE, etc.';
COMMENT ON COLUMN audit_logs.entity_type IS 'Entity affected: USER, LISTING, AI_LOG, CACHE, etc.';
