# INVOICES Module

## Overview
The INVOICES module manages sales invoices (facturas) in the invoicing system. Invoices can be created directly (standalone) or generated from an existing Order. Each invoice contains invoice lines with product details, applies differential pricing, manages stock deduction, and tracks payments with due dates.

**Key Concepts**:
- **Customer Required**: Every invoice MUST have a customer associated
- **Merchant Optional**: Invoices MAY have a merchant associated (inherited from Order or set directly)
- **Two Creation Paths**: Can be created standalone OR converted from an Order
- **Delivery Required**: Invoice must be delivered (`DELIVERED` status) before payments can be registered
- **Differential Pricing**: Invoice lines apply differential pricing rules based on customer/product
- **Stock Management**: Stock is deducted when invoice is created (if products are from warehouse)
- **Invoice Dates**: Issue date defaults to today and can be edited before saving. The due date is calculated once as `issueDate + Customer.creditDays` and persisted in `invoices.due_date`.
- **Payment Tracking**: Supports partial and full payments
- **Receivables Integration**: Invoices with balance > 0 appear in Receivables (customer debt view)

---

## Invoice Status Flow

**Important**: `OVERDUE` is NOT a status. Use query filters to find overdue invoices (dueDate < today AND balance > 0).

| Status | Description | Can Receive Payment? | Can Be Cancelled? |
|--------|-------------|---------------------|-------------------|
| `PENDING` | Created, awaiting delivery | No | Yes |
| `DELIVERED` | Delivered to customer | Yes | No |
| `PARTIALLY_PAID` | Partially paid | Yes | No |
| `PAID` | Fully paid | No | No |
| `CANCELLED` | Cancelled | No | No |

---

## Invoice Creation Paths

### Path 1: From Order (Convert)
```
Order (PENDING) --> [convert-to-invoice] --> Invoice (PENDING)
```
- Inherits: customerId, merchantId, lines, notes
- Stock deducted at conversion time
- Order status changes to INVOICED

### Path 2: Direct Creation (Standalone)
```
[Create Invoice API] --> Invoice (PENDING)
```
- Requires: customerId, lines
- Optional: merchantId, dueDate, creditDays
- Stock deducted at creation time

---

## Invoice Flow

```
                           INVOICE LIFECYCLE
 
  CREATE (from Order or Direct)
            |
            v
      +-----------+
      |  PENDING  |  <-- Invoice created, awaiting delivery
      +-----------+
            |
            +------------------+
            |                  |
     [deliver invoice]   [cancel invoice]
            |                  |
            v                  v
      +-----------+      +-----------+
      | DELIVERED |      | CANCELLED |
      +-----------+      +-----------+
            |
     [register payment]
            |
            +------------------+
            |                  |
            v                  v
       +---------------+      +-----------+
       | PARTIALLY_PAID|      |   PAID    |  <-- Full payment at once
       +---------------+      +-----------+
            |                  |
     [more payments]           |
            |                  |
            v                  |
      +-----------+            |
      |   PAID    | <----------+
      +-----------+
            |
            | (If has merchant)
            v
      +-----------+
      | COLLECTION|  <-- Commission generated
      +-----------+
            |
            v
      +-----------+
      |  PAYABLE  |  <-- Commission owed to merchant
      +-----------+
```

**State Transitions:**
- `PENDING` → `DELIVERED` (via deliver) or `CANCELLED` (via cancel)
- `DELIVERED` → `PARTIALLY_PAID` (via partial payment) or `PAID` (via full payment)
- `PARTIALLY_PAID` → `PAID` (via remaining payment)

---

## Module Structure

