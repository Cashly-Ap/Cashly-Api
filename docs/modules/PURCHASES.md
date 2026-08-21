# Purchases

Purchases register supplier acquisitions and the inventory movements generated when they are confirmed.

## API

Base path: `/api/purchases`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a purchase draft |
| GET | `/` | List purchases |
| GET | `/products` | List products available for purchase lines |
| GET | `/{id}` | Read purchase details |
| PUT | `/{id}` | Update a draft |
| PUT | `/{id}/confirm` | Confirm and apply stock |
| PUT | `/{id}/cancel` | Cancel an eligible purchase |
| DELETE | `/{id}` | Delete an eligible draft |

## Rules

- Supplier and line items are required.
- Confirmation is the inventory boundary; it is not reversible through editing.
- Cancellation restores/records the corresponding stock effect according to purchase state.
