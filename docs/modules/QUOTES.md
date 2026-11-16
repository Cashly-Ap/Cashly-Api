# Quotes

Quotes capture proposed sales, warehouse allocations, and conversions into orders or invoices.

## API

Base path: `/api/quotes`.

| Method | Path | Purpose |
|---|---|---|
| POST/GET | `/` | Create and list quotes |
| GET/PUT/DELETE | `/{id}` | Read, update, or delete a quote |
| PUT | `/{id}/cancel` | Cancel a quote |
| GET | `/warehouses` | Get allocation candidates |
| POST | `/{id}/convert-to-order` | Convert quote to order |
| POST | `/{id}/convert-to-invoice` | Convert quote to invoice |

## Rules

- Customer and valid lines are required.
- Allocation must cover requested quantities before conversion.
- Conversion creates the target document and preserves source traceability.

Standalone warehouse reassignment has been removed. Warehouse allocations are supplied by document creation/editing and are reused by conversion; no separate assignment modal or allocations endpoint is required.

Conversion persistence preserves existing historical-category snapshot identities. Cash conversion must persist the new invoice before payment allocations reference it; invoice and financial/inventory effects share one transaction and roll back together on failure.
