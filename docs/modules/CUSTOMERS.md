# CUSTOMERS Module

> Estructura actual del código: `customers/profile`, `customers/balance`, `customers/credit` y `customers/warehouse_access`. Dentro de cada responsabilidad se ubican sus capas existentes; los controllers están en `infrastructure/web`, la persistencia en `infrastructure/persistence` y los casos de uso en `application/<operación>`. Consulta [ARCHITECTURE.md](../ARCHITECTURE.md) y [el árbol completo](../../openspec/changes/screaming-backend-structure/trees/after.md) para las rutas vigentes. Los ejemplos de planificación o paquetes Lexand de esta guía requieren contrastarse con el código actual.

## Overview
The CUSTOMERS module manages customer information for the invoicing system. It handles customer registration, profile updates, contact information (emails, phones, addresses), and credit management.

**Important**: Customer is an **Aggregate Root** in DDD terms. All nested entities (emails, phones, addresses) are **internal entities** that can ONLY be managed through the Customer aggregate. There are NO separate API endpoints for emails, phones, or addresses.

---

## Aggregate Pattern

### Customer Aggregate Root
- **Root Entity**: Customer
- **Internal Entities**: CustomerEmail, CustomerPhone, CustomerAddress
- **Access Rule**: ALL operations on emails, phones, and addresses MUST go through Customer
- **API Design**: Only Customer endpoints exist (/api/customers)
- **No Direct Access**: No /api/emails, /api/phones, or /api/addresses endpoints

### Why This Matters
1. **Consistency**: Ensures all changes to customer data are coordinated
2. **Business Rules**: Billing address rule enforced at aggregate level
3. **Transactional Integrity**: All changes committed together
4. **Simpler API**: Frontend only interacts with Customer endpoints

---

## Domain Model

### Entities

#### Customer (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.customers.domain.entities.Customer`

**Description**: Represents a customer entity with complete business information, contact details, and credit settings.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `CustomerId` (UUID) | Unique identifier | Required, immutable |
| `code` | `CustomerCode` (Long) | Sequential customer code | Required, immutable, auto-generated |
| `legalName` | `LegalName` | Legal business/person name | Required, 2-200 characters |
| `type` | `CustomerType` | Customer type (enum) | Required, see CustomerType enum |
| `rif` | `Rif` | Tax ID number | Required, valid RIF format |
| `alias` | `CustomerAlias` | Short name/nickname | Required, 2-100 characters |
| `creditDays` | `CreditDays` | Payment term in days | Default: 0, min: 0, max: 365 |
| `creditLimit` | `CreditLimit` | Maximum credit amount | Default: 0.00, min: 0 |
| `active` | `boolean` | Customer active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |
| `emails` | `List<CustomerEmail>` | Customer email addresses | Collection |
| `phones` | `List<CustomerPhone>` | Customer phone numbers | Collection |
| `addresses` | `List<CustomerAddress>` | Customer addresses | Collection, max 1 billing |

**Business Methods**:
- `create(...)` - Factory method to create new customer
- `reconstitute(...)` - Factory method to rebuild customer from database
- `updateLegalName(LegalName)` - Updates legal name
- `updateRif(Rif)` - Updates tax ID
- `updateAlias(CustomerAlias)` - Updates alias
- `updateCreditDays(CreditDays)` - Updates payment terms
- `updateCreditLimit(CreditLimit)` - Updates credit limit
- `updateType(CustomerType)` - Updates customer type
- `addEmail(EmailAddress)` - Adds new email
- `addPhone(PhoneNumber)` - Adds new phone
- `addAddress(String, boolean, boolean)` - Adds new address
- `clearEmails()` - Removes all emails
- `clearPhones()` - Removes all phones
- `clearAddresses()` - Removes all addresses
- `activate()` - Activates customer
- `deactivate()` - Deactivates customer

