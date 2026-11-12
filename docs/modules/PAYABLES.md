# Payables (Cuentas por Pagar)

Accounts Payable is the mirror of Receivables applied to procurement. It exposes
supplier debt over confirmed purchases, supplier payments, supplier balances
(credit in favour of the company) and an accounts-payable PDF report.

> Source root: `src/main/java/com/cashly/cashly_api/finance/treasury/payables`
> and `.../finance/treasury/payments/payable`.
>
> This module reuses the existing `suppliers`, `purchases`, `payment_methods`,
> `cash_register_movements` and the shared `PdfBranding` infrastructure.

## Mirror mapping (Receivables -> Payables)

| Receivables | Payables |
| --- | --- |
| `Customer` | `Supplier` |
| `Invoice` | `Purchase` |
| `invoices.due_date` | `purchases.due_date` |
| `payments` (collections) | `supplier_payments` |
| `payment_allocations` | `supplier_payment_allocations` |
| `customer_balances` | `supplier_balances` |
| `credit_notes` / `debit_notes` | `purchase_credit_notes` / `purchase_debit_notes` |
| `returns` | `purchase_returns` |
| `FINANCE_TREASURY_RECEIVABLES_*` | `FINANCE_TREASURY_PAYABLES_*` |
| `/api/receivables` | `/api/payables` |
| `/api/reports/finance/accounts-receivable/pdf` | `/api/reports/finance/accounts-payable/pdf` |

## Domain changes

### Supplier (`V19__create_suppliers.sql`)

Added, mirroring `customers`:

| Column | Type | Purpose |
| --- | --- | --- |
| `credit_days` | `INT NOT NULL DEFAULT 0` | Days used to compute a purchase due date |
| `credit_limit` | `DECIMAL(15,2) NOT NULL DEFAULT 0` | Maximum supplier debt |

`CreateSupplierRequest` and `UpdateSupplierRequest` accept `creditDays` and
`creditLimit` (both optional; they default to `0`). The list/detail/create
responses expose them.

### Purchase (`V20__create_purchases.sql`)

Added, mirroring `invoices`:

| Column | Type | Purpose |
| --- | --- | --- |
| `payment_type` | `VARCHAR(20)` | `CASH` or `CREDIT` |
| `issue_date` | `DATE NOT NULL` | Issue date |
| `due_date` | `DATE NOT NULL` | `issue_date + supplier.credit_days` |
| `returned_total` | `DECIMAL(15,2) NOT NULL DEFAULT 0` | Returned amount |
| `return_status` | `VARCHAR(20)` | `PARTIAL_RETURN` / `FULLY_RETURNED` |

`PurchaseStatus` now supports:
`DRAFT, CONFIRMED, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED, PARTIAL_RETURN,
FULLY_RETURNED`.

`CreatePurchaseRequest` accepts `paymentType`, `issueDate` and `dueDate`.
`UpdatePurchaseRequest` also accepts them while the purchase is `DRAFT`; when
`dueDate` is omitted the API computes `issueDate + supplier.creditDays`.
On confirmation a `CASH` purchase becomes `PAID`; a `CREDIT` purchase remains
`CONFIRMED` (pending payment).

`PurchaseDetailResponse` exposes `paymentType`, `issueDate`, `dueDate`,
`returnedTotal`, `totalPaid` and `balance`. `PurchaseListResponse` exposes
`paymentType`, `issueDate` and `dueDate`.

### Purchase confirmation with credit validation

`PUT /api/purchases/{id}/confirm` accepts an optional body:

```json
{ "confirmationPassword": "string" }
```

For a `CREDIT` purchase whose subtotal exceeds `supplier.credit_limit` minus the
supplier's open debt, the API requires the authenticated user's password:

| Case | Response |
| --- | --- |
| Within credit limit | Confirms |
| Exceeds limit, no password | `400` `Se requiere una contraseña para confirmar compras que superen el límite de crédito` |
| Exceeds limit, wrong password | `400` `Contraseña incorrecta` |
| Exceeds limit, valid password | Confirms (override) |

`CASH` purchases ignore the password.

## New tables

The baseline is consolidated by domain. Payables tables live in:

| Migration | Tables |
| --- | --- |
| `V19__create_suppliers.sql` | `suppliers`, `supplier_emails`, `supplier_phones`, `supplier_addresses`, `supplier_balances`, `supplier_balance_tx_sequence`, `supplier_balance_transactions` |
| `V20__create_purchases.sql` | `purchases`, `purchase_lines`, `purchase_line_allocations`, `purchase_number_sequence`, `supplier_payments`, `supplier_payment_allocations`, `supplier_payment_number_sequence`, `purchase_returns`, `purchase_return_lines`, `purchase_return_number_sequence`, `purchase_credit_notes`, `purchase_credit_note_number_sequence`, `purchase_debit_notes`, `purchase_debit_note_number_sequence` |

> The returns and purchase credit/debit note tables are created by the migration
> baseline. Their HTTP use cases are the next increment; the financial core
> (debt, payments, balances) is fully implemented.

## Debt formula

