CREATE TABLE warehouses (
    id           BINARY(16) PRIMARY KEY,
    code         VARCHAR(50) NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    address      TEXT NOT NULL,
    state        VARCHAR(100),
    municipality VARCHAR(100),
    postal_code  VARCHAR(10),
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_warehouse_code   (code),
    INDEX idx_warehouse_name   (name),
    INDEX idx_warehouse_active (active)
);

CREATE TABLE warehouse_stock (
    id               BINARY(16) PRIMARY KEY,
    warehouse_id     BINARY(16) NOT NULL,
    product_id       BINARY(16) NOT NULL,
    quantity         INT NOT NULL DEFAULT 0,
    reserved         INT NOT NULL DEFAULT 0,
    last_movement_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_warehouse_product (warehouse_id, product_id),
    INDEX idx_ws_warehouse (warehouse_id),
    INDEX idx_ws_product   (product_id),
    INDEX idx_ws_reserved  (reserved),
    CONSTRAINT fk_ws_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_ws_product   FOREIGN KEY (product_id)   REFERENCES products(id)
);

CREATE TABLE stock_movements (
    id                 BINARY(16) PRIMARY KEY,
    warehouse_id       BINARY(16) NOT NULL,
    product_id         BINARY(16) NOT NULL,
    movement_type      VARCHAR(30) NOT NULL,
    quantity           INT NOT NULL,
    previous_stock     INT NOT NULL,
    new_stock          INT NOT NULL,
    origin_movement_id BINARY(16) NULL,
    transfer_batch_id  VARCHAR(50),
    reference_type     VARCHAR(20) NULL,
    reference_id       BINARY(16) NULL,
    reference_number   VARCHAR(100) NULL,
    notes              VARCHAR(500),
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sm_warehouse         (warehouse_id),
    INDEX idx_sm_product           (product_id),
    INDEX idx_sm_type              (movement_type),
    INDEX idx_sm_origin            (origin_movement_id),
    INDEX idx_sm_transfer_batch    (transfer_batch_id),
    INDEX idx_sm_created           (created_at),
    INDEX idx_sm_warehouse_product (warehouse_id, product_id),
    INDEX idx_sm_ref_type_id       (reference_type, reference_id),
    CONSTRAINT chk_sm_quantity CHECK (quantity > 0),
    CONSTRAINT fk_sm_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_sm_product   FOREIGN KEY (product_id)   REFERENCES products(id)
);

CREATE TABLE customer_warehouse_access (
    id           BINARY(16) PRIMARY KEY,
    customer_id  BINARY(16) NOT NULL,
    warehouse_id BINARY(16) NOT NULL,
    granted_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by   BINARY(16) NOT NULL,
    UNIQUE KEY uk_customer_warehouse (customer_id, warehouse_id),
    INDEX idx_cwa_customer  (customer_id),
    INDEX idx_cwa_warehouse (warehouse_id),
    CONSTRAINT fk_cwa_customer  FOREIGN KEY (customer_id)  REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_cwa_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);
