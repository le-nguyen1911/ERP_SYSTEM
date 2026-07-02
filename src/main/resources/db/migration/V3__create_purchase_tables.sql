
-- ════════════════════════════════════════════════════════════════════════════════

DROP TABLE IF EXISTS supplier CASCADE;

CREATE TABLE supplier
(
    id                  UUID PRIMARY KEY NOT NULL,
    supplier_code       VARCHAR(50)      NOT NULL UNIQUE,
    supplier_name       VARCHAR(255)     NOT NULL,
    contact_person      VARCHAR(100)     NOT NULL,
    email               VARCHAR(100)     NOT NULL,
    phone               VARCHAR(20)      NOT NULL,
    address             TEXT             NOT NULL,
    city                VARCHAR(100),
    country             VARCHAR(100),
    bank_name           VARCHAR(100),
    bank_account_no     VARCHAR(50),
    bank_account_holder VARCHAR(100),
    payment_terms       VARCHAR(50),
    rating              VARCHAR(10)      NOT NULL DEFAULT 'B',
    tax_id              VARCHAR(50),
    status              VARCHAR(20)      NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_deleted          BOOLEAN          NOT NULL DEFAULT false,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE UNIQUE INDEX idx_supplier_code
    ON supplier (supplier_code) WHERE is_deleted = false;

CREATE INDEX idx_supplier_status
    ON supplier (status) WHERE is_deleted = false;

CREATE INDEX idx_supplier_is_deleted
    ON supplier (is_deleted);

CREATE INDEX idx_supplier_created_at
    ON supplier (created_at DESC);


ALTER TABLE supplier
    ADD CONSTRAINT chk_supplier_email
        CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');

ALTER TABLE supplier
    ADD CONSTRAINT chk_supplier_phone_length
        CHECK (LENGTH(phone) >= 8);


-- TABLE 2: PURCHASE_REQUISITION (Optional)

DROP TABLE IF EXISTS purchase_requisition CASCADE;

CREATE TABLE purchase_requisition
(
    id                UUID PRIMARY KEY NOT NULL,
    pr_number         VARCHAR(50)      NOT NULL UNIQUE,
    requested_by_id   UUID             NOT NULL,
    required_date     DATE             NOT NULL,
    purpose           TEXT,
    status            VARCHAR(30)      NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'CONVERTED')),
    estimated_total   DECIMAL(19, 4),
    approved_by_id    UUID,
    approval_date     TIMESTAMP,
    purchase_order_id UUID,
    is_deleted        BOOLEAN          NOT NULL DEFAULT false,
    created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pr_number ON purchase_requisition (pr_number) WHERE is_deleted = false;
CREATE INDEX idx_pr_status ON purchase_requisition (status) WHERE is_deleted = false;
CREATE INDEX idx_pr_requested_by ON purchase_requisition (requested_by_id) WHERE is_deleted = false;


-- TABLE 3: PURCHASE_ORDER (Core Entity)

DROP TABLE IF EXISTS purchase_order CASCADE;

CREATE TABLE purchase_order
(
    id                  UUID PRIMARY KEY NOT NULL,
    po_number           VARCHAR(50)      NOT NULL UNIQUE,
    supplier_id         UUID             NOT NULL,
    warehouse_id        UUID             NOT NULL,
    requisition_id      UUID,
    po_date             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_date       DATE             NOT NULL,
    currency            VARCHAR(3)       NOT NULL DEFAULT 'VND',
    subtotal            DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    tax_percentage      DECIMAL(5, 2)    NOT NULL DEFAULT 10.00,
    shipping_cost       DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    grand_total         DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    payment_terms       VARCHAR(100),
    incoterms           VARCHAR(50),
    notes               TEXT,
    status              VARCHAR(30)      NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN (
                          'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'SENT_TO_SUPPLIER',
                          'GOODS_RECEIVED', 'REJECTED', 'CANCELLED', 'CLOSED'
            )),
    approved_by_id      UUID,
    approval_date       TIMESTAMP,
    cancelled_by_id     UUID,
    cancelled_at        TIMESTAMP,
    cancellation_reason TEXT,
    version             BIGINT           NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN          NOT NULL DEFAULT false,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE UNIQUE INDEX idx_po_number ON purchase_order (po_number) WHERE is_deleted = false;
