# DIFFERENTIAL Module

## Overview
The DIFFERENTIAL module manages price differentials (percentage adjustments) that can be applied to product prices. It handles both price increases and discounts through percentage values that modify the base sale price of products.

**Important**: Differential is a **simple Aggregate Root** in DDD terms with no internal entities. It represents a standalone price adjustment configuration that can be applied to products or orders.

---

## Aggregate Pattern

### Differential Aggregate Root
- **Root Entity**: Differential
- **Internal Entities**: None (simple aggregate)
- **Access Rule**: Direct CRUD operations on Differential
- **API Design**: Standard CRUD endpoints (/api/differentials)

### Why This Matters
1. **Consistency**: Ensures price adjustments are validated
2. **Business Rules**: Percentage range enforced at entity level
3. **Transactional Integrity**: Simple atomic operations
4. **Clear Purpose**: Single responsibility for price modifications

---

## Domain Model

### Entities

#### Differential (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.differential.domain.entities.Differential`

**Description**: Represents a price differential that can be applied to modify product prices. Supports both positive (price increase) and negative (discount) percentages.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `DifferentialId` (Long) | Unique identifier | Required, immutable |
| `percentage` | `DifferentialPercentage` (BigDecimal) | Percentage adjustment | Required, range: -100 to +100, scale: 2 |
| `active` | `boolean` | Differential active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(...)` - Factory method to create new differential
- `reconstitute(...)` - Factory method to rebuild differential from database
- `updatePercentage(DifferentialPercentage)` - Updates percentage value
- `activate()` - Activates differential
- `deactivate()` - Deactivates differential
- `calculateFinalPrice(BigDecimal salePrice)` - Calculates price with differential applied

**Business Rules**:
1. Percentage must be between -100 and +100
2. Positive percentage = price increase (e.g., +10 = 10% more expensive)
3. Negative percentage = discount (e.g., -15 = 15% discount)
4. Only active differentials can be applied to prices
5. All updates modify the `updatedAt` timestamp

**Price Calculation Formula**:
```
finalPrice = salePrice × (1 + percentage/100)
```

**Examples**:
- salePrice = $100, percentage = +10 → finalPrice = $110
- salePrice = $100, percentage = -15 → finalPrice = $85
- salePrice = $200, percentage = +25 → finalPrice = $250
- salePrice = $200, percentage = -50 → finalPrice = $100

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `DifferentialId` | Long | Differential unique identifier | Positive number |
| `DifferentialPercentage` | BigDecimal | Percentage adjustment | Range: -100 to +100, scale: 2 |

---

## API Endpoints

### 1. Create Differential
**Endpoint**: `POST /api/differentials`

**Authentication**: Required (JWT Bearer Token)

**Description**: Creates a new differential in the system. Uses `CreateDifferentialRequest`.

**Request Body** (`CreateDifferentialRequest`):
```json
{
  "percentage": 10.00,
  "active": true
}
```

**Response** (201 Created) - `DifferentialCreateResponse`:
```json
{
  "id": 1,
  "percentage": 10.00,
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T10:00:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error (percentage out of range)
- `401 Unauthorized`: Missing or invalid JWT token

---

### 2. Get Differential by ID
**Endpoint**: `GET /api/differentials/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific differential by ID. Returns `DifferentialDetailResponse`.

**Path Parameters**:
- `id` (Long) - Differential unique identifier

**Response** (200 OK) - `DifferentialDetailResponse`:
```json
{
  "id": 1,
  "percentage": 10.00,
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T10:00:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Differential not found

---

### 3. List All Differentials
**Endpoint**: `GET /api/differentials`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all differentials in the system. Returns `List<DifferentialListResponse>`.

**Query Parameters** (optional):
- `active` (Boolean): Filter by active status

**Response** (200 OK) - `List<DifferentialListResponse>`:
```json
[
  {
    "id": 1,
    "percentage": 10.00,
    "active": true
  },
  {
    "id": 2,
    "percentage": -15.00,
    "active": true
  },
  {
    "id": 3,
    "percentage": 25.00,
    "active": false
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

**Note**: Returns DifferentialListResponse (minimal data, no timestamps).

---

### 4. Update Differential
**Endpoint**: `PUT /api/differentials/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates an existing differential. Uses `UpdateDifferentialRequest`.

**Path Parameters**:
- `id` (Long) - Differential unique identifier

**Request Body** (`UpdateDifferentialRequest`):
```json
{
  "percentage": 15.00,
  "active": true
}
```

**Response** (200 OK) - `DifferentialDetailResponse`:
```json
{
  "id": 1,
  "percentage": 15.00,
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T11:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Differential not found

---

### 5. Delete Differential
**Endpoint**: `DELETE /api/differentials/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Deletes a differential from the system.

**Path Parameters**:
- `id` (Long) - Differential unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Differential not found

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
application/dto/
├── request/                    # Input DTOs for API requests
│   ├── CreateDifferentialRequest.java
│   └── UpdateDifferentialRequest.java
└── response/                   # Output DTOs for API responses
    ├── DifferentialListResponse.java
    ├── DifferentialDetailResponse.java
    └── DifferentialCreateResponse.java
```

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateDifferentialRequest
**Usage**: POST /api/differentials (Create new differential)

```java
public record CreateDifferentialRequest(
    @NotNull @DecimalMin("-100.00") @DecimalMax("100.00") BigDecimal percentage,  // Required, -100 to +100
    @NotNull Boolean active                                                         // Required
)
```

**JSON Example** (Price Increase):
```json
{
  "percentage": 10.00,
  "active": true
}
```

**JSON Example** (Discount):
```json
{
  "percentage": -15.00,
  "active": true
}
```

---

#### UpdateDifferentialRequest
**Usage**: PUT /api/differentials/{id} (Update existing differential)

```java
public record UpdateDifferentialRequest(
    @NotNull @DecimalMin("-100.00") @DecimalMax("100.00") BigDecimal percentage,  // Required, -100 to +100
    @NotNull Boolean active                                                         // Required
)
```

**JSON Example**:
```json
{
  "percentage": 20.00,
  "active": false
}
```

---

### Response DTOs

Located in: `application/dto/response/`

#### 1. DifferentialListResponse
**Usage**: GET /api/differentials (List view)

**Purpose**: Minimal data for table display in list views.

```java
public record DifferentialListResponse(
    Long id,                                    // Differential ID
    BigDecimal percentage,                      // Percentage value
    boolean active                              // Active status
)
```

**JSON Example**:
```json
{
  "id": 1,
  "percentage": 10.00,
  "active": true
}
```

---

#### 2. DifferentialDetailResponse
**Usage**: GET /api/differentials/{id} (Detail view)

**Purpose**: Complete differential data for detail/view pages.

```java
public record DifferentialDetailResponse(
    Long id,                                    // Differential ID
    BigDecimal percentage,                      // Percentage value
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp
    LocalDateTime updatedAt                     // Last update timestamp
)
```

**JSON Example**:
```json
{
  "id": 1,
  "percentage": 10.00,
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T11:30:00"
}
```

---

#### 3. DifferentialCreateResponse
**Usage**: POST /api/differentials response (After creation)

**Purpose**: Complete differential data returned after successful creation.

```java
public record DifferentialCreateResponse(
    Long id,                                    // Differential ID (generated)
    BigDecimal percentage,                      // Percentage value
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp (generated)
    LocalDateTime updatedAt                     // Update timestamp (generated)
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/differentials | None | List\<DifferentialListResponse\> |
| GET /api/differentials/{id} | None | DifferentialDetailResponse |
| POST /api/differentials | CreateDifferentialRequest | DifferentialCreateResponse |
| PUT /api/differentials/{id} | UpdateDifferentialRequest | DifferentialDetailResponse |
| DELETE /api/differentials/{id} | None | void (204 No Content) |

---

## Use Cases

### Application Layer Use Cases
1. **CreateDifferentialUseCase** - Creates a new differential
2. **GetDifferentialByIdUseCase** - Retrieves differential by ID
3. **GetAllDifferentialsUseCase** - Lists all differentials
4. **UpdateDifferentialUseCase** - Updates differential information
5. **DeleteDifferentialUseCase** - Deletes a differential

---

## Repository Interfaces

### Domain Layer Port

```java
public interface DifferentialRepository {
    Differential save(Differential differential);
    Optional<Differential> findById(DifferentialId id);
    List<Differential> findAll();
    List<Differential> findByActive(boolean active);
    void delete(DifferentialId id);
    boolean existsById(DifferentialId id);
}
```

---

## Domain Services

### PriceCalculationService
**Responsibilities**:
- Calculate final price with differential applied
- Validate differential is active before applying

**Methods**:
```java
public interface PriceCalculationService {
    BigDecimal calculateFinalPrice(BigDecimal salePrice, Differential differential);
    BigDecimal calculateFinalPrice(BigDecimal salePrice, DifferentialPercentage percentage);
}
```

**Implementation**:
```java
public BigDecimal calculateFinalPrice(BigDecimal salePrice, DifferentialPercentage percentage) {
    // finalPrice = salePrice × (1 + percentage/100)
    BigDecimal multiplier = BigDecimal.ONE.add(
        percentage.value().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
    );
    return salePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
}
```

---

## Business Rules Summary

### Percentage Rules
1. **Range**: Percentage must be between -100 and +100
2. **Positive Value**: Represents price increase (e.g., +10 = 10% increase)
3. **Negative Value**: Represents discount (e.g., -15 = 15% discount)
4. **Zero Value**: No price change (allowed)
5. **Precision**: Scale of 2 decimal places

### Active Status Rules
1. **Active Only**: Only active differentials should be applied to prices
2. **Inactive**: Inactive differentials exist but are not used
3. **Toggle**: Can be activated/deactivated as needed

### Price Calculation
1. **Formula**: `finalPrice = salePrice × (1 + percentage/100)`
2. **Rounding**: Round to 2 decimal places using HALF_UP
3. **Validation**: Validate differential is active before calculation

---

## Database Schema

### differentials Table
```sql
CREATE TABLE differentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    percentage DECIMAL(5, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_active (active),
    INDEX idx_percentage (percentage),
    
    CONSTRAINT chk_percentage_range CHECK (percentage >= -100.00 AND percentage <= 100.00)
);
```

---

## Exception Handling

### Custom Exceptions
- `DifferentialNotFoundException` - Differential not found by ID (404 Not Found)
- `InvalidPercentageException` - Percentage out of valid range (400 Bad Request)

---

## Notes for Developers

### When Creating New Features
1. **Simple Aggregate**: Differential is a simple entity with no nested structures
2. **Value Objects**: Use value objects for percentage validation
3. **DTOs**: Keep them simple, use Java records with validation annotations
4. **Active Validation**: Always check if differential is active before applying
5. **Price Calculation**: Use the domain service for consistent calculations

### API Design Rules
- ✅ DO: Validate percentage range (-100 to +100)
- ✅ DO: Return complete differential data after operations
- ✅ DO: Filter by active status when listing
- ❌ DON'T: Allow percentage values outside valid range
- ❌ DON'T: Apply inactive differentials to prices

### Testing Requirements
- Unit tests for DifferentialPercentage value object validation
- Unit tests for price calculation formula
- Unit tests for domain entity business methods
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test percentage range validation (-100 to +100)
- Test active/inactive filtering
- Test price calculation with various percentage values

---

## Usage Examples

### Example 1: 10% Price Increase
```
Differential:
- percentage: 10.00
- active: true

Product A base price: $100.00
Calculation: $100.00 × (1 + 10/100) = $100.00 × 1.10 = $110.00
Final price with differential: $110.00
```

### Example 2: 15% Discount
```
Differential:
- percentage: -15.00
- active: true

Product B base price: $200.00
Calculation: $200.00 × (1 + (-15)/100) = $200.00 × 0.85 = $170.00
Final price with differential: $170.00
```

### Example 3: 50% Discount
```
Differential:
- percentage: -50.00
- active: true

Product C base price: $80.00
Calculation: $80.00 × (1 + (-50)/100) = $80.00 × 0.50 = $40.00
Final price with differential: $40.00
```

### Example 4: 100% Increase (Double Price)
```
Differential:
- percentage: 100.00
- active: true

Product D base price: $50.00
Calculation: $50.00 × (1 + 100/100) = $50.00 × 2.00 = $100.00
Final price with differential: $100.00
```