```
purchase_balance = subtotal
                 - COALESCE(returned_total, 0)
                 - COALESCE(SUM(supplier_payment_allocations.allocated_amount), 0)
```

Only purchases with status not in `DRAFT, CANCELLED, PAID, FULLY_RETURNED` and
`balance > 0` are considered debt. Aging uses `DATEDIFF(CURRENT_DATE, due_date)`.

## Cash register integration

Supplier payments always produce an `OUT` / `EXPENSE` movement in
`cash_register_movements` with source type `SUPPLIER_PAYMENT` and source id equal
to the supplier payment id. `EXPENSE` is used because the cash register only
accepts `OUT` for `REFUND`, `CREDIT_NOTE`, `RETURN`, `EXPENSE` and `WITHDRAWAL`;
`PAYMENT` is reserved for incoming collections.

The movement is attached to the cash register whose `closure_date` matches the
payment date. If no register exists for that date, the currently open register
is used. This mirrors the requested behaviour: *the cash register is selected by
the payment date*.

## Permissions

Module: `FINANCE_TREASURY_PAYABLES`.

| Code | Use |
| --- | --- |
| `FINANCE_TREASURY_PAYABLES_VIEW_LIST` | Summary and supplier list |
| `FINANCE_TREASURY_PAYABLES_VIEW_DEBT` | Supplier detail, purchases, payment history, list payments |
| `FINANCE_TREASURY_PAYABLES_REPORT_PDF` | Accounts payable PDF |
| `FINANCE_TREASURY_PAYABLES_PAY_ALL` | Pay all supplier debt |
| `FINANCE_TREASURY_PAYABLES_PARTIAL_PAYMENT` | Partial payment |
| `FINANCE_TREASURY_PAYABLES_PAY_DIRECT` | Pay a specific purchase |
| `FINANCE_TREASURY_PAYABLES_PAY_WITH_VALIDATION` | Register a pending payment |
| `FINANCE_TREASURY_PAYABLES_APPROVE_PAYMENT` | Approve a pending payment |
| `FINANCE_TREASURY_PAYABLES_REJECT_PAYMENT` | Reject a pending payment |
| `FINANCE_TREASURY_PAYABLES_REFUND` | Refund / supplier balance |
| `FINANCE_TREASURY_PAYABLES_ADVANCE` | Advance / supplier balance |
| `FINANCE_TREASURY_PAYABLES_EDIT` | Reserved |
| `FINANCE_TREASURY_PAYABLES_DELETE` | Delete a non-applied payment |

The `OPERATOR` role seed grants `VIEW_LIST, VIEW_DEBT, PAY_ALL,
PARTIAL_PAYMENT, PAY_DIRECT, PAY_WITH_VALIDATION`.

## REST endpoints

### Payables (read)

| Method | Path | Permission | Response |
| --- | --- | --- | --- |
| GET | `/api/payables/summary` | `..._VIEW_LIST` | `PayablesSummaryResponse` |
| GET | `/api/payables/suppliers?search&page&size&sort&action` | `..._VIEW_LIST` | `PageResponse<SupplierDebtListResponse>` |
| GET | `/api/payables/suppliers/{id}` | `..._VIEW_DEBT` | `SupplierDebtSummaryResponse` |
| GET | `/api/payables/suppliers/{id}/purchases?search&status&page&size&includeNonDebt` | `..._VIEW_DEBT` | `PageResponse<SupplierPurchaseDebtResponse>` |
| GET | `/api/payables/payments/history?search&page&size` | `..._VIEW_DEBT` | `PageResponse<PayablePaymentHistoryResponse>` |
| GET | `/api/payables/suppliers/{id}/payment-history?purchaseId&page&size` | `..._VIEW_DEBT` | `PageResponse<PayablePaymentHistoryResponse>` |

`action` accepts `PAY_ALL`, `PAY_PARTIAL`, `REFUND`.
By default `/purchases` excludes `DRAFT`, `CANCELLED`, `PAID`,
`FULLY_RETURNED` and zero-balance purchases; `includeNonDebt=true` returns
them. This keeps the sum of `balance` equal to the supplier `totalDebt`.

### Supplier balance

| Method | Path | Permission |
| --- | --- | --- |
| GET | `/api/suppliers/{id}/balance` | `..._VIEW_DEBT` |
| GET | `/api/suppliers/{id}/balance/transactions?page&size` | `..._VIEW_DEBT` |
| POST | `/api/suppliers/{id}/balance/advance` | `..._ADVANCE` |
| POST | `/api/suppliers/{id}/balance/refund` | `..._REFUND` |

`advance` and `refund` accept `{ "amount", "paymentMethodId", "notes" }` and
return `SupplierBalanceResponse` with status `201`. An advance adds to
`supplier_balances` and records an `OUT / EXPENSE` cash movement with source
`SUPPLIER_ADVANCE`; a refund subtracts (rejecting amounts above the balance) and
records an `IN / PAYMENT` movement with source `SUPPLIER_REFUND`.

### Supplier payments (write)