```
└── finance
    └── billing
        └── invoices
            ├── application
            │   ├── cancel
            │   │   ├── request
            │   │   │   └── CancelInvoiceRequest.java
            │   │   └── use_case
            │   │       └── CancelInvoiceUseCase.java
            │   ├── confirm
            │   │   ├── request
            │   │   │   └── ConfirmInvoiceRequest.java
            │   │   └── use_case
            │   │       └── ConfirmInvoiceUseCase.java
            │   ├── create
            │   │   ├── from_order
            │   │   │   └── services
            │   │   │       └── OrderInvoiceCreationService.java
            │   │   ├── from_quote
            │   │   │   └── services
            │   │   │       └── QuoteInvoiceCreationService.java
            │   │   ├── request
            │   │   │   └── CreateInvoiceRequest.java
            │   │   └── use_case
            │   │       └── CreateInvoiceUseCase.java
            │   ├── delete
            │   │   └── use_case
            │   │       └── DeleteInvoiceUseCase.java
            │   ├── dto
            │   │   └── response
            │   │       ├── InvoiceDetailResponse.java
            │   │       └── InvoiceListResponse.java
            │   ├── duplicate
            │   │   ├── request
            │   │   │   └── DuplicateInvoiceRequest.java
            │   │   └── use_case
            │   │       └── DuplicateInvoiceUseCase.java
            │   ├── get
            │   │   └── use_case
            │   │       └── GetInvoiceByIdUseCase.java
            │   ├── issue
            │   │   ├── model
            │   │   │   └── InvoiceOrigin.java
            │   │   ├── request
            │   │   │   └── IssueInvoiceCommand.java
            │   │   ├── services
            │   │   │   ├── InvoicePaymentService.java
            │   │   │   └── IssueInvoiceService.java
            │   │   └── validation
            │   │       └── InvoiceIssueValidator.java
            │   ├── list
            │   │   └── use_case
            │   │       └── GetInvoicesUseCase.java
            │   ├── mappers
            │   │   └── InvoiceMapper.java
            │   ├── ports
            │   │   ├── in
            │   │   │   ├── InvoiceCreditLedgerEntry.java
            │   │   │   ├── InvoiceCreditLedgerQuery.java
            │   │   │   ├── InvoiceDocumentFilter.java
            │   │   │   ├── InvoiceDocumentQuery.java
            │   │   │   ├── InvoiceDocumentRow.java
            │   │   │   ├── InvoiceDocumentSnapshot.java
            │   │   │   ├── InvoiceNoteApi.java
            │   │   │   ├── InvoiceReturnAdjustments.java
            │   │   │   ├── InvoiceReturnCommand.java
            │   │   │   ├── OrderInvoiceCreationApi.java
            │   │   │   ├── ProductReferenceQuery.java
            │   │   │   ├── QuoteInvoiceCreationApi.java
            │   │   │   └── ReturnInvoiceQuery.java
            │   │   └── out
            │   │       ├── ActorIdentityGateway.java
            │   │       ├── CashRegisterGateway.java
            │   │       ├── CustomerBalanceGateway.java
            │   │       ├── CustomerCreditGateway.java
            │   │       ├── CustomerCreditTermsGateway.java
            │   │       ├── InventoryGateway.java
            │   │       ├── InvoiceCatalogGateway.java
            │   │       ├── InvoiceCustomerGateway.java
            │   │       ├── InvoiceFinancialPositionGateway.java
            │   │       ├── InvoiceNoteDetailsGateway.java
            │   │       ├── InvoiceNotificationGateway.java
            │   │       ├── InvoiceOrderGateway.java
            │   │       ├── InvoicePaymentSettlementGateway.java
            │   │       ├── InvoicePaymentTotalsGateway.java
            │   │       ├── InvoiceQuoteGateway.java
            │   │       ├── InvoiceReturnDetailsGateway.java
            │   │       ├── StockAvailabilityGateway.java
            │   │       └── StockDeductionGateway.java
            │   ├── queries
            │   │   └── services
            │   │       ├── InvoiceDocumentQueryService.java
            │   │       ├── RepositoryInvoiceCreditLedgerQuery.java
            │   │       ├── RepositoryInvoiceReturnAdjustments.java
            │   │       └── RepositoryReturnInvoiceQuery.java
            │   └── update
            │       ├── request
            │       │   └── UpdateInvoiceRequest.java
            │       └── use_case
            │           └── UpdateInvoiceUseCase.java
            ├── domain
            │   ├── entity
            │   │   ├── Invoice.java
            │   │   ├── InvoiceLine.java
            │   │   └── InvoiceLineAllocation.java
            │   ├── exceptions
            │   │   ├── InvoiceNotFoundException.java
            │   │   ├── InvoiceNotReturnableException.java
            │   │   └── ReturnQuantityExceedsException.java
            │   ├── repository
            │   │   └── InvoiceRepository.java
            │   ├── services
            │   │   └── InvoiceStatusService.java
            │   └── valueobjects
            │       ├── InvoiceId.java
            │       ├── InvoiceLineId.java
            │       ├── InvoiceNumber.java
            │       ├── InvoiceReturnAdjustment.java
            │       ├── InvoiceReturnStatus.java
            │       ├── InvoiceStatus.java
            │       └── PaymentType.java
            ├── infrastructure
            │   ├── config
            │   │   ├── InvoiceCreditQueryConfig.java
            │   │   ├── InvoiceDocumentQueryConfig.java
            │   │   ├── InvoiceReturnAdjustmentConfig.java
            │   │   ├── OrderInvoiceCreationConfig.java
            │   │   ├── QuoteInvoiceCreationConfig.java
            │   │   └── ReturnInvoiceQueryConfig.java
            │   ├── integration
            │   │   ├── InvoiceCustomerCreditAdapter.java
            │   │   ├── InvoiceStockAvailabilityAdapter.java
            │   │   ├── LocalActorIdentityGateway.java
            │   │   ├── LocalCustomerBalanceGateway.java
            │   │   ├── LocalCustomerCreditTermsGateway.java
            │   │   ├── LocalInventoryGatewayAdapter.java
            │   │   ├── LocalInvoiceCashRegisterGateway.java
            │   │   ├── LocalInvoiceCatalogGateway.java
            │   │   ├── LocalInvoiceCustomerGateway.java
            │   │   ├── LocalInvoiceFinancialPositionGateway.java
            │   │   ├── LocalInvoiceNoteApi.java
            │   │   ├── LocalInvoiceNoteDetailsAdapter.java
            │   │   ├── LocalInvoiceNotificationGateway.java
            │   │   ├── LocalInvoiceOrderGateway.java
            │   │   ├── LocalInvoicePaymentSettlementGateway.java
            │   │   ├── LocalInvoicePaymentTotalsAdapter.java
            │   │   ├── LocalInvoiceQuoteGateway.java
            │   │   ├── LocalInvoiceReturnDetailsAdapter.java
            │   │   └── LocalStockDeductionGateway.java
            │   ├── persistence
            │   │   ├── adapters
            │   │   │   └── InvoiceRepositoryAdapter.java
            │   │   ├── entities
            │   │   │   ├── InvoiceEntity.java
            │   │   │   ├── InvoiceLineAllocationEntity.java
            │   │   │   ├── InvoiceLineEntity.java
            │   │   │   └── InvoiceNumberSequenceEntity.java
            │   │   ├── mappers
            │   │   │   └── InvoiceJpaMapper.java
            │   │   ├── queries
            │   │   │   └── JpaInvoicesProductReferenceQuery.java
            │   │   └── repositories
            │   │       ├── InvoiceJpaRepository.java
            │   │       ├── InvoiceNumberSequenceJpaRepository.java
            │   │       └── SpringDataInvoicesProductReferenceRepository.java
            │   ├── services
            │   │   └── InvoiceStatusServiceImpl.java
            │   └── web
            │       └── InvoiceController.java
            └── reporting
                ├── application
                │   ├── dto
                │   │   └── model
                │   │       ├── InvoiceReportData.java
                │   │       ├── InvoiceReportFilter.java
                │   │       └── InvoiceReportRow.java
                │   ├── generate_invoice_pdf
                │   │   ├── services
                │   │   │   └── GenerateInvoicePdfUseCaseImpl.java
                │   │   └── use_case
                │   │       └── GenerateInvoicePdfUseCase.java
                │   ├── generate_invoices_report_pdf
                │   │   ├── services
                │   │   │   └── GenerateInvoicesReportPdfUseCaseImpl.java
                │   │   └── use_case
                │   │       └── GenerateInvoicesReportPdfUseCase.java
                │   └── ports
                │       └── out
                │           ├── InvoicePdfPort.java
                │           ├── InvoiceReportQueryGateway.java
                │           └── InvoicesReportPdfPort.java
                └── infrastructure
                    ├── config
                    │   └── InvoicesReportConfig.java
                    ├── integration
                    │   └── InvoiceDocumentQueryAdapter.java
                    ├── pdf
                    │   ├── InvoicePdfGenerator.java
                    │   └── InvoicesReportPdfGenerator.java
                    └── web
                        └── InvoicesReportController.java
```

---

## Domain Model

### Entities

#### Invoice (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.invoices.domain.entities.Invoice`

