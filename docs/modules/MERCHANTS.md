# MERCHANTS Module

## Overview
The MERCHANTS module manages merchant (comercial) accounts that can authenticate into the system with limited access. Merchants are sales representatives who can create orders on behalf of customers and earn commissions when those orders are converted to invoices and paid.

**Important**: 
- Merchants are **separate entities** from Users (not a User with a special role)
- Merchants can login to the system with **restricted permissions**
- Each merchant has a **single commission percentage** applied when invoices are paid
- Order management is handled in a **separate ORDERS module** (not documented here)

---

## What Merchants Can Do

| Action | Allowed |
|--------|---------|
| Login to system | Yes |
| Create orders | Yes |
| View own orders only | Yes |
| Cancel own pending orders | Yes |
| View products | Yes |
| View other merchants' orders | No |
| Validate/reject orders | No |
| Convert orders to invoices | No |
| Access admin features | No |

---

## Module Structure

```
merchants/
├── merchant/                           # Merchant submodule
│   ├── application/
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── CreateMerchantRequest.java
│   │   │   │   └── UpdateMerchantRequest.java
│   │   │   └── response/
│   │   │       ├── MerchantListResponse.java
│   │   │       ├── MerchantDetailResponse.java
│   │   │       └── MerchantCreateResponse.java
│   │   ├── exceptions/
│   │   │   ├── MerchantNotFoundException.java
│   │   │   └── DuplicateMerchantEmailException.java
│   │   ├── mappers/
│   │   │   └── MerchantMapper.java
│   │   └── usecases/
│   │       ├── CreateMerchantUseCase.java
│   │       ├── GetMerchantUseCase.java
│   │       ├── GetMerchantByEmailUseCase.java
│   │       ├── ListMerchantsUseCase.java
│   │       ├── UpdateMerchantUseCase.java
│   │       └── DeleteMerchantUseCase.java
│   ├── domain/
│   │   ├── entities/
│   │   │   └── Merchant.java
│   │   ├── repositories/
│   │   │   └── MerchantRepository.java
│   │   ├── services/
│   │   │   └── MerchantCodeGenerator.java
│   │   └── valueobjects/
│   │       ├── MerchantId.java
│   │       ├── MerchantCode.java
│   │       ├── MerchantName.java
│   │       └── CommissionPercentage.java
│   └── infrastructure/
│       ├── config/
│       │   └── MerchantBeanConfig.java
│       ├── persistence/
│       │   ├── entities/
│       │   │   └── MerchantJpaEntity.java
│       │   ├── mappers/
│       │   │   └── MerchantJpaMapper.java
│       │   └── repositories/
│       │       ├── JpaMerchantRepository.java
│       │       └── MerchantRepositoryAdapter.java
│       └── web/
│           └── MerchantController.java
```

---

## Domain Model

### Entities

#### Merchant (Entity)
**Domain Entity**: `com.lexand.lexand_api.merchants.merchant.domain.entities.Merchant`

