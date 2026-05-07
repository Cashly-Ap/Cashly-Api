CREATE TABLE order_line_allocations (
    id           BINARY(16)     NOT NULL,
    order_line_id BINARY(16)    NOT NULL,
    warehouse_id  BINARY(16)    NOT NULL,
    allocated     DECIMAL(15,4) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ola_order_line FOREIGN KEY (order_line_id) REFERENCES order_lines(id) ON DELETE CASCADE
);