**Description**: Represents a sales invoice. Each invoice contains one or more invoice lines, is associated with a customer, and optionally with a merchant for commission tracking. Tracks payments and calculates balance.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `InvoiceId` (UUID) | Unique identifier | Required, immutable |
| `invoiceNumber` | `InvoiceNumber` (String) | Sequential invoice number | Required, immutable, auto-generated |
| `orderId` | `OrderId` (UUID) | Source order reference | Optional, null if direct creation |
| `customerId` | `CustomerId` (UUID) | Customer who receives invoice | Required |
| `merchantId` | `MerchantId` (UUID) | Merchant for commission | Optional, null if no merchant |
| `status` | `InvoiceStatus` | Invoice status | Required, see enum |
| `issueDate` | `LocalDate` | Invoice issue date | Required, auto-generated (today) |
| `dueDate` | `LocalDate` | Payment due date | Required, calculated or manual |
| `creditDays` | `Integer` | Days of credit applied | Required, >= 0 |
| `subtotal` | `BigDecimal` | Invoice subtotal (before taxes) | Calculated, non-negative |
| `totalPaid` | `BigDecimal` | Total amount paid | Calculated, non-negative |
| `balance` | `BigDecimal` | Remaining balance | Calculated (subtotal - totalPaid) |
| `currency` | `String` | Invoice currency (ISO 4217) | Required, one of: USD, EUR, VES; default: USD |
| `notes` | `String` | Invoice notes/comments | Optional, max 1000 characters |
| `cancelledAt` | `LocalDateTime` | Cancellation timestamp | Null unless cancelled |
| `cancelledReason` | `String` | Reason for cancellation | Null unless cancelled |
| `hasPendingPayment` | `boolean` | Flag set when merchant registers a payment awaiting approval | Default: `false` |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |
| `invoiceLines` | `List<InvoiceLine>` | Invoice line items | At least 1 required |
| `payments` | `List<InvoicePayment>` | Payment records | Can be empty |

**Business Methods**:
- `create(...)` - Factory method to create new invoice (direct creation)
- `createFromOrder(Order)` - Factory method to create invoice from order
- `reconstitute(...)` - Factory method to rebuild invoice from database
- `addLine(InvoiceLine)` - Adds a line item
- `registerPayment(InvoicePayment)` - Registers a payment
- `calculateSubtotal()` - Calculates invoice subtotal
- `calculateBalance()` - Calculates remaining balance
- `cancel(String reason)` - Cancels the invoice (only if PENDING)
- `markAsOverdue()` - Marks invoice as overdue (scheduled job)
- `markAsPendingPayment()` - Sets `hasPendingPayment = true` when merchant registers a payment
- `clearPendingPayment()` - Sets `hasPendingPayment = false` when payment is approved or rejected
- `hasMerchant()` - Returns true if invoice has merchant associated
- `hasPendingPayment()` - Returns true if a merchant payment is awaiting approval
- `isPaid()` - Returns true if balance is zero
- `isOverdue()` - Returns true if past dueDate and not fully paid

**Business Rules**:
1. Invoice number is auto-generated (INV-2025-00001)
2. Invoices start with status PENDING
3. Only PENDING invoices can be cancelled
4. CANCELLED invoices cannot receive payments
5. PAID invoices cannot receive more payments
6. Invoice must have at least 1 line item
7. Customer is REQUIRED for all invoices
8. Merchant is OPTIONAL (for commission tracking)
9. `issueDate` defaults to today but can be edited when creating a draft invoice
10. `dueDate = issueDate + Customer.creditDays` and is stored on the invoice; later customer credit-day changes do not modify existing invoices
11. Differential pricing is applied to each line at creation time
12. Stock is deducted when invoice is created
13. Invoices with `hasPendingPayment = true` are excluded from all receivables debt queries until the payment is approved or rejected

---

#### InvoiceLine (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.invoices.domain.entities.InvoiceLine`

**Description**: Represents a line item in an invoice. Contains product snapshot and differential pricing. **ALWAYS managed through Invoice aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `InvoiceLineId` (UUID) | Unique identifier | Required, immutable |
| `invoiceId` | `InvoiceId` (UUID) | Parent invoice ID | Required, immutable |
| `productId` | `ProductId` (UUID) | Product reference | Required |
| `productName` | `String` | Product name snapshot | Required |
| `productCode` | `String` | Product code snapshot | Required |
| `quantity` | `BigDecimal` | Quantity invoiced | Required, > 0 |
| `baseUnitPrice` | `BigDecimal` | Original product price | Required, >= 0 |
| `differentialId` | `DifferentialId` (Long) | Applied differential | Optional, null if none |
| `differentialPercentage` | `BigDecimal` | Differential % snapshot | Optional, null if none |
| `unitPrice` | `BigDecimal` | Final price (with differential) | Required, >= 0 |
| `lineTotal` | `BigDecimal` | Line total (qty x unitPrice) | Calculated |
| `warehouseId` | `WarehouseId` (Long) | Source warehouse | Optional, for stock deduction |

**Business Rules**:
1. **NO direct API endpoints** - managed only through Invoice
2. Line total is automatically calculated
3. Product name, code, and prices are snapshots at invoice creation
4. Differential is applied: `unitPrice = baseUnitPrice x (1 + differentialPercentage/100)`
5. If no differential, `unitPrice = baseUnitPrice`
6. Quantity must be greater than zero
7. Cannot exist independently outside Invoice aggregate
8. If `warehouseId` is set, stock is deducted from that warehouse

---

#### InvoicePayment (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.invoices.domain.entities.InvoicePayment`

**Description**: Represents a payment made against an invoice. Immutable once created. **Managed through Invoice aggregate.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `InvoicePaymentId` (UUID) | Unique identifier | Required, immutable |
| `invoiceId` | `InvoiceId` (UUID) | Parent invoice ID | Required, immutable |
| `amount` | `BigDecimal` | Payment amount | Required, > 0 |
| `paymentDate` | `LocalDate` | Date of payment | Required |
| `paymentMethod` | `PaymentMethod` | Payment method | Required |
| `reference` | `String` | Payment reference/receipt | Optional, max 100 chars |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |

**Business Rules**:
1. Immutable once created (cannot modify or delete)
2. Amount must be positive
3. Amount cannot exceed invoice balance
4. Payment triggers status update (PARTIALLY_PAID or PAID)
5. If invoice has merchant, Collection is generated for each payment

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `InvoiceId` | UUID | Invoice unique identifier | Non-null UUID |
| `InvoiceNumber` | String | Sequential invoice number | Format: INV-YYYY-NNNNN |
| `InvoiceLineId` | UUID | Line item identifier | Non-null UUID |
| `InvoicePaymentId` | UUID | Payment identifier | Non-null UUID |
| `CustomerId` | UUID | Customer reference | Non-null UUID |
| `MerchantId` | UUID | Merchant reference | Nullable UUID |
| `OrderId` | UUID | Order reference | Nullable UUID |
| `ProductId` | UUID | Product reference | Non-null UUID |
| `DifferentialId` | Long | Differential reference | Nullable Long |
| `WarehouseId` | Long | Warehouse reference | Nullable Long |