**Description**: Represents a merchant account that can authenticate and create orders in the system. Each merchant has a unique commission percentage that is applied when invoices from their orders are paid.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `MerchantId` (UUID) | Unique identifier | Required, immutable |
| `code` | `MerchantCode` (Long) | Sequential merchant code | Required, immutable, auto-generated |
| `name` | `MerchantName` | Merchant business name | Required, 3-200 characters |
| `email` | `EmailAddress` | Login email address | Required, unique, valid email format |
| `password` | `Password` | Encrypted password | Required, min 8 characters |
| `phone` | `PhoneNumber` | Contact phone | Required, non-blank |
| `address` | `String` | Business address | Optional, max 500 characters |
| `commissionPercentage` | `CommissionPercentage` | Commission rate | Required, 0-100%, 2 decimal precision |
| `active` | `boolean` | Account active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(...)` - Factory method to create new merchant
- `reconstitute(...)` - Factory method to rebuild merchant from database
- `updateName(MerchantName)` - Updates merchant name
- `updatePhone(PhoneNumber)` - Updates phone number
- `updateAddress(String)` - Updates address
- `updateCommissionPercentage(CommissionPercentage)` - Updates commission
- `changePassword(Password)` - Changes password
- `activate()` - Activates merchant account
- `deactivate()` - Deactivates merchant account
- `canAuthenticate()` - Checks if merchant can log in (must be active)

**Business Rules**:
1. Merchant code is auto-generated sequentially (1, 2, 3...)
2. Email must be unique across all merchants AND users (shared auth namespace)
3. Commission percentage must be between 0% and 100%
4. Only active merchants can authenticate
5. Merchants can ONLY create orders and view their own orders
6. All updates modify the `updatedAt` timestamp
7. **Single commission**: Each merchant has exactly ONE commission percentage
8. Commission is applied when invoice is paid, NOT when order is created

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `MerchantId` | UUID | Merchant unique identifier | Non-null UUID |
| `MerchantCode` | Long | Sequential merchant number | Positive number, auto-generated |
| `MerchantName` | String | Business name | 3-200 characters, non-blank |
| `EmailAddress` | String | Login email | Valid email format, unique |
| `Password` | String | Encrypted password | Min 8 characters |
| `PhoneNumber` | String | Contact phone | Non-blank |
| `CommissionPercentage` | BigDecimal | Commission rate | 0-100, 2 decimal precision |

---

## Authentication & Authorization

### Merchant Role
Merchants authenticate using the existing AUTH module with a new role: `MERCHANT`

#### Role Permissions Matrix

| Action | ADMIN | USER | MERCHANT |
|--------|-------|------|----------|
| Create Merchant | Yes | No | No |
| Edit Merchant | Yes | No | No |
| Delete Merchant | Yes | No | No |
| View All Merchants | Yes | Yes | No |
| View Merchant Profile | Yes | Yes | Yes (own only) |
| Create Orders | Yes | Yes | Yes (own only) |
| View Orders | Yes | Yes | Yes (own only) |
| Validate/Reject Orders | Yes | Yes | No |
| Convert to Invoice | Yes | Yes | No |
| Cancel Orders | Yes | Yes | Yes (own pending only) |
| View Products | Yes | Yes | Yes |

### Merchant Login

Merchants use the same login endpoint as regular users:

**Endpoint**: `POST /api/auth/login`

The system identifies the user type by checking both users and merchants tables.

**Response includes**:
- Access token (JWT)
- Refresh token
- User type: `MERCHANT`
- Merchant details (id, name, commission percentage)

---

## API Endpoints

### Merchant Management (Admin Only)

#### 1. Create Merchant
**Endpoint**: `POST /api/merchants`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN only

**Description**: Creates a new merchant account.

**Request Body** (`CreateMerchantRequest`):
```json
{
  "name": "Juan Perez",
  "email": "juan.perez@comercial.com",
  "password": "SecurePass123!",
  "phone": "+58-424-1234567",
  "address": "Av. Principal, Local 5, Caracas",
  "commissionPercentage": 15.00,
  "active": true
}
```

**Response** (201 Created) - `MerchantCreateResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Juan Perez",
  "email": "juan.perez@comercial.com",
  "phone": "+58-424-1234567",
  "address": "Av. Principal, Local 5, Caracas",
  "commissionPercentage": 15.00,
  "active": true,
  "createdAt": "2025-12-30T10:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `409 Conflict`: Email already exists

---

#### 2. Get Merchant by ID
**Endpoint**: `GET /api/merchants/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN, USER

**Description**: Retrieves a specific merchant by ID.

**Path Parameters**:
- `id` (UUID) - Merchant unique identifier

**Response** (200 OK) - `MerchantDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Juan Perez",
  "email": "juan.perez@comercial.com",
  "phone": "+58-424-1234567",
  "address": "Av. Principal, Local 5, Caracas",
  "commissionPercentage": 15.00,
  "active": true,
  "createdAt": "2025-12-30T10:30:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN or USER
- `404 Not Found`: Merchant not found

---

#### 3. List All Merchants
**Endpoint**: `GET /api/merchants`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN, USER

**Description**: Retrieves all merchants in the system.

**Query Parameters** (optional):
- `active` (boolean) - Filter by active status
- `page` (int) - Page number (default: 0)
- `size` (int) - Page size (default: 20)

**Response** (200 OK) - `List<MerchantListResponse>`:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": 1,
    "name": "Juan Perez",
    "email": "juan.perez@comercial.com",
    "commissionPercentage": 15.00,
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "code": 2,
    "name": "Maria Garcia",
    "email": "maria.garcia@comercial.com",
    "commissionPercentage": 12.50,
    "active": true
  }
]
```

---

#### 4. Update Merchant
**Endpoint**: `PUT /api/merchants/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN

