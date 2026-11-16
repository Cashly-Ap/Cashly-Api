# ORDERS Module

## Overview
The ORDERS module manages sales orders (pedidos) in the invoicing system. Orders can be created by Users or Merchants, and can optionally have a merchant associated for commission tracking. Orders serve as the basis for invoice generation.

**Key Concepts**:
- **Customer Required**: Every order MUST have a customer associated
- **Merchant Optional**: Orders MAY have a merchant associated (for commission tracking)
- **No Validation Required**: Orders do not require validation workflow
- **Delivery Date**: Orders must specify a delivery date
- **Invoice Conversion**: Orders can be converted to invoices

---

## Who Can Create Orders

| Role | Can Create Orders | Merchant Association |
|------|-------------------|---------------------|
| USER | Yes | Optional (can assign any merchant) |
| MERCHANT | Yes | Automatic (self-assigned) |
| ADMIN | Yes | Optional (can assign any merchant) |

**Important**:
- When a **MERCHANT** creates an order, they are automatically associated as the merchant
- When a **USER/ADMIN** creates an order, they can optionally assign a merchant
- Orders without merchant → no commission charged
- Orders with merchant → commission charged when invoice is paid

---

## Order Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            ORDER LIFECYCLE                                   │
└─────────────────────────────────────────────────────────────────────────────┘

  USER / MERCHANT                    ADMIN / USER                    SISTEMA
        │                                 │                              │
        │  1. Crea pedido                │                              │
        │     (con cliente)              │                              │
        ▼                                │                              │
   ┌─────────┐                           │                              │
   │ PEDIDO  │                           │                              │
   │ PENDING │                           │                              │
   └────┬────┘                           │                              │
        │                                │                              │
        │  (puede cancelar               │                              │
        │   mientras PENDING)            │                              │
        │                                │                              │
        └───────────────────────────────►│  2. Convierte a factura     │
                                         │     (botón)                  │
                                         ▼                              │
                                   ┌───────────┐                        │
                                   │  FACTURA  │                        │
                                   │  EMITIDA  │ ──────────────────────►│
                                   └───────────┘                        │
                                         │                              │
                                         │  3. Cliente paga             │
                                         │     la factura               │
                                         ▼                              │
                                   ┌───────────┐                        │
                                   │  FACTURA  │    Si tiene merchant:  │
                                   │  PAGADA   │ ──comisión generada───►│
                                   └───────────┘                        │
                                                                        │
                                                        (solo si order tiene merchant)
