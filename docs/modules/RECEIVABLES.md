# RECEIVABLES Module (Cuentas por Cobrar)

## Overview
The RECEIVABLES module provides a **customer-centric view** of outstanding debts. It aggregates invoices with pending balances, enables flexible payment options, and implements a **payment validation workflow** where payments registered by merchants require user approval.

**Key Concepts**:
- **Customer-Centric**: Primary view is by customer, showing total debt and individual invoices
- **Payment Validation**: Merchant payments require USER/ADMIN validation before being applied
- **Flexible Payments**: Pay single invoices, partial amounts, or entire customer debt
- **Commission Tracking**: Each payment generates a commission record for the merchant (payable)
- **Role-Based Access**: Merchants can only see/pay invoices associated with them
- **Proof File Upload**: Merchants MUST attach a payment proof file (image or PDF) when registering a payment. USER/ADMIN may optionally attach one
- **Merchant Self-View**: Merchants can list all their own payments (all statuses) via a dedicated endpoint

---

## Business Flow

```
                         RECEIVABLES WORKFLOW

┌─────────────────────────────────────────────────────────────────┐
│                        CUSTOMER DEBT VIEW                        │
│                                                                   │
│  Total Global Debt: $50,000.00                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ Search: [Customer Name/RIF____________] [🔍]                │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ Customer: ABC Company S.A.                                   │ │
│  │ RIF: J-12345678-9                                            │ │
│  │ Total Debt: $5,250.00                                        │ │
│  │ Invoices with Debt: 3                                        │ │
│  │                                               [View Invoices]│ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CUSTOMER INVOICES VIEW                        │
│                                                                   │
│  Customer: ABC Company S.A.          Total Debt: $5,250.00       │
│  ───────────────────────────────────────────────────────────────│
  │  Filter: [DELIVERED ▼] [PARTIALLY_PAID ▼] [PAID ▼]               │
│  ───────────────────────────────────────────────────────────────│
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ INV-2025-00001 │ $2,000.00 │ Paid: $500   │ Debt: $1,500.00 │ │
  │  │ Status: PARTIALLY_PAID│ Merchant: Juan Perez                [Pay]  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ INV-2025-00005 │ $1,750.00 │ Paid: $0     │ Debt: $1,750.00 │ │
│  │ Status: DELIVERED │ Merchant: Juan Perez             [Pay]  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ INV-2025-00008 │ $2,000.00 │ Paid: $0     │ Debt: $2,000.00 │ │
│  │ Status: DELIVERED │ Merchant: Maria Lopez            [Pay]  │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                   │
│  ─────────────────────────────────────────────────────────────── │
│  [Pay Selected] [Pay All Debt: $5,250.00] [Pay Partial Amount]  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Payment Validation Workflow

```
                    PAYMENT VALIDATION STATES

    ┌─────────────┐
    │   MERCHANT  │ registers payment
    │   (seller)  │
    └──────┬──────┘
           │
           ▼
    ┌─────────────────┐
    │ PENDING_APPROVAL│  Payment recorded but NOT applied
    │                 │  Debt NOT reduced yet
    └────────┬────────┘
             │
     ┌───────┴───────┐
     │               │
     ▼               ▼
┌─────────┐    ┌──────────┐
│APPROVED │    │ REJECTED │
│         │    │          │
└────┬────┘    └────┬─────┘
     │              │
     ▼              ▼
  Debt is        Payment is
  reduced        discarded
     │
     ▼
  Collection
  record created
  (commission for
   merchant)


    ┌─────────────┐
    │    USER     │ or ADMIN registers payment
    │             │
    └──────┬──────┘
           │
           ▼
    ┌─────────────┐
    │  APPROVED   │  Payment auto-approved
    │             │  Debt reduced immediately
    └─────────────┘
           │
           ▼
       Collection
       record created
```

---

## Module Structure

```
└── finance
    └── treasury
        └── receivables
            ├── application
            │   ├── customer_debt
            │   │   └── use_case
            │   │       ├── GetCustomerDebtUseCase.java
            │   │       └── SearchCustomerDebtUseCase.java
            │   ├── customer_invoices
            │   │   └── use_case
            │   │       └── GetCustomerInvoicesUseCase.java
            │   ├── dto
            │   │   └── response
            │   │       ├── CustomerDebtListResponse.java
            │   │       ├── CustomerDebtSummaryResponse.java
            │   │       ├── CustomerInvoiceDebtResponse.java
            │   │       ├── PendingPaymentResponse.java
            │   │       └── ReceivablesSummaryResponse.java
            │   ├── payment_history
            │   │   └── use_case
            │   │       └── GetPaymentHistoryUseCase.java
            │   ├── ports
            │   │   ├── in
            │   │   │   ├── AccountsReceivableReportQuery.java
            │   │   │   └── AccountsReceivableReportSnapshot.java
            │   │   └── out
            │   │       ├── model
            │   │       │   └── AccountsReceivableRow.java
            │   │       ├── ReceivablesCustomerProfileGateway.java
            │   │       └── ReceivablesQueryGateway.java
            │   ├── services
            │   │   └── AccountsReceivableReportQueryService.java
            │   └── summary
            │       └── use_case
            │           └── GetReceivablesSummaryUseCase.java
            ├── infrastructure
            │   ├── config
            │   │   ├── AccountsReceivableReportQueryConfig.java
            │   │   └── ReceivablesConfig.java
            │   ├── integration
            │   │   └── LocalReceivablesCustomerProfileGatewayAdapter.java
            │   ├── persistence
            │   │   ├── adapters
            │   │   │   └── ReceivablesRepositoryAdapter.java
            │   │   └── repositories
            │   │       └── ReceivablesJpaRepository.java
            │   └── web
            │       └── ReceivablesController.java
            └── reporting
                ├── application
                │   ├── dto
                │   │   └── model
                │   │       ├── AccountsReceivableReportData.java
                │   │       └── CollectionsReportData.java
                │   ├── generate_accounts_receivable_report_pdf
                │   │   ├── services
                │   │   │   └── GenerateAccountsReceivableReportPdfUseCaseImpl.java
                │   │   └── use_case
                │   │       └── GenerateAccountsReceivableReportPdfUseCase.java
                │   ├── generate_collections_report_pdf
                │   │   ├── services
                │   │   │   └── GenerateCollectionsReportPdfUseCaseImpl.java
                │   │   └── use_case
                │   │       └── GenerateCollectionsReportPdfUseCase.java
                │   └── ports
                │       └── out
                │           ├── AccountsReceivableQueryGateway.java
                │           ├── AccountsReceivableReportPdfPort.java
                │           └── CollectionsReportPdfPort.java
                └── infrastructure
                    ├── config
                    │   └── ReceivablesReportingConfig.java
                    ├── integration
                    │   └── ReceivablesReportQueryAdapter.java
                    ├── pdf
                    │   ├── AccountsReceivableReportPdfGenerator.java
                    │   └── CollectionsReportPdfGenerator.java
                    └── web
                        └── ReceivablesReportsController.java