**Business Rules**:
1. Customer code is auto-generated sequentially
2. Legal name cannot be null or empty
3. RIF must follow valid format and be unique across all customers
4. Customer can have multiple emails, phones, and addresses
5. Only one address can be marked as billing address
6. Credit days must be between 0-365
7. Credit limit must be non-negative
8. All updates modify the `updatedAt` timestamp
9. Customer creation triggers `CustomerCreatedEvent`

---

#### CustomerEmail (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.customers.domain.entities.CustomerEmail`

**Description**: Represents a customer email address. **ALWAYS managed through Customer aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `EmailId` (UUID) | Unique identifier | Required, immutable |
| `customerId` | `CustomerId` (UUID) | Owner customer ID | Required, immutable |
| `email` | `EmailAddress` | Email address | Required, valid email format |

**Business Rules**:
1. Email must be in valid format
2. Email is ALWAYS associated with a single customer
3. Customer can have multiple emails
4. **NO direct API endpoints** - managed only through Customer CRUD operations
5. Cannot exist independently outside Customer aggregate

---

#### CustomerPhone (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.customers.domain.entities.CustomerPhone`

**Description**: Represents a customer phone number. **ALWAYS managed through Customer aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `PhoneId` (UUID) | Unique identifier | Required, immutable |
| `customerId` | `CustomerId` (UUID) | Owner customer ID | Required, immutable |
| `phone` | `PhoneNumber` | Phone number | Required, non-blank |

**Business Rules**:
1. Phone number cannot be blank
2. Phone is ALWAYS associated with a single customer
3. Customer can have multiple phones
4. **NO direct API endpoints** - managed only through Customer CRUD operations
5. Cannot exist independently outside Customer aggregate

---

#### CustomerAddress (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.customers.domain.entities.CustomerAddress`

**Description**: Represents a customer address. **ALWAYS managed through Customer aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `AddressId` (UUID) | Unique identifier | Required, immutable |
| `customerId` | `CustomerId` (UUID) | Owner customer ID | Required, immutable |
| `address` | `String` | Full address text | Required, non-blank |
| `billing` | `boolean` | Is billing address | Default: false |
| `active` | `boolean` | Address active status | Default: true |

**Business Methods**:
- `markAsBilling()` - Sets address as billing address
- `unmarkAsBilling()` - Removes billing designation
- `activate()` - Activates address
- `deactivate()` - Deactivates address

**Business Rules**:
1. Address cannot be blank
2. Only one address per customer can be billing address
3. When marking an address as billing, previous billing address is unmarked
4. Address is ALWAYS associated with a single customer
5. **NO direct API endpoints** - managed only through Customer CRUD operations
6. Cannot exist independently outside Customer aggregate

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `CustomerId` | UUID | Customer unique identifier | Non-null UUID |
| `CustomerCode` | Long | Sequential customer number | Positive number, auto-generated |
| `LegalName` | String | Legal name | 2-200 characters, non-blank |
| `CustomerType` | Enum | Customer classification | INDIVIDUAL, BUSINESS, GOVERNMENT, NON_PROFIT |
| `Rif` | String | Tax ID | Valid RIF format |
| `CustomerAlias` | String | Short name | 2-100 characters, non-blank |
| `CreditDays` | Integer | Payment terms in days | 0-365 |
| `CreditLimit` | BigDecimal | Maximum credit | Non-negative |
| `EmailId` | UUID | Email unique identifier | Non-null UUID |
| `EmailAddress` | String | Email address | Valid email format |
| `PhoneId` | UUID | Phone unique identifier | Non-null UUID |
| `PhoneNumber` | String | Phone number | Non-blank |
| `AddressId` | UUID | Address unique identifier | Non-null UUID |

---

### Enums

#### CustomerType
```java
public enum CustomerType {
    INDIVIDUAL,     // Individual person
    BUSINESS,       // Business/Company
    GOVERNMENT,     // Government entity
    NON_PROFIT      // Non-profit organization
}
```

---

## API Endpoints

**IMPORTANT**: Only Customer endpoints exist. There are NO separate endpoints for:
- ❌ `/api/customers/{id}/emails` 
- ❌ `/api/customers/{id}/phones`
- ❌ `/api/customers/{id}/addresses`
- ❌ `/api/emails/{id}`
- ❌ `/api/phones/{id}`
- ❌ `/api/addresses/{id}`