```

---

## Module Structure

```
└── sales
    └── orders
        ├── application
        │   ├── cancel
        │   │   ├── request
        │   │   │   └── CancelOrderRequest.java
        │   │   └── use_case
        │   │       └── CancelOrderUseCase.java
        │   ├── confirm
        │   │   └── use_case
        │   │       └── ConfirmOrderUseCase.java
        │   ├── convert_to_invoice
        │   │   ├── request
        │   │   │   └── ConvertToInvoiceRequest.java
        │   │   ├── response
        │   │   │   └── ConvertToInvoiceResponse.java
        │   │   └── use_case
        │   │       └── ConvertToInvoiceUseCase.java
        │   ├── create
        │   │   ├── from_quote
        │   │   │   └── services
        │   │   │       └── QuoteOrderCreationService.java
        │   │   ├── request
        │   │   │   └── CreateOrderRequest.java
        │   │   └── use_case
        │   │       └── CreateOrderUseCase.java
        │   ├── delete
        │   │   └── use_case
        │   │       └── DeleteOrderUseCase.java
        │   ├── dto
        │   │   └── response
        │   │       ├── OrderDetailResponse.java
        │   │       ├── OrderListResponse.java
        │   │       └── OrderProductResponse.java
        │   ├── get
        │   │   └── use_case
        │   │       └── GetOrderByIdUseCase.java
        │   ├── list
        │   │   └── use_case
        │   │       └── GetOrdersUseCase.java
        │   ├── mappers
        │   │   └── OrderMapper.java
        │   ├── ports
        │   │   ├── in
        │   │   │   ├── OrderDocumentFilter.java
        │   │   │   ├── OrderDocumentQuery.java
        │   │   │   ├── OrderDocumentRow.java
        │   │   │   ├── OrderDocumentSnapshot.java
        │   │   │   ├── ProductReferenceQuery.java
        │   │   │   └── QuoteOrderCreationApi.java
        │   │   └── out
        │   │       ├── CustomerCreditGateway.java
        │   │       ├── InvoiceCreationGateway.java
        │   │       ├── OrderActorGateway.java
        │   │       ├── OrderCustomerGateway.java
        │   │       ├── OrderInventoryGateway.java
        │   │       ├── OrderNotificationGateway.java
        │   │       ├── OrderProductCatalogGateway.java
        │   │       └── StockAvailabilityGateway.java
        │   ├── products
        │   │   └── use_case
        │   │       └── GetProductsForOrderUseCase.java
        │   ├── queries
        │   │   └── services
        │   │       └── OrderDocumentQueryService.java
        │   └── update
        │       ├── request
        │       │   └── UpdateOrderRequest.java
        │       └── use_case
        │           └── UpdateOrderUseCase.java
        ├── domain
        │   ├── entity
        │   │   ├── Order.java
        │   │   ├── OrderLine.java
        │   │   └── OrderLineAllocation.java
        │   ├── exceptions
        │   │   ├── OrderCannotBeDeletedException.java
        │   │   ├── OrderNotFoundException.java
        │   │   └── OrderNotPendingException.java
        │   ├── repository
        │   │   └── OrderRepository.java
        │   └── valueobjects
        │       ├── OrderId.java
        │       ├── OrderLineId.java
        │       ├── OrderNumber.java
        │       └── OrderStatus.java
        ├── infrastructure
        │   ├── config
        │   │   ├── OrderConfig.java
        │   │   ├── OrderDocumentQueryConfig.java
        │   │   └── QuoteOrderCreationConfig.java
        │   ├── integration
        │   │   ├── LocalInvoiceCreationGateway.java
        │   │   ├── LocalOrderActorAdapter.java
        │   │   ├── LocalOrderCustomerAdapter.java
        │   │   ├── LocalOrderInventoryGateway.java
        │   │   ├── LocalOrderNotificationAdapter.java
        │   │   ├── LocalOrderProductCatalogAdapter.java
        │   │   ├── OrderCustomerCreditAdapter.java
        │   │   └── OrderStockAvailabilityAdapter.java
        │   ├── persistence
        │   │   ├── adapters
        │   │   │   └── OrderRepositoryAdapter.java
        │   │   ├── entities
        │   │   │   ├── OrderEntity.java
        │   │   │   ├── OrderLineAllocationEntity.java
        │   │   │   ├── OrderLineCategorySnapshotEntity.java
        │   │   │   ├── OrderLineEntity.java
        │   │   │   └── OrderNumberSequenceEntity.java
        │   │   ├── mappers
        │   │   │   └── OrderJpaMapper.java
        │   │   ├── queries
        │   │   │   └── JpaOrdersProductReferenceQuery.java
        │   │   └── repositories
        │   │       ├── OrderJpaRepository.java
        │   │       ├── OrderNumberSequenceJpaRepository.java
        │   │       └── SpringDataOrdersProductReferenceRepository.java
        │   └── web
        │       └── OrderController.java
        └── reporting
            ├── application
            │   ├── dto
            │   │   └── model
            │   │       ├── OrderReportData.java
            │   │       ├── OrderReportFilter.java
            │   │       └── OrderReportRow.java
            │   ├── generate_order_pdf
            │   │   ├── services
            │   │   │   └── GenerateOrderPdfUseCaseImpl.java
            │   │   └── use_case
            │   │       └── GenerateOrderPdfUseCase.java
            │   ├── generate_orders_report_pdf
            │   │   ├── services
            │   │   │   └── GenerateOrdersReportPdfUseCaseImpl.java
            │   │   └── use_case
            │   │       └── GenerateOrdersReportPdfUseCase.java
            │   └── ports
            │       └── out
            │           ├── OrderPdfPort.java
            │           ├── OrderReportQueryGateway.java
            │           └── OrdersReportPdfPort.java
            └── infrastructure
                ├── config
                │   └── OrdersReportConfig.java
                ├── integration
                │   └── OrderDocumentQueryAdapter.java
                ├── pdf
                │   ├── OrderPdfGenerator.java
                │   └── OrdersReportPdfGenerator.java
                └── web
                    └── OrdersReportController.java
