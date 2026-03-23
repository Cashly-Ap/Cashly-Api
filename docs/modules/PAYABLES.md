# PAYABLES Module

## Overview
The PAYABLES module manages **commission payments owed to merchants** (vendedores). When a collection is registered and a Collection record is created, a corresponding payable is automatically generated representing the commission that the company owes to the merchant.

**Key Concepts**:
- **Merchant-Centric**: Primary view is by merchant, showing total owed and individual commissions
- **Auto-Generated**: Payables are created automatically when Collections are created
- **Partial Payments**: Payments can be applied in increments — a payable moves through PENDING → PARTIALLY_PAID → PAID
- **Admin-Only Payment Registration**: Only USER (admin) role can register payments; merchants have read-only access
- **Role-Based Access**: Merchants can only view their own payables

---

## Business Flow

```
                         PAYABLES WORKFLOW

┌─────────────────────────────────────────────────────────────────┐
│                        COLLECTION CREATED                        │
│                              │                                   │
│                              │ Commission calculated             │
│                              ▼                                   │
│                   ┌──────────────────┐                          │
│                   │    PAYABLE       │                          │
│                   │    CREATED       │                          │
│                   │   (PENDING)      │                          │
│                   └────────┬─────────┘                          │
│                            │                                     │
│                            │ ADMIN registers payment             │
│                            │ POST /api/payables/{id}/pay         │
│                            ▼                                     │
│             ┌──────────────────────────────┐                    │
│             │  amountPaid < commissionAmount│                    │
│             └──────────────┬───────────────┘                    │
│                            │                                     │
│                 ┌──────────┴──────────┐                         │
│                 │                     │                          │
│                 ▼                     ▼                          │
│      ┌──────────────────┐   ┌──────────────────┐               │
│      │  PARTIALLY_PAID  │   │      PAID         │               │
│      │  (more payments  │   │  (amountPaid >=   │               │
│      │   can follow)    │   │  commissionAmount)│               │
│      └────────┬─────────┘   └──────────────────┘               │
│               │                                                  │
│               │ ADMIN registers additional payment               │
│               ▼                                                  │
│      ┌──────────────────┐                                       │
│      │      PAID        │ (when balance reaches zero)           │
│      └──────────────────┘                                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## Payable Status Lifecycle

```
                      PAYABLE STATUS TRANSITIONS

    ┌─────────────┐
    │   PENDING   │  Commission owed, no payment registered yet
    └──────┬──────┘
           │
           │ registerPayment() — partial amount
           ▼
    ┌──────────────────┐
    │  PARTIALLY_PAID  │  Some amount paid; remainingBalance > 0
    └────────┬─────────┘
             │
             │ registerPayment() — remaining balance
             ▼
    ┌─────────────────┐
    │      PAID       │  commissionAmount fully covered; terminal state
    └─────────────────┘

    Rules:
    - PENDING → PARTIALLY_PAID: payment amount < remainingBalance
    - PENDING → PAID:           payment amount == commissionAmount
    - PARTIALLY_PAID → PAID:    payment amount == remainingBalance
    - PAID: no further payments allowed (409 PayableAlreadyPaidException)
```

---

## Module Structure

```
payables/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   ├── RegisterPaymentRequest.java
│   │   │   ├── PayAllRequest.java
│   │   │   └── PayPartialRequest.java
│   │   └── response/
│   │       ├── GlobalPayablesSummaryResponse.java
│   │       ├── MerchantPayablesSummaryResponse.java
│   │       ├── MerchantPayablesListResponse.java
│   │       ├── PayableDetailResponse.java
│   │       ├── PayableListResponse.java
│   │       ├── PayablePaymentResponse.java
│   │       ├── BulkPaymentResultResponse.java
│   │       └── PaymentResultResponse.java
│   ├── exceptions/
│   │   ├── PayableNotFoundException.java
│   │   ├── PayableAlreadyPaidException.java
│   │   └── MerchantHasNoPayablesException.java
│   ├── mappers/
│   │   └── PayablesMapper.java
│   └── usecases/
│       ├── CreatePayableUseCase.java
│       ├── GetGlobalPayablesSummaryUseCase.java
│       ├── SearchMerchantsWithPayablesUseCase.java
│       ├── GetMerchantPayablesSummaryUseCase.java
│       ├── ListMerchantPayablesUseCase.java
│       ├── GetPayableUseCase.java
│       ├── RegisterPaymentUseCase.java
│       ├── ListPayablePaymentsUseCase.java
│       ├── PayAllMerchantPayablesUseCase.java
│       └── PayPartialMerchantPayablesUseCase.java
├── domain/
│   ├── entities/
│   │   ├── Payable.java
│   │   └── PayablePayment.java
│   ├── repositories/
│   │   ├── PayableRepository.java
│   │   └── PayablePaymentRepository.java
│   └── valueobjects/
│       ├── PayableId.java
│       ├── PayablePaymentId.java
│       ├── PayableStatus.java
│       └── SettlementMethod.java
└── infrastructure/
    ├── config/
    │   └── PayablesBeanConfig.java
    ├── persistence/
    │   ├── entities/
    │   │   ├── PayableJpaEntity.java
    │   │   └── PayablePaymentJpaEntity.java
    │   ├── mappers/
    │   │   ├── PayableJpaMapper.java
    │   │   └── PayablePaymentJpaMapper.java
    │   └── repositories/
    │       ├── JpaPayableRepositoryAdapter.java
    │       ├── JpaPayablesQueryRepository.java
    │       ├── JpaPayablePaymentRepositoryAdapter.java
    │       ├── PayablesQueryRepository.java
    │       ├── SpringDataPayableRepository.java
    │       └── SpringDataPayablePaymentRepository.java
    └── web/
        ├── PayablesController.java
        └── PayablesExceptionHandler.java
```

---

## Domain Model

### Entities

#### Payable (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.payables.domain.entities.Payable`

**Description**: Represents a commission owed to a merchant. Created automatically when a Collection is created. Tracks partial and full payment of the commission by an administrator.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `PayableId` (UUID) | Unique identifier | Required, immutable |
| `collectionId` | `UUID` | Source collection reference | Required, immutable, unique |
| `merchantId` | `UUID` | Merchant to be paid | Required, immutable |
| `merchantName` | `String` | Merchant name snapshot | Required, immutable |
| `merchantCode` | `Long` | Merchant code snapshot | Required, immutable |
| `invoiceId` | `UUID` | Related invoice reference | Required, immutable |
| `invoiceNumber` | `String` | Invoice number snapshot | Required, immutable |
| `invoiceDueDate` | `LocalDate` | Invoice due date snapshot | Optional, immutable |
| `customerId` | `UUID` | Customer reference | Optional, immutable |
| `customerName` | `String` | Customer name snapshot | Optional, immutable |
| `amountCollected` | `BigDecimal` | Original payment amount | Required, > 0, immutable |
| `commissionPercentage` | `BigDecimal` | Commission % at creation | Required, immutable |
| `commissionAmount` | `BigDecimal` | Total commission owed | Required, > 0, immutable |
| `collectionDate` | `LocalDate` | Date collection was created | Required, immutable |
| `status` | `PayableStatus` | Payment status | Required, mutable |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated, immutable |
| `amountPaid` | `BigDecimal` | Cumulative amount paid so far | Default: 0.00, mutable |
| `paymentDate` | `LocalDate` | Date of the most recent payment | Null until first payment, mutable |
| `paidBy` | `UUID` | Admin user who registered the last payment | Null until first payment, mutable |
| `paymentReference` | `String` | Payment reference (e.g. transfer number) | Optional, max 100 chars, mutable |
| `paymentMethod` | `SettlementMethod` | Payment method used | Null until first payment, mutable |

