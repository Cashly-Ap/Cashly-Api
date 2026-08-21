# Customer Portal

The portal is the customer-facing adapter for authentication, catalog, orders, quotes, invoices, payments, and notifications.

## API areas

| Area | Base path |
|---|---|
| Auth | `/api/portal/auth` |
| Dashboard | `/api/portal/dashboard` |
| Profile | `/api/portal/profile` |
| Products | `/api/portal/products` |
| Orders | `/api/portal/orders` |
| Quotes | `/api/portal/quotes` |
| Invoices | `/api/portal/invoices` |
| Payments | `/api/portal/payments` and `/api/portal/invoices/{id}/pay` |
| Notifications | `/api/portal/notifications` |

## Rules

- Portal identity is customer-scoped and uses its own authentication endpoints.
- The initial portal password is the normalized customer RIF when access is enabled for the first time.
- Portal login resolves only active customers with portal access, so inactive historical records with the same RIF cannot block authentication.
- Portal reads are restricted to the authenticated customer.
- Payments may include a multipart proof file and require back-office validation.
- Portal adapters reuse domain/application use cases without exposing internal admin endpoints.
