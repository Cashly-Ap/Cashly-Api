# Returns

Returns register merchandise returned by a customer and the stock/financial effects of the return.

## API

Base path: `/api/returns`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a return |
| GET | `/` | List returns |
| GET | `/{id}` | Read return details |
| DELETE | `/{id}` | Delete an eligible return |

## Rules

- A return references a customer and source invoice/order where required.
- Quantities cannot exceed the returned/eligible quantity.
- Stock and financial adjustments are applied atomically.
