DROP TABLE IF EXISTS audit_log CASCADE;

CREATE TABLE audit_log
(
    id              UUID PRIMARY KEY NOT NULL,
    entity_type     VARCHAR(50)      NOT NULL,
    entity_id       UUID             NOT NULL,
    action          VARCHAR(30)      NOT NULL
        CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'STATUS_CHANGE')),
    old_value       JSONB,
    new_value       JSONB,
    performed_by_id UUID             NOT NULL,
    module          VARCHAR(30)      NOT NULL
        CHECK (module IN ('PURCHASE', 'SALES', 'INVENTORY', 'AUTH')),
    description     TEXT,
    performed_at    TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_performed_by ON audit_log (performed_by_id);
CREATE INDEX idx_audit_performed_at ON audit_log (performed_at DESC);
CREATE INDEX idx_audit_module ON audit_log (module);