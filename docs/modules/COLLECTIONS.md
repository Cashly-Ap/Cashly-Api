# COLLECTIONS Module

## Overview
The COLLECTIONS module manages commission records (cobranzas) that are automatically generated when payments are registered on invoices that have an associated merchant. Each collection represents the commission earned by a merchant on a specific payment.

**Key Concepts**:
- **Automatic Generation**: Collections are created automatically when a payment is registered (and approved)
- **Merchant Required**: Collections only exist for invoices with a merchant
- **Commission Calculation**: Commission = payment amount x (merchant.commissionPercentage / 100)
- **Immutable Records**: Once created, collections cannot be modified
- **Payment Link**: Each collection is linked to a specific invoice payment
- **Payables Integration**: Each collection triggers creation of a Payable (commission owed to merchant)

**Important**: Collections are created when:
- USER/ADMIN registers a payment directly → Collection created immediately
- MERCHANT registers a payment → Collection created only after payment is APPROVED (see RECEIVABLES module)

---

## Collection Flow

```
                         COLLECTION LIFECYCLE
 
  Invoice has Merchant?
            |
     +------+------+
     |             |
     NO            YES
     |             |
     v             v
  [No action]   Payment registered
                (and approved if from MERCHANT)
                     |
                     v
              +-----------------+
              |   COLLECTION    |
              |    CREATED      |
              +-----------------+
                     |
                     | Commission = amount x (merchant.commission% / 100)
                     v
              +-----------------+
              |   PAYABLE       |
              |   CREATED       |
              +-----------------+
                     |
                     | Merchant owes commission to company
                     v
              +-----------------+
              |   Reporting     |
              |   (summaries)   |
              +-----------------+
```

---

## Module Structure

```
collections/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   (no request DTOs - collections are auto-generated)
│   │   └── response/
│   │       ├── CollectionListResponse.java
│   │       ├── CollectionDetailResponse.java
│   │       └── CollectionSummaryResponse.java
│   ├── exceptions/
│   │   └── CollectionNotFoundException.java
│   ├── mappers/
│   │   └── CollectionMapper.java
│   └── usecases/
│       ├── CreateCollectionUseCase.java
│       ├── GetCollectionUseCase.java
│       ├── ListCollectionsUseCase.java
│       ├── ListCollectionsByMerchantUseCase.java
│       └── GetMerchantCollectionSummaryUseCase.java
├── domain/
│   ├── entities/
│   │   └── Collection.java
│   ├── repositories/
│   │   └── CollectionRepository.java
│   └── valueobjects/
│       ├── CollectionId.java
│       └── CommissionAmount.java
└── infrastructure/
    ├── config/
    │   └── CollectionBeanConfig.java
    ├── persistence/
    │   ├── entities/
    │   │   └── CollectionJpaEntity.java
    │   ├── mappers/
    │   │   └── CollectionJpaMapper.java
    │   └── repositories/
    │       ├── JpaCollectionRepository.java
    │       └── CollectionRepositoryAdapter.java
    └── web/
        └── CollectionController.java
```

---

## Domain Model

### Entities

#### Collection (Aggregate Root - Immutable)
**Domain Entity**: `com.lexand.lexand_api.collections.domain.entities.Collection`

**Description**: Represents a commission record generated when a payment is made on an invoice with an associated merchant. **Immutable once created - cannot be modified or deleted.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `CollectionId` (UUID) | Unique identifier | Required, immutable |
| `invoiceId` | `InvoiceId` (UUID) | Invoice reference | Required, immutable |
| `paymentId` | `InvoicePaymentId` (UUID) | Payment reference | Required, immutable |
| `customerId` | `CustomerId` (UUID) | Customer reference | Required, immutable |
| `merchantId` | `MerchantId` (UUID) | Merchant who earns commission | Required, immutable |
| `amountCollected` | `BigDecimal` | Payment amount | Required, > 0, immutable |
| `commissionPercentage` | `BigDecimal` | Merchant commission % snapshot | Required, 0-100, immutable |
| `commissionAmount` | `CommissionAmount` (BigDecimal) | Calculated commission | Required, >= 0, immutable |
| `collectionDate` | `LocalDate` | Date of payment/collection | Required, immutable |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated, immutable |

**Business Methods**:
- `create(...)` - Factory method to create new collection (only method)
- `reconstitute(...)` - Factory method to rebuild collection from database

