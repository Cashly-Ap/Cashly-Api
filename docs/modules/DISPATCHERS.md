# DISPATCHERS Module

## Overview
The DISPATCHERS module manages dispatcher (despachador) accounts that can authenticate into the system with very limited access. Dispatchers are delivery personnel whose **only function** is to mark invoices as delivered with a justification note.

**Important**: 
- Dispatchers are **separate entities** from Users and Merchants (not a User with a special role)
- Dispatchers can login to the system with **highly restricted permissions**
- Dispatchers can ONLY mark invoices as "DELIVERED" with a justification
- They CANNOT create orders, invoices, register payments, or access any other functionality

---

## What Dispatchers Can Do

| Action | Allowed |
|--------|---------|
| Login to system | Yes |
| View pending invoices (for delivery) | Yes |
| Mark invoice as delivered | Yes |
| Add delivery justification | Yes |
| Create orders | No |
| Create invoices | No |
| Register payments | No |
| View products | No |
| Access admin features | No |
| Cancel invoices | No |

---

## Module Structure

```
dispatchers/
├── dispatcher/                           # Dispatcher submodule
│   ├── application/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── CreateDispatcherRequest.java
│   │   │   │   └── UpdateDispatcherRequest.java
│   │   │   └── response/
│   │   │       ├── DispatcherListResponse.java
│   │   │       ├── DispatcherDetailResponse.java
│   │   │       └── DispatcherCreateResponse.java
│   │   ├── exceptions/
│   │   │   ├── DispatcherNotFoundException.java
│   │   │   └── DuplicateDispatcherEmailException.java
│   │   ├── mappers/
│   │   │   └── DispatcherMapper.java
│   │   └── usecases/
│   │       ├── CreateDispatcherUseCase.java
│   │       ├── GetDispatcherUseCase.java
│   │       ├── GetDispatcherByEmailUseCase.java
│   │       ├── ListDispatchersUseCase.java
│   │       ├── UpdateDispatcherUseCase.java
│   │       └── DeleteDispatcherUseCase.java
│   ├── domain/
│   │   ├── entities/
│   │   │   └── Dispatcher.java
│   │   ├── repositories/
│   │   │   └── DispatcherRepository.java
│   │   ├── services/
│   │   │   └── DispatcherCodeGenerator.java
│   │   └── valueobjects/
│   │       ├── DispatcherId.java
│   │       ├── DispatcherCode.java
│   │       └── DispatcherName.java
│   └── infrastructure/
│       ├── config/
│       │   └── DispatcherBeanConfig.java
│       ├── persistence/
│       │   ├── entities/
│       │   │   └── DispatcherJpaEntity.java
│       │   ├── mappers/
│       │   │   └── DispatcherJpaMapper.java
│       │   └── repositories/
│       │       ├── JpaDispatcherRepository.java
│       │       └── DispatcherRepositoryAdapter.java
│       └── web/
│           └── DispatcherController.java
```

---

## Domain Model

### Entities

#### Dispatcher (Entity)
**Domain Entity**: `com.lexand.lexand_api.dispatchers.dispatcher.domain.entities.Dispatcher`

