CREATE TABLE purchases (
    id BINARY(16) PRIMARY KEY,
    purchase_number VARCHAR(20) NOT NULL UNIQUE,
    supplier_id BINARY(16) NOT NULL,
    supplier_name VARCHAR(200) NULL,
    supplier_rif VARCHAR(50) NULL,
    created_by BINARY(16) NULL,
    created_by_name VARCHAR(200) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes VARCHAR(1000) NULL,
    subtotal DECIMAL(15,2) NULL,
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancelled_reason VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE INDEX idx_purchases_number ON purchases (purchase_number);
CREATE INDEX idx_purchases_supplier ON purchases (supplier_id);
CREATE INDEX idx_purchases_created_by ON purchases (created_by);
CREATE INDEX idx_purchases_status ON purchases (status);
CREATE INDEX idx_purchases_created_at ON purchases (created_at);

CREATE TABLE purchase_lines (
    id BINARY(16) PRIMARY KEY,
    purchase_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    product_name VARCHAR(200) NULL,
    product_code VARCHAR(50) NULL,
    quantity DECIMAL(15,4) NOT NULL,
    unit_cost DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NULL,
    CONSTRAINT fk_purchase_lines_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_lines_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_purchase_lines_purchase ON purchase_lines (purchase_id);
CREATE INDEX idx_purchase_lines_product ON purchase_lines (product_id);

CREATE TABLE purchase_line_allocations (
    id BINARY(16) PRIMARY KEY,
    purchase_line_id BINARY(16) NOT NULL,
    warehouse_id BINARY(16) NOT NULL,
    quantity DECIMAL(15,4) NOT NULL,
    CONSTRAINT fk_pla_line FOREIGN KEY (purchase_line_id) REFERENCES purchase_lines(id) ON DELETE CASCADE,
    CONSTRAINT fk_pla_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE INDEX idx_pla_line ON purchase_line_allocations (purchase_line_id);
CREATE INDEX idx_pla_warehouse ON purchase_line_allocations (warehouse_id);

CREATE TABLE purchase_number_sequence (
    year INT PRIMARY KEY,
    next_value INT NOT NULL DEFAULT 1
);
