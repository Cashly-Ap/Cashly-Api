# Suppliers

Suppliers are the purchasing-side business partners used by the purchases module.

## API

Base path: `/api/suppliers`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create supplier |
| GET | `/` | Search and paginate suppliers |
| GET | `/{id}` | Read supplier |
| PUT | `/{id}` | Update supplier |
| DELETE | `/{id}` | Delete an eligible supplier |

## Rules

- Supplier identity and tax/contact data are validated at input boundaries.
- Suppliers referenced by purchases remain part of historical records.