```

---

## Domain Model

### Entities

#### Order (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.orders.order.domain.entities.Order`

**Description**: Represents a sales order created by a user or merchant. Each order contains one or more order lines, is associated with a customer, and optionally with a merchant for commission tracking.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `OrderId` (UUID) | Unique identifier | Required, immutable |
| `orderNumber` | `OrderNumber` (String) | Sequential order number | Required, immutable, auto-generated |
| `customerId` | `CustomerId` (UUID) | Customer who receives order | Required |
| `merchantId` | `MerchantId` (UUID) | Merchant for commission | Optional, null if no merchant |
| `createdBy` | `UserId` (UUID) | User/Merchant who created | Required, immutable |
| `createdByType` | `CreatorType` | USER or MERCHANT | Required, immutable |
| `status` | `OrderStatus` | Order status | Required, see enum |
| `deliveryDate` | `LocalDate` | Expected delivery date | Required, must be today or future |
| `notes` | `String` | Order notes/comments | Optional, max 1000 characters |
| `subtotal` | `BigDecimal` | Order subtotal | Calculated, non-negative |
| `invoiceId` | `InvoiceId` (UUID) | Reference to generated invoice | Null until converted |
| `cancelledAt` | `LocalDateTime` | Cancellation timestamp | Null unless cancelled |
| `cancelledReason` | `String` | Reason for cancellation | Null unless cancelled |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |
| `orderLines` | `List<OrderLine>` | Order line items | At least 1 required |

**Business Methods**:
- `create(...)` - Factory method to create new order
- `reconstitute(...)` - Factory method to rebuild order from database
- `addLine(OrderLine)` - Adds a line item
- `removeLine(OrderLineId)` - Removes a line item
- `updateLine(OrderLineId, quantity, unitPrice)` - Updates a line item
- `calculateSubtotal()` - Calculates order subtotal
- `cancel(String reason)` - Cancels the order (only if PENDING)
- `convertToInvoice(InvoiceId)` - Links to generated invoice
- `hasMerchant()` - Returns true if order has merchant associated

**Business Rules**:
1. Order number is auto-generated (ORD-2025-00001)
2. Orders start with status PENDING
3. Only PENDING orders can be cancelled
4. Only PENDING orders can be converted to invoices
5. Cancelled orders cannot be modified
6. Order must have at least 1 line item
7. Customer is REQUIRED for all orders
8. Merchant is OPTIONAL (for commission tracking)
9. Delivery date must be today or in the future
10. When merchant creates order, they are automatically set as merchantId

---

#### OrderLine (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.orders.order.domain.entities.OrderLine`

**Description**: Represents a line item in an order. **ALWAYS managed through Order aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `OrderLineId` (UUID) | Unique identifier | Required, immutable |
| `orderId` | `OrderId` (UUID) | Parent order ID | Required, immutable |
| `productId` | `ProductId` (UUID) | Product reference | Required |
| `productName` | `String` | Product name snapshot | Required |
| `productCode` | `String` | Product code snapshot | Required |
| `quantity` | `BigDecimal` | Quantity ordered | Required, > 0 |
| `unitPrice` | `BigDecimal` | Price per unit | Required, >= 0 |
| `lineTotal` | `BigDecimal` | Line total (qty × price) | Calculated |

