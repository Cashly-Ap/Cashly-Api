# Credit Notes

Credit notes reduce a customer invoice balance and record the reason for the adjustment.

## API

Base path: `/api/credit-notes`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a credit note |
| GET | `/` | List notes with filters |
| GET | `/{id}` | Read note details |
| DELETE | `/{id}` | Delete an eligible note |

## Rules

- The customer and invoice must exist and belong together.
- Amounts cannot exceed the refundable invoice balance.
- Applying a note updates the invoice/customer balance atomically.
- Authorization and validation are enforced at the controller boundary.

## Code map

- Domain: `credit_notes/domain`
- Use cases and DTOs: `credit_notes/application`
- HTTP and persistence: `credit_notes/infrastructure`
- Tests: `src/test/.../credit_notes`
