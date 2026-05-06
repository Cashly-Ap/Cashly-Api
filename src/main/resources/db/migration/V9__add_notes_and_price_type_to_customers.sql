ALTER TABLE customers
    ADD COLUMN notes TEXT NULL,
    ADD COLUMN price_type_id BINARY(16) NULL,
    ADD CONSTRAINT fk_customers_price_type
        FOREIGN KEY (price_type_id) REFERENCES price_types(id) ON DELETE SET NULL;
