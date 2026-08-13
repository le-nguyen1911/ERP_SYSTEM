
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS created_by_id UUID;

CREATE INDEX IF NOT EXISTS idx_po_created_by
    ON purchase_order (created_by_id) WHERE is_deleted = false;

ALTER TABLE sales_order
    ADD COLUMN IF NOT EXISTS created_by_id UUID;

CREATE INDEX IF NOT EXISTS idx_so_created_by
    ON sales_order (created_by_id) WHERE is_deleted = false;

DROP TABLE IF EXISTS notification CASCADE;

CREATE TABLE notification
(
    id              UUID PRIMARY KEY NOT NULL,
    recipient_id    UUID             NOT NULL,
    title           VARCHAR(255)     NOT NULL,
    message         TEXT             NOT NULL,
    type            VARCHAR(20)      NOT NULL DEFAULT 'INFO'
        CHECK (type IN ('INFO', 'SUCCESS', 'WARNING', 'ERROR')),
    module          VARCHAR(30)      NOT NULL
        CHECK (module IN ('PURCHASE', 'SALES', 'INVENTORY', 'AUTH', 'SYSTEM')),
    reference_type  VARCHAR(50),
    reference_id    UUID,
    is_read         BOOLEAN          NOT NULL DEFAULT false,
    read_at         TIMESTAMP,
    created_at      TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_recipient ON notification (recipient_id, is_read);
CREATE INDEX idx_notification_recipient_created ON notification (recipient_id, created_at DESC);
CREATE INDEX idx_notification_reference ON notification (reference_type, reference_id);