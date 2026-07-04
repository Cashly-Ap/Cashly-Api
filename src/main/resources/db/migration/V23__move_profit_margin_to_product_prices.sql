ALTER TABLE product_prices
    ADD COLUMN profit_margin DECIMAL(5,2) NULL AFTER price;

UPDATE product_prices pp
    JOIN products p ON pp.product_id = p.id
SET pp.profit_margin = p.profit_margin;

ALTER TABLE products
    DROP COLUMN base_price;

ALTER TABLE products
    DROP COLUMN profit_margin;
