CREATE TABLE debit_notes (
    id              BINARY(16) PRIMARY KEY,
    note_number     VARCHAR(20) NOT NULL UNIQUE,
    customer_id     BINARY(16) NOT NULL,
    invoice_id      BINARY(16) NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    reason          VARCHAR(500) NOT NULL,
    status          ENUM('DRAFT', 'APPLIED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    applied_amount  DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_by      BINARY(16) NOT NULL,
    created_by_name VARCHAR(200) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_debit_notes_note_number    (note_number),
    INDEX idx_debit_notes_customer_id    (customer_id),
    INDEX idx_debit_notes_invoice_id     (invoice_id),
    INDEX idx_debit_notes_status         (status),
    INDEX idx_debit_notes_created_at     (created_at),

    CONSTRAINT fk_debit_notes_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_debit_notes_invoice  FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_debit_notes_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE debit_note_number_sequence (
    year        INT NOT NULL,
    next_value  INT NOT NULL DEFAULT 1,
    PRIMARY KEY (year)
);