**Business Rules**:
1. **NO direct API endpoints** - managed only through Order
2. Line total is automatically calculated
3. Product name and code are snapshots at order creation time
4. Quantity must be greater than zero
5. Cannot exist independently outside Order aggregate

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `OrderId` | UUID | Order unique identifier | Non-null UUID |
| `OrderNumber` | String | Sequential order number | Format: ORD-YYYY-NNNNN |
| `OrderLineId` | UUID | Line item identifier | Non-null UUID |
| `CustomerId` | UUID | Customer reference | Non-null UUID |
| `MerchantId` | UUID | Merchant reference | Nullable UUID |
| `UserId` | UUID | User reference | Non-null UUID |

---

### Enums

#### OrderStatus
```java
public enum OrderStatus {
    PENDING,      // Created, awaiting conversion to invoice
    INVOICED,     // Converted to invoice
    CANCELLED     // Cancelled by user/merchant
}
```

#### CreatorType
```java
public enum CreatorType {
    USER,         // Created by a system user (ADMIN/USER role)
    MERCHANT      // Created by a merchant
}
```

---

## API Endpoints

### Order Management

#### 1. Create Order
**Endpoint**: `POST /api/orders`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN, MERCHANT

**Description**: Creates a new order. When a MERCHANT creates an order, they are automatically assigned as the merchant.

**Request Body** (`CreateOrderRequest`):
```json
{
  "customerId": "customer-uuid-here",
  "merchantId": "merchant-uuid-here",
  "deliveryDate": "2025-01-15",
  "notes": "Entrega en horario de oficina",
  "lines": [
    {
      "productId": "product-001-uuid",
      "quantity": 10,
      "unitPrice": 100.00
    },
    {
      "productId": "product-002-uuid",
      "quantity": 5.5,
      "unitPrice": 50.00
    }
  ]
}
```

**Notes on merchantId**:
- **USER/ADMIN**: Can send `merchantId` (optional) or omit it (null)
- **MERCHANT**: The `merchantId` field is **ignored** - system automatically assigns the authenticated merchant

**Response** (201 Created) - `OrderCreateResponse`:
```json
{
  "id": "order-001-uuid",
  "orderNumber": "ORD-2025-00001",
  "customerId": "customer-uuid-here",
  "customerName": "ABC Company S.A.",
  "merchantId": "merchant-uuid-here",
  "merchantName": "Juan Perez",
  "createdBy": "user-uuid-here",
  "createdByType": "USER",
  "status": "PENDING",
  "deliveryDate": "2025-01-15",
  "notes": "Entrega en horario de oficina",
  "subtotal": 1275.00,
  "invoiceId": null,
  "createdAt": "2025-12-30T10:30:00",
  "lines": [
    {
      "id": "line-001-uuid",
      "productId": "product-001-uuid",
      "productName": "Producto A",
      "productCode": "PRD-00001",
      "quantity": 10,
      "unitPrice": 100.00,
      "lineTotal": 1000.00
    },
    {
      "id": "line-002-uuid",
      "productId": "product-002-uuid",
      "productName": "Producto B",
      "productCode": "PRD-00002",
      "quantity": 5.5,
      "unitPrice": 50.00,
      "lineTotal": 275.00
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Customer or product not found
- `422 Unprocessable Entity`: Delivery date is in the past

---

#### 2. Get Order by ID
**Endpoint**: `GET /api/orders/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view all orders
- MERCHANT: Can only view own orders (where merchantId = authenticated merchant)

**Description**: Retrieves a specific order by ID.

**Path Parameters**:
- `id` (UUID) - Order unique identifier

