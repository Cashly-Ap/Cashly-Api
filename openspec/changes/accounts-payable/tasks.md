# Tasks

## Spec and design
- [ ] Write delta specs for supplier credit terms, payables balances, aging and supplier payments.
- [ ] Design the aggregate boundaries: `SupplierCreditTerms`, `Payable`, `SupplierPayment` and their events.
- [ ] Decide how a confirmed purchase generates a payable and its due date (`purchase date + payment term days`).
- [ ] Define the permission set for the module (at minimum `PURCHASES:READ`, `FINANCES:EXECUTE`, `FINANCES:CREATE`).

## Database
- [ ] New migration: supplier credit terms (credit limit, payment term days, currency-independent amounts).
- [ ] New migration: payables (purchase reference, supplier, amount, paid amount, due date, status).
- [ ] New migration: supplier payments and their allocations to payables.
- [ ] Indexes for supplier + due date and for due date + status (aging queries).
- [ ] Extend the deterministic seed with at least one supplier with overdue debt, one partially paid and one current.

## Backend
- [ ] Domain: credit terms value object with validation, payable aggregate with partial payments and status transitions.
- [ ] Application: use cases for registering a payable on purchase confirmation, paying a payable, paying all, and regeneration on purchase cancellation.
- [ ] Infrastructure: JPA entities, repositories, query adapters for balances and aging.
- [ ] Web: REST endpoints under `/api/v1/payables` and `/api/v1/suppliers/{id}/payables`, with validation and localized errors.
- [ ] Reporting: supplier statement PDF reusing the existing PDF patterns.
- [ ] Cash register: outgoing payment registers a movement in the open session and is blocked when no session is open.
- [ ] Architecture guards: place every class under the responsibility folders the guards enforce.

## Frontend
- [ ] `finance/treasury/payables` feature with list, filters by supplier/status/due date and aging summary cards.
- [ ] Supplier debt dialog mirroring `customer-debt-dialog`: credit terms, debt summary, invoices with status badges and pay/pay-all/partial actions.
- [ ] Supplier detail page: add credit limit and payment term fields to the edit form.
- [ ] Navbar entries under Finance → Treasury and the matching keyboard shortcuts.
- [ ] Reuse `app-text-badge`, `DocumentConfirmationDialog` and the shared table patterns; no `p-tag`.

## Verification
- [ ] Unit tests for credit terms, payable status transitions and aging buckets.
- [ ] Integration tests for the payables queries and the payment allocation.
- [ ] Frontend specs for the list, the debt dialog and the supplier form.
- [ ] Manual browser verification: create a purchase, confirm it, see the payable, pay it partially and fully, verify cash register and aging.
- [ ] Document the module in the module documentation once delivered.
