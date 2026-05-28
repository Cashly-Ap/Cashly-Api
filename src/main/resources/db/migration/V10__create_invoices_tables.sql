CREATE TABLE invoices (
    id               BINARY(16) PRIMARY KEY,
    invoice_number   VARCHAR(20) NOT NULL UNIQUE,
    order_id         BINARY(16) NULL,
    order_number     VARCHAR(20) NULL,
    customer_id      BINARY(16) NOT NULL,
    customer_name    VARCHAR(200) NOT NULL,
    customer_rif     VARCHAR(50) NOT NULL,
    created_by       BINARY(16) NOT NULL,
    created_by_name  VARCHAR(200) NOT NULL,
    status           VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    payment_type     VARCHAR(20) NULL,
    notes            VARCHAR(1000) NULL,
    subtotal         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cancelled_at     TIMESTAMP NULL,
    cancelled_reason VARCHAR(500) NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_invoices_invoice_number (invoice_number),
    INDEX idx_invoices_order_id       (order_id),
    INDEX idx_invoices_customer_id    (customer_id),
    INDEX idx_invoices_created_by     (created_by),
    INDEX idx_invoices_status         (status),
    INDEX idx_invoices_created_at     (created_at),

    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE invoice_lines (
    id           BINARY(16) PRIMARY KEY,
    invoice_id   BINARY(16) NOT NULL,
    product_id   BINARY(16) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity     DECIMAL(15, 4) NOT NULL,
    unit_price   DECIMAL(15, 2) NOT NULL,
    line_total   DECIMAL(15, 2) NOT NULL,

    INDEX idx_invoice_lines_invoice_id (invoice_id),
    INDEX idx_invoice_lines_product_id (product_id),

    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_invoice_lines_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE invoice_number_sequence (
    year       INT NOT NULL,
    next_value INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