---

### Enums

#### InvoiceStatus
```java
public enum InvoiceStatus {
    PENDING,          // Created, awaiting delivery
    DELIVERED,        // Delivered to customer, can receive payments
    PARTIALLY_PAID,   // Partially paid
    PAID,             // Fully paid
    CANCELLED         // Cancelled (only from PENDING)
}
```

#### PaymentMethod
```java
public enum PaymentMethod {
    CASH,         // Efectivo
    TRANSFER,     // Transferencia bancaria
    CHECK,        // Cheque
    CARD,         // Tarjeta de credito/debito
    OTHER         // Otro metodo
}
```

---

## API Endpoints

### Invoice Management

#### 1. Create Invoice (Direct)
**Endpoint**: `POST /api/invoices`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN (MERCHANTS cannot create invoices directly)

**Description**: Creates a new invoice directly (not from an order). Applies differential pricing and deducts stock.

**Request Body** (`CreateInvoiceRequest`):
```json
{
  "customerId": "customer-uuid-here",
  "merchantId": "merchant-uuid-here",
  "creditDays": 30,
  "dueDate": null,
  "currency": "USD",
  "notes": "Factura directa",
  "lines": [
    {
      "productId": "product-001-uuid",
      "quantity": 10,
      "warehouseId": 1
    },
    {
      "productId": "product-002-uuid",
      "quantity": 5.5,
      "warehouseId": 1
    }
  ]
}
```

**Notes**:
- If `creditDays` is null and `dueDate` is null, uses `Customer.creditDays`
- If `dueDate` is provided, `creditDays` is calculated from `issueDate` to `dueDate`
- If `creditDays` is provided, `dueDate = issueDate + creditDays`
- `merchantId` is optional
- `warehouseId` per line is optional (if provided, stock is deducted)
- Differential pricing is applied automatically based on customer

**Response** (201 Created) - `InvoiceCreateResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "orderId": null,
  "customerId": "customer-uuid-here",
  "customerName": "ABC Company S.A.",
  "merchantId": "merchant-uuid-here",
  "merchantName": "Juan Perez",
  "status": "PENDING",
  "issueDate": "2025-01-09",
  "dueDate": "2025-02-08",
  "creditDays": 30,
  "subtotal": 1275.00,
  "totalPaid": 0.00,
  "balance": 1275.00,
  "currency": "USD",
  "notes": "Factura directa",
  "createdAt": "2025-01-09T10:30:00",
  "lines": [
    {
      "id": "line-001-uuid",
      "productId": "product-001-uuid",
      "productName": "Producto A",
      "productCode": "PRD-00001",
      "quantity": 10,
      "baseUnitPrice": 100.00,
      "differentialId": 1,
      "differentialPercentage": 10.00,
      "unitPrice": 110.00,
      "lineTotal": 1100.00,
      "warehouseId": 1
    },
    {
      "id": "line-002-uuid",
      "productId": "product-002-uuid",
      "productName": "Producto B",
      "productCode": "PRD-00002",
      "quantity": 5.5,
      "baseUnitPrice": 50.00,
      "differentialId": null,
      "differentialPercentage": null,
      "unitPrice": 50.00,
      "lineTotal": 275.00,
      "warehouseId": 1
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT role cannot create invoices
- `404 Not Found`: Customer, product, or warehouse not found
- `422 Unprocessable Entity`: Insufficient stock
- `422 Unprocessable Entity`: Customer credit limit exceeded

---

#### 2. Get Invoice by ID
**Endpoint**: `GET /api/invoices/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT (MERCHANT can only view invoices from their orders)

**Description**: Retrieves a specific invoice by ID with full details.

**Path Parameters**:
- `id` (UUID) - Invoice unique identifier

**Response** (200 OK) - `InvoiceDetailResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "orderId": "order-001-uuid",
  "orderNumber": "ORD-2025-00001",
  "customerId": "customer-uuid-here",
  "customerName": "ABC Company S.A.",
  "customerRif": "J-12345678-9",
  "createdBy": "user-uuid-here",
  "createdByName": "Juan Perez",
  "status": "PENDING_PAYMENT",
  "statusLabel": "Pendiente de pago",
  "paymentType": "CREDIT",
  "subtotal": 1275.00,
  "creditApplied": 123.00,
  "debitApplied": 50.00,
  "total": 1202.00,
  "notesList": [
    {
      "id": "credit-note-uuid",
      "noteNumber": "NC-2026-00001",
      "type": "CREDIT",
      "amount": 123.00,
      "reason": "Ajuste comercial",
      "applicationDate": "2026-08-14T10:30:00"
    }
  ],
  "notes": "Entrega en horario de oficina",
  "cancelledAt": null,
  "cancelledReason": null,
  "createdAt": "2025-01-09T10:30:00",
  "updatedAt": "2025-01-15T14:00:00",
  "lines": [...],
  "returnedTotal": 0.00,
  "returnStatus": null,
  "returns": []
}
```

Each item in `notesList` includes the human-readable credit or debit note number in `noteNumber`.

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT trying to access another merchant's invoice
- `404 Not Found`: Invoice not found

---

#### 3. List Invoices
**Endpoint**: `GET /api/invoices`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view all invoices
- MERCHANT: Can only view invoices from their orders

**Query Parameters** (optional):
| Parameter | Type | Description |
|-----------|------|-------------|
| `customerId` | UUID | Filter by customer |
| `merchantId` | UUID | Filter by merchant (ignored for MERCHANT role) |
| `status` | String | Filter by status (PENDING, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED) |
| `fromDate` | Date | Filter from issue date |
| `toDate` | Date | Filter to issue date |
| `overdue` | Boolean | Filter overdue invoices (dueDate < today AND status IN (PENDING, PARTIALLY_PAID)) |
| `search` | String | Search by invoice number, customer name |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |
| `sort` | String | Sort field (default: createdAt,desc) |