**All email, phone, and address management is done through Customer create/update operations.**

---

### 1. Create Customer
**Endpoint**: `POST /api/customers`

**Authentication**: Required (JWT Bearer Token)

**Description**: Registers a new customer in the system. Uses `CreateCustomerRequest`.

**Request Body** (`CreateCustomerRequest`):
```json
{
  "legalName": "ABC Company S.A.",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ABC Corp",
  "creditDays": 30,
  "creditLimit": 50000.00,
  "active": true,
  "emails": [
    { "email": "contact@abc.com" },
    { "email": "billing@abc.com" }
  ],
  "phones": [
    { "phone": "+58-212-1234567" },
    { "phone": "+58-424-9876543" }
  ],
  "addresses": [
    {
      "address": "Av. Principal, Edificio Torre ABC, Piso 5, Caracas",
      "billing": true,
      "active": true
    },
    {
      "address": "Zona Industrial Los Ruices, Galpón 15, Caracas",
      "billing": false,
      "active": true
    }
  ]
}
```

**Response** (201 Created) - `CustomerCreateResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "legalName": "ABC Company S.A.",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ABC Corp",
  "creditDays": 30,
  "creditLimit": 50000.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "emails": [
    {
      "id": "email-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "contact@abc.com"
    },
    {
      "id": "email-002",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "billing@abc.com"
    }
  ],
  "phones": [
    {
      "id": "phone-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "phone": "+58-212-1234567"
    },
    {
      "id": "phone-002",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "phone": "+58-424-9876543"
    }
  ],
  "addresses": [
    {
      "id": "addr-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "address": "Av. Principal, Edificio Torre ABC, Piso 5, Caracas",
      "billing": true,
      "active": true
    },
    {
      "id": "addr-002",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "address": "Zona Industrial Los Ruices, Galpón 15, Caracas",
      "billing": false,
      "active": true
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token

---

### 2. Get Customer by ID
**Endpoint**: `GET /api/customers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific customer by ID. Returns `CustomerDetailResponse`.

**Path Parameters**:
- `id` (UUID) - Customer unique identifier

**Response** (200 OK) - `CustomerDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "legalName": "ABC Company S.A.",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ABC Corp",
  "creditDays": 30,
  "creditLimit": 50000.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "emails": [
    {
      "id": "email-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "contact@abc.com"
    }
  ],
  "phones": [
    {
      "id": "phone-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "phone": "+58-212-1234567"
    }
  ],
  "addresses": [
    {
      "id": "addr-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "address": "Av. Principal, Edificio Torre ABC, Piso 5, Caracas",
      "billing": true,
      "active": true
    }
  ]
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Customer not found

---

### 3. List All Customers
**Endpoint**: `GET /api/customers`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all customers in the system (minimal data for list view). Returns `List<CustomerListResponse>`.

**Response** (200 OK) - `List<CustomerListResponse>`:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": 1,
    "alias": "ABC Corp",
    "legalName": "ABC Company S.A.",
    "rif": "J-12345678-9",
    "type": "BUSINESS",
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "code": 2,
    "alias": "XYZ",
    "legalName": "XYZ Solutions C.A.",
    "rif": "J-98765432-1",
    "type": "BUSINESS",
    "active": true
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

**Note**: Returns CustomerListResponse (minimal data, no emails/phones/addresses).

---

### 4. Update Customer
**Endpoint**: `PUT /api/customers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates an existing customer. Uses `UpdateCustomerRequest`.

**Path Parameters**:
- `id` (UUID) - Customer unique identifier

**Request Body** (`UpdateCustomerRequest`):
```json
{
  "legalName": "ABC Company S.A. Updated",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ABC Corp Updated",
  "creditDays": 45,
  "creditLimit": 75000.00,
  "active": true,
  "emails": [
    { "email": "new-contact@abc.com" }
  ],
  "phones": [
    { "phone": "+58-212-9999999" }
  ],
  "addresses": [
    {
      "address": "New Address, Caracas",
      "billing": true,
      "active": true
    }
  ]
}
```