**Description**: Updates an existing merchant.

**Request Body** (`UpdateMerchantRequest`):
```json
{
  "name": "Juan Perez Actualizado",
  "phone": "+58-424-9999999",
  "address": "Nueva Direccion, Caracas",
  "commissionPercentage": 18.00,
  "active": true
}
```

**Response** (200 OK) - `MerchantDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Juan Perez Actualizado",
  "email": "juan.perez@comercial.com",
  "phone": "+58-424-9999999",
  "address": "Nueva Direccion, Caracas",
  "commissionPercentage": 18.00,
  "active": true,
  "createdAt": "2025-12-30T10:30:00"
}
```

**Note**: Email cannot be changed after creation.

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `404 Not Found`: Merchant not found

---

#### 5. Delete Merchant
**Endpoint**: `DELETE /api/merchants/{id}`

**Authentication**: Required (JWT Bearer Token)

**Roles**: ADMIN only

**Description**: Deletes a merchant from the system.

**Path Parameters**:
- `id` (UUID) - Merchant unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: User is not ADMIN
- `404 Not Found`: Merchant not found
- `409 Conflict`: Cannot delete merchant with pending orders

---

#### 6. Get Merchant Profile (Merchant Self)
**Endpoint**: `GET /api/merchants/me`

**Authentication**: Required (JWT Bearer Token)

**Roles**: MERCHANT only

**Description**: Retrieves the authenticated merchant's own profile.

**Response** (200 OK) - `MerchantDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "name": "Juan Perez",
  "email": "juan.perez@comercial.com",
  "phone": "+58-424-1234567",
  "address": "Av. Principal, Local 5, Caracas",
  "commissionPercentage": 15.00,
  "active": true,
  "createdAt": "2025-12-30T10:30:00"
}
```

---

## DTOs (Data Transfer Objects)

### Request DTOs

#### CreateMerchantRequest
```java
public record CreateMerchantRequest(
    @NotBlank @Size(min = 3, max = 200) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String phone,
    @Size(max = 500) String address,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal commissionPercentage,
    @NotNull Boolean active
)
```

#### UpdateMerchantRequest
```java
public record UpdateMerchantRequest(
    @NotBlank @Size(min = 3, max = 200) String name,
    @NotBlank String phone,
    @Size(max = 500) String address,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal commissionPercentage,
    @NotNull Boolean active
)
```

**Note**: Email is not included in UpdateMerchantRequest - it cannot be changed after creation.

---

### Response DTOs

#### MerchantListResponse
```java
public record MerchantListResponse(
    String id,
    Long code,
    String name,
    String email,
    BigDecimal commissionPercentage,
    boolean active
)
```

#### MerchantDetailResponse
```java
public record MerchantDetailResponse(
    String id,
    Long code,
    String name,
    String email,
    String phone,
    String address,
    BigDecimal commissionPercentage,
    boolean active,
    LocalDateTime createdAt
)
```

#### MerchantCreateResponse
```java
public record MerchantCreateResponse(
    String id,
    Long code,
    String name,
    String email,
    String phone,
    String address,
    BigDecimal commissionPercentage,
    boolean active,
    LocalDateTime createdAt
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/merchants | None | List\<MerchantListResponse\> |
| GET /api/merchants/{id} | None | MerchantDetailResponse |
| GET /api/merchants/me | None | MerchantDetailResponse |
| POST /api/merchants | CreateMerchantRequest | MerchantCreateResponse |
| PUT /api/merchants/{id} | UpdateMerchantRequest | MerchantDetailResponse |
| DELETE /api/merchants/{id} | None | void (204) |

---

## Use Cases

### Merchant Management Use Cases
1. **CreateMerchantUseCase** - Creates a new merchant account
2. **GetMerchantUseCase** - Retrieves merchant by ID
3. **GetMerchantByEmailUseCase** - Retrieves merchant by email (for auth)
4. **ListMerchantsUseCase** - Lists all merchants with optional filters
5. **UpdateMerchantUseCase** - Updates merchant information
6. **DeleteMerchantUseCase** - Deletes a merchant (soft delete recommended)

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface MerchantRepository {
    Merchant save(Merchant merchant);
    Optional<Merchant> findById(MerchantId id);
    Optional<Merchant> findByEmail(EmailAddress email);
    List<Merchant> findAll();
    List<Merchant> findByActive(boolean active);
    void delete(MerchantId id);
    boolean existsById(MerchantId id);
    boolean existsByEmail(EmailAddress email);
    Long getNextCode();
}
```

