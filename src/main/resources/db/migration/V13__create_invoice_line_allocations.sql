CREATE TABLE invoice_line_allocations (
    id              BINARY(16)     NOT NULL,
    invoice_line_id BINARY(16)     NOT NULL,
    warehouse_id    BINARY(16)     NOT NULL,
    allocated       DECIMAL(15,4)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ila_invoice_line FOREIGN KEY (invoice_line_id) REFERENCES invoice_lines(id) ON DELETE CASCADE
);

CREATE INDEX idx_ila_invoice_line_id ON invoice_line_allocations(invoice_line_id);