**Response** (200 OK) - `OrderDetailResponse`:
```json
{
  "id": "order-001-uuid",
  "orderNumber": "ORD-2025-00001",
  "customerId": "customer-uuid-here",
  "customerName": "ABC Company S.A.",
  "customerRif": "J-12345678-9",
  "merchantId": "merchant-uuid-here",
  "merchantName": "Juan Perez",
  "merchantCommission": 15.00,
  "createdBy": "user-uuid-here",
  "createdByName": "Admin User",
  "createdByType": "USER",
  "status": "PENDING",
  "deliveryDate": "2025-01-15",
  "notes": "Entrega en horario de oficina",
  "subtotal": 1275.00,
  "invoiceId": null,
  "invoiceNumber": null,
  "cancelledAt": null,
  "cancelledReason": null,
  "createdAt": "2025-12-30T10:30:00",
  "updatedAt": "2025-12-30T10:30:00",
  "lines": [
    {
      "id": "line-001-uuid",
      "productId": "product-001-uuid",
      "productName": "Producto A",
      "productCode": "PRD-00001",
      "quantity": 10,
      "unitPrice": 100.00,
      "lineTotal": 1000.00
    }
  ]
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Merchant trying to access another merchant's order
- `404 Not Found`: Order not found

---

#### 3. List Orders
**Endpoint**: `GET /api/orders`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view all orders
- MERCHANT: Can only view own orders

**Query Parameters** (optional):
- `customerId` (UUID) - Filter by customer
- `merchantId` (UUID) - Filter by merchant (ignored for MERCHANT role)
- `status` (string) - Filter by status (PENDING, INVOICED, CANCELLED)
- `fromDate` (date) - Filter from delivery date
- `toDate` (date) - Filter to delivery date
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 20)

**Response** (200 OK) - `List<OrderListResponse>`:
```json
[
  {
    "id": "order-001-uuid",
    "orderNumber": "ORD-2025-00001",
    "customerId": "customer-uuid-here",
    "customerName": "ABC Company S.A.",
    "merchantId": "merchant-uuid-here",
    "merchantName": "Juan Perez",
    "status": "PENDING",
    "deliveryDate": "2025-01-15",
    "subtotal": 1275.00,
    "lineCount": 2,
    "invoiceId": null,
    "createdAt": "2025-12-30T10:30:00"
  }
]
```

---

#### 4. Update Order
**Endpoint**: `PUT /api/orders/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can update any PENDING order
- MERCHANT: Can only update own PENDING orders

**Description**: Updates an existing order. Only PENDING orders can be updated.

**Request Body** (`UpdateOrderRequest`):
```json
{
  "deliveryDate": "2025-01-20",
  "notes": "Nueva nota actualizada",
  "lines": [
    {
      "productId": "product-001-uuid",
      "quantity": 15,
      "unitPrice": 100.00
    }
  ]
}
```

**Note**: `customerId` and `merchantId` cannot be changed after creation.

**Response** (200 OK) - `OrderDetailResponse`

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Merchant trying to update another's order
- `404 Not Found`: Order not found
- `409 Conflict`: Cannot update order (not in PENDING status)

---

#### 5. Cancel Order
**Endpoint**: `PUT /api/orders/{id}/cancel`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can cancel any PENDING order
- MERCHANT: Can only cancel own PENDING orders

**Description**: Cancels a pending order.

**Request Body**:
```json
{
  "reason": "Cliente solicitó cancelación"
}
```

**Response** (200 OK) - `OrderDetailResponse`:
```json
{
  "id": "order-001-uuid",
  "orderNumber": "ORD-2025-00001",
  "status": "CANCELLED",
  "cancelledAt": "2025-12-30T11:00:00",
  "cancelledReason": "Cliente solicitó cancelación",
  ...
}
```

**Errors**:
- `400 Bad Request`: Reason is required
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Merchant trying to cancel another's order
- `404 Not Found`: Order not found
- `409 Conflict`: Cannot cancel order (not in PENDING status)

---

#### 6. Convert Order to Invoice
**Endpoint**: `POST /api/orders/{id}/convert-to-invoice`

