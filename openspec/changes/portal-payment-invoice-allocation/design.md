# Design: Portal Payment Invoice Allocation

## Migration

`src/main/resources/db/migration/V24__add_target_invoice_and_voucher_to_payments.sql`:

```sql
ALTER TABLE payments
    ADD COLUMN target_invoice_id BINARY(16) NULL,
    ADD COLUMN voucher_path VARCHAR(500) NULL;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_target_invoice
    FOREIGN KEY (target_invoice_id) REFERENCES invoices(id);
```

(Exact column order/style to match `V14__create_payments_tables.sql` conventions; confirm FK naming style against existing migrations before finalizing.)

## Domain layer (`payments/domain`)

- `Payment`: add `targetInvoiceId: UUID` (nullable) and `voucherPath: String` (nullable) fields, getters, and threading through both `create(...)` overloads and `reconstitute(...)` (extend the 19-arg signature to 21 args; do not touch the deprecated 15-arg overload's behavior beyond defaulting the 2 new fields to `null`).
- New value object `payments/domain/valueobjects/InvoiceRemainingBalance.java`: `record InvoiceRemainingBalance(UUID invoiceId, BigDecimal remainingAmount)`.
- `PaymentAllocationService` (`payments/domain/services/PaymentAllocationService.java`): add a new **pure** method, no repository/I-O dependency:
  ```java
  public List<PaymentAllocation> allocateOldestFirst(
      UUID paymentId,
      List<InvoiceRemainingBalance> invoicesOldestFirst,
      BigDecimal amountToAllocate
  )
  ```
  Iterates `invoicesOldestFirst` in order, allocates `min(remainingAmountToDistribute, invoice.remainingAmount())` per invoice, skips invoices with zero remaining, stops once the amount is exhausted, returns the list of `PaymentAllocation` domain objects (not persisted — caller persists). This is the single source of truth for "spread an amount across invoices oldest-first," replacing the 3 duplicated inline loops (`PayAllCustomerDebtUseCase` cash+balance loops, `PayPartialDebtUseCase` cash+balance loops — 4 call sites collapse to 4 callers of 1 method).

## Application layer (`payments/application`)

- `CreatePaymentRequest`: add `targetInvoiceId: UUID` (nullable, no `@NotNull`) and `voucherPath: String` (nullable, `@Size(max = 500)`).
- `CreatePaymentUseCase`: pass both through to `Payment.create(...)`. `PaymentController.createPayment` (the one non-portal caller) passes `null` for both — no behavior change for admin direct-create.
- `PayAllCustomerDebtUseCase` / `PayPartialDebtUseCase`: refactor their allocation loops to build `List<InvoiceRemainingBalance>` from the already-fetched invoices (using the same `calculateTotal(credits, debits)` minus already-allocated logic they use today) and call `paymentAllocationService.allocateOldestFirst(...)`, then persist the returned allocations and call `invoiceStatusService.recalculate(...)` per distinct invoice touched, exactly as today. Assertions in `PayAllCustomerDebtUseCaseTest` / `PayPartialDebtUseCaseTest` must keep passing unmodified — only test wiring for the new service dependency changes.
- `ApprovePaymentUseCase`: replace the `notes.startsWith("INVOICE_ID:")` branch with:
  ```java
  if (payment.getTargetInvoiceId() != null) {
      applyToInvoice(payment, payment.getTargetInvoiceId()); // unchanged existing method
  } else {
      applyToUnpaidInvoicesOldestFirst(payment);
  }
  ```
  New private method `applyToUnpaidInvoicesOldestFirst`: fetch `invoiceRepository.findAllWithFilters(null, null, payment.getCustomerId(), List.of("PENDING_PAYMENT", "PARTIALLY_PAID"), null, null, null, null, null)` (identical call to `PayAllCustomerDebtUseCase`), compute each invoice's remaining balance (reuse the same credit/debit + already-allocated computation `PayAllCustomerDebtUseCase` uses — extract a small shared helper if the computation itself is non-trivially duplicated, otherwise inline is acceptable since it's a single call site here), build `List<InvoiceRemainingBalance>`, call `paymentAllocationService.allocateOldestFirst(payment.getId().value(), balances, payment.getAmount())`, persist each returned allocation, call `invoiceStatusService.recalculate(...)` per distinct invoice, sum the allocated total, and call `payment.apply(allocatedTotal)` (guarding `allocatedTotal > 0` since `Payment.apply` may reject a zero-amount call — verify `apply`'s validation and skip the call entirely when `allocatedTotal.compareTo(ZERO) == 0`, leaving `appliedAmount = 0`).
- `RegisterPortalPaymentUseCase`: stop concatenating the stored file path into `notes`; set `CreatePaymentRequest.voucherPath` instead. Keep `buildNotes` behavior for any non-file notes content, just drop the `"Justificante: <path>"` suffix logic.
- `PayPortalInvoiceUseCase`: stop prefixing `notes` with `"INVOICE_ID:"`; set `CreatePaymentRequest.targetInvoiceId = invoiceId` directly. Accept an optional `MultipartFile`, store it via `FileStorageService` (same `JUSTIFICANTE_PATH` convention as `RegisterPortalPaymentUseCase`) when present, set `voucherPath`.
- `PayPortalAllDebtUseCase` / `PayPortalPartialDebtUseCase`: same file-handling addition as above; `targetInvoiceId` stays `null` explicitly (pass `null`, don't omit, so the intent reads clearly at the call site).
- `PaymentMapper.toResponse` / `toListResponse`: add `voucherUrl = payment.getVoucherPath() == null ? null : "/api/files/" + payment.getVoucherPath()` and `targetInvoiceId = payment.getTargetInvoiceId()`.
- New use case `GetPendingPortalPaymentsUseCase` (or extend `ListPortalPaymentsUseCase` — prefer a new small use case to avoid overloading the general list endpoint's existing filter semantics): `execute(UUID customerId, UUID invoiceIdOrNull)`. Query: `paymentRepository` needs a method to fetch `PENDING` payments for a customer optionally filtered by `(targetInvoiceId = X OR targetInvoiceId IS NULL)` vs `(targetInvoiceId IS NULL)` — add a repository port method, e.g. `findPendingByCustomerAndTargetInvoice(UUID customerId, UUID invoiceIdOrNull, boolean includeGeneralDebt)`, or simpler: fetch all `PENDING` payments for the customer (already-existing capability via `findAllWithFilters`-style method if one exists, otherwise add one) and filter in the use case (small in-memory filter over an already customer-scoped, status-scoped, typically-small result set — pragmatic given YAGNI, avoid a bespoke SQL predicate for a low-cardinality case).

## Infrastructure layer (`payments/infrastructure`, `portal/infrastructure`)

- `PaymentEntity`: add `targetInvoiceId` (`BINARY(16)` via `@JdbcTypeCode(SqlTypes.BINARY)`, nullable) and `voucherPath` (`@Column(length = 500)`, nullable) fields with Lombok getters/setters (consistent with existing style).
- `PaymentJpaMapper`: thread both fields through `toEntity`/`toDomain`.
- `PortalPaymentController`: convert `payInvoice`, `payAllDebt`, `payPartialDebt` from `@RequestBody` to the multipart pattern already used by `registerPayment`:
  ```java
  @PostMapping(value = "/invoices/{id}/pay", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PaymentResponse> payInvoice(
      @PathVariable UUID id,
      @Valid @RequestPart("request") PayPortalInvoiceRequest request,
      @RequestPart(value = "justificante", required = false) MultipartFile justificante
  )
  ```
  Same shape for the other two. Add new endpoint:
  ```java
  @GetMapping("/payments/pending-check")
  public ResponseEntity<List<PaymentListResponse>> pendingCheck(
      @RequestParam(required = false) UUID invoiceId
  )
  ```

## Frontend contract impact (for the sibling `parsun-app`/`parsun-portal-web` changes)

- `PaymentResponse` / `PaymentListResponse` gain `voucherUrl: string | null` and `targetInvoiceId: string | null` — additive, no existing consumer breaks.
- The 3 portal payment endpoints now require `multipart/form-data` with a `request` part (JSON, `Content-Type: application/json` within the part) and an optional `justificante` file part — this is a breaking wire-format change for `parsun-portal-web` only.
- New `GET /api/portal/payments/pending-check?invoiceId={optional}` endpoint for `parsun-portal-web`'s payment dialogs.

## Alternatives considered

- **Storing a "pending allocation plan" at payment-creation time** instead of recalculating at approval time: rejected per explicit user decision — balances can shift between request and approval (another payment could be approved first), so recalculating fresh at approval is correct and simpler (no plan invalidation logic needed).
- **Making the oldest-first extraction a static utility method** instead of a `PaymentAllocationService` instance method: rejected — the service already exists as the natural home for payment-allocation domain logic and is already constructor-injected where needed; adding a static method would fragment the pattern for no benefit.
- **Bypassing `CreatePaymentUseCase` for portal flows and constructing `Payment` directly**: rejected — `RegisterPortalPaymentUseCase` already goes through `CreatePaymentUseCase`, and it's the only creation path with cash-movement/notification side effects wired in; duplicating that orchestration for 3 more call sites would be a regression.
