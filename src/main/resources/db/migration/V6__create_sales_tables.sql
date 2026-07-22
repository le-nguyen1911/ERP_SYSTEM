-- TABLE 1: CUSTOMER
DROP TABLE IF EXISTS customer CASCADE;

CREATE TABLE customer
(
    id                  UUID PRIMARY KEY NOT NULL,
    customer_code       VARCHAR(50)      NOT NULL UNIQUE,
    customer_name       VARCHAR(255)     NOT NULL,
    contact_person      VARCHAR(100)     NOT NULL,
    email               VARCHAR(100)     NOT NULL,
    phone               VARCHAR(20)      NOT NULL,
    address             TEXT             NOT NULL,
    city                VARCHAR(100),
    country             VARCHAR(100),
    tax_id              VARCHAR(50),
    payment_terms       VARCHAR(50),
    status              VARCHAR(20)      NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    is_deleted          BOOLEAN          NOT NULL DEFAULT false,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_customer_code ON customer (customer_code) WHERE is_deleted = false;
CREATE INDEX idx_customer_status ON customer (status) WHERE is_deleted = false;
CREATE INDEX idx_customer_is_deleted ON customer (is_deleted);

ALTER TABLE customer ADD CONSTRAINT chk_customer_email
    CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');
ALTER TABLE customer ADD CONSTRAINT chk_customer_phone_length CHECK (LENGTH(phone) >= 8);


-- TABLE 2: SALES_ORDER
DROP TABLE IF EXISTS sales_order CASCADE;

CREATE TABLE sales_order
(
    id                  UUID PRIMARY KEY NOT NULL,
    so_number           VARCHAR(50)      NOT NULL UNIQUE,
    customer_id         UUID             NOT NULL,
    warehouse_id        UUID             NOT NULL,
    so_date             TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_date       DATE             NOT NULL,
    currency            VARCHAR(3)       NOT NULL DEFAULT 'VND',
    subtotal            DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    tax_percentage      DECIMAL(5, 2)    NOT NULL DEFAULT 10.00,
    shipping_cost       DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    grand_total         DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    payment_terms       VARCHAR(100),
    shipping_address    TEXT,
    notes               TEXT,
    status              VARCHAR(30)      NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN (
                          'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'CONFIRMED',
                          'DELIVERED', 'REJECTED', 'CANCELLED', 'CLOSED'
            )),
    approved_by_id      UUID,
    approval_date       TIMESTAMP,
    cancelled_by_id     UUID,
    cancelled_at        TIMESTAMP,
    cancellation_reason TEXT,
    rejection_reason    TEXT,
    version             BIGINT           NOT NULL DEFAULT 0,
    is_deleted          BOOLEAN          NOT NULL DEFAULT false,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_so_number ON sales_order (so_number) WHERE is_deleted = false;
CREATE INDEX idx_so_customer_id ON sales_order (customer_id) WHERE is_deleted = false;
CREATE INDEX idx_so_status ON sales_order (status) WHERE is_deleted = false;
CREATE INDEX idx_so_customer_status ON sales_order (customer_id, status) WHERE is_deleted = false;
CREATE INDEX idx_so_so_date ON sales_order (so_date DESC) WHERE is_deleted = false;

ALTER TABLE sales_order ADD CONSTRAINT fk_so_customer
    FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE RESTRICT;
ALTER TABLE sales_order ADD CONSTRAINT chk_so_delivery_date CHECK (delivery_date > CURRENT_DATE);
ALTER TABLE sales_order ADD CONSTRAINT chk_so_totals
    CHECK (grand_total = subtotal + tax_amount + shipping_cost - discount_amount);
ALTER TABLE sales_order ADD CONSTRAINT chk_so_financial_values
    CHECK (subtotal >= 0 AND tax_amount >= 0 AND shipping_cost >= 0);



-- TABLE 3: SALES_ORDER_ITEM
DROP TABLE IF EXISTS sales_order_item CASCADE;

CREATE TABLE sales_order_item
(
    id                  UUID PRIMARY KEY NOT NULL,
    sales_order_id      UUID             NOT NULL,
    product_id          UUID             NOT NULL,
    product_code        VARCHAR(50)      NOT NULL,
    product_name        VARCHAR(255)     NOT NULL,
    product_unit        VARCHAR(20)      NOT NULL,
    line_number         INTEGER          NOT NULL,
    quantity            DECIMAL(19, 4)   NOT NULL,
    unit_price          DECIMAL(19, 4)   NOT NULL,
    description         TEXT,
    delivered_quantity  DECIMAL(19, 4)   NOT NULL DEFAULT 0,
    status              VARCHAR(30)      NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PARTIALLY_DELIVERED', 'FULLY_DELIVERED', 'CANCELLED')),
    notes               TEXT,
    is_deleted          BOOLEAN          NOT NULL DEFAULT false,
    created_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_soi_so_id ON sales_order_item (sales_order_id) WHERE is_deleted = false;
CREATE INDEX idx_soi_product_id ON sales_order_item (product_id) WHERE is_deleted = false;
CREATE INDEX idx_soi_status ON sales_order_item (status) WHERE is_deleted = false;

ALTER TABLE sales_order_item ADD CONSTRAINT fk_soi_so
    FOREIGN KEY (sales_order_id) REFERENCES sales_order (id) ON DELETE CASCADE;
ALTER TABLE sales_order_item ADD CONSTRAINT chk_soi_line_unique UNIQUE (sales_order_id, line_number);
ALTER TABLE sales_order_item ADD CONSTRAINT chk_soi_quantity CHECK (quantity > 0);
ALTER TABLE sales_order_item ADD CONSTRAINT chk_soi_unit_price CHECK (unit_price > 0);
ALTER TABLE sales_order_item ADD CONSTRAINT chk_soi_delivered CHECK (delivered_quantity <= quantity);


-- TABLE 4: DELIVERY
DROP TABLE IF EXISTS delivery CASCADE;

CREATE TABLE delivery
(
    id                      UUID PRIMARY KEY NOT NULL,
    delivery_number         VARCHAR(50)      NOT NULL UNIQUE,
    sales_order_id          UUID             NOT NULL,
    customer_id             UUID             NOT NULL,
    warehouse_id            UUID             NOT NULL,
    delivery_date           TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_by_id         UUID             NOT NULL,
    inventory_export_status VARCHAR(30)      NOT NULL DEFAULT 'PENDING'
        CHECK (inventory_export_status IN ('PENDING', 'SUCCESS', 'FAILED')),
    inventory_error_message TEXT,
    last_inventory_retry_at TIMESTAMP,
    status                  VARCHAR(30)      NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'EXPORTED', 'DELIVERED', 'CANCELLED')),
    rejection_reason        TEXT,
    version                 BIGINT           NOT NULL DEFAULT 0,
    is_deleted              BOOLEAN          NOT NULL DEFAULT false,
    created_at              TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_delivery_number ON delivery (delivery_number) WHERE is_deleted = false;
CREATE INDEX idx_delivery_so_id ON delivery (sales_order_id) WHERE is_deleted = false;
CREATE INDEX idx_delivery_customer_id ON delivery (customer_id) WHERE is_deleted = false;
CREATE INDEX idx_delivery_status ON delivery (status) WHERE is_deleted = false;
CREATE INDEX idx_delivery_export_failed
    ON delivery (inventory_export_status) WHERE inventory_export_status = 'FAILED';

ALTER TABLE delivery ADD CONSTRAINT fk_delivery_so
    FOREIGN KEY (sales_order_id) REFERENCES sales_order (id) ON DELETE RESTRICT;
ALTER TABLE delivery ADD CONSTRAINT fk_delivery_customer
    FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE RESTRICT;


-- TABLE 5: DELIVERY_ITEM
DROP TABLE IF EXISTS delivery_item CASCADE;

CREATE TABLE delivery_item
(
    id                   UUID PRIMARY KEY NOT NULL,
    delivery_id          UUID             NOT NULL,
    sales_order_item_id  UUID             NOT NULL,
    product_id           UUID             NOT NULL,
    quantity_delivered   DECIMAL(19, 4)   NOT NULL CHECK (quantity_delivered > 0),
    batch_number         VARCHAR(50),
    notes                TEXT,
    is_deleted           BOOLEAN          NOT NULL DEFAULT false,
    created_at           TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_di_delivery_id ON delivery_item (delivery_id) WHERE is_deleted = false;
CREATE INDEX idx_di_soi_id ON delivery_item (sales_order_item_id) WHERE is_deleted = false;

ALTER TABLE delivery_item ADD CONSTRAINT fk_di_delivery
    FOREIGN KEY (delivery_id) REFERENCES delivery (id) ON DELETE CASCADE;
ALTER TABLE delivery_item ADD CONSTRAINT fk_di_soi
    FOREIGN KEY (sales_order_item_id) REFERENCES sales_order_item (id) ON DELETE RESTRICT;


-- ════════════════════════════════════════════════════════════════════
-- SEQUENCE
-- ════════════════════════════════════════════════════════════════════
CREATE SEQUENCE IF NOT EXISTS seq_so_number START WITH 1 INCREMENT BY 1 NO CYCLE;
CREATE SEQUENCE IF NOT EXISTS seq_delivery_number START WITH 1 INCREMENT BY 1 NO CYCLE;