**Business Rules**:
1. **Immutable**: Once created, cannot be modified or deleted
2. **Auto-generated**: Created automatically when payment registered on invoice with merchant
3. **Commission Snapshot**: `commissionPercentage` is snapshot at creation time
4. **Commission Formula**: `commissionAmount = amountCollected x (commissionPercentage / 100)`
5. **One-to-One with Payment**: Each payment generates exactly one collection (if merchant exists)
6. **Merchant Required**: Cannot create collection without merchant

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `CollectionId` | UUID | Collection unique identifier | Non-null UUID |
| `InvoiceId` | UUID | Invoice reference | Non-null UUID |
| `InvoicePaymentId` | UUID | Payment reference | Non-null UUID |
| `CustomerId` | UUID | Customer reference | Non-null UUID |
| `MerchantId` | UUID | Merchant reference | Non-null UUID |
| `CommissionAmount` | BigDecimal | Calculated commission | Non-negative, scale 2 |

---

## API Endpoints

### Collection Management

#### 1. List Collections
**Endpoint**: `GET /api/collections`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view all collections
- MERCHANT: Can only view own collections (auto-filtered)

**Query Parameters** (optional):
| Parameter | Type | Description |
|-----------|------|-------------|
| `merchantId` | UUID | Filter by merchant (ignored for MERCHANT role) |
| `customerId` | UUID | Filter by customer |
| `invoiceId` | UUID | Filter by invoice |
| `fromDate` | Date | Filter from collection date |
| `toDate` | Date | Filter to collection date |
| `search` | String | Free-text search on invoice number or customer name |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |
| `sort` | String | Sort field (default: collectionDate,desc) |

**Response** (200 OK) - Paginated `List<CollectionListResponse>`:
```json
{
  "content": [
    {
      "id": "collection-001-uuid",
      "invoiceId": "invoice-001-uuid",
      "invoiceNumber": "INV-2025-00001",
      "paymentId": "payment-001-uuid",
      "customerId": "customer-uuid-here",
      "customerName": "ABC Company S.A.",
      "merchantId": "merchant-uuid-here",
      "merchantName": "Juan Perez",
      "amountCollected": 500.00,
      "commissionPercentage": 15.00,
      "commissionAmount": 75.00,
      "collectionDate": "2025-01-15",
      "createdAt": "2025-01-15T14:00:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 2,
  "size": 20,
  "number": 0
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

#### 2. Get Collection by ID
**Endpoint**: `GET /api/collections/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view any collection
- MERCHANT: Can only view own collections

**Description**: Retrieves a specific collection by ID.

**Path Parameters**:
- `id` (UUID) - Collection unique identifier

**Response** (200 OK) - `CollectionDetailResponse`:
```json
{
  "id": "collection-001-uuid",
  "invoiceId": "invoice-001-uuid",
  "invoiceNumber": "INV-2025-00001",
  "paymentId": "payment-001-uuid",
  "customerId": "customer-uuid-here",
  "customerName": "ABC Company S.A.",
  "customerRif": "J-12345678-9",
  "merchantId": "merchant-uuid-here",
  "merchantName": "Juan Perez",
  "amountCollected": 500.00,
  "commissionPercentage": 15.00,
  "commissionAmount": 75.00,
  "collectionDate": "2025-01-15",
  "createdAt": "2025-01-15T14:00:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT trying to access another merchant's collection
- `404 Not Found`: Collection not found

---

#### 3. List Collections by Merchant
**Endpoint**: `GET /api/collections/merchant/{merchantId}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view any merchant's collections
- MERCHANT: Can only view own collections (merchantId must match)

**Description**: Retrieves all collections for a specific merchant.

**Path Parameters**:
- `merchantId` (UUID) - Merchant unique identifier

**Query Parameters** (optional):
| Parameter | Type | Description |
|-----------|------|-------------|
| `fromDate` | Date | Filter from collection date |
| `toDate` | Date | Filter to collection date |
| `search` | String | Free-text search on invoice number or customer name |
| `page` | Integer | Page number (default: 0) |
| `size` | Integer | Page size (default: 20) |

**Response** (200 OK) - Paginated `List<CollectionListResponse>`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT trying to access another merchant's collections
- `404 Not Found`: Merchant not found

---

#### 4. Get Merchant Collection Summary
**Endpoint**: `GET /api/collections/merchant/{merchantId}/summary`

**Authentication**: Required (JWT Bearer Token)

**Roles**: 
- USER, ADMIN: Can view any merchant's summary
- MERCHANT: Can only view own summary

**Description**: Retrieves a summary of collections for a merchant within a date range.

**Path Parameters**:
- `merchantId` (UUID) - Merchant unique identifier

**Query Parameters**:
| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `fromDate` | Date | Start of period | First day of current month |
| `toDate` | Date | End of period | Today |

