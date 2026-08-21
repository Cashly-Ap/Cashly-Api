# Payments

Payments record customer money, support invoice allocation, and expose approval workflows for portal payments.

## API

Base path: `/api/payments`.

| Method | Path | Purpose |
|---|---|---|
| POST | `/` | Register a payment |
| GET | `/` | List payments by date/customer/status |
| GET | `/{id}` | Read payment details |
| POST | `/{id}/approve` | Approve a pending payment |
| POST | `/{id}/reject` | Reject a pending payment |
| POST | `/invoices/{invoiceId}/pay` | Pay one invoice |
| POST | `/customers/{customerId}/pay-all` | Pay all customer debt |
| POST | `/customers/{customerId}/pay-partial` | Pay a partial amount |
| PUT/DELETE | `/{id}` | Update or remove an eligible payment |

## Rules

- Amounts must be positive and cannot exceed the applicable balance.
- Allocation is explicit and prevents overpayment.
- Portal-originated payments remain pending until validation.
- Approval/rejection and invoice allocation are transactional.

## Code map

Core logic lives in `payments/domain`; use cases/DTOs in `payments/application`; adapters in `payments/infrastructure`.