```

---

## Domain Model

### Entities

#### PendingPayment (New Entity for Validation Workflow)
**Domain Entity**: `com.lexand.lexand_api.receivables.domain.entities.PendingPayment`

**Description**: Represents a payment registered by a merchant that requires validation from a USER/ADMIN before being applied to the invoice.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `PendingPaymentId` (UUID) | Unique identifier | Required, immutable |
| `invoiceId` | `InvoiceId` (UUID) | Target invoice | Required |
| `invoiceNumber` | `String` | Invoice number snapshot | Required |
| `customerId` | `UUID` | Customer reference | Required |
| `customerName` | `String` | Customer name snapshot | Required |
| `merchantId` | `UUID` | Merchant who registered | Required |
| `merchantName` | `String` | Merchant name snapshot | Required |
| `amount` | `BigDecimal` | Payment amount | Required, > 0, <= invoice balance |
| `paymentDate` | `LocalDate` | Date of payment | Required |
| `paymentMethod` | `PaymentMethod` | Payment method | Required |
| `reference` | `String` | Payment reference | Optional, max 100 chars |
| `proofFileUrl` | `String` | URL of stored payment proof file | Required for MERCHANT, optional for USER/ADMIN |
| `status` | `PaymentValidationStatus` | Validation status | Required |
| `registeredBy` | `UUID` | Who registered (merchant) | Required |
| `registeredAt` | `LocalDateTime` | Registration timestamp | Auto-generated |
| `validatedBy` | `UUID` | Who validated (user/admin) | Null until validated |
| `validatedAt` | `LocalDateTime` | Validation timestamp | Null until validated |
| `rejectionReason` | `String` | Reason if rejected | Null unless rejected |

**Business Rules**:
1. Only created when MERCHANT registers a payment
2. USER/ADMIN payments bypass this - go directly to invoice
3. Status transitions: `PENDING_APPROVAL` → `APPROVED` or `REJECTED`
4. Once APPROVED: Creates real InvoicePayment + Collection
5. Once REJECTED: Record kept for audit, no effect on invoice
6. Cannot be modified after validation
7. MERCHANT payments MUST include a proof file (image or PDF)
8. USER/ADMIN payments may optionally include a proof file

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `PendingPaymentId` | UUID | Payment identifier | Non-null UUID |
| `PaymentValidationStatus` | Enum | Validation state | PENDING_APPROVAL, APPROVED, REJECTED |

---

### Enums

#### PaymentValidationStatus
```java
public enum PaymentValidationStatus {
    PENDING_APPROVAL,  // Waiting for USER/ADMIN validation
    APPROVED,          // Validated and applied to invoice
    REJECTED           // Rejected, not applied
}
```

---

## API Endpoints

### Global Debt Overview

#### 1. Get Global Debt Summary
**Endpoint**: `GET /api/receivables/summary`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN

**Description**: Returns the total debt across all customers.

**Response** (200 OK) - `GlobalDebtSummaryResponse`:
```json
{
  "totalDebt": 125750.00,
  "totalCustomersWithDebt": 45,
  "totalInvoicesWithDebt": 128,
  "debtByStatus": {
    "DELIVERED": 85000.00,
    "PARTIALLY_PAID": 40750.00
  },
  "calculatedAt": "2025-01-15T10:30:00"
}
```

---

### Customer Debt Management

#### 2. Search Customers with Debt
**Endpoint**: `GET /api/receivables/customers`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: See all customers with debt
- MERCHANT: See only customers from their invoices

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `search` | String | Search by customer name or RIF |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |
| `sort` | String | Sort field (default: totalDebt,desc) |

**Response** (200 OK) - Paginated `List<CustomerDebtListResponse>`:
```json
{
  "content": [
    {
      "customerId": "customer-001-uuid",
      "customerName": "ABC Company S.A.",
      "customerRif": "J-12345678-9",
      "totalDebt": 5250.00,
      "invoicesWithDebt": 3,
      "oldestInvoiceDate": "2025-01-02",
      "pendingValidationAmount": 2000.00,
      "pendingValidationCount": 1,
      "availableDebt": 3250.00
    },
    {
      "customerId": "customer-002-uuid",
      "customerName": "XYZ Corp",
      "customerRif": "J-98765432-1",
      "totalDebt": 3500.00,
      "invoicesWithDebt": 2,
      "oldestInvoiceDate": "2025-01-05",
      "pendingValidationAmount": 0.00,
      "pendingValidationCount": 0,
      "availableDebt": 3500.00
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

#### 3. Get Customer Debt Detail
**Endpoint**: `GET /api/receivables/customers/{customerId}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: View any customer
- MERCHANT: Only if has invoices with this customer

**Path Parameters**:
- `customerId` (UUID) - Customer unique identifier

**Response** (200 OK) - `CustomerDebtSummaryResponse`:
```json
{
  "customerId": "customer-001-uuid",
  "customerName": "ABC Company S.A.",
  "customerRif": "J-12345678-9",
  "customerPhone": "+58-212-1234567",
  "customerEmail": "contacto@abc.com",
  "totalDebt": 5250.00,
  "invoicesWithDebt": 3,
  "pendingPaymentsCount": 1,
  "pendingPaymentsAmount": 500.00,
  "debtBreakdown": {
    "DELIVERED": 3750.00,
    "PARTIALLY_PAID": 1500.00
  }
}
```

---

#### 4. List Customer Invoices with Debt
**Endpoint**: `GET /api/receivables/customers/{customerId}/invoices`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: View any customer's invoices
- MERCHANT: Only their associated invoices for this customer

**Path Parameters**:
- `customerId` (UUID) - Customer unique identifier

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `status` | String | DELIVERED,PARTIALLY_PAID | Filter by invoice status |
| `page` | Integer | 0 | Page number |
| `size` | Integer | 20 | Page size |
| `sort` | String | invoiceNumber,asc | Sort field |

**Response** (200 OK) - Paginated `List<CustomerInvoiceDebtResponse>`:
```json
{
  "content": [
    {
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "status": "PARTIALLY_PAID",
      "invoiceDate": "2025-01-02",
      "subtotal": 2000.00,
      "totalPaid": 500.00,
      "balance": 1500.00,
      "pendingPayment": {
        "id": "pending-001-uuid",
        "amount": 500.00,
        "status": "PENDING_APPROVAL",
        "registeredAt": "2025-01-14T10:00:00"
      },
      "merchantId": "merchant-001-uuid",
      "merchantName": "Juan Perez",
      "lineCount": 3
    },
    {
      "invoiceId": "invoice-005-uuid",
      "invoiceNumber": "INV-2025-00005",
      "status": "DELIVERED",
      "invoiceDate": "2025-01-05",
      "subtotal": 1750.00,
      "totalPaid": 0.00,
      "balance": 1750.00,
      "pendingPayment": null,
      "merchantId": "merchant-001-uuid",
      "merchantName": "Juan Perez",
      "lineCount": 2
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### Payment Operations

#### 5. Pay Single Invoice
**Endpoint**: `POST /api/receivables/invoices/{invoiceId}/pay`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT

**Description**: Registers a payment for a single invoice.
- **USER/ADMIN**: Payment is auto-approved and applied immediately. Proof file is optional.
- **MERCHANT**: Payment goes to PENDING_APPROVAL status. **Proof file is required.**

**Path Parameters**:
- `invoiceId` (UUID) - Invoice unique identifier

**Request**: `multipart/form-data`

| Part | Type | Description |
|------|------|-------------|
| `data` | JSON (`PayInvoiceRequest`) | Payment data as JSON part |
| `proofFile` | File (image or PDF) | Payment proof. **Required for MERCHANT**, optional for USER/ADMIN |

**Accepted file types**: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, `application/pdf`

**`data` part** (`PayInvoiceRequest`):
```json
{
  "amount": 750.00,
  "paymentDate": "2025-01-15",
  "paymentMethod": "TRANSFER",
  "reference": "REF-123456"
}
```

**Response** (201 Created) - `PaymentResultResponse`:

For USER/ADMIN (auto-approved):
```json
{
  "paymentId": "payment-001-uuid",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "amount": 750.00,
  "status": "APPROVED",
  "newInvoiceStatus": "PARTIALLY_PAID",
  "newInvoiceBalance": 750.00,
  "requiresValidation": false,
  "collectionGenerated": true,
  "commissionAmount": 112.50
}
```

For MERCHANT (pending validation):
```json
{
  "paymentId": "pending-001-uuid",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "amount": 750.00,
  "status": "PENDING_APPROVAL",
  "newInvoiceStatus": "PARTIALLY_PAID",
  "newInvoiceBalance": 1500.00,
  "requiresValidation": true,
  "collectionGenerated": false,
  "commissionAmount": null,
  "message": "Payment registered. Awaiting validation from administrator."
}
```

**Business Logic**:
1. Validate invoice exists and has balance > 0
2. Validate amount <= invoice balance
3. If caller is MERCHANT:
   - Create PendingPayment with status PENDING_APPROVAL
   - DO NOT update invoice balance yet
4. If caller is USER/ADMIN:
   - Create InvoicePayment directly
   - Update invoice balance and status
   - Create Collection record (if merchant exists)

**Errors**:
- `400 Bad Request`: Invalid request data
- `400 Bad Request`: Proof file is required for merchant payments
- `401 Unauthorized`: Missing or invalid JWT
- `403 Forbidden`: MERCHANT trying to pay another merchant's invoice
- `404 Not Found`: Invoice not found
- `409 Conflict`: Invoice already fully paid
- `422 Unprocessable Entity`: Amount exceeds balance

---

#### 6. Pay All Customer Debt
**Endpoint**: `POST /api/receivables/customers/{customerId}/pay-all`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT

**Description**: Pays all outstanding debt for a customer in one operation.
- **MERCHANT**: Only pays invoices associated with them. **Proof file is required.**
- **USER/ADMIN**: Pays all invoices. Proof file is optional.

**Path Parameters**:
- `customerId` (UUID) - Customer unique identifier

**Request**: `multipart/form-data`

| Part | Type | Description |
|------|------|-------------|
| `data` | JSON (`PayCustomerDebtRequest`) | Payment data as JSON part |
| `proofFile` | File (image or PDF) | Payment proof. **Required for MERCHANT**, optional for USER/ADMIN |

**Accepted file types**: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, `application/pdf`

**`data` part** (`PayCustomerDebtRequest`):
```json
{
  "paymentDate": "2025-01-15",
  "paymentMethod": "TRANSFER",
  "reference": "BULK-PAY-123"
}
```

**Response** (201 Created) - `PaymentResultResponse`:
```json
{
  "customerId": "customer-001-uuid",
  "customerName": "ABC Company S.A.",
  "totalAmountPaid": 5250.00,
  "invoicesPaid": 3,
  "status": "APPROVED",
  "requiresValidation": false,
  "payments": [
    {
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "amount": 1500.00,
      "newStatus": "PAID"
    },
    {
      "invoiceId": "invoice-005-uuid",
      "invoiceNumber": "INV-2025-00005",
      "amount": 1750.00,
      "newStatus": "PAID"
    },
    {
      "invoiceId": "invoice-008-uuid",
      "invoiceNumber": "INV-2025-00008",
      "amount": 2000.00,
      "newStatus": "PAID"
    }
  ]
}
```

---

#### 7. Pay Partial Customer Debt
**Endpoint**: `POST /api/receivables/customers/{customerId}/pay-partial`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT

**Description**: Pays a specific amount that is distributed across invoices (oldest first).

**Path Parameters**:
- `customerId` (UUID) - Customer unique identifier

**Request**: `multipart/form-data`

| Part | Type | Description |
|------|------|-------------|
| `data` | JSON (`PayPartialDebtRequest`) | Payment data as JSON part |
| `proofFile` | File (image or PDF) | Payment proof. **Required for MERCHANT**, optional for USER/ADMIN |

**Accepted file types**: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, `application/pdf`

**`data` part** (`PayPartialDebtRequest`):
```json
{
  "amount": 3000.00,
  "paymentDate": "2025-01-15",
  "paymentMethod": "CASH",
  "reference": "PARTIAL-PAY-001"
}
```

**Response** (201 Created) - `PaymentResultResponse`:
```json
{
  "customerId": "customer-001-uuid",
  "customerName": "ABC Company S.A.",
  "totalAmountPaid": 3000.00,
  "remainingDebt": 2250.00,
  "invoicesAffected": 2,
  "status": "APPROVED",
  "requiresValidation": false,
  "payments": [
    {
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "amount": 1500.00,
      "newStatus": "PAID"
    },
    {
      "invoiceId": "invoice-005-uuid",
      "invoiceNumber": "INV-2025-00005",
      "amount": 1500.00,
      "newStatus": "PARTIALLY_PAID"
    }
  ]
}
```

**Distribution Logic**:
1. Get all invoices with debt ordered by invoice number (oldest first)
2. Apply payment to each invoice until amount is exhausted
3. If amount covers full invoice balance, mark as PAID
4. If amount is less than balance, apply partial payment

---

### Payment Validation (USER/ADMIN Only)

#### 8. List Pending Payments
**Endpoint**: `GET /api/receivables/payments/pending`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN only

**Description**: Lists all payments pending validation from merchants.

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `merchantId` | UUID | Filter by merchant |
| `customerId` | UUID | Filter by customer |
| `fromDate` | Date | Filter from registration date |
| `toDate` | Date | Filter to registration date |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

**Response** (200 OK) - Paginated `List<PendingPaymentResponse>`:
```json
{
  "content": [
    {
      "id": "pending-001-uuid",
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "customerId": "customer-001-uuid",
      "customerName": "ABC Company S.A.",
      "merchantId": "merchant-001-uuid",
      "merchantName": "Juan Perez",
      "amount": 500.00,
      "paymentDate": "2025-01-14",
      "paymentMethod": "CASH",
      "reference": "CASH-001",
      "status": "PENDING_APPROVAL",
      "registeredAt": "2025-01-14T10:00:00",
      "invoiceBalance": 1500.00,
      "invoiceSubtotal": 2000.00
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

#### 9. Approve Payment
**Endpoint**: `PUT /api/receivables/payments/{paymentId}/approve`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN only

**Description**: Approves a pending payment and applies it to the invoice.

**Path Parameters**:
- `paymentId` (UUID) - Pending payment identifier

**Response** (200 OK) - `PaymentResultResponse`:
```json
{
  "paymentId": "pending-001-uuid",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "amount": 500.00,
  "status": "APPROVED",
  "validatedBy": "user-001-uuid",
  "validatedAt": "2025-01-15T11:00:00",
  "newInvoiceStatus": "PARTIALLY_PAID",
  "newInvoiceBalance": 1000.00,
  "collectionGenerated": true,
  "commissionAmount": 75.00
}
```

**Business Logic**:
1. Validate pending payment exists and is PENDING_APPROVAL
2. Create real InvoicePayment from pending payment data
3. Update invoice balance and status
4. Create Collection record (commission for merchant)
5. Update pending payment status to APPROVED
6. Record who validated and when

---

#### 10. Reject Payment
**Endpoint**: `PUT /api/receivables/payments/{paymentId}/reject`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN only

**Description**: Rejects a pending payment. No changes to invoice.

**Path Parameters**:
- `paymentId` (UUID) - Pending payment identifier

**Request Body** (`ValidatePaymentRequest`):
```json
{
  "reason": "Payment reference not found in bank statement"
}
```

**Response** (200 OK) - `PaymentResultResponse`:
```json
{
  "paymentId": "pending-001-uuid",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "amount": 500.00,
  "status": "REJECTED",
  "validatedBy": "user-001-uuid",
  "validatedAt": "2025-01-15T11:00:00",
  "rejectionReason": "Payment reference not found in bank statement",
  "newInvoiceStatus": "PARTIALLY_PAID",
  "newInvoiceBalance": 1500.00,
  "collectionGenerated": false
}
```

**Business Logic**:
1. Validate pending payment exists and is PENDING_APPROVAL
2. Update status to REJECTED
3. Record reason, who rejected, and when
4. DO NOT modify invoice

---

#### 11. List My Payments (MERCHANT)
**Endpoint**: `GET /api/receivables/payments/my`

**Authentication**: Required (JWT Bearer Token)

**Roles**: MERCHANT only

**Description**: Returns all payments registered by the authenticated merchant, regardless of their validation status (PENDING_APPROVAL, APPROVED, REJECTED).

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | Integer | 0 | Page number |
| `size` | Integer | 20 | Page size |
| `sort` | String | registeredAt,desc | Sort field |

**Response** (200 OK) - Paginated `Page<PendingPaymentResponse>`:
```json
{
  "content": [
    {
      "id": "pending-001-uuid",
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "customerId": "customer-001-uuid",
      "customerName": "ABC Company S.A.",
      "merchantId": "merchant-001-uuid",
      "merchantName": "Juan Perez",
      "amount": 500.00,
      "paymentDate": "2025-01-14",
      "paymentMethod": "TRANSFER",
      "reference": "REF-001",
      "proofFileUrl": "payment-proofs/abc123-uuid.pdf",
      "status": "PENDING_APPROVAL",
      "registeredAt": "2025-01-14T10:00:00",
      "registeredBy": "merchant-001-uuid",
      "invoiceBalance": 1500.00,
      "invoiceSubtotal": 2000.00
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT
- `403 Forbidden`: Caller does not have MERCHANT role

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### PayInvoiceRequest
```java
public record PayInvoiceRequest(
    @NotNull @Positive BigDecimal amount,
    @NotNull LocalDate paymentDate,
    @NotNull PaymentMethod paymentMethod,
    @Size(max = 100) String reference
)
```

#### PayCustomerDebtRequest
```java
public record PayCustomerDebtRequest(
    @NotNull LocalDate paymentDate,
    @NotNull PaymentMethod paymentMethod,
    @Size(max = 100) String reference
)
```

#### PayPartialDebtRequest
```java
public record PayPartialDebtRequest(
    @NotNull @Positive BigDecimal amount,
    @NotNull LocalDate paymentDate,
    @NotNull PaymentMethod paymentMethod,
    @Size(max = 100) String reference
)
```

#### ValidatePaymentRequest
```java
public record ValidatePaymentRequest(
    @Size(max = 500) String reason  // Required for rejection, optional for approval
)
```

---

### Response DTOs

#### GlobalDebtSummaryResponse
```java
public record GlobalDebtSummaryResponse(
    BigDecimal totalDebt,
    int totalCustomersWithDebt,
    int totalInvoicesWithDebt,
    Map<String, BigDecimal> debtByStatus,
    LocalDateTime calculatedAt
)
```

#### CustomerDebtListResponse
```java
public record CustomerDebtListResponse(
    String customerId,
    String customerName,
    String customerRif,
    BigDecimal totalDebt,
    Long invoicesWithDebt,
    LocalDate oldestInvoiceDate,
    BigDecimal pendingValidationAmount,
    Long pendingValidationCount,
    BigDecimal availableDebt
)
```

#### CustomerDebtSummaryResponse
```java
public record CustomerDebtSummaryResponse(
    String customerId,
    String customerName,
    String customerRif,
    String customerPhone,
    String customerEmail,
    BigDecimal totalDebt,
    int invoicesWithDebt,
    int pendingPaymentsCount,
    BigDecimal pendingPaymentsAmount,
    Map<String, BigDecimal> debtBreakdown
)
```

#### CustomerInvoiceDebtResponse
```java
public record CustomerInvoiceDebtResponse(
    String invoiceId,
    String invoiceNumber,
    String status,
    LocalDate invoiceDate,
    BigDecimal subtotal,
    BigDecimal totalPaid,
    BigDecimal balance,
    PendingPaymentSummary pendingPayment,  // null if none
    String merchantId,
    String merchantName,
    int lineCount
)

public record PendingPaymentSummary(
    String id,
    BigDecimal amount,
    String status,
    LocalDateTime registeredAt
)
```

#### PendingPaymentResponse
```java
public record PendingPaymentResponse(
    String id,
    String invoiceId,
    String invoiceNumber,
    String customerId,
    String customerName,
    String merchantId,
    String merchantName,
    BigDecimal amount,
    LocalDate paymentDate,
    String paymentMethod,
    String reference,
    String proofFileUrl,        // URL of stored payment proof file (null if not provided)
    String status,
    LocalDateTime registeredAt,
    String registeredBy,
    BigDecimal invoiceBalance,
    BigDecimal invoiceSubtotal
)
```

#### PaymentResultResponse
```java
public record PaymentResultResponse(
    String paymentId,
    String invoiceId,
    String invoiceNumber,
    BigDecimal amount,
    String status,
    String newInvoiceStatus,
    BigDecimal newInvoiceBalance,
    boolean requiresValidation,
    boolean collectionGenerated,
    BigDecimal commissionAmount,
    String message
)
```

---

## Business Rules Summary

### Debt Calculation
1. Debt = Invoice Balance (subtotal - totalPaid)
2. Only DELIVERED and PARTIALLY_PAID invoices count as debt
3. PAID and CANCELLED invoices have no debt
4. Customer total debt = Sum of all invoice balances
5. **Invoices with `hasPendingPayment = true` are excluded from individual debt totals and invoice availability** — however, customers still appear in the customer debt list if they have at least one invoice with `hasPendingPayment = false` and balance > 0 (`availableDebt > 0`). The response includes `pendingValidationAmount`, `pendingValidationCount`, and `availableDebt` to show the breakdown.

### Payment Registration
1. Amount must be > 0 and <= invoice balance
2. Cannot pay more than what customer owes
3. USER/ADMIN payments are auto-approved
4. MERCHANT payments require validation
5. MERCHANT payments MUST include a proof file (image or PDF)
6. USER/ADMIN payments may optionally include a proof file
7. Proof files are stored in the `payment-proofs/` folder via FileStorageService
8. When a MERCHANT payment is registered, `invoice.hasPendingPayment` is set to `true` — the invoice balance is excluded from `availableDebt` but the customer remains visible in the debt list if they have at least one other invoice with `hasPendingPayment = false` and balance > 0
9. When the payment is approved or rejected, `invoice.hasPendingPayment` is set back to `false` and the invoice is included again in `availableDebt` calculations (if it still has balance)

### Payment Validation
1. Only USER/ADMIN can approve/reject payments
2. Approved payments create InvoicePayment + Collection
3. Rejected payments are recorded but don't affect invoice
4. Once validated, cannot be changed

### Role-Based Access
1. USER/ADMIN: See all customers and invoices
2. MERCHANT: Only see invoices where they are the merchant
3. MERCHANT: Can only pay invoices associated with them
4. MERCHANT: Payments require USER validation
5. MERCHANT: Can view all their own payments (all statuses) via `/payments/my`

### Commission Generation
1. Commission calculated when payment is APPROVED
2. Commission = amount × (merchant.commissionPercentage / 100)
3. Collection record created linking payment to commission
4. Creates "Account Payable" to merchant (see PAYABLES module)

### hasPendingPayment Flag (Invoice-level)
| Event | `hasPendingPayment` value | Effect on debt queries |
|-------|---------------------------|------------------------|
| Invoice created | `false` | Visible in debt lists |
| MERCHANT registers payment | `true` | **Invoice balance excluded from `availableDebt`; customer hidden only if ALL their invoices are in validation** |
| ADMIN approves payment | `false` | Visible again (if balance > 0) |
| ADMIN rejects payment | `false` | Visible again (balance unchanged) |

**Query behavior** in `JpaReceivablesQueryRepository` regarding `hasPendingPayment`:

- **`findCustomersWithDebt`** and **`findCustomersWithDebtByMerchant`**: use `CASE WHEN i.hasPendingPayment = false THEN i.balance ELSE 0 END` to split debt into `availableDebt` and `pendingValidationAmount`, and filter with `HAVING SUM(...availableDebt...) > 0` so only customers with at least one available invoice appear.
- **`countCustomersWithDebtTotal`**: uses `AND i.hasPendingPayment = false` in WHERE clause (equivalent to the HAVING filter above, more efficient for COUNT DISTINCT).
- **The remaining 8 queries** use `AND i.hasPendingPayment = false` directly in WHERE:
  - `calculateTotalDebt`
  - `countCustomersWithDebt`
  - `countInvoicesWithDebt`
  - `calculateCustomerDebt`
  - `calculateCustomerDebtByMerchant`
  - `countCustomerInvoicesWithDebt`
  - `countCustomerInvoicesWithDebtByMerchant`
  - `findInvoicesWithDebtByCustomerAsList`

**Exception — customer invoice listing query does NOT filter by `hasPendingPayment`**:
- `findInvoicesWithDebtByCustomerAndMerchant` — invoices with `hasPendingPayment = true` **do appear** in the customer invoice listing (e.g. `GET /api/receivables/customers/{customerId}/invoices`) so the customer can see the validation badge on their invoice

---

### pending_payments Table
```sql
CREATE TABLE pending_payments (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    invoice_number VARCHAR(20) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    customer_name VARCHAR(200) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    merchant_name VARCHAR(200) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    reference VARCHAR(100),
    proof_file_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL',
    registered_by VARCHAR(36) NOT NULL,
    registered_at TIMESTAMP NOT NULL,
    validated_by VARCHAR(36),
    validated_at TIMESTAMP,
    rejection_reason VARCHAR(500),
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_registered_at (registered_at),
    
    CONSTRAINT chk_status CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);
```

---

## Integration with Other Modules

### Invoices Module
- Reads invoice data (balance, status)
- Creates InvoicePayment when payment approved
- Updates invoice status (PARTIALLY_PAID, PAID)

### Collections Module
- Creates Collection record when payment approved
- Links payment to commission calculation

### Payables Module (New)
- Creates "Account Payable" entry for merchant commission
- Triggered when Collection is created

### Merchants Module
- Validates merchant can access invoice
- Gets commission percentage for calculation

### Customers Module
- Gets customer details for display
- Groups invoices by customer

---

## Notes for Developers

### Key Design Decisions
1. **Validation Workflow**: Merchants cannot self-approve payments
2. **Two-Phase Payment**: PendingPayment → InvoicePayment (upon approval)
3. **Audit Trail**: All pending payments kept even after rejection
4. **Customer-Centric**: Primary view is by customer, not by invoice
5. **Oldest First**: Partial debt payments applied to oldest invoices first

### API Design Rules
- **DO**: Validate merchant can only access their invoices
- **DO**: Auto-approve USER/ADMIN payments
- **DO**: Create PendingPayment for MERCHANT payments
- **DO**: Generate Collection when payment approved
- **DO**: Require proof file for MERCHANT payments (`multipart/form-data`)
- **DO**: Accept optional proof file for USER/ADMIN payments
- **DO**: Store proof files in `payment-proofs/` folder via `FileStorageService`
- **DON'T**: Allow MERCHANT to approve own payments
- **DON'T**: Allow payment exceeding debt
- **DON'T**: Delete pending payments (soft delete/status change only)

---

## Testing

### Unit Test Coverage

The RECEIVABLES module has **comprehensive unit test coverage** for all use cases following TDD best practices.

#### Test Statistics
- **Total Tests**: 149 tests (131 unit + 18 integration)
- **Test Framework**: JUnit 5 + Mockito + AssertJ + Spring Boot Test
- **Coverage**: 100% of use cases
- **Execution Time**: ~1 second (unit tests)

#### Use Case Tests

##### 1. PaySingleInvoiceUseCaseUnitTest (10 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/PaySingleInvoiceUseCaseUnitTest.java`

**Coverage**:
- ✅ User payment flow (direct processing)
- ✅ Merchant payment flow (creates PENDING_APPROVAL)
- ✅ Full payment vs. partial payment
- ✅ Collection generation on approval
- ✅ Validation: invoice not found
- ✅ Validation: invoice not delivered
- ✅ Validation: payment exceeds debt
- ✅ Validation: merchant access control

**Test Groups**:
- `UserPaymentTests` (5 tests): Direct payment processing for users/admins
- `MerchantPaymentTests` (1 test): PENDING_APPROVAL workflow for merchants
- `ValidationTests` (4 tests): Error handling and business rule validation

---

##### 2. ApprovePaymentUseCaseUnitTest (8 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/ApprovePaymentUseCaseUnitTest.java`

**Coverage**:
- ✅ Payment approval workflow
- ✅ Debt reduction on approval
- ✅ Collection record generation
- ✅ Status change tracking
- ✅ Validation: payment not found
- ✅ Validation: payment not pending
- ✅ Validation: invoice state validation

**Test Groups**:
- `ApprovalTests` (4 tests): Approval workflow and debt reduction
- `ValidationTests` (4 tests): Payment status and existence validation

---

##### 3. RejectPaymentUseCaseUnitTest (8 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/RejectPaymentUseCaseUnitTest.java`

**Coverage**:
- ✅ Payment rejection workflow
- ✅ Rejection reason recording
- ✅ Status change to REJECTED
- ✅ Debt remains unchanged
- ✅ Validation: payment not found
- ✅ Validation: payment not pending
- ✅ Validation: rejection reason required

**Test Groups**:
- `RejectionTests` (4 tests): Rejection workflow and reason tracking
- `ValidationTests` (4 tests): Payment status and rejection reason validation

---

##### 4. GetCustomerDebtUseCaseUnitTest (7 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/GetCustomerDebtUseCaseUnitTest.java`

**Coverage**:
- ✅ Customer debt query
- ✅ Merchant filter (role-based access)
- ✅ Invoice breakdown by status
- ✅ Pending payment aggregation
- ✅ No debt scenarios
- ✅ Validation: customer not found
- ✅ Validation: invalid customer ID

**Test Groups**:
- `QueryTests` (5 tests): Debt calculation and breakdown
- `ValidationTests` (2 tests): Customer validation

---

##### 5. ListPendingPaymentsUseCaseUnitTest (11 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/ListPendingPaymentsUseCaseUnitTest.java`

**Coverage**:
- ✅ List all pending payments
- ✅ Filter by merchant
- ✅ Filter by customer
- ✅ Pagination support
- ✅ Empty result handling

**Test Groups**:
- `ListTests` (11 tests): Filtering and pagination

---

##### 6. SearchCustomersWithDebtUseCaseUnitTest (7 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/SearchCustomersWithDebtUseCaseUnitTest.java`

**Coverage**:
- ✅ Search by customer name
- ✅ Search by customer RIF
- ✅ Merchant-specific search (role-based)
- ✅ Global search (admin)
- ✅ Pagination support
- ✅ No results handling
- ✅ Response content validation

**Test Groups**:
- `SearchTests` (7 tests): Search functionality and filtering

---

##### 7. PayAllCustomerDebtUseCaseUnitTest (7 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/PayAllCustomerDebtUseCaseUnitTest.java`

**Coverage**:
- ✅ Pay entire customer debt (all invoices)
- ✅ User payment flow (direct processing)
- ✅ Merchant payment flow (PENDING_APPROVAL)
- ✅ Multiple invoice payment
- ✅ Collection generation for each invoice
- ✅ Validation: customer not found
- ✅ Validation: no debt to pay

**Test Groups**:
- `UserPaymentTests` (3 tests): Direct multi-invoice payment
- `MerchantPaymentTests` (2 tests): PENDING_APPROVAL workflow
- `ValidationTests` (2 tests): Customer and debt validation

---

##### 8. PayPartialCustomerDebtUseCaseUnitTest (10 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/PayPartialCustomerDebtUseCaseUnitTest.java`

**Coverage**:
- ✅ Partial debt payment (oldest first)
- ✅ User payment flow
- ✅ Merchant payment flow
- ✅ Multiple invoice allocation
- ✅ Exact invoice amount payment
- ✅ Split payment across invoices
- ✅ Validation: amount exceeds debt
- ✅ Validation: invalid amount (zero/negative)
- ✅ Response breakdown validation

**Test Groups**:
- `UserPaymentTests` (5 tests): Partial payment distribution logic
- `MerchantPaymentTests` (2 tests): PENDING_APPROVAL workflow
- `ValidationTests` (3 tests): Amount and debt validation

---

##### 9. GetGlobalDebtSummaryUseCaseUnitTest (7 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/GetGlobalDebtSummaryUseCaseUnitTest.java`

**Coverage**:
- ✅ Global debt summary calculation
- ✅ Total debt aggregation
- ✅ Customer count with debt
- ✅ Invoice count with debt
- ✅ Merchant-specific summary (role-based)
- ✅ Zero debt scenarios
- ✅ Response content validation

**Tests**: 7 unit tests covering summary calculations and role-based filtering

---

##### 10. ListCustomerInvoicesWithDebtUseCaseUnitTest (10 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/application/usecases/ListCustomerInvoicesWithDebtUseCaseUnitTest.java`

**Coverage**:
- ✅ List customer invoices with debt
- ✅ Filter by invoice status
- ✅ Filter by merchant (role-based)
- ✅ Pagination support
- ✅ Response content validation
- ✅ Pending payment info inclusion
- ✅ Merchant info inclusion
- ✅ Empty result handling

**Test Groups**:
- `GeneralQueryTests` (3 tests): Basic query and filtering
- `MerchantFilterTests` (2 tests): Role-based access control
- `PaginationTests` (2 tests): Pagination behavior
- `ResponseContentTests` (3 tests): Response structure validation

---

##### 11. ListMerchantPaymentsUseCase (covered by integration tests)
**Coverage**: Covered by `ReceivablesControllerIntegrationTest`
- ✅ Merchant retrieves own payments
- ✅ Returns all statuses (PENDING_APPROVAL, APPROVED, REJECTED)
- ✅ Pagination support
- ✅ MERCHANT role enforcement (403 for other roles)

---

#### Domain Entity & Value Object Tests

##### PendingPaymentUnitTest (31 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/domain/entities/PendingPaymentUnitTest.java`

**Coverage**:
- ✅ Entity creation via `create()` and `reconstitute()`
- ✅ `proofFileUrl` field (required for merchant, optional for user/admin)
- ✅ All status transition validations (PENDING → APPROVED / REJECTED)
- ✅ Immutability after validation
- ✅ Business invariants enforcement

##### PendingPaymentIdUnitTest (10 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/domain/valueobjects/PendingPaymentIdUnitTest.java`

##### PaymentValidationStatusUnitTest (5 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/domain/valueobjects/PaymentValidationStatusUnitTest.java`

---

#### Integration Tests

##### ReceivablesControllerIntegrationTest (18 tests)
**File**: `src/test/java/com/lexand/lexand_api/receivables/infrastructure/web/ReceivablesControllerIntegrationTest.java`

**Coverage**:
- ✅ Full payment flows (single, all, partial) end-to-end
- ✅ Multipart file upload validation
- ✅ Proof file required for MERCHANT, optional for USER/ADMIN
- ✅ `GET /payments/my` endpoint (MERCHANT self-view)
- ✅ Role-based access control
- ✅ Approval and rejection workflows

---

### Test Patterns Used

#### 1. Nested Test Classes (`@Nested`)
Tests are organized by functional area (e.g., `UserPaymentTests`, `ValidationTests`) for better readability and maintenance.

#### 2. Mockito for Dependencies
All external dependencies (repositories, mappers) are mocked to ensure true unit testing in isolation.

#### 3. Builder Pattern for Test Data
Helper methods like `createInvoice()`, `createPaymentRequest()` generate consistent test data.

#### 4. AssertJ for Assertions
Fluent assertions provide clear, readable test validations:
```java
assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
assertThat(result.invoicePayments()).hasSize(2);
```

#### 5. Descriptive Test Names
Convention: `should_DoSomething_When_Condition()`
```java
should_CreatePendingPayment_When_MerchantPays()
should_ThrowException_When_InvoiceNotFound()
```

---

### Running Tests

#### All RECEIVABLES Use Case Tests
```bash
docker-compose exec api ./mvnw test -Dtest="PaySingleInvoiceUseCaseUnitTest,ApprovePaymentUseCaseUnitTest,RejectPaymentUseCaseUnitTest,GetCustomerDebtUseCaseUnitTest,ListPendingPaymentsUseCaseUnitTest,SearchCustomersWithDebtUseCaseUnitTest,PayAllCustomerDebtUseCaseUnitTest,PayPartialCustomerDebtUseCaseUnitTest,GetGlobalDebtSummaryUseCaseUnitTest,ListCustomerInvoicesWithDebtUseCaseUnitTest,PendingPaymentUnitTest,PendingPaymentIdUnitTest,PaymentValidationStatusUnitTest,ReceivablesControllerIntegrationTest"
```

#### Single Use Case Test
```bash
docker-compose exec api ./mvnw test -Dtest="PaySingleInvoiceUseCaseUnitTest"
```

#### Coverage Report
```bash
docker-compose exec api ./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

### Key Testing Learnings

#### 1. PageImpl Total Elements Validation
Spring Data's `PageImpl` validates consistency between page number, page size, content size, and total elements. If inconsistent, it auto-corrects the total using:
```java
totalElements = (pageNumber * pageSize) + contentSize
```

**Solution**: Always ensure test data matches real pagination logic:
```java
// ✅ CORRECT: Page 1 (size 10) with 1 element → total should be 11
PageRequest.of(1, 10) + content(1 element) + total(11)

// ❌ WRONG: Page 1 (size 10) with 1 element → total 15 is inconsistent
PageRequest.of(1, 10) + content(1 element) + total(15) // Auto-corrects to 11
```

#### 2. Mockito Matcher Consistency
Use consistent matchers throughout tests. Mixing `eq(null)`, `isNull()`, `any()` can cause unexpected mock behavior.

**Best Practice**:
```java
// ✅ Use isNull() for null checks
when(repository.find(any(UUID.class), isNull(), any(Pageable.class)))

// ❌ Avoid eq(null)
when(repository.find(any(UUID.class), eq(null), any(Pageable.class)))
```

#### 3. Test Isolation with @BeforeEach
Each test should be independent. Use `@BeforeEach` to reset state if needed, but prefer immutable test data.

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **INVOICES** | Source of debt data, payment records |
| **COLLECTIONS** | Generated when payment approved |
| **PAYABLES** | Commission payable created for merchant |
| **MERCHANTS** | Role validation, commission percentage |
| **CUSTOMERS** | Customer data and grouping |

---

**Last Updated**: 2026-03-12

**Module Status**: ✅ Implemented & Tested (149 tests passing — 131 unit + 18 integration)
