CREATE TABLE returns (
    id              BINARY(16) PRIMARY KEY,
    return_number   VARCHAR(20) NOT NULL UNIQUE,
    invoice_id      BINARY(16) NOT NULL,
    customer_id     BINARY(16) NOT NULL,
    return_type     ENUM('AUTOMATIC', 'MANUAL') NOT NULL,
    reason          VARCHAR(500) NOT NULL,
    created_by      BINARY(16) NOT NULL,
    created_by_name VARCHAR(200) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_returns_return_number  (return_number),
    INDEX idx_returns_invoice_id     (invoice_id),
    INDEX idx_returns_customer_id    (customer_id),
    INDEX idx_returns_return_type    (return_type),
    INDEX idx_returns_created_at     (created_at),

    CONSTRAINT fk_returns_invoice   FOREIGN KEY (invoice_id)   REFERENCES invoices(id),
    CONSTRAINT fk_returns_customer  FOREIGN KEY (customer_id)  REFERENCES customers(id),
    CONSTRAINT fk_returns_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE return_lines (
    id              BINARY(16) PRIMARY KEY,
    return_id       BINARY(16) NOT NULL,
    invoice_line_id BINARY(16) NOT NULL,
    product_id      BINARY(16) NOT NULL,
    quantity        DECIMAL(15, 4) NOT NULL,
    warehouse_id    BINARY(16) NOT NULL,
    unit_price      DECIMAL(15, 2) NOT NULL,
    line_total      DECIMAL(15, 2) NOT NULL,

    INDEX idx_return_lines_return_id   (return_id),
    INDEX idx_return_lines_product_id  (product_id),

    CONSTRAINT fk_return_lines_return       FOREIGN KEY (return_id)       REFERENCES returns(id) ON DELETE CASCADE,
    CONSTRAINT fk_return_lines_invoice_line FOREIGN KEY (invoice_line_id) REFERENCES invoice_lines(id),
    CONSTRAINT fk_return_lines_product      FOREIGN KEY (product_id)      REFERENCES products(id),
    CONSTRAINT fk_return_lines_warehouse    FOREIGN KEY (warehouse_id)    REFERENCES warehouses(id)
);

CREATE TABLE return_number_sequence (
    year        INT NOT NULL,
    next_value  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
