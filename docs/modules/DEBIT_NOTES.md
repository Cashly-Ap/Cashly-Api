# Debit Notes

Debit notes increase a customer balance when an additional charge must be recorded.

## API

Base path: `/api/debit-notes`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a debit note |
| GET | `/` | List notes with filters |
| GET | `/{id}` | Read note details |
| DELETE | `/{id}` | Delete an eligible note |

## Rules

- Notes reference a valid customer and supporting document.
- Positive amounts and a reason are required.
- Balance changes and note creation run in one transaction.
- Domain state is framework-independent; adapters own HTTP and persistence.

## Code map

`debit_notes/domain`, `debit_notes/application`, and `debit_notes/infrastructure` contain the module layers.
