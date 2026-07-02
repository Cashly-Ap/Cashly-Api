-- =============================================================================
-- Parsun API - Master Seed File
-- Loads all seed files in order.
--
-- Run manually:
--   docker compose exec -T mysql mysql -umyuser -psecret --default-character-set=utf8mb4 mydatabase < db/seed.sql
--
-- Individual seeds are in db/seeds/:
--   01_users.sql                           - Users and module permissions
--   02_customers.sql                       - Customers, emails, phones, addresses
--   03_categories.sql                      - Product categories
--   04_warehouses.sql                      - Warehouses and customer warehouse access
--   05_products.sql                        - Simple/composite products, stock, movements
--   05b_price_types_and_product_prices.sql - Price types (Base, Mayor, Especial)
--   06_orders.sql                          - Orders
--   07_invoices.sql                        - Invoices
--   08_quotes.sql                          - Quotes
--   09_payment_methods.sql                 - Payment methods
--   10_payments.sql                        - Payments
--   11_credit_debit_notes.sql              - Credit and debit notes
--   12_returns.sql                         - Returns
--   13_warehouse_movements_and_customer_balances.sql - Movements and balances
--   14_cash_registers.sql                  - Cash registers
--   15_purchases.sql                       - Purchases (compras)
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Order matters: respect foreign key dependencies
SOURCE /seeds/01_users.sql;
SOURCE /seeds/02_customers.sql;
SOURCE /seeds/03_categories.sql;
SOURCE /seeds/04_warehouses.sql;
SOURCE /seeds/05_products.sql;
SOURCE /seeds/05b_price_types_and_product_prices.sql;
SOURCE /seeds/06_orders.sql;
SOURCE /seeds/07_invoices.sql;
SOURCE /seeds/08_quotes.sql;
SOURCE /seeds/09_payment_methods.sql;
SOURCE /seeds/10_payments.sql;
SOURCE /seeds/11_credit_debit_notes.sql;
SOURCE /seeds/12_returns.sql;
SOURCE /seeds/13_warehouse_movements_and_customer_balances.sql;
SOURCE /seeds/14_cash_registers.sql;
SOURCE /seeds/15_purchases.sql;

SET FOREIGN_KEY_CHECKS = 1;