**Business Methods**:
- `create(...)` — Factory method to create a new payable from a collection. Sets initial status to `PENDING` and `amountPaid` to `0.00`
- `reconstitute(...)` — Factory method to rebuild the entity from the database (JPA mapper)
- `registerPayment(paymentAmount, paidBy, reference, method)` — Applies a payment. Updates `amountPaid`, `paidBy`, `paymentDate`, `paymentReference`, and `paymentMethod`. Transitions status to `PARTIALLY_PAID` or `PAID`
- `getRemainingBalance()` — Returns `commissionAmount - amountPaid`
- `isPending()` — Returns `true` if status is `PENDING`
- `isPartiallyPaid()` — Returns `true` if status is `PARTIALLY_PAID`
- `isPaid()` — Returns `true` if status is `PAID`
- `hasOutstandingBalance()` — Returns `true` if status is `PENDING` or `PARTIALLY_PAID`
- `belongsToMerchant(UUID)` — Ownership check used by merchant-scoped endpoints

**Business Rules**:
1. One-to-one with Collection: each Collection generates exactly one Payable
2. Auto-generated: created when a Collection is created; cannot be manually created via API
3. Payments cannot exceed the remaining balance; `IllegalArgumentException` is thrown
4. Once status is `PAID`, no further payments are accepted; `PayableAlreadyPaidException` is thrown
5. `paidBy` cannot be null when calling `registerPayment`; the calling use case passes the authenticated admin's UUID
6. Status transitions are one-directional: `PENDING` → `PARTIALLY_PAID` → `PAID`
7. Cannot be deleted (audit trail requirement)

---

#### PayablePayment (Audit Entity)
**Domain Entity**: `com.lexand.lexand_api.payables.domain.entities.PayablePayment`

**Description**: Immutable audit record of a single payment transaction applied to a `Payable`. Each call to `RegisterPaymentUseCase`, `PayAllMerchantPayablesUseCase`, or `PayPartialMerchantPayablesUseCase` creates one `PayablePayment` record. Provides the full payment history for any given payable.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `PayablePaymentId` (UUID) | Unique identifier for this payment record | Auto-generated, immutable |
| `payableId` | `PayableId` (UUID) | Reference to the parent `Payable` | Required, immutable |
| `merchantId` | `UUID` | Merchant that received the payment | Required, immutable |
| `amount` | `BigDecimal` | Amount applied in this transaction | Required, > 0, immutable |
| `paymentDate` | `LocalDate` | Date the payment was applied (`LocalDate.now()`) | Required, immutable |
| `paymentMethod` | `SettlementMethod` | Payment method used | Required, immutable |
| `reference` | `String` | Optional reference string (e.g. wire transfer ID) | Optional, max 100 chars, immutable |
| `paidBy` | `UUID` | Admin user who registered this payment | Required, immutable |
| `registeredAt` | `LocalDateTime` | Server-side timestamp of record creation | Auto-generated, immutable |

**Factory Methods**:
- `create(payableId, merchantId, amount, paymentDate, paymentMethod, reference, paidBy)` — Creates a new audit record. Validates all required fields
- `reconstitute(...)` — Rebuilds the entity from the database (JPA mapper)

**Design Notes**:
- Fully immutable: no setters, no mutation methods
- One record is written per payment transaction, even for bulk operations (one record per payable touched)
- Not exposed to the merchant role — only visible to `USER` (admin) via `GET /api/payables/{id}/payments`

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `PayableId` | UUID | Payable unique identifier | Non-null UUID; factory methods `generate()` and `from(String)` |
| `PayablePaymentId` | UUID | Payment audit record identifier | Non-null UUID; factory method `generate()` |
| `PayableStatus` | Enum | Payment progress status | `PENDING`, `PARTIALLY_PAID`, `PAID` |
| `SettlementMethod` | Enum | Payment method used | `TRANSFER`, `CASH`, `CHECK` |

---

### Enums

#### PayableStatus
```java
public enum PayableStatus {
    PENDING,         // Commission owed; no payment registered yet
    PARTIALLY_PAID,  // Some payment made; remaining balance > 0
    PAID;            // Commission fully paid; terminal state

    public boolean isPending()           { return this == PENDING; }
    public boolean isPartiallyPaid()     { return this == PARTIALLY_PAID; }
    public boolean isPaid()              { return this == PAID; }
    public boolean isFullyPaid()         { return this == PAID; }
    public boolean hasOutstandingBalance() { return this == PENDING || this == PARTIALLY_PAID; }
}
```

#### SettlementMethod
```java
public enum SettlementMethod {
    TRANSFER,  // Bank transfer
    CASH,      // Cash payment
    CHECK      // Check payment
}
```

---

## Use Cases

### Application Layer Use Cases

| Use Case | Trigger | Description |
|----------|---------|-------------|
| `CreatePayableUseCase` | Internal (Collections module) | Creates a new payable when a collection is registered. Idempotent: returns null if a payable already exists for the collection |
| `GetGlobalPayablesSummaryUseCase` | `GET /api/payables/summary` | Calculates totals across all merchants (total pending, merchant count, top merchants by pending amount) |
| `SearchMerchantsWithPayablesUseCase` | `GET /api/payables/merchants` | Paginated search of merchants with pending payables |
| `GetMerchantPayablesSummaryUseCase` | `GET /api/payables/merchants/{id}` and `GET /api/payables/me` | Returns full financial summary for a specific merchant |
| `ListMerchantPayablesUseCase` | `GET /api/payables/merchants/{id}/items` and `GET /api/payables/me/items` | Paginated list of individual payable records for a merchant, optionally filtered by status, `fromDate` (LocalDate, optional), and `toDate` (LocalDate, optional) |
| `GetPayableUseCase` | `GET /api/payables/{id}` | Retrieves a single payable by ID with full detail including payment tracking fields |
| `RegisterPaymentUseCase` | `POST /api/payables/{id}/pay` | Registers a full or partial payment on a single payable. Only available to `USER` (admin) role. Writes a `PayablePayment` audit record |
| `ListPayablePaymentsUseCase` | `GET /api/payables/{id}/payments` | Returns the full ordered payment history for a single payable as a list of `PayablePaymentResponse`. Only available to `USER` (admin) role |
| `PayAllMerchantPayablesUseCase` | `POST /api/payables/merchants/{merchantId}/pay-all` | Settles ALL outstanding payables (`PENDING` and `PARTIALLY_PAID`) for a merchant in a single transaction. Writes one `PayablePayment` record per payable settled |
| `PayPartialMerchantPayablesUseCase` | `POST /api/payables/merchants/{merchantId}/pay-partial` | Distributes a fixed budget across a merchant's outstanding payables, oldest first (FIFO by `collectionDate`). Writes one `PayablePayment` record per payable touched |