**Response** (200 OK) - Paginated `List<InvoiceListResponse>`:
```json
{
  "content": [
    {
      "id": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "customerId": "customer-uuid-here",
      "customerName": "ABC Company S.A.",
      "merchantId": "merchant-uuid-here",
      "merchantName": "Juan Perez",
      "status": "PARTIALLY_PAID",
      "issueDate": "2025-01-09",
      "dueDate": "2025-02-08",
      "subtotal": 1275.00,
      "totalPaid": 500.00,
      "balance": 775.00,
      "currency": "USD",
      "lineCount": 2,
      "createdAt": "2025-01-09T10:30:00"
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

---

#### 4. Register Payment
**Endpoint**: `POST /api/invoices/{id}/payments`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT

**Description**: Registers a payment against an invoice. 
- **USER/ADMIN**: Payment is applied immediately
- **MERCHANT**: Payment goes to PENDING_APPROVAL (see RECEIVABLES module)

**Important**: For the full payment workflow with merchant validation, use the **RECEIVABLES** module endpoints instead. This endpoint is for direct payment registration.

**Path Parameters**:
- `id` (UUID) - Invoice unique identifier

**Request Body** (`RegisterPaymentRequest`):
```json
{
  "amount": 500.00,
  "paymentDate": "2025-01-15",
  "paymentMethod": "TRANSFER",
  "reference": "REF-123456"
}
```

**Response** (201 Created) - `InvoiceDetailResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "status": "PARTIALLY_PAID",
  "subtotal": 1275.00,
  "totalPaid": 500.00,
  "balance": 775.00,
  "payments": [
    {
      "id": "payment-001-uuid",
      "amount": 500.00,
      "paymentDate": "2025-01-15",
      "paymentMethod": "TRANSFER",
      "reference": "REF-123456",
      "createdAt": "2025-01-15T14:00:00"
    }
  ],
  ...
}
```

**Business Logic**:
1. Validates invoice exists and is in `DELIVERED` or `PARTIALLY_PAID` status
2. Validates amount <= balance
3. Creates InvoicePayment record
4. Updates totalPaid and balance
5. Updates status: PARTIALLY_PAID if balance > 0, PAID if balance = 0
6. **If invoice has merchant**: Creates Collection record with commission calculation
7. **Collection triggers**: Payable creation for merchant (see PAYABLES module)

**Note**: For merchant payment validation workflow (PENDING_APPROVAL → APPROVED/REJECTED), use RECEIVABLES module.

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Invoice not found
- `409 Conflict`: Invoice is CANCELLED, PAID, or still PENDING (not delivered)
- `422 Unprocessable Entity`: Amount exceeds balance

---

#### 5. Cancel Invoice
**Endpoint**: `PUT /api/invoices/{id}/cancel`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN only

**Description**: Cancels a pending invoice. Only PENDING invoices with no payments can be cancelled.

**Path Parameters**:
- `id` (UUID) - Invoice unique identifier

**Request Body**:
```json
{
  "reason": "Cliente solicito cancelacion"
}
```

**Response** (200 OK) - `InvoiceDetailResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "status": "CANCELLED",
  "cancelledAt": "2025-01-10T11:00:00",
  "cancelledReason": "Cliente solicito cancelacion",
  ...
}
```

**Business Logic**:
1. Validates invoice is PENDING
2. Validates no payments have been made
3. **Restores stock** if products were from warehouse
4. Updates status to CANCELLED

**Errors**:
- `400 Bad Request`: Reason is required
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT role cannot cancel invoices
- `404 Not Found`: Invoice not found
- `409 Conflict`: Invoice is not in PENDING status or has payments

---

#### 6. Deliver Invoice
**Endpoint**: `PUT /api/invoices/{id}/deliver`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, DISPATCHER

**Description**: Marks an invoice as delivered. Required before payments can be registered. Can include delivery proof (photo/signature).

**Path Parameters**:
- `id` (UUID) - Invoice unique identifier

**Request Body** (optional):
```json
{
  "dispatcherId": "dispatcher-uuid-here",
  "deliveryProofUrl": "https://storage.example.com/proof/123.jpg"
}
```

**Response** (200 OK) - `InvoiceDetailResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "status": "DELIVERED",
  "dispatcherId": "dispatcher-uuid-here",
  "dispatcherName": "Pedro Dispatcher",
  "deliveryProofUrl": "https://storage.example.com/proof/123.jpg",
  "deliveredAt": "2025-01-10T14:00:00",
  ...
}
```

**Business Logic**:
1. Validates invoice exists and is in PENDING status
2. Updates status to DELIVERED
3. Records dispatcher and delivery proof if provided
4. Records delivery timestamp

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User does not have permission
- `404 Not Found`: Invoice not found
- `409 Conflict`: Invoice is not in PENDING status

---

#### 7. Get Invoices with Debt (for Receivables)
**Endpoint**: `GET /api/invoices/with-debt`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT (filtered)

**Description**: Retrieves invoices that have outstanding balance (for use in Receivables module).

**Query Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `customerId` | UUID | Filter by customer |
| `merchantId` | UUID | Filter by merchant |
| `status` | String | Filter by status (DELIVERED, PARTIALLY_PAID) |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

**Response** (200 OK) - Paginated `List<InvoiceListResponse>` (only invoices with balance > 0)

---

#### 8. Get Overdue Invoices (Query, not status)
**Endpoint**: `GET /api/invoices/overdue`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN

**Description**: Retrieves invoices that are past their due date and not fully paid. Note: "Overdue" is a query filter, NOT a status - invoices remain in DELIVERED or PARTIALLY_PAID status.

**Query Parameters** (optional):
| Parameter | Type | Description |
|-----------|------|-------------|
| `customerId` | UUID | Filter by customer |
| `merchantId` | UUID | Filter by merchant |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

**Response** (200 OK) - Paginated `List<InvoiceListResponse>`

**Note**: This returns invoices where `dueDate < today AND status IN (DELIVERED, PARTIALLY_PAID) AND balance > 0`

---

#### 9. Get Due Soon Invoices
**Endpoint**: `GET /api/invoices/due-soon`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT (MERCHANT can only view their invoices)

**Description**: Retrieves invoices that will be due within the specified number of days. Useful for proactive payment collection and preventing overdue invoices.

**Query Parameters**:
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `days` | Integer | 7 | Number of days to look ahead (1-90) |
| `customerId` | UUID | null | Filter by customer |
| `merchantId` | UUID | null | Filter by merchant (ignored for MERCHANT role) |
| `page` | Integer | 0 | Page number |
| `size` | Integer | 20 | Page size |
| `sort` | String | dueDate,asc | Sort field and direction |

**Response** (200 OK) - Paginated `List<DueSoonInvoiceResponse>`:
```json
{
  "content": [
    {
      "id": "invoice-001-uuid",
      "invoiceNumber": "INV-2026-00005",
      "customerId": "customer-001-uuid",
      "customerName": "ABC Company S.A.",
      "customerRif": "J-12345678-9",
      "merchantId": "merchant-001-uuid",
      "merchantName": "Juan Perez",
      "orderId": "order-001-uuid",
      "status": "PENDING",
      "issueDate": "2026-01-01",
      "dueDate": "2026-01-18",
      "creditDays": 17,
      "subtotal": 2500.00,
      "totalPaid": 0.00,
      "balance": 2500.00,
      "lineCount": 3,
      "paymentCount": 0,
      "createdAt": "2026-01-01T10:00:00",
      "daysUntilDue": 5
    },
    {
      "id": "invoice-002-uuid",
      "invoiceNumber": "INV-2026-00008",
      "customerId": "customer-002-uuid",
      "customerName": "XYZ Corp",
      "customerRif": "J-98765432-1",
      "merchantId": null,
      "merchantName": null,
      "orderId": null,
      "status": "PARTIALLY_PAID",
      "issueDate": "2026-01-05",
      "dueDate": "2026-01-20",
      "creditDays": 15,
      "subtotal": 1800.00,
      "totalPaid": 500.00,
      "balance": 1300.00,
      "lineCount": 2,
      "paymentCount": 1,
      "createdAt": "2026-01-05T14:30:00",
      "daysUntilDue": 7
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Business Logic**:
1. Calculates date range: `today <= dueDate <= today + days`
2. Filters by status: Only `DELIVERED` or `PARTIALLY_PAID` invoices (not yet fully paid)
3. Calculates `daysUntilDue` for each invoice: `dueDate - today`
4. Orders by `dueDate ASC` by default (most urgent first)
5. For MERCHANT role: Automatically filters to show only their invoices

**Use Cases**:
- **Daily Collection Review**: Call with `days=7` to see next week's due invoices
- **Urgent Collection**: Call with `days=3` to focus on immediate deadlines
- **Monthly Planning**: Call with `days=30` for upcoming month's collections
- **Customer-Specific**: Add `customerId` to see a specific customer's upcoming dues

**Errors**:
- `400 Bad Request`: Invalid `days` parameter (must be 1-90)
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT trying to filter by another merchant

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### CreateInvoiceRequest
```java
public record CreateInvoiceRequest(
    @NotNull UUID customerId,                           // Required
    UUID merchantId,                                     // Optional
    Integer creditDays,                                  // Optional (uses customer.creditDays if null)
    LocalDate dueDate,                                   // Optional (calculated if null)
    @Pattern(regexp = "^(USD|EUR|VES)$") String currency, // Optional, default: USD
    @Size(max = 1000) String notes,                     // Optional
    @Valid @NotEmpty List<CreateInvoiceLineRequest> lines // At least 1 required
)
```

#### CreateInvoiceLineRequest
```java
public record CreateInvoiceLineRequest(
    @NotNull UUID productId,                            // Required
    @NotNull @Positive BigDecimal quantity,             // Required, > 0
    Long warehouseId                                     // Optional (for stock deduction)
)
```

#### RegisterPaymentRequest
```java
public record RegisterPaymentRequest(
    @NotNull @Positive BigDecimal amount,               // Required, > 0
    @NotNull LocalDate paymentDate,                     // Required
    @NotNull PaymentMethod paymentMethod,               // Required
    @Size(max = 100) String reference                   // Optional
)
```

#### CancelInvoiceRequest
```java
public record CancelInvoiceRequest(
    @NotBlank @Size(max = 500) String reason            // Required
)
```

---

### Response DTOs

#### InvoiceListResponse
```java
public record InvoiceListResponse(
    String id,
    String invoiceNumber,
    String customerId,
    String customerName,
    String merchantId,              // null if no merchant
    String merchantName,            // null if no merchant
    String status,
    LocalDate issueDate,
    LocalDate dueDate,
    BigDecimal subtotal,
    BigDecimal totalPaid,
    BigDecimal balance,
    String currency,
    int lineCount,
    LocalDateTime createdAt
)
```

#### InvoiceDetailResponse
```java
public record InvoiceDetailResponse(
    String id,
    String invoiceNumber,
    String orderId,                 // null if direct creation
    String orderNumber,             // null if direct creation
    String customerId,
    String customerName,
    String customerRif,
    String merchantId,              // null if no merchant
    String merchantName,            // null if no merchant
    BigDecimal merchantCommission,  // null if no merchant
    String status,
    LocalDate issueDate,
    LocalDate dueDate,
    int creditDays,
    BigDecimal subtotal,
    BigDecimal totalPaid,
    BigDecimal balance,
    String currency,
    String notes,
    LocalDateTime cancelledAt,      // null if not cancelled
    String cancelledReason,         // null if not cancelled
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<InvoiceLineResponse> lines,
    List<InvoicePaymentResponse> payments
)
```

#### InvoiceCreateResponse
```java
public record InvoiceCreateResponse(
    String id,
    String invoiceNumber,
    String orderId,
    String customerId,
    String customerName,
    String merchantId,
    String merchantName,
    String status,
    LocalDate issueDate,
    LocalDate dueDate,
    int creditDays,
    BigDecimal subtotal,
    BigDecimal totalPaid,
    BigDecimal balance,
    String currency,
    String notes,
    LocalDateTime createdAt,
    List<InvoiceLineResponse> lines
)
```

#### InvoiceLineResponse
```java
public record InvoiceLineResponse(
    String id,
    String productId,
    String productName,
    String productCode,
    BigDecimal quantity,
    BigDecimal baseUnitPrice,
    Long differentialId,            // null if no differential
    BigDecimal differentialPercentage, // null if no differential
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    Long warehouseId                // null if not from warehouse
)
```

#### InvoicePaymentResponse
```java
public record InvoicePaymentResponse(
    String id,
    BigDecimal amount,
    LocalDate paymentDate,
    String paymentMethod,
    String reference,
    LocalDateTime createdAt
)
```

#### ProcessOverdueResponse
```java
public record ProcessOverdueResponse(
    LocalDateTime processedAt,
    int totalFound,
    int totalMarked,
    List<OverdueInvoiceMarkedResponse> invoicesMarked
)
```

#### OverdueInvoiceMarkedResponse
```java
public record OverdueInvoiceMarkedResponse(
    String invoiceId,
    String invoiceNumber,
    String customerId,
    String customerName,
    LocalDate dueDate,
    int daysOverdue,
    BigDecimal balance,
    String previousStatus
)
```

#### DueSoonInvoiceResponse
```java
public record DueSoonInvoiceResponse(
    String id,
    String invoiceNumber,
    String customerId,
    String customerName,
    String customerRif,
    String merchantId,
    String merchantName,
    String orderId,
    String status,
    LocalDate issueDate,
    LocalDate dueDate,
    int creditDays,
    BigDecimal subtotal,
    BigDecimal totalPaid,
    BigDecimal balance,
    int lineCount,
    int paymentCount,
    LocalDateTime createdAt,
    int daysUntilDue       // Calculated field: dueDate - today
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/invoices | None | Page\<InvoiceListResponse\> |
| GET /api/invoices/{id} | None | InvoiceDetailResponse |
| POST /api/invoices | CreateInvoiceRequest | InvoiceCreateResponse |
| POST /api/invoices/{id}/payments | RegisterPaymentRequest | InvoiceDetailResponse |
| PUT /api/invoices/{id}/cancel | CancelInvoiceRequest | InvoiceDetailResponse |
| PUT /api/invoices/{id}/deliver | DeliverInvoiceRequest | InvoiceDetailResponse |
| GET /api/invoices/with-debt | None | Page\<InvoiceListResponse\> |
| GET /api/invoices/overdue | None | Page\<InvoiceListResponse\> |
| GET /api/invoices/due-soon | None | Page\<DueSoonInvoiceResponse\> |

---

## Use Cases

### Invoice Management Use Cases
1. **CreateInvoiceUseCase** - Creates a new invoice (direct creation). Validates customer credit limit before saving: if the customer's outstanding balance plus the new invoice total exceeds `customer.creditLimit`, throws `CreditLimitExceededException` (HTTP 422).
2. **CreateInvoiceFromOrderUseCase** - Creates invoice from an existing order
3. **GetInvoiceUseCase** - Retrieves invoice by ID (with role-based access)
4. **ListInvoicesUseCase** - Lists invoices with filters (role-based filtering)
5. **DeliverInvoiceUseCase** - Marks invoice as delivered (PENDING → DELIVERED)
6. **RegisterPaymentUseCase** - Registers a payment (triggers Collection creation)
7. **CancelInvoiceUseCase** - Cancels a pending invoice (restores stock)
8. **GetInvoicesWithDebtUseCase** - Lists invoices with balance > 0 (for Receivables)
9. **GetOverdueInvoicesUseCase** - Lists overdue invoices (dueDate < today AND balance > 0)
10. **GetDueSoonInvoicesUseCase** - Lists invoices due within specified days

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(InvoiceId id);
    List<Invoice> findAll();
    List<Invoice> findByCustomerId(CustomerId customerId);
    List<Invoice> findByMerchantId(MerchantId merchantId);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByDueDateBeforeAndStatusIn(LocalDate date, List<InvoiceStatus> statuses);
    Page<Invoice> findAll(Specification<Invoice> spec, Pageable pageable);
    void delete(InvoiceId id);
    boolean existsById(InvoiceId id);
    String getNextInvoiceNumber();
}
```

---

## Domain Services

### InvoiceNumberGenerator
**Responsibilities**:
- Generate sequential invoice numbers
- Format: INV-YYYY-NNNNN (e.g., INV-2025-00001)
- Reset sequence each year

### DifferentialPriceCalculator
**Responsibilities**:
- Look up applicable differential for customer/product combination
- Calculate final price: `basePrice x (1 + differential/100)`
- Return null differential if none applies

**Interface**:
```java
public interface DifferentialPriceCalculator {
    DifferentialResult calculatePrice(CustomerId customerId, ProductId productId, BigDecimal basePrice);
}

public record DifferentialResult(
    BigDecimal finalPrice,
    Long differentialId,          // null if none
    BigDecimal differentialPercentage // null if none
)
```

---

## Business Rules Summary

### Invoice Creation
1. Customer is REQUIRED for all invoices
2. Merchant is OPTIONAL (for commission tracking)
3. At least one invoice line is required
4. Product snapshots (name, code, price) are captured at creation time
5. Differential pricing is applied automatically per line
6. Stock is deducted if warehouseId is provided on lines
7. Subtotal is automatically calculated from lines
8. `creditDays` defaults to `Customer.creditDays` if not provided
9. `dueDate = issueDate + creditDays`
10. **Credit limit enforcement**: Before saving, the system calculates the customer's outstanding balance (sum of `balance` for all invoices in non-`PAID`, non-`CANCELLED` status). If `outstandingBalance + newInvoiceSubtotal > customer.creditLimit`, the invoice is rejected with HTTP 422 (`CreditLimitExceededException`).

### Invoice from Order
1. Inherits: customerId, merchantId, notes from order
2. Order lines are converted to invoice lines
3. Differential pricing is applied to invoice lines
4. Stock is deducted at conversion time
5. Order status changes to INVOICED
6. Order.invoiceId is set to new invoice ID

### Invoice Lifecycle
```
PENDING ──┬──► DELIVERED (via deliver - required before payments)
          │       │
          │       ├──► PARTIALLY_PAID (via partial payment)
          │       │           │
          │       │           └──► PAID (via final payment)
          │       │
          │       └──► PAID (via full payment at once)
          │
          └──► CANCELLED (via cancel, only if PENDING with no payments)
```

**Important Notes**:
- `DELIVERED` status is REQUIRED before any payment can be registered
- `OVERDUE` is NOT a status - it's a query filter (dueDate < today AND balance > 0)
- Only `PENDING` invoices can be cancelled
- Only `DELIVERED` or `PARTIALLY_PAID` invoices can receive payments

### Payment Rules
1. Only `DELIVERED` or `PARTIALLY_PAID` invoices can receive payments
2. `PENDING` invoices must be delivered first before payments
3. Payment amount must be > 0
4. Payment amount cannot exceed balance
5. Each payment triggers Collection creation if merchant exists
6. Collection triggers Payable creation for merchant commission
7. Partial payment changes status to PARTIALLY_PAID
8. Final payment (balance = 0) changes status to PAID

### Cancellation Rules
1. Only PENDING invoices can be cancelled
2. Invoices with any payments cannot be cancelled
3. Cancellation restores stock to warehouse
4. Reason is required for cancellation

### Stock Rules
1. Stock is deducted when invoice is created (if warehouseId on lines)
2. Stock is restored when invoice is cancelled
3. Validates sufficient stock before invoice creation

### Commission Rules
1. Commission only applies if invoice has a merchant
2. Commission is calculated when payment is registered
3. Collection record is created for each payment
4. Commission = payment amount x (merchant.commissionPercentage / 100)

---

## Database Schema

### invoices Table
```sql
CREATE TABLE invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    order_id VARCHAR(36),
    customer_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36),
    status VARCHAR(20) NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    credit_days INT NOT NULL DEFAULT 0,
    subtotal DECIMAL(15,2) NOT NULL,
    total_paid DECIMAL(15,2) NOT NULL DEFAULT 0,
    balance DECIMAL(15,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    notes VARCHAR(1000),
    cancelled_at TIMESTAMP,
    cancelled_reason VARCHAR(500),
    has_pending_payment BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_issue_date (issue_date),
    INDEX idx_due_date (due_date),
    INDEX idx_created_at (created_at),
    INDEX idx_invoice_has_pending_payment (has_pending_payment)
);
```

### invoice_lines Table
```sql
CREATE TABLE invoice_lines (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity DECIMAL(15,3) NOT NULL,
    base_unit_price DECIMAL(15,2) NOT NULL,
    differential_id BIGINT,
    differential_percentage DECIMAL(5,2),
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    warehouse_id BIGINT,
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (differential_id) REFERENCES differentials(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_product_id (product_id)
);
```

### invoice_payments Table
```sql
CREATE TABLE invoice_payments (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_method (payment_method)
);
```

---

## Integration with Other Modules

### Orders Module
- Invoice can be created from an Order (ConvertToInvoice)
- Order status changes to INVOICED after conversion
- Order.invoiceId links to the created invoice
- Invoice inherits customer, merchant, notes from order

### Customers Module
- Invoice REQUIRES a valid customer
- Customer.creditDays used as default for due date calculation
- Customer name and RIF shown in invoice responses

### Merchants Module
- Invoice can OPTIONALLY have a merchant
- Merchant commission percentage used for Collection calculation
- Commission triggered when invoice payment is registered

### Products Module
- Invoice lines reference products
- Product name, code, and price are snapshot at creation
- Base price comes from product.salePrice

### Differential Module
- Differential pricing applied to invoice lines
- Each line can have a different differential
- Price calculation: `unitPrice = basePrice x (1 + differential/100)`

### Warehouse Module
- Stock deducted when invoice is created (if warehouseId on lines)
- Stock restored when invoice is cancelled
- StockMovement with type EXIT created for each line

### Collections Module
- Collection record created when payment is registered
- Only if invoice has a merchant associated
- Commission calculated from payment amount

---

## Exception Handling

### Custom Exceptions
- `InvoiceNotFoundException` - Invoice not found by ID (404 Not Found)
- `InvalidInvoiceStatusException` - Cannot perform action on current status (409 Conflict)
- `InvoiceAlreadyPaidException` - Invoice already fully paid (409 Conflict)
- `InvalidPaymentAmountException` - Payment amount invalid (422 Unprocessable Entity)
- `InsufficientStockException` - Not enough stock (422 Unprocessable Entity)
- `CreditLimitExceededException` - Thrown when a new invoice would cause the customer's outstanding balance to exceed their credit limit (422 Unprocessable Entity)
- `UnauthorizedInvoiceAccessException` - Merchant accessing another's invoice (403 Forbidden)
- `CustomerNotFoundException` - Customer not found (404 Not Found)
- `ProductNotFoundException` - Product not found (404 Not Found)
- `WarehouseNotFoundException` - Warehouse not found (404 Not Found)

---

## Notes for Developers

### Key Design Decisions
1. **Aggregate Root**: Invoice is the aggregate root; InvoiceLine and InvoicePayment are internal entities
2. **Two Creation Paths**: Direct creation OR from Order conversion
3. **Differential at Creation**: Differential pricing is calculated and stored at invoice creation time
4. **Stock Deduction**: Happens at invoice creation, not at order creation
5. **Immutable Payments**: InvoicePayment records cannot be modified once created
6. **Commission on Payment**: Collection is generated per payment, not per invoice
7. **Role-Based Access**: Merchants can only view invoices from their orders

### API Design Rules
- **Invoice sources**: A draft invoice may link multiple draft or confirmed orders and draft customer or administrative quotes belonging to the selected customer. Links are stored in `invoice_order_links` and `invoice_quote_links`.
- **Invoice search**: The list endpoint accepts `search` and matches invoice number, customer name, and customer RIF.
- **DO**: Validate customer exists before creating invoice
- **DO**: Apply differential pricing automatically
- **DO**: Deduct stock at invoice creation (if warehouseId provided)
- **DO**: Create Collection when payment registered (if merchant exists)
- **DO**: Restore stock when invoice cancelled
- **DON'T**: Allow MERCHANT to create/cancel invoices or register payments
- **DON'T**: Allow modifying invoice lines after creation
- **DON'T**: Allow cancelling invoices with payments
- **DON'T**: Allow payments exceeding balance

### Testing Requirements
- Unit tests for all value objects
- Unit tests for Invoice, InvoiceLine, InvoicePayment domain entities
- Unit tests for differential price calculation
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test invoice number generation (year rollover)
- Test differential pricing application
- Test stock deduction and restoration
- Test payment registration and status transitions
- Test Collection creation on payment
- Test role-based access control

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **ORDERS** | Invoices can be created from orders |
| **CUSTOMERS** | Invoices require a customer; creditDays from customer |
| **MERCHANTS** | Invoices can have a merchant for commission |
| **DISPATCHERS** | Dispatchers deliver invoices (PENDING → DELIVERED) |
| **PRODUCTS** | Invoice lines reference products |
| **DIFFERENTIAL** | Pricing adjustments applied to invoice lines |
| **WAREHOUSE** | Stock deducted/restored for invoice lines |
| **COLLECTIONS** | Generated when payment registered (if merchant) |
| **RECEIVABLES** | Customer debt view - aggregates invoices with balance > 0 |
| **PAYABLES** | Merchant commission payments - generated from Collections |

---

**Last Updated**: 2026-03-12

**Module Status**: In Development

## Centralized issuance

`application/issue/IssueInvoiceService` is the internal coordinator shared by quote conversion, confirmed-order creation and manual draft confirmation. Existing public quote/order creation ports keep their neutral contracts; the coordinator's `Invoice` argument is internal to this module.

`InvoiceIssueValidator` performs read-only credit/payment checks and creates a cash payment plan. `InvoicePaymentService` applies balance consumption and settlement. `IssueInvoiceCommand` carries the payment options and `InvoiceOrigin` selects the existing inventory policy.

For CASH issuance, the coordinator persists and flushes the draft before settlement can insert an allocation referencing its identifier. It then applies payment effects, confirms the aggregate, applies the source inventory policy and persists the result in the same transaction. A flush does not commit: failures still roll back invoice, payment, balance, stock and the caller's source status.

Quote conversion deducts the specified warehouse allocations. Confirmed orders already deducted stock at confirmation, so invoice issuance performs no second deduction. Manual drafts confirm existing reservations. Standalone invoice creation still saves a DRAFT without issuing or settling it.

The manual draft path retains its existing invoice cash-register entry and confirmation notification. The confirmed-order path retains created/confirmed notifications; quote notifications remain with the quote conversion use case. This refactor does not redefine accounting entry semantics.
