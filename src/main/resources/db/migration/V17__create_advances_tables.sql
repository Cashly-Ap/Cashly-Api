CREATE TABLE advances (
    id               BINARY(16) PRIMARY KEY,
    advance_number   VARCHAR(20) NOT NULL UNIQUE,
    customer_id      BINARY(16) NOT NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    remaining_amount DECIMAL(15, 2) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes            VARCHAR(1000) NULL,
    created_by       BINARY(16) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE advance_consumptions (
    id               BINARY(16) PRIMARY KEY,
    advance_id       BINARY(16) NOT NULL,
    invoice_id       BINARY(16) NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    payment_id       BINARY(16) NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (advance_id) REFERENCES advances(id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id)
);