**Response** (200 OK) - `CustomerDetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 1,
  "legalName": "ABC Company S.A. Updated",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ABC Corp Updated",
  "creditDays": 45,
  "creditLimit": 75000.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "emails": [
    {
      "id": "email-new-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "new-contact@abc.com"
    }
  ],
  "phones": [
    {
      "id": "phone-new-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "phone": "+58-212-9999999"
    }
  ],
  "addresses": [
    {
      "id": "addr-new-001",
      "customerId": "550e8400-e29b-41d4-a716-446655440000",
      "address": "New Address, Caracas",
      "billing": true,
      "active": true
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Customer not found

**Note**: Update replaces all emails, phones, and addresses. Send complete collections.

---

### 5. Delete Customer
**Endpoint**: `DELETE /api/customers/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Deletes a customer from the system.

**Path Parameters**:
- `id` (UUID) - Customer unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Customer not found

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
application/dto/
├── request/                    # Input DTOs for API requests
│   ├── CreateCustomerRequest.java
│   ├── UpdateCustomerRequest.java
│   ├── EmailRequest.java
│   ├── PhoneRequest.java
│   └── AddressRequest.java
└── response/                   # Output DTOs for API responses
    ├── CustomerListResponse.java
    ├── CustomerDetailResponse.java
    ├── CustomerEditResponse.java
    ├── CustomerCreateResponse.java
    ├── EmailResponse.java
    ├── PhoneResponse.java
    └── AddressResponse.java
```

**Note**: Currently, all DTOs are in the same package. Future refactoring should organize them into `request/` and `response/` subpackages.

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateCustomerRequest
**Usage**: POST /api/customers (Create new customer)

```java
public record CreateCustomerRequest(
    @NotBlank String legalName,                 // Required, 3-200 chars
    @NotBlank String type,                      // Required, valid CustomerType
    @NotBlank String rif,                       // Required, valid RIF format
    @NotBlank String alias,                     // Required, 3-100 chars
    @PositiveOrZero Integer creditDays,         // Required, >= 0
    @PositiveOrZero BigDecimal creditLimit,     // Required, >= 0
    @NotNull Boolean active,                    // Required, default: true
    @Valid @NotEmpty List<EmailRequest> emails,           // At least 1 required
    @Valid @NotEmpty List<PhoneRequest> phones,           // At least 1 required
    @Valid @NotEmpty List<AddressRequest> addresses       // At least 1 required, max 1 billing
)
```

**JSON Example**:
```json
{
  "legalName": "Nueva Empresa C.A.",
  "type": "BUSINESS",
  "rif": "J-98765432-1",
  "alias": "Nueva Empresa",
  "creditDays": 15,
  "creditLimit": 25000.00,
  "active": true,
  "emails": [
    { "email": "info@nuevaempresa.com" }
  ],
  "phones": [
    { "phone": "0212-9876543" }
  ],
  "addresses": [
    {
      "address": "Calle Principal, Edificio Nuevo, Piso 3",
      "billing": true,
      "active": true
    }
  ]
}
```

---

#### UpdateCustomerRequest
**Usage**: PUT /api/customers/{id} (Update existing customer)

```java
public record UpdateCustomerRequest(
    @NotBlank String legalName,                 // Required, 3-200 chars
    @NotBlank String type,                      // Required, valid CustomerType
    @NotBlank String rif,                       // Required, valid RIF format
    @NotBlank String alias,                     // Required, 3-100 chars
    @PositiveOrZero Integer creditDays,         // Required, >= 0
    @PositiveOrZero BigDecimal creditLimit,     // Required, >= 0
    @NotNull Boolean active,                    // Required
    @Valid @NotEmpty List<EmailRequest> emails,           // Replaces all emails
    @Valid @NotEmpty List<PhoneRequest> phones,           // Replaces all phones
    @Valid @NotEmpty List<AddressRequest> addresses       // Replaces all addresses
)
```

**JSON Example**:
```json
{
  "legalName": "Acme Corporation C.A. (Actualizada)",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ACME Corp Updated",
  "creditDays": 45,
  "creditLimit": 75000.00,
  "active": true,
  "emails": [
    { "email": "contacto@acme.com" },
    { "email": "nuevo@acme.com" }
  ],
  "phones": [
    { "phone": "0212-1234567" }
  ],
  "addresses": [
    {
      "address": "Av. Principal, Edificio Acme, Piso 5, Caracas",
      "billing": true,
      "active": true
    }
  ]
}
```

**Note**: Update replaces all nested collections (emails, phones, addresses). Send complete arrays.

---

#### EmailRequest
**Usage**: Nested in Create/Update requests

```java
public record EmailRequest(
    @NotBlank @Email String email               // Required, valid email format
)
```

---

#### PhoneRequest
**Usage**: Nested in Create/Update requests

```java
public record PhoneRequest(
    @NotBlank String phone                      // Required, Venezuelan format recommended
)
```

---

#### AddressRequest
**Usage**: Nested in Create/Update requests

```java
public record AddressRequest(
    @NotBlank String address,                   // Required, full address
    @NotNull Boolean billing,                   // Required
    @NotNull Boolean active                     // Required
)
```

---

### Response DTOs

Located in: `application/dto/response/`

#### 1. CustomerListResponse
**Usage**: GET /api/customers (List view)

**Purpose**: Minimal data for table display in list views.

```java
public record CustomerListResponse(
    String id,                                  // UUID as string
    Long code,                                  // Sequential customer code
    String alias,                               // Short name
    String legalName,                           // Legal name
    String rif,                                 // Tax ID
    String type,                                // CustomerType as string
    boolean active                              // Active status
)
```

**JSON Example**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": 1001,
  "alias": "ACME Corp",
  "legalName": "Acme Corporation C.A.",
  "rif": "J-12345678-9",
  "type": "BUSINESS",
  "active": true
}
```