CREATE INDEX idx_po_supplier_id ON purchase_order (supplier_id) WHERE is_deleted = false;
CREATE INDEX idx_po_status ON purchase_order (status) WHERE is_deleted = false;
CREATE INDEX idx_po_supplier_status ON purchase_order (supplier_id, status) WHERE is_deleted = false;
CREATE INDEX idx_po_po_date ON purchase_order (po_date DESC) WHERE is_deleted = false;
CREATE INDEX idx_po_delivery_date ON purchase_order (delivery_date) WHERE is_deleted = false;


ALTER TABLE purchase_order
    ADD CONSTRAINT fk_po_supplier
        FOREIGN KEY (supplier_id)
            REFERENCES supplier (id)
            ON DELETE RESTRICT;

ALTER TABLE purchase_order
    ADD CONSTRAINT chk_po_delivery_date
        CHECK (delivery_date > CURRENT_DATE);

ALTER TABLE purchase_order
    ADD CONSTRAINT chk_po_totals
        CHECK (grand_total = subtotal + tax_amount + shipping_cost - discount_amount);

ALTER TABLE purchase_order
    ADD CONSTRAINT chk_po_financial_values
        CHECK (subtotal >= 0 AND tax_amount >= 0 AND shipping_cost >= 0);


-- TABLE 4: PURCHASE_ORDER_ITEM

DROP TABLE IF EXISTS purchase_order_item CASCADE;

CREATE TABLE purchase_order_item
(
    id                UUID PRIMARY KEY NOT NULL,
    purchase_order_id UUID             NOT NULL,
    product_id        UUID             NOT NULL, -- Cross-module reference
    product_code      VARCHAR(50)      NOT NULL,
    product_name      VARCHAR(255)     NOT NULL,
    product_unit      VARCHAR(20)      NOT NULL,
    line_number       INTEGER          NOT NULL,
    quantity          DECIMAL(19, 4)   NOT NULL,
    unit_price        DECIMAL(19, 4)   NOT NULL,
    description       TEXT,
    received_quantity DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    rejected_quantity DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    status            VARCHAR(30)      NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PARTIALLY_RECEIVED', 'FULLY_RECEIVED', 'CANCELLED')),
    notes             TEXT,
    is_deleted        BOOLEAN          NOT NULL DEFAULT false,
    created_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX idx_poi_po_id ON purchase_order_item (purchase_order_id) WHERE is_deleted = false;
CREATE INDEX idx_poi_product_id ON purchase_order_item (product_id) WHERE is_deleted = false;
CREATE INDEX idx_poi_status ON purchase_order_item (status) WHERE is_deleted = false;


ALTER TABLE purchase_order_item
    ADD CONSTRAINT fk_poi_po
        FOREIGN KEY (purchase_order_id)
            REFERENCES purchase_order (id)
            ON DELETE CASCADE;

ALTER TABLE purchase_order_item
    ADD CONSTRAINT chk_poi_line_unique
        UNIQUE (purchase_order_id, line_number);

ALTER TABLE purchase_order_item
    ADD CONSTRAINT chk_poi_quantity
        CHECK (quantity > 0);

ALTER TABLE purchase_order_item
    ADD CONSTRAINT chk_poi_unit_price
        CHECK (unit_price > 0);

ALTER TABLE purchase_order_item
    ADD CONSTRAINT chk_poi_received
        CHECK (received_quantity <= quantity);

ALTER TABLE purchase_order_item
    ADD CONSTRAINT chk_poi_quantities
        CHECK (received_quantity + rejected_quantity <= quantity);


-- TABLE 5: GOODS_RECEIPT (Warehouse Receipt)

DROP TABLE IF EXISTS goods_receipt CASCADE;

CREATE TABLE goods_receipt
(
    id                      UUID PRIMARY KEY NOT NULL,
    gr_number               VARCHAR(50)      NOT NULL UNIQUE,
    purchase_order_id       UUID             NOT NULL,
    supplier_id             UUID             NOT NULL,
    warehouse_id            UUID             NOT NULL, -- Cross-module reference
    gr_date                 TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_by_id          UUID             NOT NULL,
    quality_check_status    VARCHAR(30)      NOT NULL DEFAULT 'PENDING'
        CHECK (quality_check_status IN ('PENDING', 'PASSED', 'FAILED')),
    quality_check_notes     TEXT,
    quality_checked_by_id   UUID,
    quality_check_date      TIMESTAMP,
    inventory_import_status VARCHAR(30)      NOT NULL DEFAULT 'PENDING'
        CHECK (inventory_import_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    inventory_error_message TEXT,
    last_inventory_retry_at TIMESTAMP,
    status                  VARCHAR(30)      NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'RECEIVED', 'QC_PASSED', 'QC_FAILED', 'IMPORTED', 'CANCELLED')),
    rejection_reason        TEXT,
    version                 BIGINT           NOT NULL DEFAULT 0,
    is_deleted              BOOLEAN          NOT NULL DEFAULT false,
    created_at              TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);