---

## Repository Interfaces

### Domain Layer Port

```java
public interface PayableRepository {
    Payable save(Payable payable);
    Optional<Payable> findById(PayableId id);
    Optional<Payable> findByCollectionId(UUID collectionId);
    Page<Payable> findByMerchantIdAndStatus(UUID merchantId, PayableStatus status, Pageable pageable);
    List<Payable> findByMerchantIdAndStatus(UUID merchantId, PayableStatus status);
    Page<Payable> findByMerchantId(UUID merchantId, Pageable pageable);
    Page<Payable> findByMerchantIdSorted(UUID merchantId, Pageable pageable);
    Page<Payable> findByMerchantIdAndStatusSorted(UUID merchantId, PayableStatus status, Pageable pageable);
    List<Payable> findAllById(List<PayableId> ids);
    boolean existsByCollectionId(UUID collectionId);
    int countByMerchantIdAndStatus(UUID merchantId, PayableStatus status);
}
```

**Implementation**: `JpaPayableRepositoryAdapter` backed by `SpringDataPayableRepository` (Spring Data JPA)

**Query Repository**: `PayablesQueryRepository` / `JpaPayablesQueryRepository` — provides aggregate queries used by the summary use cases (total pending, merchant counts, top merchant breakdowns)

---

### PayablePaymentRepository (Domain Port)

```java
public interface PayablePaymentRepository {
    PayablePayment save(PayablePayment payment);
    List<PayablePayment> findByPayableId(PayableId payableId);
}
```

**Implementation**: `JpaPayablePaymentRepositoryAdapter` backed by `SpringDataPayablePaymentRepository` (Spring Data JPA)

---

## API Endpoints

### Summary

| # | Method | Endpoint | Role | Description |
|---|--------|----------|------|-------------|
| 1 | GET | `/api/payables/summary` | USER | Global payables summary |
| 2 | GET | `/api/payables/merchants` | USER | Paginated list of merchants with payables |
| 3 | GET | `/api/payables/merchants/{merchantId}` | USER | Full summary for one merchant |
| 4 | GET | `/api/payables/merchants/{merchantId}/items` | USER | Paginated list of payables for one merchant |
| 5 | POST | `/api/payables/merchants/{merchantId}/pay-all` | USER only | Settle all outstanding payables for a merchant |
| 6 | POST | `/api/payables/merchants/{merchantId}/pay-partial` | USER only | Distribute a fixed budget across a merchant's payables |
| 7 | GET | `/api/payables/{payableId}` | USER | Single payable detail |
| 8 | POST | `/api/payables/{payableId}/pay` | USER only | Register a payment on a single payable |
| 9 | GET | `/api/payables/{payableId}/payments` | USER only | Full payment history for a payable |
| 10 | GET | `/api/payables/me` | MERCHANT only | Merchant views own summary |
| 11 | GET | `/api/payables/me/items` | MERCHANT only | Merchant views own payable list |

---

### 1. Get Global Payables Summary
**Endpoint**: `GET /api/payables/summary`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER

**Description**: Returns aggregate totals across all merchants — total amount pending, number of merchants with pending payables, and a breakdown of the top 10 merchants by pending amount.

**Response** (200 OK) — `GlobalPayablesSummaryResponse`:
```json
{
  "totalPending": 12500.00,
  "totalMerchantsWithPending": 8,
  "totalPendingPayables": 45,
  "totalSettledThisMonth": 8750.00,
  "pendingByMerchant": [
    {
      "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "merchantName": "Juan Perez",
      "merchantCode": 1,
      "totalPending": 3250.00,
      "pendingCount": 12
    }
  ],
  "calculatedAt": "2026-03-05T10:30:00"
}
```

---

### 2. Search Merchants with Payables
**Endpoint**: `GET /api/payables/merchants`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `search` | String | null | Filter by merchant name or code |
| `page` | Integer | 0 | Page number (0-indexed) |
| `size` | Integer | 20 | Page size |
| `sort` | String | `totalPending,desc` | Sort field and direction |

**Response** (200 OK) — Paginated `Page<MerchantPayablesListResponse>`:
```json
{
  "content": [
    {
      "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "merchantName": "Juan Perez",
      "merchantCode": 1,
      "commissionPercentage": 15.00,
      "totalPending": 3250.00,
      "pendingPayablesCount": 12,
      "oldestPendingDate": "2026-01-05"
    }
  ],
  "totalElements": 8,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 3. Get Merchant Payables Summary
**Endpoint**: `GET /api/payables/merchants/{merchantId}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER

**Path Parameters**:
- `merchantId` (UUID) — Merchant unique identifier

**Response** (200 OK) — `MerchantPayablesSummaryResponse`:
```json
{
  "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "merchantName": "Juan Perez",
  "merchantCode": 1,
  "commissionPercentage": 15.00,
  "totalPending": 3250.00,
  "pendingPayablesCount": 12,
  "totalPartiallyPaid": 500.00,
  "totalPaid": 8500.00,
  "paidPayablesCount": 35,
  "totalAllTime": 12250.00,
  "pendingBreakdown": {
    "thisWeek": 750.00,
    "thisMonth": 1500.00,
    "older": 1000.00
  }
}
```

**Errors**:
- `404 Not Found`: No payables found for this merchant

---

### 4. List Merchant Payables
**Endpoint**: `GET /api/payables/merchants/{merchantId}/items`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER

**Path Parameters**:
- `merchantId` (UUID) — Merchant unique identifier

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `status` | String | null | Filter by status: `PENDING`, `PARTIALLY_PAID`, `PAID` |
| `fromDate` | LocalDate | null | Filter payables from this date (inclusive), format: `YYYY-MM-DD` |
| `toDate` | LocalDate | null | Filter payables up to this date (inclusive), format: `YYYY-MM-DD` |
| `page` | Integer | 0 | Page number |
| `size` | Integer | 20 | Page size |
| `sort` | String | `createdAt,desc` | Sort field |

