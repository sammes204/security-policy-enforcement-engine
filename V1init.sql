
Copy

-- ============================================================
-- V1__init.sql  — Core Schema for Tool-60
-- Security Policy Enforcement Engine
-- ============================================================
 
-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id            BIGSERIAL       PRIMARY KEY,
    username      VARCHAR(100)    NOT NULL UNIQUE,
    email         VARCHAR(255)    NOT NULL UNIQUE,
    password_hash VARCHAR(255)    NOT NULL,
    role          VARCHAR(20)     NOT NULL DEFAULT 'USER'
                                  CHECK (role IN ('ADMIN', 'ANALYST', 'USER')),
    active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
 
CREATE INDEX idx_users_email    ON users (email);
CREATE INDEX idx_users_username ON users (username);
 
-- ── Security Policies ────────────────────────────────────────
CREATE TABLE security_policies (
    id               BIGSERIAL       PRIMARY KEY,
    name             VARCHAR(255)    NOT NULL,
    description      TEXT,
    category         VARCHAR(100)    NOT NULL,
    severity         VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM'
                                     CHECK (severity IN ('CRITICAL','HIGH','MEDIUM','LOW','INFO')),
    status           VARCHAR(20)     NOT NULL DEFAULT 'DRAFT'
                                     CHECK (status IN ('DRAFT','ACTIVE','INACTIVE','ARCHIVED')),
    risk_score       INTEGER         CHECK (risk_score BETWEEN 0 AND 100),
    owner            VARCHAR(255),
    target_systems   TEXT,
    enforcement_type VARCHAR(50)     NOT NULL DEFAULT 'ADVISORY'
                                     CHECK (enforcement_type IN ('MANDATORY','ADVISORY','AUTOMATED')),
    ai_description   TEXT,
    ai_recommendations TEXT,
    is_deleted       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_by       BIGINT          REFERENCES users(id),
    updated_by       BIGINT          REFERENCES users(id),
    effective_date   DATE,
    review_date      DATE,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
 
CREATE INDEX idx_policies_status   ON security_policies (status) WHERE is_deleted = FALSE;
CREATE INDEX idx_policies_severity ON security_policies (severity);
CREATE INDEX idx_policies_category ON security_policies (category);
CREATE INDEX idx_policies_search   ON security_policies USING GIN (to_tsvector('english', name || ' ' || COALESCE(description,'')));
 
-- ── Comments (on policies) ────────────────────────────────────
CREATE TABLE policy_comments (
    id          BIGSERIAL   PRIMARY KEY,
    policy_id   BIGINT      NOT NULL REFERENCES security_policies(id) ON DELETE CASCADE,
    author_id   BIGINT      NOT NULL REFERENCES users(id),
    content     TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
 
CREATE INDEX idx_comments_policy ON policy_comments (policy_id);
 
COMMENT ON TABLE  security_policies IS 'Core entity — security policies managed by the enforcement engine.';
COMMENT ON COLUMN security_policies.risk_score IS '0 = no risk, 100 = critical risk.';
COMMENT ON COLUMN security_policies.enforcement_type IS 'MANDATORY = must comply, ADVISORY = suggested, AUTOMATED = system-enforced.';