**Authentication**: Required (JWT Bearer Token)

**Roles**: USER, ADMIN only (MERCHANTS cannot convert orders)

**Description**: Converts a pending order into a real invoice. This is a manual action triggered by a button in the UI. The invoice is created with all order data (customer, lines, merchant, notes).

**Request Body**: None

**Response** (201 Created) - `ConvertToInvoiceResponse`:
```json
{
  "orderId": "order-001-uuid",
  "orderNumber": "ORD-2025-00001",
  "orderStatus": "INVOICED",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "hasMerchant": true,
  "merchantId": "merchant-uuid",
  "merchantName": "Juan Perez",
  "merchantCommission": 15.00,
  "customerId": "customer-uuid",
  "customerName": "ABC Company S.A.",
  "issueDate": "2025-01-13",
  "dueDate": "2025-02-12",
  "creditDays": 30,
  "subtotal": 1275.00,
  "lineCount": 2
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT role cannot convert orders
- `404 Not Found`: Order not found
- `404 Not Found`: Customer not found
- `409 Conflict`: Order is not in PENDING status
- `409 Conflict`: Order already has an invoice (already converted)

**Business Logic**:
1. Validates order is in PENDING status
2. Validates order has not been converted before
3. Validates order has at least one line
4. Fetches customer data (name, RIF, credit days)
5. Generates new invoice number
6. **Creates real invoice in database** with:
   - Customer info (ID, name, RIF)
   - Merchant ID (if order has merchant)
   - Order ID reference
   - Credit days from customer
   - Notes from order
   - All order lines copied to invoice lines
7. Links order to invoice (sets invoiceId on order)
8. Updates order status to INVOICED
9. **If order has merchant**: When invoice is paid, commission charge is generated

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### CreateOrderRequest
```java
public record CreateOrderRequest(
    @NotNull UUID customerId,                           // Required
    UUID merchantId,                                     // Optional (ignored for MERCHANT role)
    @NotNull @FutureOrPresent LocalDate deliveryDate,   // Required, today or future
    @Size(max = 1000) String notes,                     // Optional
    @Valid @NotEmpty List<OrderLineRequest> lines       // At least 1 required
)
```

#### UpdateOrderRequest
```java
public record UpdateOrderRequest(
    @NotNull @FutureOrPresent LocalDate deliveryDate,   // Required
    @Size(max = 1000) String notes,                     // Optional
    @Valid @NotEmpty List<OrderLineRequest> lines       // Replaces all lines
)
```

#### OrderLineRequest
```java
public record OrderLineRequest(
    @NotNull UUID productId,
    @NotNull @Positive BigDecimal quantity,
    @NotNull @PositiveOrZero BigDecimal unitPrice
)
```

#### CancelOrderRequest
```java
public record CancelOrderRequest(
    @NotBlank @Size(max = 500) String reason
)
```

---

### Response DTOs

#### OrderListResponse
```java
public record OrderListResponse(
    String id,
    String orderNumber,
    String customerId,
    String customerName,
    String merchantId,              // null if no merchant
    String merchantName,            // null if no merchant
    String status,
    LocalDate deliveryDate,
    BigDecimal subtotal,
    int lineCount,
    String invoiceId,               // null if not invoiced
    LocalDateTime createdAt
)
```

#### OrderDetailResponse
```java
public record OrderDetailResponse(
    String id,
    String orderNumber,
    String customerId,
    String customerName,
    String customerRif,
    String merchantId,              // null if no merchant
    String merchantName,            // null if no merchant
    BigDecimal merchantCommission,  // null if no merchant
    String createdBy,
    String createdByName,
    String createdByType,           // USER or MERCHANT
    String status,
    LocalDate deliveryDate,
    String notes,
    BigDecimal subtotal,
    String invoiceId,               // null if not invoiced
    String invoiceNumber,           // null if not invoiced
    LocalDateTime cancelledAt,      // null if not cancelled
    String cancelledReason,         // null if not cancelled
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderLineResponse> lines
)
```

#### OrderCreateResponse
```java
public record OrderCreateResponse(
    String id,
    String orderNumber,
    String customerId,
    String customerName,
    String merchantId,
    String merchantName,
    String createdBy,
    String createdByType,
    String status,
    LocalDate deliveryDate,
    String notes,
    BigDecimal subtotal,
    String invoiceId,
    LocalDateTime createdAt,
    List<OrderLineResponse> lines
)
```

#### OrderLineResponse
```java
public record OrderLineResponse(
    String id,
    String productId,
    String productName,
    String productCode,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
)
```

#### ConvertToInvoiceResponse
```java
public record ConvertToInvoiceResponse(
    String orderId,
    String orderNumber,
    String orderStatus,
    String invoiceId,
    String invoiceNumber,
    boolean hasMerchant,
    String merchantId,
    String merchantName,
    BigDecimal merchantCommission,
    String customerId,
    String customerName,
    LocalDate issueDate,
    LocalDate dueDate,
    int creditDays,
    BigDecimal subtotal,
    int lineCount
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/orders | None | List\<OrderListResponse\> |
| GET /api/orders/{id} | None | OrderDetailResponse |
| POST /api/orders | CreateOrderRequest | OrderCreateResponse |
| PUT /api/orders/{id} | UpdateOrderRequest | OrderDetailResponse |
| PUT /api/orders/{id}/cancel | CancelOrderRequest | OrderDetailResponse |
| POST /api/orders/{id}/convert-to-invoice | None | ConvertToInvoiceResponse |

---

## Use Cases

### Order Management Use Cases
1. **CreateOrderUseCase** - Creates a new order (handles MERCHANT auto-assignment)
2. **GetOrderUseCase** - Retrieves order by ID (with role-based access)
3. **ListOrdersUseCase** - Lists orders with filters (role-based filtering)
4. **UpdateOrderUseCase** - Updates a pending order
5. **CancelOrderUseCase** - Cancels a pending order
6. **ConvertToInvoiceUseCase** - Converts order to invoice (USER/ADMIN only)

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAll();
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findByMerchantId(MerchantId merchantId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByDeliveryDateBetween(LocalDate from, LocalDate to);
    void delete(OrderId id);
    boolean existsById(OrderId id);
    String getNextOrderNumber();
}
```

---

## Domain Services

### OrderNumberGenerator
**Responsibilities**:
- Generate sequential order numbers
- Format: ORD-YYYY-NNNNN (e.g., ORD-2025-00001)
- Reset sequence each year

**Implementation**: `SequentialOrderNumberGenerator`
- Queries database for max order number of current year
- Increments by 1
- Starts from 00001 each new year

---

## Business Rules Summary

### Order Creation
1. Customer is REQUIRED for all orders
2. Merchant is OPTIONAL for USER/ADMIN created orders
3. When MERCHANT creates order, they are automatically assigned as merchantId
4. Delivery date must be today or in the future
5. At least one order line is required
6. Product snapshots (name, code) are captured at creation time
7. Subtotal is automatically calculated from lines

### Order Lifecycle
1. Orders start with status PENDING
2. Only PENDING orders can be updated
3. Only PENDING orders can be cancelled
4. Only PENDING orders can be converted to invoices
5. Cancelled orders cannot be modified

### Order Status Transitions
```
PENDING ──┬──► INVOICED (via convert-to-invoice)
          │
          └──► CANCELLED (via cancel)
```

### Access Control
1. USER/ADMIN can view all orders
2. USER/ADMIN can update/cancel any PENDING order
3. USER/ADMIN can convert any PENDING order to invoice
4. MERCHANT can only view their own orders
5. MERCHANT can only update/cancel their own PENDING orders
6. MERCHANT cannot convert orders to invoices

### Commission Rules
1. Commission only applies if order has a merchant associated
2. **Commission is NOT calculated on the order**
3. Commission is charged when the **invoice is paid**
4. Commission = Invoice Total × (Merchant Commission % / 100)
5. Orders without merchant → no commission charged

---

## Database Schema

### orders Table
```sql
CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36),
    created_by VARCHAR(36) NOT NULL,
    created_by_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    delivery_date DATE NOT NULL,
    notes VARCHAR(1000),
    subtotal DECIMAL(15,2) NOT NULL,
    invoice_id VARCHAR(36),
    cancelled_at TIMESTAMP,
    cancelled_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    INDEX idx_order_number (order_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_status (status),
    INDEX idx_delivery_date (delivery_date),
    INDEX idx_created_at (created_at)
);
```

### order_lines Table
```sql
CREATE TABLE order_lines (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity DECIMAL(15,3) NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_id (order_id)
);
```

---

## Integration with Other Modules

### Customers Module
- Order REQUIRES a valid customer
- Customer name and RIF are shown in order responses
- Orders reference customer by `customerId`

### Merchants Module
- Order can OPTIONALLY have a merchant
- Merchant commission percentage is retrieved for display
- When MERCHANT creates order, they are auto-assigned
- Commission is charged when invoice is paid

### Products Module
- Order lines reference products
- Product name and code are snapshot at creation
- Stock validation on convert to invoice
- Stock decremented when invoice is created

### Invoices Module
- Orders can be converted to invoices
- `invoiceId` is set when converted
- Invoice inherits customer, merchant, and lines from order
- Commission tracking happens in invoice payment

---

## Exception Handling

### Custom Exceptions
- `OrderNotFoundException` - Order not found by ID (404 Not Found)
- `InvalidOrderStatusException` - Cannot perform action on current status (409 Conflict)
- `OrderAlreadyInvoicedException` - Order already converted to invoice (409 Conflict)
- `UnauthorizedOrderAccessException` - Merchant accessing another's order (403 Forbidden)
- `CustomerNotFoundException` - Customer not found (404 Not Found)
- `MerchantNotFoundException` - Merchant not found (404 Not Found)
- `ProductNotFoundException` - Product not found (404 Not Found)
- `InsufficientStockException` - Not enough stock (422 Unprocessable Entity)

---

## Notes for Developers

### Key Design Decisions
1. **Aggregate Root**: Order is the aggregate root, OrderLine is internal entity
2. **Customer Required**: All orders must have a customer
3. **Merchant Optional**: Merchant association is for commission tracking only
4. **No Validation Workflow**: Orders go directly from PENDING to INVOICED
5. **Snapshot Pattern**: Product name/code are captured at order creation
6. **Role-Based Access**: Merchants can only see/modify their own orders

### API Design Rules
- **DO**: Validate customer exists before creating order
- **DO**: Auto-assign merchant when MERCHANT role creates order
- **DO**: Ignore merchantId in request when MERCHANT creates order
- **DO**: Filter orders by merchantId for MERCHANT role
- **DO**: Validate stock before converting to invoice
- **DON'T**: Allow changing customerId after creation
- **DON'T**: Allow changing merchantId after creation
- **DON'T**: Allow MERCHANT to convert orders to invoices

### Testing Requirements
- Unit tests for all value objects
- Unit tests for Order and OrderLine domain entities
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test order number generation (year rollover)
- Test MERCHANT auto-assignment on create
- Test role-based access control
- Test order status transitions
- Test convert to invoice flow
- Test stock validation on convert

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **CUSTOMERS** | Orders require a customer (customerId) |
| **MERCHANTS** | Orders can optionally have a merchant (merchantId) |
| **PRODUCTS** | Order lines reference products |
| **INVOICES** | Orders are converted to invoices |
| **WAREHOUSE** | Stock is validated/decremented on invoice creation |

---

**Last Updated**: 2025-12-30

**Module Status**: Planned