---

## Domain Services

### MerchantCodeGenerator
**Responsibilities**:
- Generate sequential merchant codes
- Ensure code uniqueness

**Implementation**: `SequentialMerchantCodeGenerator`
- Queries database for max code
- Increments by 1
- Starts from 1 if no merchants exist

---

## Business Rules Summary

### Merchant Registration
1. Email must be unique across merchants AND users
2. Password must be at least 8 characters
3. Commission percentage must be between 0% and 100%
4. Merchant code is auto-generated sequentially
5. New merchants are active by default
6. **Single commission**: One percentage per merchant, no variations

### Merchant Authentication
1. Merchants use the same login endpoint as users
2. Merchant role has restricted permissions
3. Only active merchants can authenticate
4. Merchants can only access their own data

### Commission Rules
1. Commission percentage is stored on merchant (0-100%)
2. **Commission is NOT deducted from the order**
3. Commission is charged when the **invoice is paid**
4. Commission = Invoice Total x (Merchant Commission % / 100)
5. Merchant receives invoice total minus commission
6. Pending payments are reported to merchant via PDF

### Commission Calculation Example

```
Merchant: Juan Perez (15% commission)
─────────────────────────────────────
Invoice Total:           $1,000.00
Commission (15%):        $150.00    <- Charged to merchant when invoice is paid
Merchant receives:       $850.00    <- Via PDF report of pending payments
```

---

## Database Schema

### merchants Table
```sql
CREATE TABLE merchants (
    id VARCHAR(36) PRIMARY KEY,
    code BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address VARCHAR(500),
    commission_percentage DECIMAL(5,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_code (code),
    INDEX idx_email (email),
    INDEX idx_active (active)
);
```

---

## Integration with Orders Module

### Order Association
- Orders can **optionally** have a merchant associated
- When an order has a merchant:
  - It requires validation before converting to invoice
  - Commission is calculated when invoice is paid
  - Order is visible to the merchant in their order list

### Commission Flow
```
Order created (with merchant) -> Validated -> Converted to Invoice -> Invoice Paid -> Commission Charged
```

**Note**: Order management, validation, and invoice conversion are documented in the **ORDERS module** (not here).

---

## Exception Handling

### Custom Exceptions
- `MerchantNotFoundException` - Merchant not found by ID (404 Not Found)
- `DuplicateMerchantEmailException` - Email already exists (409 Conflict)
- `MerchantNotActiveException` - Merchant account is inactive (403 Forbidden)

---

## Notes for Developers

### Key Design Decisions
1. **Separate Entity**: Merchant is NOT a User with a special role - it's a separate entity
2. **Single Commission**: Each merchant has ONE commission percentage (not per product/category)
3. **Orders Module**: Order management is in a separate module, not here
4. **Shared Auth**: Merchants use the same auth system but with MERCHANT role
5. **Email Uniqueness**: Email must be unique across both merchants AND users tables

### API Design Rules
- **DO**: Check user role for every endpoint
- **DO**: Filter merchant data based on authenticated user
- **DO**: Validate email uniqueness across merchants AND users
- **DON'T**: Allow merchants to access other merchants' data
- **DON'T**: Allow merchants to change their own commission percentage
- **DON'T**: Include order management endpoints in this module

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entity (Merchant)
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test merchant code generation uniqueness
- Test email uniqueness across merchants AND users
- Test role-based access control
- Test authentication flow for merchants

---

## Related Modules

| Module | Relationship |
|--------|--------------|
| **AUTH** | Merchants authenticate through AUTH module with MERCHANT role |
| **ORDERS** | Orders can have an optional merchant association |
| **INVOICES** | Commission is calculated when invoice is paid |
| **PRODUCTS** | Merchants can view products to create orders |

---

**Last Updated**: 2025-12-30

**Module Status**: Planned