**Description**: Represents a dispatcher account that can authenticate and mark invoices as delivered. Each dispatcher has a unique code and can only perform delivery confirmation operations.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `DispatcherId` (UUID) | Unique identifier | Required, immutable |
| `code` | `DispatcherCode` (Long) | Sequential dispatcher code | Required, immutable, auto-generated |
| `name` | `DispatcherName` | Dispatcher full name | Required, 3-200 characters |
| `email` | `EmailAddress` | Login email address | Required, unique, valid email format |
| `password` | `Password` | Encrypted password | Required, min 8 characters |
| `phone` | `PhoneNumber` | Contact phone | Required, non-blank |
| `active` | `boolean` | Account active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(...)` - Factory method to create new dispatcher
- `reconstitute(...)` - Factory method to rebuild dispatcher from database
- `updateName(DispatcherName)` - Updates dispatcher name
- `updatePhone(PhoneNumber)` - Updates phone number
- `changePassword(Password)` - Changes password
- `activate()` - Activates dispatcher account
- `deactivate()` - Deactivates dispatcher account
- `canAuthenticate()` - Checks if dispatcher can log in (must be active)

**Business Rules**:
1. Dispatcher code is auto-generated sequentially (1, 2, 3...)
2. Email must be unique across all dispatchers, merchants AND users (shared auth namespace)
3. Only active dispatchers can authenticate
4. Dispatchers can ONLY mark invoices as delivered
5. All updates modify the `updatedAt` timestamp

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `DispatcherId` | UUID | Dispatcher unique identifier | Non-null UUID |
| `DispatcherCode` | Long | Sequential dispatcher number | Positive number, auto-generated |
| `DispatcherName` | String | Full name | 3-200 characters, non-blank |
| `EmailAddress` | String | Login email | Valid email format, unique |
| `Password` | String | Encrypted password | Min 8 characters |
| `PhoneNumber` | String | Contact phone | Non-blank |

---

## Authentication & Authorization

### Dispatcher Role
Dispatchers authenticate using the existing AUTH module with a new role: `DISPATCHER`

#### Role Permissions Matrix

| Action | ADMIN | USER | MERCHANT | DISPATCHER |
|--------|-------|------|----------|------------|
| Create Dispatcher | Yes | No | No | No |
| Edit Dispatcher | Yes | No | No | No |
| Delete Dispatcher | Yes | No | No | No |
| View All Dispatchers | Yes | Yes | No | No |
| View Dispatcher Profile | Yes | Yes | No | Yes (own only) |
| View Pending Invoices (delivery) | Yes | Yes | No | Yes |
| Mark Invoice as Delivered | Yes | Yes | No | Yes |
| Create Orders | Yes | Yes | Yes | No |
| Create Invoices | Yes | Yes | No | No |
| Register Payments | Yes | Yes | No | No |
| View Products | Yes | Yes | Yes | No |

### Dispatcher Login

Dispatchers use the same login endpoint as regular users:

**Endpoint**: `POST /api/auth/login`

The system identifies the user type by checking users, merchants, and dispatchers tables.

**Response includes**:
- Access token (JWT)
- Refresh token
- User type: `DISPATCHER`
- Dispatcher details (id, name, code)

---

## API Endpoints

### Dispatcher Management (Admin Only)

#### 1. Create Dispatcher
**Endpoint**: `POST /api/dispatchers`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN only

**Description**: Creates a new dispatcher account.

**Request Body** (`CreateDispatcherRequest`):
```json
{
  "name": "Carlos Rodriguez",
  "email": "carlos.rodriguez@despacho.com",
  "password": "SecurePass123!",
  "phone": "+58-424-1234567",
  "active": true
}
```

**Response** (201 Created) - `DispatcherCreateResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Carlos Rodriguez",
  "email": "carlos.rodriguez@despacho.com",
  "phone": "+58-424-1234567",
  "active": true,
  "createdAt": "2026-01-15T10:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `409 Conflict`: Email already exists

---

#### 2. Get Dispatcher by ID
**Endpoint**: `GET /api/dispatchers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN, USER

**Description**: Retrieves a specific dispatcher by ID.

**Path Parameters**:
- `id` (UUID) - Dispatcher unique identifier

**Response** (200 OK) - `DispatcherDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Carlos Rodriguez",
  "email": "carlos.rodriguez@despacho.com",
  "phone": "+58-424-1234567",
  "active": true,
  "createdAt": "2026-01-15T10:30:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN or USER
- `404 Not Found`: Dispatcher not found

---

#### 3. List All Dispatchers
**Endpoint**: `GET /api/dispatchers`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN, USER

**Description**: Retrieves all dispatchers in the system.

**Query Parameters** (optional):
- `active` (boolean) - Filter by active status
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 20)

