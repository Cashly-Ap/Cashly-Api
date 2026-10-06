# Tasks: Portal Payment Invoice Allocation (cashly-api)

TDD throughout: failing test first, minimal implementation, refactor. Run `./run-tests.sh` (never `mvn test` directly) from `cashly-api/`.

1. [ ] Flyway migration `V24__add_target_invoice_and_voucher_to_payments.sql` (confirm exact style/FK naming against `V14`/most recent migration before writing).
2. [ ] `Payment` domain entity: add `targetInvoiceId`/`voucherPath` fields, extend `create`/`reconstitute`, unit tests in `PaymentUnitTest` for both new fields (default null, set via create/reconstitute, getters).
3. [ ] `InvoiceRemainingBalance` value object + unit test.
4. [ ] `PaymentAllocationService.allocateOldestFirst(...)`: unit tests first — exact match, partial (amount < total debt), over-payment (amount > total debt), zero unpaid invoices, single invoice, skip-zero-remaining-invoice — then implement.
5. [ ] `PaymentEntity` + `PaymentJpaMapper`: add columns/fields, update `PaymentRepositoryAdapterTest` for round-trip of both new fields.
6. [ ] `CreatePaymentRequest` + `CreatePaymentUseCase`: thread new fields through, update `CreatePaymentUseCaseUnitTest`.
7. [ ] Refactor `PayAllCustomerDebtUseCase` and `PayPartialDebtUseCase` to call `allocateOldestFirst`; existing tests (`PayAllCustomerDebtUseCaseTest`, `PayPartialDebtUseCaseTest`) must pass with only wiring/mocks changed, assertions untouched — run them before and after the refactor to prove behavior parity.
8. [ ] `ApprovePaymentUseCase`: new tests first (targeted-invoice branch parity with today, oldest-first branch: exact/partial/over-payment/zero-invoices scenarios from spec.md), then implement, replacing the `notes` string-parsing branch entirely.
9. [ ] `PayPortalInvoiceUseCase`: tests first (sets `targetInvoiceId`, no `INVOICE_ID:` in notes, optional file → `voucherPath` set), then implement + accept `MultipartFile`.
10. [ ] `PayPortalAllDebtUseCase` / `PayPortalPartialDebtUseCase`: tests first (`targetInvoiceId` stays null, optional file → `voucherPath` set), then implement.
11. [ ] `RegisterPortalPaymentUseCase`: update test/implementation to set `voucherPath` directly, drop `"Justificante:"` notes concatenation.
12. [ ] `PaymentMapper`: tests first for `voucherUrl`/`targetInvoiceId` derivation (present/absent voucher), then implement. Update `PaymentMapperTest`.
13. [ ] New `GetPendingPortalPaymentsUseCase` (or equivalent): tests first per the 3 pending-check spec scenarios, then implement, including any new repository port method + adapter test.
14. [ ] `PortalPaymentController`: convert 3 endpoints to multipart, add `pending-check` endpoint. New/updated controller tests (Mockito-style, matching repo convention) for all 4 endpoints including file-present/file-absent cases and multipart content-type.
15. [ ] `PaymentConfig` (and `PortalConfig` if it's the one wiring portal use cases): update bean wiring for any new/changed constructor dependencies.
16. [ ] Full `./run-tests.sh` run — zero failures, review Jacoco coverage for touched packages against thresholds (domain > 90%, application > 80%, infrastructure > 60%).
17. [ ] Manual API verification (curl inside Docker) for at least: pay-invoice with file, pay-all-debt without file, approve a debt payment and confirm invoice balance actually drops, `pending-check` with and without `invoiceId`.
