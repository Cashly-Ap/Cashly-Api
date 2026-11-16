-- Read-only audit: roles that hold child permissions for a module without
-- holding the module VIEW_LIST root permission.
--
-- Run before enabling strict validation, or whenever role data is imported.
-- Each row is a module that must be reconciled for the listed role.
--
-- Remediation policies (decide with business before applying):
--   Policy A (grant access): insert the missing VIEW_LIST root.
--   Policy B (reduce access): delete the orphan child permissions.
-- Both policies are intentionally left commented so this script never mutates.

SELECT
    LOWER(CONCAT(
        SUBSTRING(HEX(r.id), 1, 8), '-',
        SUBSTRING(HEX(r.id), 9, 4), '-',
        SUBSTRING(HEX(r.id), 13, 4), '-',
        SUBSTRING(HEX(r.id), 17, 4), '-',
        SUBSTRING(HEX(r.id), 21, 12)
    )) AS role_id,
    r.name AS role_name,
    p.module AS module_without_root,
    GROUP_CONCAT(CONCAT(p.module, '_', p.action) ORDER BY p.action SEPARATOR ', ') AS orphan_permissions,
    COUNT(*) AS orphan_permission_count
FROM auth_role_permissions p
JOIN auth_roles r ON r.id = p.role_id
WHERE p.action <> 'VIEW_LIST'
  AND p.module IN (
      'SALES_QUOTES',
      'SALES_ORDERS',
      'PURCHASES',
      'CATALOG_PRODUCTS',
      'CATALOG_CATEGORIES',
      'CATALOG_PRICES',
      'INVENTORY_WAREHOUSES',
      'FINANCE_BILLING_INVOICES',
      'FINANCE_BILLING_RETURNS',
      'FINANCE_BILLING_CREDIT_NOTES',
      'FINANCE_BILLING_DEBIT_NOTES',
      'FINANCE_TREASURY_CASH_REGISTER',
      'FINANCE_TREASURY_RECEIVABLES',
      'FINANCE_TREASURY_PAYMENT_METHODS',
      'CONTACTS_CUSTOMERS',
      'CONTACTS_SUPPLIERS',
      'USERS',
      'ROLES',
      'DEPARTMENTS'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM auth_role_permissions root
      WHERE root.role_id = p.role_id
        AND root.module = p.module
        AND root.action = 'VIEW_LIST'
  )
GROUP BY r.id, r.name, p.module
ORDER BY r.name, p.module;

-- Policy A: grant the root to roles that keep their children.
-- INSERT INTO auth_role_permissions(role_id, module, action)
-- SELECT DISTINCT p.role_id, p.module, 'VIEW_LIST'
-- FROM auth_role_permissions p
-- JOIN auth_roles r ON r.id = p.role_id
-- WHERE p.action <> 'VIEW_LIST'
--   AND p.module IN ('SALES_QUOTES', 'SALES_ORDERS')
--   AND NOT EXISTS (
--       SELECT 1 FROM auth_role_permissions root
--       WHERE root.role_id = p.role_id
--         AND root.module = p.module
--         AND root.action = 'VIEW_LIST'
--   );

-- Policy B: remove the orphan children.
-- DELETE p FROM auth_role_permissions p
-- WHERE p.action <> 'VIEW_LIST'
--   AND p.module IN ('SALES_QUOTES', 'SALES_ORDERS')
--   AND NOT EXISTS (
--       SELECT 1 FROM (SELECT * FROM auth_role_permissions) root
--       WHERE root.role_id = p.role_id
--         AND root.module = p.module
--         AND root.action = 'VIEW_LIST'
--   );