**Response** (200 OK) - `CollectionSummaryResponse`:
```json
{
  "merchantId": "merchant-uuid-here",
  "merchantName": "Juan Perez",
  "merchantCommissionPercentage": 15.00,
  "fromDate": "2025-01-01",
  "toDate": "2025-01-15",
  "totalCollections": 10,
  "totalAmountCollected": 5000.00,
  "totalCommission": 750.00,
  "averageCommission": 75.00,
  "collections": [
    {
      "collectionDate": "2025-01-15",
      "count": 3,
      "amountCollected": 1500.00,
      "commissionAmount": 225.00
    },
    {
      "collectionDate": "2025-01-10",
      "count": 5,
      "amountCollected": 2500.00,
      "commissionAmount": 375.00
    },
    {
      "collectionDate": "2025-01-05",
      "count": 2,
      "amountCollected": 1000.00,
      "commissionAmount": 150.00
    }
  ]
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: MERCHANT trying to access another merchant's summary
- `404 Not Found`: Merchant not found

---

## DTOs (Data Transfer Objects)

### Request DTOs

**Note**: Collections are auto-generated. There are NO request DTOs for creating collections.

The `CreateCollectionUseCase` is called internally by `RegisterPaymentUseCase` in the Invoices module.

---

### Response DTOs

#### CollectionListResponse
```java
public record CollectionListResponse(
    String id,
    String invoiceId,
    String invoiceNumber,
    String paymentId,
    String customerId,
    String customerName,
    String merchantId,
    String merchantName,
    BigDecimal amountCollected,
    BigDecimal commissionPercentage,
    BigDecimal commissionAmount,
    LocalDate collectionDate,
    LocalDateTime createdAt
)
```

#### CollectionDetailResponse
```java
public record CollectionDetailResponse(
    String id,
    String invoiceId,
    String invoiceNumber,
    String paymentId,
    String customerId,
    String customerName,
    String customerRif,
    String merchantId,
    String merchantName,
    BigDecimal amountCollected,
    BigDecimal commissionPercentage,
    BigDecimal commissionAmount,
    LocalDate collectionDate,
    LocalDateTime createdAt
)
```

#### CollectionSummaryResponse
```java
public record CollectionSummaryResponse(
    String merchantId,
    String merchantName,
    BigDecimal merchantCommissionPercentage,
    LocalDate fromDate,
    LocalDate toDate,
    int totalCollections,
    BigDecimal totalAmountCollected,
    BigDecimal totalCommission,
    BigDecimal averageCommission,
    List<DailyCollectionSummary> collections
)
```

#### DailyCollectionSummary
```java
public record DailyCollectionSummary(
    LocalDate collectionDate,
    int count,
    BigDecimal amountCollected,
    BigDecimal commissionAmount
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/collections | None | Page\<CollectionListResponse\> |
| GET /api/collections/{id} | None | CollectionDetailResponse |
| GET /api/collections/merchant/{merchantId} | None | Page\<CollectionListResponse\> |
| GET /api/collections/merchant/{merchantId}/summary | None | CollectionSummaryResponse |

---

## Use Cases

### Collection Management Use Cases
1. **CreateCollectionUseCase** - Creates a new collection (internal, called by Invoices module)
2. **GetCollectionUseCase** - Retrieves collection by ID (with role-based access)
3. **ListCollectionsUseCase** - Lists collections with filters (role-based filtering); supports `search: String (optional)` for free-text search on invoice number or customer name
4. **ListCollectionsByMerchantUseCase** - Lists collections for specific merchant; supports `search: String (optional)` for free-text search on invoice number or customer name
5. **GetMerchantCollectionSummaryUseCase** - Calculates collection summary for merchant

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface CollectionRepository {
    Collection save(Collection collection);
    Optional<Collection> findById(CollectionId id);
    List<Collection> findByMerchantId(MerchantId merchantId);
    List<Collection> findByInvoiceId(InvoiceId invoiceId);
    List<Collection> findByPaymentId(InvoicePaymentId paymentId);
    List<Collection> findByCustomerId(CustomerId customerId);
    List<Collection> findByMerchantIdAndCollectionDateBetween(
        MerchantId merchantId, 
        LocalDate fromDate, 
        LocalDate toDate
    );
    Page<Collection> findAll(Specification<Collection> spec, Pageable pageable);
    boolean existsByPaymentId(InvoicePaymentId paymentId);
    
    // Aggregation queries for summaries
    BigDecimal sumAmountCollectedByMerchantIdAndDateRange(
        MerchantId merchantId, 
        LocalDate fromDate, 
        LocalDate toDate
    );
    BigDecimal sumCommissionByMerchantIdAndDateRange(
        MerchantId merchantId, 
        LocalDate fromDate, 
        LocalDate toDate
    );
    long countByMerchantIdAndDateRange(
        MerchantId merchantId, 
        LocalDate fromDate, 
        LocalDate toDate
    );
}
```

---

## Business Rules Summary

### Collection Creation
1. Collections are created **automatically** when payment is registered
2. Collection is only created if invoice has a merchant
3. One collection per payment (1:1 relationship)
4. Cannot create collection without merchant
5. Cannot create duplicate collection for same payment

### Commission Calculation
1. **Formula**: `commissionAmount = amountCollected x (commissionPercentage / 100)`
2. `commissionPercentage` is snapshot from `Merchant.commissionPercentage` at creation time
3. Commission is calculated per payment, not per invoice
4. Partial payments generate proportional commissions

### Immutability
1. Collections cannot be modified after creation
2. Collections cannot be deleted
3. All fields are immutable (set only at creation)
4. Historical accuracy is preserved

### Access Control
1. USER/ADMIN can view all collections
2. MERCHANT can only view their own collections
3. No create/update/delete endpoints for external use

### Reporting
1. Summary reports group by date
2. Summary includes totals and averages
3. Date range filtering supported
4. Merchant-specific summaries

---

## Database Schema

### collections Table
```sql
CREATE TABLE collections (
    id VARCHAR(36) PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    payment_id VARCHAR(36) NOT NULL UNIQUE,
    customer_id VARCHAR(36) NOT NULL,
    merchant_id VARCHAR(36) NOT NULL,
    amount_collected DECIMAL(15,2) NOT NULL,
    commission_percentage DECIMAL(5,2) NOT NULL,
    commission_amount DECIMAL(15,2) NOT NULL,
    collection_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (payment_id) REFERENCES invoice_payments(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (merchant_id) REFERENCES merchants(id),
    
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_collection_date (collection_date),
    INDEX idx_merchant_date (merchant_id, collection_date),
    
    CONSTRAINT uk_payment_id UNIQUE (payment_id),
    CONSTRAINT chk_amount_positive CHECK (amount_collected > 0),
    CONSTRAINT chk_commission_percentage CHECK (commission_percentage >= 0 AND commission_percentage <= 100),
    CONSTRAINT chk_commission_amount CHECK (commission_amount >= 0)
);
```

---

## Integration with Other Modules

### Invoices Module
- Collection created when payment registered on invoice (USER/ADMIN direct payment)
- Requires invoice to have merchantId
- Collection links to invoiceId and paymentId
- Collection inherits customerId from invoice

### Receivables Module
- When MERCHANT registers payment → goes to PENDING_APPROVAL
- When payment is APPROVED → Collection is created
- Collection not created for REJECTED payments

### Payables Module
- Each Collection automatically creates a Payable
- Payable represents commission owed to merchant
- Payable can be settled (paid to merchant) by USER/ADMIN

### Merchants Module
- Collection requires a merchant
- `commissionPercentage` snapshot from merchant at creation
- Merchant can view their own collections
- Summary reports per merchant

### Customers Module
- Collection stores customerId for reference
- Customer info displayed in collection responses

---

## Exception Handling

### Custom Exceptions
- `CollectionNotFoundException` - Collection not found by ID (404 Not Found)
- `UnauthorizedCollectionAccessException` - Merchant accessing another's collection (403 Forbidden)
- `MerchantNotFoundException` - Merchant not found (404 Not Found)

---

## Notes for Developers

### Key Design Decisions
1. **Immutable Records**: Collections are audit records and cannot be modified
2. **Automatic Creation**: No manual creation - triggered by payment registration
3. **Snapshot Pattern**: Commission percentage is captured at creation time
4. **One-to-One with Payment**: Each payment generates exactly one collection
5. **No Deletion**: Collections are permanent records for auditing

### API Design Rules
- **DO**: Auto-filter by merchantId for MERCHANT role
- **DO**: Include enriched data (invoice number, customer name, etc.)
- **DO**: Support date range queries for reporting
- **DO**: Calculate summaries efficiently with aggregation queries
- **DON'T**: Expose create/update/delete endpoints
- **DON'T**: Allow modification of collection records
- **DON'T**: Allow MERCHANT to view other merchants' collections

### Internal Integration
The `CreateCollectionUseCase` is called internally by `RegisterPaymentUseCase`:

```java
// In RegisterPaymentUseCase (Invoices module)
if (invoice.hasMerchant()) {
    createCollectionUseCase.execute(
        invoice.getId(),
        payment.getId(),
        invoice.getCustomerId(),
        invoice.getMerchantId(),
        payment.getAmount(),
        merchant.getCommissionPercentage(),
        payment.getPaymentDate()
    );
}
```

### Testing Requirements
- Unit tests for Collection domain entity
- Unit tests for commission calculation
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test immutability (no updates allowed)
- Test role-based access control (MERCHANT can only see own)
- Test summary calculations
- Test date range filtering
- Test automatic creation from payment registration

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **INVOICES** | Collections generated from invoice payments |
| **RECEIVABLES** | Payment approval triggers Collection creation for MERCHANT payments |
| **PAYABLES** | Each Collection triggers a Payable (commission owed to merchant) |
| **MERCHANTS** | Merchant receives commission; used for filtering |
| **CUSTOMERS** | Customer info stored for reference |

---

**Last Updated**: 2025-01-15

**Module Status**: Planned
