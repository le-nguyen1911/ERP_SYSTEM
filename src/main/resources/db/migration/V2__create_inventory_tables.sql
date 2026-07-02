-- V2__create_inventory_tables.sql

CREATE TABLE IF NOT EXISTS categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,

    CONSTRAINT uq_categories_name UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS units (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_units_name UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS warehouses (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(255),
    description VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT uq_warehouses_name UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS products (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(50)    NOT NULL,
    name         VARCHAR(200)   NOT NULL,
    description  TEXT,
    price        NUMERIC(15,2)  NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id  UUID           NOT NULL,
    -- UUID thay vì BIGINT cho FK
    unit_id      UUID           NOT NULL,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,

    CONSTRAINT uq_products_code UNIQUE (code),
    CONSTRAINT chk_products_price_positive CHECK (price >= 0),

    CONSTRAINT fk_products_category
    FOREIGN KEY (category_id)
    REFERENCES categories(id),

    CONSTRAINT fk_products_unit
    FOREIGN KEY (unit_id)
    REFERENCES units(id)
    );

CREATE TABLE IF NOT EXISTS product_stocks (
    id            UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID    NOT NULL,
    warehouse_id  UUID    NOT NULL,
    quantity      INTEGER NOT NULL DEFAULT 0,
    min_quantity  INTEGER NOT NULL DEFAULT 0,
    version       BIGINT  NOT NULL DEFAULT 0,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,

    CONSTRAINT uq_product_warehouse
    UNIQUE (product_id, warehouse_id),

    CONSTRAINT fk_stocks_product
    FOREIGN KEY (product_id)
    REFERENCES products(id),

    CONSTRAINT fk_stocks_warehouse
    FOREIGN KEY (warehouse_id)
    REFERENCES warehouses(id),

    CONSTRAINT chk_stocks_quantity_non_negative
    CHECK (quantity >= 0),

    CONSTRAINT chk_stocks_min_quantity_non_negative
    CHECK (min_quantity >= 0)
    );

CREATE TABLE IF NOT EXISTS stock_transactions (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id    UUID         NOT NULL,
    warehouse_id  UUID         NOT NULL,
    type          VARCHAR(10)  NOT NULL,
    quantity      INTEGER      NOT NULL,
    unit_price    NUMERIC(15,2),
    note          VARCHAR(500),
    created_by    VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,

    CONSTRAINT fk_transactions_product
    FOREIGN KEY (product_id)
    REFERENCES products(id),

    CONSTRAINT fk_transactions_warehouse
    FOREIGN KEY (warehouse_id)
    REFERENCES warehouses(id),

    CONSTRAINT chk_transactions_type
    CHECK (type IN ('IMPORT', 'EXPORT')),

    CONSTRAINT chk_transactions_quantity_positive
    CHECK (quantity > 0)
    );