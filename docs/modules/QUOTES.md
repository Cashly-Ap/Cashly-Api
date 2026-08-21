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
| PUT | `/{id}/allocations` | Save warehouse allocations |
| POST | `/{id}/convert-to-order` | Convert quote to order |
| POST | `/{id}/convert-to-invoice` | Convert quote to invoice |

## Rules

- Customer and valid lines are required.
- Allocation must cover requested quantities before conversion.
- Conversion creates the target document and preserves source traceability.
