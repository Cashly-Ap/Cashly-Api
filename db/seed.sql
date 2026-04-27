-- =============================================================================
-- Parsun API - Master Seed File
-- Loads all seed files in order.
--
-- Run manually:
--   docker compose exec -T mysql mysql -umyuser -psecret mydatabase < db/seed.sql
--
-- Individual seeds are in db/seeds/:
--   01_users.sql       - Users and module permissions
--   02_customers.sql   - Customers, emails, phones, addresses
--   03_categories.sql  - Product categories
--   04_warehouses.sql  - Warehouses and customer warehouse access
--   05_products.sql    - Simple/composite products, stock, movements
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Order matters: respect foreign key dependencies
SOURCE /seeds/01_users.sql;
SOURCE /seeds/02_customers.sql;
SOURCE /seeds/03_categories.sql;
SOURCE /seeds/04_warehouses.sql;
SOURCE /seeds/05_products.sql;

SET FOREIGN_KEY_CHECKS = 1;