**Response** (200 OK) — Paginated `Page<PayableListResponse>`:
```json
{
  "content": [
    {
      "id": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
      "collectionId": "cc22dd33-ee44-ff55-aa66-bb77cc88dd99",
      "invoiceId": "dd33ee44-ff55-aa66-bb77-cc88dd99ee00",
      "invoiceNumber": "INV-2026-00001",
      "invoiceDueDate": "2026-02-15",
      "customerId": "ee44ff55-aa66-bb77-cc88-dd99ee00ff11",
      "customerName": "ABC Company S.A.",
      "amountCollected": 500.00,
      "commissionPercentage": 15.00,
      "commissionAmount": 75.00,
      "amountPaid": 30.00,
      "remainingBalance": 45.00,
      "collectionDate": "2026-01-10",
      "status": "PARTIALLY_PAID",
      "createdAt": "2026-01-10T14:00:00"
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Example URL with date range**:
```
GET /api/payables/merchants/{merchantId}/items?fromDate=2026-01-01&toDate=2026-03-31
```

---

### 5. Pay All Merchant Payables
**Endpoint**: `POST /api/payables/merchants/{merchantId}/pay-all`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER only (admin)

**Description**: Settles ALL outstanding payables (`PENDING` and `PARTIALLY_PAID`) for a merchant in a single atomic transaction. Each payable receives its full remaining balance. One `PayablePayment` audit record is written per payable settled. If the merchant has no outstanding payables, returns `404`.

**Path Parameters**:
- `merchantId` (UUID) — Merchant unique identifier

**Request Body** (`PayAllRequest`):
```json
{
  "paymentMethod": "TRANSFER",
  "reference": "TRF-BATCH-2026-001"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `paymentMethod` | String (SettlementMethod) | Yes | `TRANSFER`, `CASH`, or `CHECK` |
| `reference` | String | No | Max 100 characters |

**Response** (200 OK) — `BulkPaymentResultResponse`:
```json
{
  "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "merchantName": "Juan Perez",
  "invoicesSettled": 3,
  "totalAmountPaid": 225.00,
  "remainingUnpaidBalance": 0.00,
  "payments": [
    {
      "payablePaymentId": "pp11aa22-bb33-cc44-dd55-ee66ff778899",
      "payableId": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
      "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "merchantName": "Juan Perez",
      "status": "PAID",
      "commissionAmount": 75.00,
      "amountPaid": 75.00,
      "remainingBalance": 0.00,
      "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
      "paymentDate": "2026-03-10",
      "paymentMethod": "TRANSFER",
      "reference": "TRF-BATCH-2026-001"
    }
  ],
  "paymentDate": "2026-03-10"
}
```

**Errors**:
- `400 Bad Request`: Validation failure (null payment method, reference too long)
- `404 Not Found`: No outstanding payables for this merchant (`MerchantHasNoPayablesException`)

---

### 6. Pay Partial Merchant Payables
**Endpoint**: `POST /api/payables/merchants/{merchantId}/pay-partial`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER only (admin)

**Description**: Distributes a fixed payment budget across a merchant's outstanding payables, applying payments oldest-first (FIFO by `collectionDate`). Each payable receives as much of the budget as it needs; leftover budget rolls to the next payable. If the budget is exhausted before all payables are settled, remaining payables are left untouched. One `PayablePayment` audit record is written per payable that received any amount.

**Path Parameters**:
- `merchantId` (UUID) — Merchant unique identifier

**Request Body** (`PayPartialRequest`):
```json
{
  "amount": 100.00,
  "paymentMethod": "CASH",
  "reference": null
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `amount` | BigDecimal | Yes | Positive; the total budget to distribute |
| `paymentMethod` | String (SettlementMethod) | Yes | `TRANSFER`, `CASH`, or `CHECK` |
| `reference` | String | No | Max 100 characters |

**Response** (200 OK) — `BulkPaymentResultResponse`:
```json
{
  "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "merchantName": "Juan Perez",
  "invoicesSettled": 1,
  "totalAmountPaid": 100.00,
  "remainingUnpaidBalance": 125.00,
  "payments": [
    {
      "payablePaymentId": "pp22bb33-cc44-dd55-ee66-ff7788990011",
      "payableId": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
      "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "merchantName": "Juan Perez",
      "status": "PAID",
      "commissionAmount": 75.00,
      "amountPaid": 75.00,
      "remainingBalance": 0.00,
      "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
      "paymentDate": "2026-03-10",
      "paymentMethod": "CASH",
      "reference": null
    },
    {
      "payablePaymentId": "pp33cc44-dd55-ee66-ff77-8899001122bb",
      "payableId": "cc22dd33-ee44-ff55-aa66-bb77cc88dd99",
      "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "merchantName": "Juan Perez",
      "status": "PARTIALLY_PAID",
      "commissionAmount": 150.00,
      "amountPaid": 25.00,
      "remainingBalance": 125.00,
      "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
      "paymentDate": "2026-03-10",
      "paymentMethod": "CASH",
      "reference": null
    }
  ],
  "paymentDate": "2026-03-10"
}
```

**Errors**:
- `400 Bad Request`: Validation failure (null amount, non-positive amount, null payment method, reference too long)
- `404 Not Found`: No outstanding payables for this merchant (`MerchantHasNoPayablesException`)

---

### 7. Get Payable Detail
**Endpoint**: `GET /api/payables/{payableId}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER

**Path Parameters**:
- `payableId` (UUID) — Payable unique identifier

**Response** (200 OK) — `PayableDetailResponse`:
```json
{
  "id": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
  "collectionId": "cc22dd33-ee44-ff55-aa66-bb77cc88dd99",
  "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "merchantName": "Juan Perez",
  "merchantCode": 1,
  "invoiceId": "dd33ee44-ff55-aa66-bb77-cc88dd99ee00",
  "invoiceNumber": "INV-2026-00001",
  "invoiceDueDate": "2026-02-15",
  "customerId": "ee44ff55-aa66-bb77-cc88-dd99ee00ff11",
  "customerName": "ABC Company S.A.",
  "amountCollected": 500.00,
  "commissionPercentage": 15.00,
  "commissionAmount": 75.00,
  "amountPaid": 30.00,
  "remainingBalance": 45.00,
  "collectionDate": "2026-01-10",
  "status": "PARTIALLY_PAID",
  "createdAt": "2026-01-10T14:00:00",
  "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
  "paymentDate": "2026-02-01",
  "paymentReference": "TRF-2026-00050",
  "paymentMethod": "TRANSFER"
}
```

**Errors**:
- `404 Not Found`: Payable not found

---

### 8. Register Payment
**Endpoint**: `POST /api/payables/{payableId}/pay`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER only (admin)

**Description**: Registers a full or partial payment on a payable. The authenticated user's UUID is automatically recorded as `paidBy`. If the payment covers the remaining balance exactly, status transitions to `PAID`. If it is less than the remaining balance, status transitions to `PARTIALLY_PAID`.

**Path Parameters**:
- `payableId` (UUID) — Payable unique identifier

**Request Body** (`RegisterPaymentRequest`):
```json
{
  "amount": 30.00,
  "paymentMethod": "TRANSFER",
  "reference": "TRF-2026-00050",
  "paymentDate": "2026-02-01"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `amount` | BigDecimal | Yes | Positive; must not exceed remaining balance |
| `paymentMethod` | String (SettlementMethod) | Yes | `TRANSFER`, `CASH`, or `CHECK` |
| `reference` | String | No | Max 100 characters |

> **Note**: The `paymentDate` in the request body is currently not a field on `RegisterPaymentRequest` — the payment date is set automatically to `LocalDate.now()` inside `Payable.registerPayment()`. The request only carries `amount`, `paymentMethod`, and `reference`.

**Response** (200 OK) — `PaymentResultResponse`:
```json
{
  "payablePaymentId": "pp44dd55-ee66-ff77-aa88-bb99001122cc",
  "payableId": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
  "merchantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "merchantName": "Juan Perez",
  "status": "PARTIALLY_PAID",
  "commissionAmount": 75.00,
  "amountPaid": 30.00,
  "remainingBalance": 45.00,
  "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
  "paymentDate": "2026-02-01",
  "paymentMethod": "TRANSFER",
  "reference": "TRF-2026-00050"
}
```

**Errors**:
- `400 Bad Request`: Validation failure (null amount, non-positive amount, null payment method, reference too long) or payment amount exceeds remaining balance
- `404 Not Found`: Payable not found
- `409 Conflict`: Payable is already fully paid (`PayableAlreadyPaidException`)

---

### 9. Get Payable Payment History
**Endpoint**: `GET /api/payables/{payableId}/payments`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER only (admin)

**Description**: Returns the full ordered list of payment transactions recorded against a payable. Each entry corresponds to one `PayablePayment` audit record. Returns an empty list if no payments have been registered yet.

**Path Parameters**:
- `payableId` (UUID) — Payable unique identifier

**Response** (200 OK) — `List<PayablePaymentResponse>`:
```json
[
  {
    "id": "pp11aa22-bb33-cc44-dd55-ee66ff778899",
    "payableId": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
    "amount": 30.00,
    "paymentDate": "2026-02-01",
    "paymentMethod": "TRANSFER",
    "reference": "TRF-2026-00050",
    "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
    "registeredAt": "2026-02-01T14:22:10"
  },
  {
    "id": "pp22bb33-cc44-dd55-ee66-ff7788990011",
    "payableId": "bb11cc22-dd33-ee44-ff55-aa66bb77cc88",
    "amount": 45.00,
    "paymentDate": "2026-03-10",
    "paymentMethod": "CASH",
    "reference": null,
    "paidBy": "ff55aa66-bb77-cc88-dd99-ee00ff110011",
    "registeredAt": "2026-03-10T09:05:33"
  }
]
```

**Errors**:
- `404 Not Found`: Payable not found

---

### 10. Get My Payables Summary (Merchant)
**Endpoint**: `GET /api/payables/me`

**Authentication**: Required (JWT Bearer Token)

**Roles**: MERCHANT only

**Description**: Returns the commission summary for the authenticated merchant. The merchant ID is derived from the JWT token. Merchants can ONLY view their own data.

**Response** (200 OK) — `MerchantPayablesSummaryResponse`

Same structure as endpoint 3. See example above.

**Errors**:
- `404 Not Found`: No payables found for this merchant

---

### 11. List My Payables (Merchant)
**Endpoint**: `GET /api/payables/me/items`

**Authentication**: Required (JWT Bearer Token)

**Roles**: MERCHANT only

**Description**: Returns the paginated list of individual payables for the authenticated merchant. The merchant ID is derived from the JWT token.

**Query Parameters**: Same as endpoint 4 (`status`, `fromDate`, `toDate`, `page`, `size`, `sort`)

**Response** (200 OK) — Paginated `Page<PayableListResponse>`

Same structure as endpoint 4. See example above.

**Example URL with date range**:
```
GET /api/payables/me/items?fromDate=2026-01-01&toDate=2026-03-31
```

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

```
application/dto/
├── request/
│   ├── RegisterPaymentRequest.java
│   ├── PayAllRequest.java
│   └── PayPartialRequest.java
└── response/
    ├── GlobalPayablesSummaryResponse.java
    ├── MerchantPayablesSummaryResponse.java
    ├── MerchantPayablesListResponse.java
    ├── PayableDetailResponse.java
    ├── PayableListResponse.java
    ├── PayablePaymentResponse.java
    ├── BulkPaymentResultResponse.java
    └── PaymentResultResponse.java
```

---

### Request DTOs

#### RegisterPaymentRequest
**Usage**: `POST /api/payables/{payableId}/pay`

```java
public record RegisterPaymentRequest(
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Payment method is required")
    SettlementMethod paymentMethod,

    @Size(max = 100, message = "Reference must be at most 100 characters")
    String reference
) {}
```

**JSON Example**:
```json
{
  "amount": 75.00,
  "paymentMethod": "TRANSFER",
  "reference": "TRF-2026-00123"
}
```

---

#### PayAllRequest
**Usage**: `POST /api/payables/merchants/{merchantId}/pay-all`

```java
public record PayAllRequest(
    @NotNull(message = "Payment method is required")
    SettlementMethod paymentMethod,

    @Size(max = 100, message = "Reference must be at most 100 characters")
    String reference
) {}
```

**JSON Example**:
```json
{
  "paymentMethod": "TRANSFER",
  "reference": "TRF-BATCH-2026-001"
}
```

---

#### PayPartialRequest
**Usage**: `POST /api/payables/merchants/{merchantId}/pay-partial`

```java
public record PayPartialRequest(
    @NotNull(message = "Payment amount is required")
    @Positive(message = "Payment amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Payment method is required")
    SettlementMethod paymentMethod,

    @Size(max = 100, message = "Reference must be at most 100 characters")
    String reference
) {}
```

**JSON Example**:
```json
{
  "amount": 100.00,
  "paymentMethod": "CASH",
  "reference": null
}
```

---

### Response DTOs

#### GlobalPayablesSummaryResponse
**Usage**: `GET /api/payables/summary`

```java
public record GlobalPayablesSummaryResponse(
    BigDecimal totalPending,
    int totalMerchantsWithPending,
    int totalPendingPayables,
    BigDecimal totalSettledThisMonth,
    List<MerchantPendingSummary> pendingByMerchant,
    LocalDateTime calculatedAt
) {
    public record MerchantPendingSummary(
        String merchantId,
        String merchantName,
        Long merchantCode,
        BigDecimal totalPending,
        int pendingCount
    ) {}
}
```

---

#### MerchantPayablesListResponse
**Usage**: `GET /api/payables/merchants` (list item)

```java
public record MerchantPayablesListResponse(
    String merchantId,
    String merchantName,
    Long merchantCode,
    BigDecimal commissionPercentage,
    BigDecimal totalPending,
    int pendingPayablesCount,
    LocalDate oldestPendingDate
) {}
```

---

#### MerchantPayablesSummaryResponse
**Usage**: `GET /api/payables/merchants/{merchantId}`, `GET /api/payables/me`

```java
public record MerchantPayablesSummaryResponse(
    String merchantId,
    String merchantName,
    Long merchantCode,
    BigDecimal commissionPercentage,
    BigDecimal totalPending,
    int pendingPayablesCount,
    BigDecimal totalPartiallyPaid,      // Amount in PARTIALLY_PAID payables
    BigDecimal totalPaid,               // Amount in fully PAID payables
    int paidPayablesCount,              // Count of PAID payables
    BigDecimal totalAllTime,
    PendingBreakdown pendingBreakdown
) {
    public record PendingBreakdown(
        BigDecimal thisWeek,
        BigDecimal thisMonth,
        BigDecimal older
    ) {}
}
```

---

#### PayableListResponse
**Usage**: `GET /api/payables/merchants/{merchantId}/items`, `GET /api/payables/me/items`

```java
public record PayableListResponse(
    String id,
    String collectionId,
    String invoiceId,
    String invoiceNumber,
    LocalDate invoiceDueDate,
    String customerId,
    String customerName,
    BigDecimal amountCollected,
    BigDecimal commissionPercentage,
    BigDecimal commissionAmount,
    BigDecimal amountPaid,
    BigDecimal remainingBalance,
    LocalDate collectionDate,
    String status,
    LocalDateTime createdAt
) {}
```

---

#### PayableDetailResponse
**Usage**: `GET /api/payables/{payableId}`

```java
public record PayableDetailResponse(
    String id,
    String collectionId,
    String merchantId,
    String merchantName,
    Long merchantCode,
    String invoiceId,
    String invoiceNumber,
    LocalDate invoiceDueDate,
    String customerId,
    String customerName,
    BigDecimal amountCollected,
    BigDecimal commissionPercentage,
    BigDecimal commissionAmount,
    BigDecimal amountPaid,
    BigDecimal remainingBalance,
    LocalDate collectionDate,
    String status,
    LocalDateTime createdAt,
    String paidBy,
    LocalDate paymentDate,
    String paymentReference,
    String paymentMethod
) {}
```

---

#### PaymentResultResponse
**Usage**: `POST /api/payables/{payableId}/pay`, `POST /api/payables/merchants/{merchantId}/pay-all`, `POST /api/payables/merchants/{merchantId}/pay-partial` (per-payable entry in `payments` list)

```java
public record PaymentResultResponse(
    String payablePaymentId,   // ID of the PayablePayment audit record
    String payableId,
    String merchantId,
    String merchantName,
    String status,
    BigDecimal commissionAmount,
    BigDecimal amountPaid,
    BigDecimal remainingBalance,
    String paidBy,
    LocalDate paymentDate,
    String paymentMethod,
    String reference
) {}
```

---

#### PayablePaymentResponse
**Usage**: `GET /api/payables/{payableId}/payments`

```java
public record PayablePaymentResponse(
    String id,
    String payableId,
    BigDecimal amount,
    LocalDate paymentDate,
    String paymentMethod,
    String reference,
    String paidBy,
    LocalDateTime registeredAt
) {}
```

---

#### BulkPaymentResultResponse
**Usage**: `POST /api/payables/merchants/{merchantId}/pay-all`, `POST /api/payables/merchants/{merchantId}/pay-partial`

```java
public record BulkPaymentResultResponse(
    String merchantId,
    String merchantName,
    int invoicesSettled,
    BigDecimal totalAmountPaid,
    BigDecimal remainingUnpaidBalance,
    List<PaymentResultResponse> payments,
    LocalDate paymentDate
) {}
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| `GET /api/payables/summary` | None | `GlobalPayablesSummaryResponse` |
| `GET /api/payables/merchants` | None | `Page<MerchantPayablesListResponse>` |
| `GET /api/payables/merchants/{merchantId}` | None | `MerchantPayablesSummaryResponse` |
| `GET /api/payables/merchants/{merchantId}/items` | None | `Page<PayableListResponse>` |
| `POST /api/payables/merchants/{merchantId}/pay-all` | `PayAllRequest` | `BulkPaymentResultResponse` |
| `POST /api/payables/merchants/{merchantId}/pay-partial` | `PayPartialRequest` | `BulkPaymentResultResponse` |
| `GET /api/payables/{payableId}` | None | `PayableDetailResponse` |
| `POST /api/payables/{payableId}/pay` | `RegisterPaymentRequest` | `PaymentResultResponse` |
| `GET /api/payables/{payableId}/payments` | None | `List<PayablePaymentResponse>` |
| `GET /api/payables/me` | None | `MerchantPayablesSummaryResponse` |
| `GET /api/payables/me/items` | None | `Page<PayableListResponse>` |

---

## Business Rules Summary

### Payable Creation
1. Payables are created automatically when a Collection is created via `CreatePayableUseCase`
2. One-to-one relationship: 1 Collection → 1 Payable (enforced by `UNIQUE` constraint on `collection_id`)
3. `CreatePayableUseCase` is idempotent: if a payable for the given `collectionId` already exists, it returns null without throwing an error
4. All financial data is snapshot from the Collection at creation time and is immutable
5. Initial status is always `PENDING`; `amountPaid` defaults to `0.00`
6. Payables cannot be created directly via the API

### Payment Rules
1. Only `USER` (admin) role can register payments via `POST /api/payables/{id}/pay`
2. Payments can be partial — a single payable can receive multiple payments over time
3. Payment amount must be positive and must not exceed the remaining balance
4. When `amountPaid >= commissionAmount`, status transitions to `PAID`
5. When `amountPaid < commissionAmount` (and > 0), status transitions to `PARTIALLY_PAID`
6. Once status is `PAID`, any further payment attempt returns `409 PayableAlreadyPaidException`
7. The payment date is set automatically to the current date (`LocalDate.now()`) at the time `registerPayment()` is called
8. The `paidBy` field always records the admin user who made the most recent payment registration
9. Every payment transaction (single, bulk all, or bulk partial) writes an immutable `PayablePayment` audit record to `payable_payments`

### Bulk Payment Rules
1. **Pay-All**: Settles every outstanding (`PENDING` and `PARTIALLY_PAID`) payable for a merchant in one transaction. `remainingUnpaidBalance` in the response is always `0.00`
2. **Pay-Partial**: Distributes a fixed budget FIFO by `collectionDate` (oldest first). Budget is applied until exhausted; remaining payables are untouched. `remainingUnpaidBalance` reflects the total still owed after the operation
3. Both bulk endpoints throw `MerchantHasNoPayablesException` (404) if the merchant has no outstanding payables

### Role-Based Access
| Action | USER (admin) | MERCHANT |
|--------|-------------|----------|
| View global summary | ✅ | ❌ |
| View all merchants' payables | ✅ | ❌ |
| View single payable (any merchant) | ✅ | ❌ |
| View payment history for a payable | ✅ | ❌ |
| Register single payment | ✅ | ❌ |
| Pay all outstanding for a merchant | ✅ | ❌ |
| Pay partial (budget) for a merchant | ✅ | ❌ |
| View own summary | ✅ | ✅ |
| View own payable list | ✅ | ✅ |

### Immutability and Audit
1. Payables are never deleted
2. Immutable fields (`collectionId`, `merchantId`, `commissionAmount`, etc.) are set at creation and never change
3. Payment tracking fields (`amountPaid`, `paidBy`, `paymentDate`, `paymentReference`, `paymentMethod`) on the `payables` table record the **most recent payment** and are overwritten on each call to `registerPayment()`
4. The `amountPaid` field accumulates across multiple payments (it is added to, not replaced)
5. Each individual payment transaction is permanently recorded as an immutable `PayablePayment` row in the `payable_payments` table — this provides the full audit history

---

## Database Schema

### payables Table

```sql
CREATE TABLE payables (
    id                    BINARY(16)      PRIMARY KEY,
    collection_id         BINARY(16)      NOT NULL UNIQUE,
    merchant_id           BINARY(16)      NOT NULL,
    merchant_name         VARCHAR(200)    NOT NULL,
    merchant_code         BIGINT          NOT NULL,
    invoice_id            BINARY(16)      NOT NULL,
    invoice_number        VARCHAR(20)     NOT NULL,
    invoice_due_date      DATE            NULL,
    customer_id           BINARY(16),
    customer_name         VARCHAR(200),
    amount_collected      DECIMAL(15,2)   NOT NULL,
    commission_percentage DECIMAL(5,2)    NOT NULL,
    commission_amount     DECIMAL(15,2)   NOT NULL,
    collection_date       DATE            NOT NULL,
    status                VARCHAR(20)     NOT NULL,
    amount_paid           DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    payment_date          DATE,
    paid_by               BINARY(16),
    payment_reference     VARCHAR(100),
    payment_method        VARCHAR(20),
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_payable_collection      (collection_id),
    INDEX idx_payable_merchant        (merchant_id),
    INDEX idx_payable_status          (status),
    INDEX idx_payable_merchant_status (merchant_id, status),
    INDEX idx_payable_created_at      (created_at),
    INDEX idx_payable_collection_date (collection_date),
    INDEX idx_payable_invoice_due_date(invoice_due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

*(Migration: V13)*

**Column Notes**:
- `status`: One of `PENDING`, `PARTIALLY_PAID`, `PAID`
- `amount_paid`: Cumulative amount paid; starts at `0.00`; accumulates across multiple `registerPayment()` calls
- `payment_date`: Date of the most recent payment registration (set to `CURRENT_DATE` by the domain entity)
- `paid_by`: UUID of the admin user who registered the most recent payment
- `payment_reference`: Optional reference string from the most recent payment (e.g. wire transfer number)
- `payment_method`: Most recent payment method used (`TRANSFER`, `CASH`, `CHECK`)

---

### payable_payments Table

```sql
CREATE TABLE payable_payments (
    id              BINARY(16)      NOT NULL,
    payable_id      BINARY(16)      NOT NULL,
    merchant_id     BINARY(16)      NOT NULL,
    amount          DECIMAL(15,2)   NOT NULL,
    payment_date    DATE            NOT NULL,
    payment_method  VARCHAR(20)     NOT NULL,
    reference       VARCHAR(100),
    paid_by         BINARY(16)      NOT NULL,
    registered_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_payable_payment_payable_id    (payable_id),
    INDEX idx_payable_payment_merchant_id   (merchant_id),
    INDEX idx_payable_payment_payment_date  (payment_date),
    INDEX idx_payable_payment_registered_at (registered_at),

    CONSTRAINT fk_payable_payment_payable
        FOREIGN KEY (payable_id) REFERENCES payables(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

*(Migration: V15)*

**Column Notes**:
- `id`: UUID of the `PayablePayment` record (auto-generated by `PayablePaymentId.generate()`)
- `payable_id`: Foreign key reference to `payables(id)` — cascading deletion is RESTRICTED
- `merchant_id`: Snapshot of the merchant UUID at payment time
- `amount`: Exact amount applied in this single transaction
- `payment_date`: Date the payment was applied (`LocalDate.now()` at time of registration)
- `payment_method`: Payment method used (`TRANSFER`, `CASH`, `CHECK`)
- `reference`: Optional transfer reference or note
- `paid_by`: UUID of the admin who registered this payment
- `registered_at`: Server-side timestamp of record insertion

**Design Note**: This table is append-only. Rows are never updated or deleted. It provides the complete ordered audit trail of all payments made against any given payable.

---

## Exception Handling

### Custom Exceptions

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `PayableNotFoundException` | 404 Not Found | Payable not found by ID |
| `PayableAlreadyPaidException` | 409 Conflict | Attempting to register payment on a fully paid payable |
| `MerchantHasNoPayablesException` | 404 Not Found | No payables found for the given merchant |
| `IllegalArgumentException` (domain) | 400 Bad Request | Validation failure inside the domain entity (null amount, non-positive amount, amount exceeds remaining balance, null `paidBy`, null payment method) |
| `IllegalStateException` (domain) | 409 Conflict | Attempting to call `registerPayment()` on a PAID payable at domain level |

### Exception Handler

`PayablesExceptionHandler` (`@RestControllerAdvice`) handles all exceptions within the `payables` package and returns RFC 9457 `ProblemDetail` responses with a `timestamp` property.

---

## Integration with Other Modules

### Collections Module
- `CreatePayableUseCase` is called internally by the Collections module whenever a new Collection is persisted
- One-to-one relationship via `collection_id`
- All financial data (merchant, invoice, customer, amounts, commission) is inherited from the Collection at creation time

### Merchants Module
- `merchant_id`, `merchant_name`, and `merchant_code` are snapshot from the Merchant at collection time
- The MERCHANT role scope (endpoints `/me` and `/me/items`) is resolved by reading the authenticated user's UUID from the security context and using it as the `merchantId`

### Receivables Module
- Collection payment approval triggers Collection creation, which triggers Payable creation
- Chain: Payment Approved → Collection Created → Payable Created

### Invoices Module
- `invoice_id` and `invoice_number` are stored on the Payable for context and traceability
- Customer information is inherited from the invoice via the Collection

---

## Notes for Developers

### Key Design Decisions
1. **No Settlement Workflow**: There is no merchant-initiated request/approval flow. Payments are registered directly by admins via `POST /api/payables/{id}/pay` (single) or the bulk endpoints
2. **Partial Payment Support**: The domain is designed for incremental payments. `amountPaid` accumulates; `remainingBalance` is always derived as `commissionAmount - amountPaid`
3. **Full Payment History**: Every payment transaction is recorded as an immutable `PayablePayment` row in `payable_payments`. The `payables` table columns (`payment_date`, `paid_by`, `payment_reference`, `payment_method`) always reflect the most recent payment, while the full history is accessible via `GET /api/payables/{id}/payments`
4. **Idempotent Creation**: `CreatePayableUseCase` silently returns null if a payable for the collection already exists, making it safe to call multiple times
5. **Merchant Read-Only**: MERCHANT role has zero write access to payables. The endpoints `/me` and `/me/items` are purely informational
6. **ADMIN vs USER role**: The payment endpoints (`POST /{id}/pay`, `POST /merchants/{id}/pay-all`, `POST /merchants/{id}/pay-partial`) are restricted to `hasRole('USER')`, meaning only the USER (admin) user type can register payments

### Payment Amount Guard
The domain entity enforces that payment amounts do not exceed the remaining balance:
```java
BigDecimal remaining = getRemainingBalance();
if (paymentAmount.compareTo(remaining) > 0) {
    throw new IllegalArgumentException(
        "Payment amount " + paymentAmount + " exceeds remaining balance " + remaining);
}
```
This is caught by the `PayablesExceptionHandler` and returned as a `400 Bad Request`.

### Testing Requirements
- Unit tests for `Payable` domain entity:
  - `registerPayment()` transitions PENDING → PARTIALLY_PAID
  - `registerPayment()` transitions PENDING → PAID (full amount)
  - `registerPayment()` transitions PARTIALLY_PAID → PAID (remaining balance)
  - Throws when payment amount exceeds remaining balance
  - Throws when calling `registerPayment()` on a PAID payable
  - Throws on null `paidBy`, null `paymentMethod`, non-positive `paymentAmount`
- Unit tests for `RegisterPaymentUseCase`:
  - Returns `PaymentResultResponse` with correct `remainingBalance` and `status`
  - Throws `PayableNotFoundException` when payable does not exist
  - Throws `PayableAlreadyPaidException` when payable is already PAID
- Unit tests for `CreatePayableUseCase`:
  - Creates payable when no existing payable for collection
  - Returns null (no error) when payable already exists for collection
- Integration tests for all controller endpoints
- Role enforcement tests (MERCHANT cannot call `POST /{id}/pay`; USER cannot call `GET /me`)

---

## Usage Examples

### Example 1: Admin Registers Full Payment
```
Admin (USER role):

GET /api/payables/merchants/{merchantId}
→ sees totalPending: $75.00, pendingPayablesCount: 1

POST /api/payables/{payableId}/pay
Body: { "amount": 75.00, "paymentMethod": "TRANSFER", "reference": "TRF-2026-00123" }

→ 200 OK
{
  "status": "PAID",
  "commissionAmount": 75.00,
  "amountPaid": 75.00,
  "remainingBalance": 0.00,
  ...
}
```

### Example 2: Admin Registers Partial Payment
```
Admin (USER role):

POST /api/payables/{payableId}/pay
Body: { "amount": 30.00, "paymentMethod": "CASH", "reference": null }

→ 200 OK
{
  "status": "PARTIALLY_PAID",
  "commissionAmount": 75.00,
  "amountPaid": 30.00,
  "remainingBalance": 45.00,
  ...
}

POST /api/payables/{payableId}/pay  (second call)
Body: { "amount": 45.00, "paymentMethod": "CASH", "reference": null }

→ 200 OK
{
  "status": "PAID",
  "commissionAmount": 75.00,
  "amountPaid": 75.00,
  "remainingBalance": 0.00,
  ...
}
```

### Example 3: Admin Attempts Payment on Already-Paid Payable
```
POST /api/payables/{payableId}/pay
Body: { "amount": 10.00, "paymentMethod": "TRANSFER", "reference": "TRF-EXTRA" }

→ 409 Conflict
{
  "title": "Payable Already Paid",
  "detail": "Payable is already fully paid: {payableId}",
  "status": 409,
  "timestamp": "2026-03-05T11:00:00"
}
```

### Example 4: Merchant Views Own Commissions
```
Merchant (MERCHANT role):

GET /api/payables/me
→ 200 OK — MerchantPayablesSummaryResponse showing their totals

GET /api/payables/me/items?status=PENDING
→ 200 OK — paginated list of their PENDING payables

GET /api/payables/me/items?fromDate=2026-01-01&toDate=2026-03-31
→ 200 OK — paginated list of their payables within the date range

POST /api/payables/{payableId}/pay
→ 403 Forbidden — merchants cannot register payments
```

### Example 5: Admin Settles All Pending for a Merchant
```
Admin (USER role):

POST /api/payables/merchants/{merchantId}/pay-all
Body: { "paymentMethod": "TRANSFER", "reference": "TRF-BATCH-2026-001" }

→ 200 OK
{
  "merchantName": "Juan Perez",
  "invoicesSettled": 3,
  "totalAmountPaid": 225.00,
  "remainingUnpaidBalance": 0.00,
  "payments": [ ... 3 PaymentResultResponse objects ... ],
  "paymentDate": "2026-03-10"
}
```

### Example 6: Admin Distributes a Partial Budget (FIFO)
```
Admin (USER role) — merchant has 3 outstanding payables: $75, $150, $60

POST /api/payables/merchants/{merchantId}/pay-partial
Body: { "amount": 200.00, "paymentMethod": "CASH", "reference": null }

Distribution (oldest first):
  Payable 1: $75.00 → PAID   (budget: $200 - $75 = $125 remaining)
  Payable 2: $125 of $150 → PARTIALLY_PAID (budget: $125 - $125 = $0)
  Payable 3: untouched

→ 200 OK
{
  "invoicesSettled": 1,
  "totalAmountPaid": 200.00,
  "remainingUnpaidBalance": 85.00,   ← $25 on payable 2 + $60 on payable 3
  "payments": [ ... 2 entries ... ]
}
```

### Example 7: Admin Views Full Payment History for a Payable
```
Admin (USER role):

GET /api/payables/{payableId}/payments
→ 200 OK
[
  { "id": "...", "amount": 30.00, "paymentDate": "2026-02-01", ... },
  { "id": "...", "amount": 45.00, "paymentDate": "2026-03-10", ... }
]
```

---

**Last Updated**: 2026-03-10

**Module Status**: ✅ Implemented
