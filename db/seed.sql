-- =============================================================================
-- Cashly API - Master Seed File
-- Loads all seed files in order.
--
-- Run with scripts/reset-beta-data.sh <beta|dev|test> <database>.
-- The previous direct command is intentionally guarded; do not use --force.
-- Historical command:
--   docker compose --env-file .env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 --user="$MYSQL_USER" "$MYSQL_DATABASE" < /seed.sql'
--
-- Individual seeds are in db/seeds/:
--   00_access_references.sql               - Roles, departments and permission matrix
--   01_users.sql                           - Detailed users and required references
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
-- A direct SOURCE without explicit target acknowledgement fails before any deletion.
DROP PROCEDURE IF EXISTS cashly_assert_seed_target;
DELIMITER $$
CREATE PROCEDURE cashly_assert_seed_target()
BEGIN
    IF @cashly_seed_environment IS NULL OR @cashly_seed_environment NOT IN ('beta','dev','test')
       OR @cashly_seed_database IS NULL OR @cashly_seed_database <> DATABASE()
       OR DATABASE() NOT IN ('mydatabase','cashly','cashly_beta','cashly_dev','cashly_test') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Seed reset requires explicit beta/dev/test environment and acknowledged allowed database';
    END IF;
END$$
DELIMITER ;
CALL cashly_assert_seed_target();
DROP PROCEDURE cashly_assert_seed_target;
SOURCE /seeds/00_reset.sql;
SOURCE /seeds/00_access_references.sql;
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

SOURCE /seeds/16_reconcile.sql;
SOURCE /seeds/99_integrity.sql;
