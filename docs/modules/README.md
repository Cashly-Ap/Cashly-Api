# Module documentation

This directory contains module-level references for the Cashly API. Source ownership and package boundaries are defined in [ARCHITECTURE.md](../ARCHITECTURE.md); this index only links each module to its guide.

## Current modules

| Area | Module | Source root | Guide |
| --- | --- | --- | --- |
| Platform | Authentication, tokens and MFA | `platform/auth/{authentication,token,mfa}` | [AUTH.md](AUTH.md) |
| Platform | Users, roles, departments, permissions | `platform/auth/{user,role,department,authorization}` | [ACCESS_CONTROL.md](ACCESS_CONTROL.md) |
| Platform | Company settings and branding | `platform/company` | — |
| Platform | Notifications | `platform/notifications` | [NOTIFICATIONS.md](NOTIFICATIONS.md) |
| Partners | Customers, balances, credit and warehouse access | `partners/customers` | [CUSTOMERS.md](CUSTOMERS.md) |
| Partners | Suppliers | `partners/suppliers` | [SUPPLIERS.md](SUPPLIERS.md) |
| Catalog | Products and product reporting | `catalog/products` | [PRODUCTS.md](PRODUCTS.md) |
| Catalog | Categories | `catalog/categories` | [CATEGORY.md](CATEGORY.md) |
| Catalog | Price types | `catalog/price_types` | [PRICE_TYPES.md](PRICE_TYPES.md) |
| Inventory | Warehouses, stock and movements | `inventory/warehouses` | [WAREHOUSE.md](WAREHOUSE.md) |
| Sales | Quotes | `sales/quotes` | [QUOTES.md](QUOTES.md) |
| Sales | Orders | `sales/orders` | [ORDERS.md](ORDERS.md) |
| Procurement | Purchases | `procurement/purchases` | [PURCHASES.md](PURCHASES.md) |
| Finance billing | Invoices | `finance/billing/invoices` | [INVOICES.md](INVOICES.md) |
| Finance billing | Returns | `finance/billing/returns` | [RETURNS.md](RETURNS.md) |
| Finance billing | Credit notes | `finance/billing/credit_notes` | [CREDIT_NOTES.md](CREDIT_NOTES.md) |
| Finance billing | Debit notes | `finance/billing/debit_notes` | [DEBIT_NOTES.md](DEBIT_NOTES.md) |
| Finance treasury | Cash register | `finance/treasury/cash_register` | [CASH_REGISTER.md](CASH_REGISTER.md) |
| Finance treasury | Payments and collections | `finance/treasury/payments` | [PAYMENTS.md](PAYMENTS.md), [COLLECTIONS.md](COLLECTIONS.md) |
| Finance treasury | Receivables | `finance/treasury/receivables` | [RECEIVABLES.md](RECEIVABLES.md) |
| Finance treasury | Payables | `finance/treasury/payables` | [PAYABLES.md](PAYABLES.md) |
| Finance treasury | Payment methods | `finance/treasury/payment_methods` | [PAYMENT_METHODS.md](PAYMENT_METHODS.md) |
| Analytics | Dashboard | `analytics/dashboard` | [DASHBOARD.md](DASHBOARD.md) |
| Reporting | Customer reports | `partners/customers` reporting | [REPORTS_CUSTOMERS.md](REPORTS_CUSTOMERS.md) |
| Reporting | Product reports | `catalog/products/reporting` | [REPORTS_PRODUCTS.md](REPORTS_PRODUCTS.md) |
| Reporting | Sales reports | `sales/*/reporting` | [REPORTS_SALES.md](REPORTS_SALES.md) |

## Documentation rules

- Use the module's real package root from `src/main/java`; do not copy paths from older Lexand-era guides.
- Authorization is documented with permission codes, not `MODULE:ACTION` strings. See [ACCESS_CONTROL.md](ACCESS_CONTROL.md).
- Endpoints require an explicit `@PreAuthorize` code. Unmapped handlers fail closed.
- Database sections must reference the baseline migrations under `src/main/resources/db/migration`.
- Use [TEMPLATE.md](TEMPLATE.md) for new module guides.

Historical migration plans and completed changes live under `openspec/changes/archive`; they are not current implementation guidance.
