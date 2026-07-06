CREATE TABLE return_cancelled_allocations (
    id BINARY(16) NOT NULL,
    return_id BINARY(16) NOT NULL,
    payment_id BINARY(16) NOT NULL,
    invoice_id BINARY(16) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_return_cancelled_allocations_return
        FOREIGN KEY (return_id) REFERENCES returns(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
