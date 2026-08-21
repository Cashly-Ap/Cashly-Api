# Price Types

Price types classify customer-facing prices such as retail, wholesale, and distributor rates.

## API

Base path: `/api/price-types`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a price type |
| PUT | `/{id}` | Update a price type |
| DELETE | `/{id}` | Delete an eligible type |
| GET | `/{id}` | Read one type |
| GET | `/` | Paginated list |

## Rules

- Names are required and unique.
- Active state controls whether a type can be assigned to new prices.
- Existing product prices retain their historical type reference.
