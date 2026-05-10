-- ============================================================
-- V2__create_audit_log.sql
-- Immutable audit trail for all CUD operations
-- ============================================================
 
CREATE TABLE audit_log (
    id            BIGSERIAL       PRIMARY KEY,
    entity_type   VARCHAR(100)    NOT NULL,
    entity_id     VARCHAR(100)    NOT NULL,
    action        VARCHAR(20)     NOT NULL
                                  CHECK (action IN ('CREATE','UPDATE','DELETE','READ')),
    actor         VARCHAR(255)    NOT NULL,
    actor_ip      VARCHAR(45),
    old_value     JSONB,
    new_value     JSONB,
    diff          JSONB,
    status        VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS'
                                  CHECK (status IN ('SUCCESS','FAILURE')),
    error_message TEXT,
    request_id    VARCHAR(64),
    session_id    VARCHAR(64),
    user_agent    TEXT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
 
-- Prevent updates/deletes — audit log is immutable
-- (enforced at application layer via @PreAuthorize)
 
CREATE INDEX idx_audit_entity      ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_actor       ON audit_log (actor);
CREATE INDEX idx_audit_created_at  ON audit_log (created_at DESC);
CREATE INDEX idx_audit_entity_time ON audit_log (entity_type, entity_id, created_at DESC);
CREATE INDEX idx_audit_request_id  ON audit_log (request_id) WHERE request_id IS NOT NULL;
CREATE INDEX idx_audit_action      ON audit_log (action);
 
COMMENT ON TABLE  audit_log               IS 'Immutable record of every state-changing operation.';
COMMENT ON COLUMN audit_log.entity_type   IS 'Logical entity name, e.g. SecurityPolicy, User.';
COMMENT ON COLUMN audit_log.entity_id     IS 'PK of the affected entity as text (handles UUID and Long).';
COMMENT ON COLUMN audit_log.action        IS 'CRUD action performed.';
COMMENT ON COLUMN audit_log.actor         IS 'Username or system process that triggered the action.';
COMMENT ON COLUMN audit_log.actor_ip      IS 'Client IP at time of request (null for batch jobs).';
COMMENT ON COLUMN audit_log.old_value     IS 'Full JSON snapshot before the change.';
COMMENT ON COLUMN audit_log.new_value     IS 'Full JSON snapshot after the change.';
COMMENT ON COLUMN audit_log.diff          IS 'Only the changed key-value pairs.';
COMMENT ON COLUMN audit_log.created_at    IS 'Wall-clock UTC time the event was recorded.';