| Method | Path | Permission |
| --- | --- | --- |
| POST | `/api/supplier-payments` | `..._PAY_WITH_VALIDATION` |
| POST | `/api/supplier-payments/purchases/{purchaseId}/pay` | `..._PAY_DIRECT` |
| POST | `/api/supplier-payments/suppliers/{supplierId}/pay-all` | `..._PAY_ALL` |
| POST | `/api/supplier-payments/suppliers/{supplierId}/pay-partial` | `..._PARTIAL_PAYMENT` |
| POST | `/api/supplier-payments/{id}/approve` | `..._APPROVE_PAYMENT` |
| POST | `/api/supplier-payments/{id}/reject` | `..._REJECT_PAYMENT` |
| GET | `/api/supplier-payments?dateFrom&dateTo&supplierId&status` | `..._VIEW_DEBT` |
| DELETE | `/api/supplier-payments/{id}` | `..._DELETE` |

### Reports

| Method | Path | Permission |
| --- | --- | --- |
| GET | `/api/reports/finance/accounts-payable/pdf?supplierId&overdueOnly` | `..._REPORT_PDF` |

## Main DTOs

```java
record PayablesSummaryResponse(BigDecimal totalDebt,
        long totalSuppliersWithDebt, long totalPurchasesWithDebt,
        BigDecimal totalPendingValidationAmount,
        BigDecimal totalSupplierBalance, LocalDateTime calculatedAt);

record SupplierDebtListResponse(UUID supplierId, String supplierName,
        String supplierRif, BigDecimal totalDebt,
        BigDecimal pendingPaymentsAmount, long pendingPurchasesCount,
        LocalDate oldestDueDate, int creditDays, BigDecimal creditLimit,
        BigDecimal availableCredit, BigDecimal supplierBalance);

record SupplierPurchaseDebtResponse(UUID purchaseId, String purchaseNumber,
        String status, String paymentType, LocalDate issueDate,
        LocalDate dueDate, BigDecimal subtotal, BigDecimal returnedTotal,
        BigDecimal totalPaid, BigDecimal total, BigDecimal balance,
        UUID pendingPaymentId, String pendingPaymentStatus);

record SupplierPaymentResponse(UUID id, String paymentNumber, UUID supplierId,
        BigDecimal amount, BigDecimal appliedAmount, UUID paymentMethodId,
        String source, String referenceNumber, LocalDate paymentDate,
        String notes, String status, LocalDateTime createdAt);

record PayablePaymentHistoryResponse(UUID id, String paymentNumber,
        UUID supplierId, String supplierName, BigDecimal amount,
        BigDecimal appliedAmount, String paymentMethodName, String status,
        String source, String referenceNumber, LocalDate paymentDate,
        String notes, LocalDateTime createdAt, String createdByName,
        String purchaseNumber);
```

## Package layout

```
finance/treasury/payables/
  application/{summary,customer_debt,supplier_purchases,payment_history}/
  application/dto/response/
  application/ports/out/PayablesQueryGateway.java
  infrastructure/persistence/adapters/PayablesRepositoryAdapter.java
  infrastructure/web/PayablesController.java
  infrastructure/config/PayablesConfig.java
  reporting/{application,infrastructure}/{use_case,pdf,web,config}

finance/treasury/payments/payable/
  domain/{entity,repository,valueobjects}/
  application/{create,pay_purchase,pay_supplier_debt,approve,reject,delete,list}/use_case/
  application/services/SupplierPaymentSettlementService.java
  application/ports/out/{SupplierPaymentCashRegisterGateway,SupplierBalanceGateway}.java
  infrastructure/persistence/{entities,repositories,mappers,adapters}/
  infrastructure/integration/{LocalSupplierPaymentCashRegisterGateway,LocalSupplierBalanceGateway}.java
  infrastructure/web/SupplierPaymentController.java
  infrastructure/config/SupplierPaymentConfig.java
```

## Seed

`V21SuppliersSeed` now sets `creditDays = 30` and `creditLimit = 1000.00`.
`V22PurchasesSeed` sets `paymentType = CREDIT`, `issueDate`, `dueDate` and
`returnedTotal = 0`.
`V31SupplierPaymentsSeed` creates two applied supplier payments with their
allocations and a supplier balance of `5.00` for `supplier.one`.

Reset the disposable database after editing baseline migrations:

```bash
docker compose stop app
docker exec cashly-api-mysql mysql -uroot -pverysecret \
  -e "DROP DATABASE IF EXISTS mydatabase; CREATE DATABASE mydatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
CASHLY_SEED_ENABLED=true docker compose up -d --build --force-recreate --no-deps app
```

## Verification

```bash
docker compose exec app ./mvnw test
curl http://localhost:8080/actuator/health
```

Verified locally after a full database reset:

- Flyway applied 25 consolidated migrations.
- Seed completed 31 steps.
- `GET /api/payables/summary` returned `totalDebt`, supplier count and balances.
- `GET /api/payables/suppliers` returned paginated supplier debt.
- `GET /api/payables/suppliers/{id}` returned the supplier detail with purchases.
- `GET /api/payables/payments/history` returned seeded supplier payments.
- `GET /api/reports/finance/accounts-payable/pdf` returned a valid 1-page PDF.