CREATE UNIQUE INDEX idx_gr_number ON goods_receipt (gr_number) WHERE is_deleted = false;
CREATE INDEX idx_gr_po_id ON goods_receipt (purchase_order_id) WHERE is_deleted = false;
CREATE INDEX idx_gr_supplier_id ON goods_receipt (supplier_id) WHERE is_deleted = false;
CREATE INDEX idx_gr_status ON goods_receipt (status) WHERE is_deleted = false;
CREATE INDEX idx_gr_warehouse_id ON goods_receipt (warehouse_id) WHERE is_deleted = false;

CREATE INDEX idx_gr_inventory_import_failed
    ON goods_receipt (inventory_import_status) WHERE inventory_import_status = 'FAILED';



ALTER TABLE goods_receipt
    ADD CONSTRAINT fk_gr_po
        FOREIGN KEY (purchase_order_id)
            REFERENCES purchase_order (id)
            ON DELETE RESTRICT;

ALTER TABLE goods_receipt
    ADD CONSTRAINT fk_gr_supplier
        FOREIGN KEY (supplier_id)
            REFERENCES supplier (id)
            ON DELETE RESTRICT;




DROP TABLE IF EXISTS goods_receipt_item CASCADE;

CREATE TABLE goods_receipt_item
(
    id                     UUID PRIMARY KEY NOT NULL,
    goods_receipt_id       UUID             NOT NULL,
    purchase_order_item_id UUID             NOT NULL,
    product_id             UUID             NOT NULL, -- Cross-module reference
    quantity_accepted      DECIMAL(19, 4)   NOT NULL CHECK (quantity_accepted >= 0),
    quantity_rejected      DECIMAL(19, 4)   NOT NULL DEFAULT 0 CHECK (quantity_rejected >= 0),
    batch_number           VARCHAR(50),               -- Optional: For tracking inventory batches
    expiry_date            DATE,                      -- Optional: For perishable items
    notes                  TEXT,
    is_deleted             BOOLEAN          NOT NULL DEFAULT false,
    created_at             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);



CREATE INDEX idx_gri_gr_id ON goods_receipt_item (goods_receipt_id) WHERE is_deleted = false;
CREATE INDEX idx_gri_poi_id ON goods_receipt_item (purchase_order_item_id) WHERE is_deleted = false;

ALTER TABLE goods_receipt_item
    ADD CONSTRAINT fk_gri_gr
        FOREIGN KEY (goods_receipt_id)
            REFERENCES goods_receipt (id)
            ON DELETE CASCADE;

ALTER TABLE goods_receipt_item
    ADD CONSTRAINT fk_gri_poi
        FOREIGN KEY (purchase_order_item_id)
            REFERENCES purchase_order_item (id)
            ON DELETE RESTRICT;