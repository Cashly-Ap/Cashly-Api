-- =============================================================================
-- Parsun API - Master Seed File
-- Loads all seed files in order.
--
-- Run manually:
--   docker compose exec -T mysql mysql -umyuser -psecret --default-character-set=utf8mb4 mydatabase < db/seed.sql
--
-- Individual seeds are in db/seeds/:
--   01_users.sql       - Users and module permissions
--   02_customers.sql   - Customers, emails, phones, addresses
--   03_categories.sql  - Product categories
--   04_warehouses.sql  - Warehouses and customer warehouse access
--   05_products.sql    - Simple/composite products, stock, movements
--   06_price_types.sql - Price types (Base, Mayor, Especial)
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Order matters: respect foreign key dependencies
SOURCE /seeds/01_users.sql;
SOURCE /seeds/02_customers.sql;
SOURCE /seeds/03_categories.sql;
SOURCE /seeds/04_warehouses.sql;
SOURCE /seeds/05_products.sql;
SOURCE /seeds/06_price_types.sql;

SET FOREIGN_KEY_CHECKS = 1;
