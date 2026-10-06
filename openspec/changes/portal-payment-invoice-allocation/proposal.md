# Proposal: Portal Payment Invoice Allocation

## Intent

Portal customer payments (pay one invoice, pay total debt, pay partial debt) are approved by staff today but the money never actually reduces invoice balances for the two debt flows, because invoice targeting is encoded as a fragile `"INVOICE_ID:"` prefix inside the free-text `notes` field, and the debt flows never write that marker at all. This is a real money bug: approved debt payments leave invoices exactly as unpaid as before.

Fix it by giving `Payment` a real structured `target_invoice_id` column, wiring the portal use cases to set it (or leave it `null` for multi-invoice debt payments), and rewriting `ApprovePaymentUseCase` to allocate off that column — reusing the existing oldest-first allocation logic instead of duplicating it a third time. Also add structured voucher/receipt file persistence (`voucher_path`) end to end, since the portal already silently drops the file customers attach, and admins currently have a dead, unpopulated `voucherPath` field in the receivables UI.

## Why

- **Money correctness**: approved portal debt payments currently have zero effect on invoice balances. This under-reports what a customer still owes.
- **Fragility**: string-prefix parsing of `notes` is a landmine — any future change to notes formatting silently reintroduces the bug for the one flow that currently works by accident (single-invoice portal payment).
- **Audit/UX**: staff approving a payment and customers checking their invoices can't see what file was attached as proof of payment, even though the portal already collects it on one of four payment flows.

## Scope

In scope:
- Flyway migration adding `target_invoice_id` (nullable FK to `invoices.id`) and `voucher_path` (nullable) to `payments`.
- `Payment` domain entity + `PaymentEntity` JPA entity + `PaymentJpaMapper`: add both fields.
- `CreatePaymentRequest` / `CreatePaymentUseCase`: accept and persist both fields (additive, nullable).
- `PayPortalInvoiceUseCase`: set `target_invoice_id` directly instead of stuffing `notes`.
- `PayPortalAllDebtUseCase` / `PayPortalPartialDebtUseCase`: explicitly leave `target_invoice_id = null`.
- `RegisterPortalPaymentUseCase`: store the voucher file path in `voucher_path` instead of concatenating it into `notes`.
- New shared oldest-first allocation algorithm (pure domain calculation) extracted out of `PayAllCustomerDebtUseCase` and `PayPartialDebtUseCase`, reused by both (refactor, behavior-preserving) and by the new `ApprovePaymentUseCase` debt-spread branch.
- `ApprovePaymentUseCase`: replace `notes` string parsing with `target_invoice_id` branching (single-invoice allocation vs. oldest-first spread across the customer's unpaid invoices).
- `PortalPaymentController`: convert the 3 JSON-only payment endpoints (`/invoices/{id}/pay`, `/debt/pay`, `/debt/pay-partial`) to multipart (JSON part + optional file part), matching the existing `/payments/register` pattern.
- `PaymentMapper` / `PaymentResponse` / `PaymentListResponse`: expose `voucherUrl` (derived from `voucher_path`), `targetInvoiceId`.
- New portal query endpoint to check for an applicable PENDING payment (per invoice, or general debt) so the portal frontend can show it.

Out of scope:
- Any change to `PayInvoiceUseCase`'s (admin, single-invoice, immediate) already-correct behavior beyond call-site adjustments needed for the shared algorithm extraction.
- Domain events (not requested).
- A UI for admins to re-open/re-run allocation on already-applied payments.
- Handling the edge case of a debt payment amount exceeding total outstanding debt with anything beyond "allocate what there's debt for, leave the remainder unapplied on the Payment" — no customer-credit-balance feature is being built here.

## Risks

- **Schema change on a money table**: mitigated by making both new columns nullable and additive; no backfill needed since old payments simply have `target_invoice_id = NULL` and continue being ignored by `ApprovePaymentUseCase`'s new debt-spread branch exactly as intended for historical general-debt payments (already applied, won't be re-approved).
- **Behavior change in `ApprovePaymentUseCase`**: single-invoice branch must be provably identical to today's `applyToInvoice` behavior (same allocation amount, same recalculate call) — covered by tests asserting parity.
- **Breaking API contract**: 3 portal endpoints move from `application/json` to `multipart/form-data`. Only `cashly-portal-web` consumes them (confirmed during Explore — `cashly-app` has entirely separate admin payment DTOs/endpoints), so both sides are fixed together in this change.
- **Refactor risk**: extracting the oldest-first algorithm out of `PayAllCustomerDebtUseCase`/`PayPartialDebtUseCase` touches already-tested, currently-correct code. Existing tests for both must keep passing unmodified in assertions (only mocks/setup may change if constructor signatures shift).

## Rollback

All schema changes are additive/nullable — a follow-up Flyway migration dropping the two columns is sufficient if needed. Code changes are a standard `git revert`; no data backfill or migration of historical `notes`-encoded `INVOICE_ID:` markers is required for rollback, since those payments are already either correctly applied (single-invoice flow) or already incorrectly unapplied (debt flows) independent of this change.

## Open design decisions made unilaterally (flagging for visibility)

1. **`voucherUrl` shape**: the codebase's existing convention (e.g. product photos) is "backend returns the raw relative stored path, frontend builds the full URL." The approved design explicitly asks for a field named `voucherUrl` "built on top of `GET /api/files/**`". Resolved as: backend returns `voucherUrl = "/api/files/" + voucherPath` (a usable relative URL) when a voucher exists, `null` otherwise — satisfies the literal ask without inventing an absolute-URL-building convention that doesn't exist elsewhere in the codebase.
2. **Pending-payment check endpoint shape**: added as `GET /api/portal/payments/pending-check?invoiceId={optional}`. With `invoiceId`: returns the customer's PENDING payments where `target_invoice_id = invoiceId` OR `target_invoice_id IS NULL` (a general-debt payment would also apply to this invoice once approved). Without `invoiceId`: returns the customer's PENDING payments where `target_invoice_id IS NULL` only (used by the "pay total debt" modal).
3. **Over-payment on approval of a debt-spread payment**: if the approved amount exceeds total outstanding debt at approval time, the shared algorithm allocates until debt is exhausted and `payment.apply()` is called with the actually-allocated total (which can be less than `payment.amount()`), leaving the remainder unapplied on the `Payment` record for manual admin follow-up. No new customer-credit feature is introduced to absorb the remainder.
4. **`cashly-app`'s dead `voucherPath` field**: the `PendingPayment` TypeScript interface currently declares `voucherPath`, but the admin `pending-payments-tab` actually consumes `PaymentListResponse` (via `GET /payments?status=PENDING`), which will expose `voucherUrl` (not `voucherPath`) per decision 1. The interface field is renamed `voucherUrl` to match reality — safe since it was unused dead code (only referenced in two test mocks).