**Response** (200 OK) - `List<DispatcherListResponse>`:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": 1,
    "name": "Carlos Rodriguez",
    "email": "carlos.rodriguez@despacho.com",
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "code": 2,
    "name": "Maria Gonzalez",
    "email": "maria.gonzalez@despacho.com",
    "active": true
  }
]
```

---

#### 4. Update Dispatcher
**Endpoint**: `PUT /api/dispatchers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN

**Description**: Updates an existing dispatcher.

**Request Body** (`UpdateDispatcherRequest`):
```json
{
  "name": "Carlos Rodriguez Actualizado",
  "phone": "+58-424-9999999",
  "active": true
}
```

**Response** (200 OK) - `DispatcherDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Carlos Rodriguez Actualizado",
  "email": "carlos.rodriguez@despacho.com",
  "phone": "+58-424-9999999",
  "active": true,
  "createdAt": "2026-01-15T10:30:00"
}
```

**Note**: Email cannot be changed after creation.

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `404 Not Found`: Dispatcher not found

---

#### 5. Delete Dispatcher
**Endpoint**: `DELETE /api/dispatchers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN only

**Description**: Deletes a dispatcher from the system.

**Path Parameters**:
- `id` (UUID) - Dispatcher unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `404 Not Found`: Dispatcher not found

---

#### 6. Get Dispatcher Profile (Dispatcher Self)
**Endpoint**: `GET /api/dispatchers/me`

**Authentication**: Required (JWT Bearer Token)

**Roles**: DISPATCHER only

**Description**: Retrieves the authenticated dispatcher's own profile.

**Response** (200 OK) - `DispatcherDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Carlos Rodriguez",
  "email": "carlos.rodriguez@despacho.com",
  "phone": "+58-424-1234567",
  "active": true,
  "createdAt": "2026-01-15T10:30:00"
}
```

---

### Delivery Operations (Dispatcher Main Function)

#### 7. Get Pending Invoices for Delivery
**Endpoint**: `GET /api/dispatchers/invoices/pending`

**Authentication**: Required (JWT Bearer Token)

**Roles**: DISPATCHER, ADMIN, USER

**Description**: Retrieves all invoices that are pending delivery (status PENDING, not yet delivered).

**Query Parameters** (optional):
- `customerId` (UUID) - Filter by customer
- `search` (String) - Search by invoice number or customer name
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 20)

**Response** (200 OK) - `List<InvoicePendingDeliveryResponse>`:
```json
[
  {
    "id": "invoice-001-uuid",
    "invoiceNumber": "INV-2026-00001",
    "customerId": "customer-uuid-here",
    "customerName": "ABC Company S.A.",
    "customerAddress": "Av. Principal, Local 5, Caracas",
    "customerPhone": "+58-424-1234567",
    "issueDate": "2026-01-10",
    "subtotal": 1500.00,
    "lineCount": 3,
    "createdAt": "2026-01-10T10:30:00"
  },
  {
    "id": "invoice-002-uuid",
    "invoiceNumber": "INV-2026-00002",
    "customerId": "customer-uuid-here",
    "customerName": "XYZ Corp",
    "customerAddress": "Calle 10, Edificio Central",
    "customerPhone": "+58-412-9876543",
    "issueDate": "2026-01-12",
    "subtotal": 2300.00,
    "lineCount": 5,
    "createdAt": "2026-01-12T14:00:00"
  }
]
```

---

#### 8. Mark Invoice as Delivered
**Endpoint**: `PUT /api/dispatchers/invoices/{id}/deliver`

**Authentication**: Required (JWT Bearer Token)

**Roles**: DISPATCHER, ADMIN, USER

**Description**: Marks an invoice as delivered with a justification note. This is the **main and only operational function** for dispatchers. **IMPORTANT**: Only DELIVERED invoices can receive payments.

**Path Parameters**:
- `id` (UUID) - Invoice unique identifier

**Request Body** (`MarkDeliveredRequest`):
```json
{
  "justification": "Entregado a recepcion, firma de Maria Garcia, CI: 12345678"
}
```

**Response** (200 OK) - `InvoiceDeliveredResponse`:
```json
{
  "id": "invoice-001-uuid",
  "invoiceNumber": "INV-2026-00001",
  "customerName": "ABC Company S.A.",
  "status": "DELIVERED",
  "deliveredAt": "2026-01-15T14:30:00",
  "deliveredBy": {
    "id": "dispatcher-uuid",
    "code": 1,
    "name": "Carlos Rodriguez"
  },
  "justification": "Entregado a recepcion, firma de Maria Garcia, CI: 12345678"
}
```

**Business Logic**:
1. Validates invoice exists
2. Validates invoice status allows delivery (PENDING status)
3. Updates invoice status to DELIVERED
4. Records delivery timestamp
5. Records dispatcher who made the delivery
6. Stores justification note

**Errors**:
- `400 Bad Request`: Justification is required
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Invoice not found
- `409 Conflict`: Invoice is not in a deliverable status (must be PENDING)

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### CreateDispatcherRequest
```java
public record CreateDispatcherRequest(
    @NotBlank @Size(min = 3, max = 200) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String phone,
    @NotNull Boolean active
)
```

#### UpdateDispatcherRequest
```java
public record UpdateDispatcherRequest(
    @NotBlank @Size(min = 3, max = 200) String name,
    @NotBlank String phone,
    @NotNull Boolean active
)
```

**Note**: Email is not included in UpdateDispatcherRequest - it cannot be changed after creation.

#### MarkDeliveredRequest
```java
public record MarkDeliveredRequest(
    @NotBlank @Size(max = 500) String justification
)
```

---

### Response DTOs

#### DispatcherListResponse
```java
public record DispatcherListResponse(
    String id,
    Long code,
    String name,
    String email,
    boolean active
)
```

#### DispatcherDetailResponse
```java
public record DispatcherDetailResponse(
    String id,
    Long code,
    String name,
    String email,
    String phone,
    boolean active,
    LocalDateTime createdAt
)
```

#### DispatcherCreateResponse
```java
public record DispatcherCreateResponse(
    String id,
    Long code,
    String name,
    String email,
    String phone,
    boolean active,
    LocalDateTime createdAt
)
```

#### InvoicePendingDeliveryResponse
```java
public record InvoicePendingDeliveryResponse(
    String id,
    String invoiceNumber,
    String customerId,
    String customerName,
    String customerAddress,
    String customerPhone,
    LocalDate issueDate,
    BigDecimal subtotal,
    int lineCount,
    LocalDateTime createdAt
)
```

#### InvoiceDeliveredResponse
```java
public record InvoiceDeliveredResponse(
    String id,
    String invoiceNumber,
    String customerName,
    String status,
    LocalDateTime deliveredAt,
    DispatcherSummary deliveredBy,
    String justification
)