---

#### 2. CustomerDetailResponse
**Usage**: GET /api/customers/{id} (Detail view)

**Purpose**: Complete customer data for detail/view pages.

```java
public record CustomerDetailResponse(
    String id,                                  // UUID as string
    Long code,                                  // Sequential code
    String legalName,                           // Legal name
    String type,                                // CustomerType as string
    String rif,                                 // Tax ID
    String alias,                               // Short name
    int creditDays,                             // Payment terms
    BigDecimal creditLimit,                     // Credit limit
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp
    List<EmailResponse> emails,                 // Email list with IDs
    List<PhoneResponse> phones,                 // Phone list with IDs
    List<AddressResponse> addresses             // Address list with IDs
)
```

**JSON Example**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": 1001,
  "legalName": "Acme Corporation C.A.",
  "type": "BUSINESS",
  "rif": "J-12345678-9",
  "alias": "ACME Corp",
  "creditDays": 30,
  "creditLimit": 50000.00,
  "active": true,
  "createdAt": "2025-01-15T10:30:00Z",
  "emails": [
    {
      "id": "email-001",
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "email": "contacto@acme.com"
    },
    {
      "id": "email-002",
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "email": "ventas@acme.com"
    }
  ],
  "phones": [
    {
      "id": "phone-001",
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "phone": "0212-1234567"
    }
  ],
  "addresses": [
    {
      "id": "addr-001",
      "customerId": "123e4567-e89b-12d3-a456-426614174000",
      "address": "Av. Principal, Edificio Acme, Piso 5, Caracas",
      "billing": true,
      "active": true
    }
  ]
}
```

---

#### 3. CustomerEditResponse
**Usage**: Frontend form state management (OPTIONAL - not currently implemented in backend)

**Purpose**: Data structure for edit forms. This DTO is **OPTIONAL** and may not be implemented in the backend. The frontend can use `CustomerDetailResponse` from GET `/api/customers/{id}` for editing.

**If Implemented**:
```java
public record CustomerEditResponse(
    String id,                                  // UUID as string (required for update)
    String legalName,                           // Legal name
    String type,                                // CustomerType as string
    String rif,                                 // Tax ID
    String alias,                               // Short name
    int creditDays,                             // Payment terms
    BigDecimal creditLimit,                     // Credit limit
    boolean active,                             // Active status
    List<EmailEditResponse> emails,             // Emails with optional IDs
    List<PhoneEditResponse> phones,             // Phones with optional IDs
    List<AddressEditResponse> addresses         // Addresses with optional IDs
)
```

**Note**: 
- This DTO is NOT required if frontend can use `CustomerDetailResponse`
- No separate endpoint exists for this DTO
- Frontend receives `CustomerDetailResponse` from GET and prepares `UpdateCustomerRequest` for PUT
- The `id`, `customerId` fields in nested entities should be ignored when preparing update request

**Recommended Approach**: 
- Use `CustomerDetailResponse` for both view and edit forms
- Frontend strips IDs from nested entities when preparing `UpdateCustomerRequest`

---

#### 4. CustomerCreateResponse
**Usage**: POST /api/customers response (After creation)

**Purpose**: Complete customer data returned after successful creation.

```java
public record CustomerCreateResponse(
    String id,                                  // UUID as string (generated)
    Long code,                                  // Sequential code (generated)
    String legalName,                           // Legal name
    String type,                                // CustomerType as string
    String rif,                                 // Tax ID
    String alias,                               // Short name
    int creditDays,                             // Payment terms
    BigDecimal creditLimit,                     // Credit limit
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp (generated)
    List<EmailResponse> emails,                 // Created emails with IDs
    List<PhoneResponse> phones,                 // Created phones with IDs
    List<AddressResponse> addresses             // Created addresses with IDs
)
```

**JSON Example**: (Same as CustomerDetailResponse)

---

#### Nested Response DTOs

##### EmailResponse
```java
public record EmailResponse(
    String id,                                  // UUID as string
    String customerId,                          // Owner customer ID
    String email                                // Email address
)
```

##### PhoneResponse
```java
public record PhoneResponse(
    String id,                                  // UUID as string
    String customerId,                          // Owner customer ID
    String phone                                // Phone number
)
```

##### AddressResponse
```java
public record AddressResponse(
    String id,                                  // UUID as string
    String customerId,                          // Owner customer ID
    String address,                             // Full address
    boolean billing,                            // Is billing address
    boolean active                              // Is active
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/customers | None | List\<CustomerListResponse\> |
| GET /api/customers/{id} | None | CustomerDetailResponse |
| POST /api/customers | CreateCustomerRequest | CustomerCreateResponse |
| PUT /api/customers/{id} | UpdateCustomerRequest | CustomerDetailResponse |
| DELETE /api/customers/{id} | None | void (204 No Content) |

**Note**: 
- `CustomerEditResponse` is for frontend form state, NOT a separate endpoint
- GET `/api/customers/{id}` returns `CustomerDetailResponse` which can be used for both viewing and editing
- No `/api/customers/{id}/edit` endpoint exists

---

### Current vs Planned DTO Structure

**Current Implementation** (needs refactoring):
- All DTOs in `application/dto/` package
- Using single `CustomerResponse` for all response types

**Planned Structure**:
```
application/dto/
├── request/
│   ├── CreateCustomerRequest.java
│   ├── UpdateCustomerRequest.java
│   ├── EmailRequest.java
│   ├── PhoneRequest.java
│   └── AddressRequest.java
└── response/
    ├── CustomerListResponse.java
    ├── CustomerDetailResponse.java
    ├── CustomerEditResponse.java
    ├── CustomerCreateResponse.java
    ├── EmailResponse.java
    ├── PhoneResponse.java
    └── AddressResponse.java
```

**Migration Notes**:
- Refactor `CustomerResponse` into 4 specific response types
- Move DTOs into `request/` and `response/` subpackages
- Update mappers to handle specific response types
- Frontend already expects this structure

---

## Managing Nested Entities (Emails, Phones, Addresses)

### Creation Pattern
When creating a customer, emails, phones, and addresses are sent WITHOUT IDs:

```json
{
  "legalName": "New Company",
  "emails": [
    { "email": "contact@company.com" }  // No ID
  ],
  "phones": [
    { "phone": "0212-1234567" }  // No ID
  ],
  "addresses": [
    { "address": "Main Street 123", "billing": true, "active": true }  // No ID
  ]
}
```

Backend generates IDs and returns them in the response.

---

### Update Pattern (Replace All)
When updating a customer, the backend **replaces ALL nested entities**:

```json
{
  "legalName": "Updated Company",
  "emails": [
    { "email": "new@company.com" }  // Old emails are deleted, new one created
  ],
  "phones": [
    { "phone": "0212-9999999" }  // Old phones are deleted, new one created
  ],
  "addresses": [
    { "address": "New Address", "billing": true, "active": true }  // Old addresses deleted
  ]
}
```

**Important**:
- ❌ Backend does NOT merge old + new
- ❌ Backend does NOT check for IDs to update vs create
- ✅ Backend DELETES all old entities and CREATES new ones
- ✅ Frontend must send complete arrays with ALL desired entities

---

### Why Replace All?
1. **Simplicity**: No complex merge logic
2. **Consistency**: Clear state after update
3. **Frontend Control**: Frontend decides what to keep/remove
4. **Transactional**: All changes in one atomic operation

---

### Frontend Responsibility
When editing a customer:
1. Fetch complete customer data (GET /api/customers/{id})
2. Allow user to add/remove/edit emails, phones, addresses
3. Send ALL desired entities in update request (PUT /api/customers/{id})
4. Backend will delete old + create new

**Example Flow**:
```typescript
// 1. Get customer
const customer = await getCustomer(id);
// customer.emails = [{ id: "1", email: "old@mail.com" }]

// 2. User edits in form
formData.emails = [
  { email: "old@mail.com" },      // Keep this one
  { email: "new@mail.com" }       // Add this one
];

// 3. Send update (WITHOUT IDs)
await updateCustomer(id, {
  ...formData,
  emails: formData.emails.map(e => ({ email: e.email }))  // Remove IDs
});

// 4. Backend deletes old email (id: "1") and creates 2 new emails
```

---

## Use Cases

### Application Layer Use Cases
1. **RegisterCustomerUseCase** - Creates a new customer
2. **GetCustomerUseCase** - Retrieves customer by ID
3. **ListCustomersUseCase** - Lists all customers
4. **UpdateCustomerUseCase** - Updates customer information
5. **DeleteCustomerUseCase** - Deletes a customer

---

## Repository Interfaces

### Domain Layer Port
```java
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    List<Customer> findAll();
    void delete(CustomerId id);
    boolean existsById(CustomerId id);
}
```

---

## Domain Services

### CustomerCodeGenerator
**Responsibilities**:
- Generate sequential customer codes
- Ensure code uniqueness

**Implementation**: `SequentialCustomerCodeGenerator`
- Queries database for max code
- Increments by 1
- Starts from 1 if no customers exist

---

## Domain Events

### CustomerCreatedEvent
**Triggered**: When a new customer is created

**Payload**:
- `customerId` (CustomerId)
- `customerCode` (CustomerCode)
- `legalName` (LegalName)
- `type` (CustomerType)
- `rif` (Rif)
- `alias` (CustomerAlias)
- `occurredOn` (LocalDateTime)

**Purpose**: Notify other modules about new customer creation

---

## Business Rules Summary

### Aggregate Root Pattern
1. **Customer is the Aggregate Root** - All operations go through Customer
2. **No direct access to nested entities** - Cannot CRUD emails/phones/addresses independently
3. **Transactional boundary** - All changes to Customer + nested entities in one transaction
4. **Consistency guarantee** - Aggregate ensures all business rules are enforced

### Customer Registration
1. Customer code is auto-generated sequentially (1, 2, 3...)
2. Legal name is required and must be 3-200 characters
3. RIF must follow valid format
4. Alias is required and must be 3-100 characters
5. Customer type must be valid enum value
6. Credit days must be >= 0, required field
7. Credit limit must be >= 0, required field
8. Active status is required, defaults to true
9. At least one email is required
10. At least one phone is required
11. At least one address is required
12. Only one address can be marked as billing

### Contact Information (Emails & Phones)
1. **Managed ONLY through Customer** - No separate endpoints
2. Email addresses must be in valid format
3. Phone numbers cannot be blank
4. Customer must have at least one email
5. Customer must have at least one phone
6. **Update replaces all** - Send complete arrays on update

### Address Management
1. **Managed ONLY through Customer** - No separate endpoints
2. Addresses cannot be blank
3. Customer must have at least one address
4. Only one address can be marked as billing address
5. When marking an address as billing, previous billing address is unmarked
6. Addresses can be active or inactive
7. **Update replaces all** - Send complete arrays on update

### Credit Management
1. Credit days range: 0 to 365
2. Credit limit must be non-negative
3. Zero credit days means immediate payment
4. Zero credit limit means no credit allowed

### Updates
1. Update operation replaces all emails, phones, and addresses
2. All updates modify the `updatedAt` timestamp
3. Customer code cannot be changed
4. Only one billing address allowed per customer

---

## Database Schema

### customers Table
```sql
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    code BIGINT NOT NULL UNIQUE,
    legal_name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    rif VARCHAR(50) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    credit_days INT NOT NULL DEFAULT 0,
    credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_code (code),
    INDEX idx_rif (rif),
    INDEX idx_active (active)
);
```

### customer_emails Table
```sql
CREATE TABLE customer_emails (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);
```

### customer_phones Table
```sql
CREATE TABLE customer_phones (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);
```

### customer_addresses Table
```sql
CREATE TABLE customer_addresses (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    address TEXT NOT NULL,
    billing BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);
```

---

## Exception Handling

### Custom Exceptions
- `CustomerNotFoundException` - Customer not found by ID (404 Not Found)
- `DuplicateCustomerRifException` - RIF already assigned to another customer (409 Conflict)

---

## Notes for Developers

### When Creating New Features
1. **Aggregate Root**: Customer is the aggregate root, always access emails/phones/addresses through Customer
2. **Value Objects**: Use value objects for all domain fields
3. **DTOs**: Keep them simple, use Java records with validation annotations
4. **No Nested Endpoints**: NEVER create endpoints like `/api/customers/{id}/emails` or `/api/emails/{id}`
5. **Replace Pattern**: Update operations replace entire collections (emails, phones, addresses)
6. **Customer Code**: Never modify customer code after creation
7. **Billing Address**: Ensure only one address is marked as billing at aggregate level
8. **Cascading**: Deleting a customer deletes all related emails, phones, and addresses
9. **Transactional**: All Customer + nested entities changes in one transaction

### API Design Rules
- ✅ DO: Manage nested entities through Customer endpoints
- ✅ DO: Use POST/PUT on `/api/customers` with full nested data
- ✅ DO: Return complete Customer with all nested entities
- ❌ DON'T: Create separate endpoints for emails, phones, or addresses
- ❌ DON'T: Allow partial updates of nested collections
- ❌ DON'T: Try to update/delete nested entities independently

### DTO Structure Rules
- **Request DTOs**: No IDs for nested entities (emails, phones, addresses)
- **Response DTOs**: Include IDs for all nested entities
- **Update Logic**: Backend deletes all old + creates all new nested entities
- **Frontend**: Must send complete arrays on update

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entities (Customer, CustomerEmail, CustomerPhone, CustomerAddress)
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test customer code generation uniqueness
- Test RIF uniqueness on customer creation and update
- Test billing address constraint (only one billing address)
- Test cascade deletion of related entities
- **Test aggregate pattern**: Verify no direct access to nested entities
- **Test replace logic**: Verify update deletes old + creates new nested entities
- **Test validation**: At least 1 email, 1 phone, 1 address required
