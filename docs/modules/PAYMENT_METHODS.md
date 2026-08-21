# Payment Methods

Payment methods define the instruments available to record income and expenses.

## API

Base path: `/api/payment-methods`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Create a method |
| GET | `/` | List methods with pagination/filters |
| GET | `/{id}` | Read a method |
| PUT | `/{id}` | Update a method |
| PATCH | `/{id}/toggle` | Toggle active state |
| DELETE | `/{id}` | Delete an unused method |

## Rules

- Names are required and unique within the configured scope.
- Inactive methods remain valid for historical records but cannot be selected for new payments.
- Methods referenced by payments must not be physically removed.
