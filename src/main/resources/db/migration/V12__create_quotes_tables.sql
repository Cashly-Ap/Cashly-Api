CREATE TABLE quotes (
    id               BINARY(16) PRIMARY KEY,
    quote_number     VARCHAR(20) NOT NULL UNIQUE,
    customer_id      BINARY(16) NOT NULL,
    customer_name    VARCHAR(200) NOT NULL,
    customer_rif     VARCHAR(50) NOT NULL,
    created_by       BINARY(16) NOT NULL,
    created_by_name  VARCHAR(200) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    expiry_date      DATE NULL,
    notes            VARCHAR(1000) NULL,
    subtotal         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    order_id         BINARY(16) NULL,
    order_number     VARCHAR(20) NULL,
    invoice_id       BINARY(16) NULL,
    invoice_number   VARCHAR(20) NULL,
    cancelled_at     TIMESTAMP NULL,
    cancelled_reason VARCHAR(500) NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_quotes_quote_number  (quote_number),
    INDEX idx_quotes_customer_id   (customer_id),
    INDEX idx_quotes_created_by    (created_by),
    INDEX idx_quotes_status        (status),
    INDEX idx_quotes_expiry_date   (expiry_date),
    INDEX idx_quotes_created_at    (created_at),

    CONSTRAINT fk_quotes_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE quote_lines (
    id           BINARY(16) PRIMARY KEY,
    quote_id     BINARY(16) NOT NULL,
    product_id   BINARY(16) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity     DECIMAL(15, 4) NOT NULL,
    unit_price   DECIMAL(15, 2) NOT NULL,
    line_total   DECIMAL(15, 2) NOT NULL,

    INDEX idx_quote_lines_quote_id   (quote_id),
    INDEX idx_quote_lines_product_id (product_id),

    CONSTRAINT fk_quote_lines_quote FOREIGN KEY (quote_id) REFERENCES quotes(id)
);

CREATE TABLE quote_allocations (
    id                 BINARY(16) PRIMARY KEY,
    quote_id           BINARY(16) NOT NULL,
    quote_line_id      BINARY(16) NOT NULL,
    product_id         BINARY(16) NOT NULL,
    warehouse_id       BINARY(16) NOT NULL,
    warehouse_name     VARCHAR(200) NOT NULL,
    allocated_quantity DECIMAL(15, 4) NOT NULL,

    INDEX idx_quote_allocations_quote_id      (quote_id),
    INDEX idx_quote_allocations_quote_line_id (quote_line_id),
    INDEX idx_quote_allocations_warehouse_id  (warehouse_id),
    INDEX idx_quote_allocations_product_id    (product_id),

    CONSTRAINT fk_quote_allocations_quote      FOREIGN KEY (quote_id)      REFERENCES quotes(id),
    CONSTRAINT fk_quote_allocations_quote_line FOREIGN KEY (quote_line_id) REFERENCES quote_lines(id) ON DELETE CASCADE
);

CREATE TABLE quote_number_sequence (
    year        INT NOT NULL,
    next_value  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