public record DispatcherSummary(
    String id,
    Long code,
    String name
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/dispatchers | None | List\<DispatcherListResponse\> |
| GET /api/dispatchers/{id} | None | DispatcherDetailResponse |
| GET /api/dispatchers/me | None | DispatcherDetailResponse |
| POST /api/dispatchers | CreateDispatcherRequest | DispatcherCreateResponse |
| PUT /api/dispatchers/{id} | UpdateDispatcherRequest | DispatcherDetailResponse |
| DELETE /api/dispatchers/{id} | None | void (204) |
| GET /api/dispatchers/invoices/pending | None | List\<InvoicePendingDeliveryResponse\> |
| PUT /api/dispatchers/invoices/{id}/deliver | MarkDeliveredRequest | InvoiceDeliveredResponse |

---

## Use Cases

### Dispatcher Management Use Cases
1. **CreateDispatcherUseCase** - Creates a new dispatcher account
2. **GetDispatcherUseCase** - Retrieves dispatcher by ID
3. **GetDispatcherByEmailUseCase** - Retrieves dispatcher by email (for auth)
4. **ListDispatchersUseCase** - Lists all dispatchers with optional filters
5. **UpdateDispatcherUseCase** - Updates dispatcher information
6. **DeleteDispatcherUseCase** - Deletes a dispatcher

### Delivery Use Cases
7. **GetPendingDeliveryInvoicesUseCase** - Lists invoices pending delivery
8. **MarkInvoiceAsDeliveredUseCase** - Marks invoice as delivered with justification

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface DispatcherRepository {
    Dispatcher save(Dispatcher dispatcher);
    Optional<Dispatcher> findById(DispatcherId id);
    Optional<Dispatcher> findByEmail(EmailAddress email);
    List<Dispatcher> findAll();
    List<Dispatcher> findByActive(boolean active);
    void delete(DispatcherId id);
    boolean existsById(DispatcherId id);
    boolean existsByEmail(EmailAddress email);
    Long getNextCode();
}
```

---

## Domain Services

### DispatcherCodeGenerator
**Responsibilities**:
- Generate sequential dispatcher codes
- Ensure code uniqueness

**Implementation**: `SequentialDispatcherCodeGenerator`
- Queries database for max code
- Increments by 1
- Starts from 1 if no dispatchers exist

---

## Business Rules Summary

### Dispatcher Registration
1. Email must be unique across dispatchers, merchants AND users
2. Password must be at least 8 characters
3. Dispatcher code is auto-generated sequentially
4. New dispatchers are active by default

### Dispatcher Authentication
1. Dispatchers use the same login endpoint as users
2. Dispatcher role has highly restricted permissions
3. Only active dispatchers can authenticate
4. Dispatchers can only access delivery-related endpoints

### Delivery Rules
1. Dispatcher can only mark PENDING invoices as DELIVERED
2. Justification is REQUIRED when marking as delivered
3. Delivery records the dispatcher who made it
4. Delivery records the timestamp
5. Once delivered, invoice status changes to DELIVERED
6. **CRITICAL**: Only DELIVERED invoices can receive payments
7. Payments CANNOT be registered on PENDING invoices

---

## Database Schema

### dispatchers Table
```sql
CREATE TABLE dispatchers (
    id VARCHAR(36) PRIMARY KEY,
    code BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    
    INDEX idx_code (code),
    INDEX idx_email (email),
    INDEX idx_active (active)
);
```

### Invoice Delivery Fields (Added to invoices table)
```sql
ALTER TABLE invoices ADD COLUMN delivered_at TIMESTAMP NULL;
ALTER TABLE invoices ADD COLUMN delivered_by VARCHAR(36) NULL;
ALTER TABLE invoices ADD COLUMN delivery_justification VARCHAR(500) NULL;

ALTER TABLE invoices ADD CONSTRAINT fk_delivered_by 
    FOREIGN KEY (delivered_by) REFERENCES dispatchers(id);

CREATE INDEX idx_delivered_at ON invoices(delivered_at);
CREATE INDEX idx_delivered_by ON invoices(delivered_by);
```

### Invoice Status Update
Update the InvoiceStatus enum to reflect the new flow:
```java
public enum InvoiceStatus {
    PENDING,        // Created, awaiting DELIVERY (cannot receive payments)
    DELIVERED,      // Delivered to customer, can now receive payments
    PARTIALLY_PAID, // Partially paid
    PAID,           // Fully paid (final status)
    CANCELLED       // Cancelled (only from PENDING)
}
```

### Payment Validation Change
The Invoice module must be updated to validate:
```java
// In RegisterPaymentUseCase
if (invoice.getStatus() == InvoiceStatus.PENDING) {
    throw new InvoiceNotDeliveredException("Cannot register payment. Invoice must be delivered first.");
}
```---

## Invoice Status Flow with Delivery

```
                           INVOICE LIFECYCLE WITH DELIVERY
 
  CREATE (from Order or Direct)
            |
            v
      +-----------+
      |  PENDING  |  <-- Invoice created, awaiting DELIVERY first
      +-----------+
            |
     [dispatcher marks delivered]
            |
            v
      +-----------+
      | DELIVERED |  <-- Now can receive payments
      +-----------+
            |
     [register payment]
            |
            +------------------+
            |                  |
            v                  v
      +---------------+  +-----------+
      | PARTIALLY_PAID|  |   PAID    |
      +---------------+  +-----------+
            |                  
     [more payments]           
            |                  
            v                  
      +-----------+            
      |   PAID    |  <-- Final status, fully paid
      +-----------+


  CANCELLATION:
  
  PENDING --> [cancel] --> CANCELLED (only if not delivered)
```

### Key Flow Rules
1. **PENDING** → Can only be marked as DELIVERED or CANCELLED
2. **DELIVERED** → Can receive payments (partial or full)
3. **PARTIALLY_PAID** → Has some payments, awaiting more
4. **PAID** → Fully paid, final status
5. **CANCELLED** → Only from PENDING status

### Payment Restriction
- ❌ **PENDING invoices CANNOT receive payments**
- ✅ **Only DELIVERED or PARTIALLY_PAID invoices can receive payments**

---

## Exception Handling

### Custom Exceptions
- `DispatcherNotFoundException` - Dispatcher not found by ID (404 Not Found)
- `DuplicateDispatcherEmailException` - Email already exists (409 Conflict)
- `DispatcherNotActiveException` - Dispatcher account is inactive (403 Forbidden)
- `InvoiceNotDeliverableException` - Invoice cannot be marked as delivered (409 Conflict)
- `InvoiceNotDeliveredException` - Cannot pay invoice that hasn't been delivered (409 Conflict)

---

## Notes for Developers

### Key Design Decisions
1. **Separate Entity**: Dispatcher is NOT a User with a special role - it's a separate entity
2. **Single Purpose**: Dispatchers have ONE function - mark invoices as delivered
3. **Shared Auth**: Dispatchers use the same auth system but with DISPATCHER role
4. **Email Uniqueness**: Email must be unique across dispatchers, merchants AND users tables
5. **Delivery Tracking**: Invoice stores who delivered it and when

### API Design Rules
- **DO**: Check user role for every endpoint
- **DO**: Validate email uniqueness across dispatchers, merchants AND users
- **DO**: Require justification for delivery
- **DO**: Record dispatcher ID and timestamp on delivery
- **DON'T**: Allow dispatchers to access any other functionality
- **DON'T**: Allow marking non-PAID invoices as delivered
- **DON'T**: Allow changing delivery status once set

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entity (Dispatcher)
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test dispatcher code generation uniqueness
- Test email uniqueness across dispatchers, merchants AND users
- Test role-based access control
- Test authentication flow for dispatchers
- Test delivery marking with valid/invalid invoice status
- Test justification requirement

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **AUTH** | Dispatchers authenticate through AUTH module with DISPATCHER role |
| **INVOICES** | Dispatchers mark invoices as delivered; Invoice payment blocked until delivered |
| **CUSTOMERS** | Customer information shown in pending delivery list |

---

## Impact on INVOICES Module

### Required Changes to Invoices Module

1. **Add DELIVERED status** to InvoiceStatus enum (between PENDING and PARTIAL)

2. **Block payments on PENDING invoices**:
   - Update `RegisterPaymentUseCase` to reject payments on PENDING invoices
   - Only DELIVERED or PARTIALLY_PAID invoices can receive payments

3. **Add delivery fields to Invoice entity**:
   - `deliveredAt` (LocalDateTime)
   - `deliveredBy` (DispatcherId)
   - `deliveryJustification` (String)

4. **Update cancellation rules**:
   - Only PENDING invoices can be cancelled
   - DELIVERED invoices cannot be cancelled (already delivered to customer)

5. **Update overdue logic**:
   - There is no `OVERDUE` status — overdue invoices retain their current status (DELIVERED or PARTIALLY_PAID)
   - Due date tracking is informational only; overdue filtering is done by querying `dueDate < today` on DELIVERED/PARTIALLY_PAID invoices

---

**Last Updated**: 2026-01-15

**Module Status**: Planned
