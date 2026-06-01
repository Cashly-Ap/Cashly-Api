CREATE TABLE payment_methods (
    id          BINARY(16) PRIMARY KEY,
    code        VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id               BINARY(16) PRIMARY KEY,
    payment_number   VARCHAR(20) NOT NULL UNIQUE,
    customer_id      BINARY(16) NOT NULL,
    type             ENUM('PAYMENT', 'ADVANCE') NOT NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    payment_method_id BINARY(16) NOT NULL,
    reference_number VARCHAR(100) NULL,
    payment_date     DATE NOT NULL,
    notes            VARCHAR(1000) NULL,
    status           ENUM('APPLIED', 'CANCELLED') NOT NULL DEFAULT 'APPLIED',
    applied_amount   DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_by       BINARY(16) NOT NULL,
    created_by_name  VARCHAR(200) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_payments_payment_number   (payment_number),
    INDEX idx_payments_customer_id      (customer_id),
    INDEX idx_payments_type             (type),
    INDEX idx_payments_status           (status),
    INDEX idx_payments_payment_date     (payment_date),
    INDEX idx_payments_created_at       (created_at),

    CONSTRAINT fk_payments_customer       FOREIGN KEY (customer_id)       REFERENCES customers(id),
    CONSTRAINT fk_payments_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
    CONSTRAINT fk_payments_created_by     FOREIGN KEY (created_by)        REFERENCES users(id)
);

CREATE TABLE payment_allocations (
    id               BINARY(16) PRIMARY KEY,
    payment_id       BINARY(16) NOT NULL,
    invoice_id       BINARY(16) NOT NULL,
    allocated_amount DECIMAL(15, 2) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_payment_allocations_payment_id (payment_id),
    INDEX idx_payment_allocations_invoice_id (invoice_id),

    CONSTRAINT fk_payment_allocations_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_allocations_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE TABLE payment_number_sequence (
    year        INT NOT NULL,
    next_value  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
