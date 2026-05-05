CREATE TABLE orders (
    id              BINARY(16) PRIMARY KEY,
    order_number    VARCHAR(20) NOT NULL UNIQUE,
    customer_id     BINARY(16) NOT NULL,
    customer_name   VARCHAR(200) NOT NULL,
    customer_rif    VARCHAR(50) NOT NULL,
    created_by      BINARY(16) NOT NULL,
    created_by_name VARCHAR(200) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    delivery_date   DATE NOT NULL,
    notes           VARCHAR(1000) NULL,
    subtotal        DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    invoice_id      BINARY(16) NULL,
    invoice_number  VARCHAR(20) NULL,
    cancelled_at    TIMESTAMP NULL,
    cancelled_reason VARCHAR(500) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_orders_order_number  (order_number),
    INDEX idx_orders_customer_id   (customer_id),
    INDEX idx_orders_created_by    (created_by),
    INDEX idx_orders_status        (status),
    INDEX idx_orders_delivery_date (delivery_date),
    INDEX idx_orders_created_at    (created_at),

    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE order_lines (
    id           BINARY(16) PRIMARY KEY,
    order_id     BINARY(16) NOT NULL,
    product_id   BINARY(16) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity     DECIMAL(15, 4) NOT NULL,
    unit_price   DECIMAL(15, 2) NOT NULL,
    line_total   DECIMAL(15, 2) NOT NULL,

    INDEX idx_order_lines_order_id   (order_id),
    INDEX idx_order_lines_product_id (product_id),

    CONSTRAINT fk_order_lines_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_order_lines_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE order_number_sequence (
    year        INT NOT NULL,
    next_value  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
