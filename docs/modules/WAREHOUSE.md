# WAREHOUSE Module

> Estructura actual del código: `warehouses/management` y `warehouses/stock` (incluye reservas y movimientos). Dentro de cada responsabilidad se ubican sus capas existentes; los controllers están en `infrastructure/web`, la persistencia en `infrastructure/persistence` y los casos de uso en `application/<operación>`. Consulta [ARCHITECTURE.md](../ARCHITECTURE.md) y [el árbol completo](../../openspec/changes/screaming-backend-structure/trees/after.md) para las rutas vigentes. Los ejemplos de planificación o paquetes Lexand de esta guía requieren contrastarse con el código actual.

## Overview
The WAREHOUSE module manages physical warehouses, product stock per warehouse, and maintains a complete audit trail of all inventory movements. It handles multi-warehouse support, stock operations (entry, exit, adjustments), and transfers between warehouses.

**Important**: Warehouse is an **Aggregate Root** in DDD terms. Stock and Movement entities are managed in their own bounded contexts but interact with Warehouse for validation. Stock operations require an active warehouse to proceed.

---

## Aggregate Pattern

### Warehouse Aggregate Root
- **Root Entity**: Warehouse
- **Related Entities**: WarehouseStock, StockMovement
- **Access Rule**: Stock operations validate warehouse existence and active status
- **API Design**: Separate endpoints for warehouse CRUD and stock operations

### Why This Matters
1. **Consistency**: Ensures all stock operations are tied to valid warehouses
2. **Business Rules**: Active warehouse rule enforced at aggregate level
3. **Transactional Integrity**: Stock changes committed with movement records
4. **Full Traceability**: Complete audit trail of all stock movements

---

## Domain Model

### Entities

#### Warehouse (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.warehouse.warehouse.domain.entities.Warehouse`

**Description**: Represents a physical warehouse where inventory is stored.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `WarehouseId` (Long) | Unique identifier | Required, immutable |
| `code` | `WarehouseCode` (String) | Warehouse code | Required, immutable, unique, pattern: WH-XXX |
| `name` | `WarehouseName` (String) | Warehouse name | Required, 3-100 characters |
| `location` | `WarehouseLocation` (String) | Physical address | Required, max 200 characters |
| `active` | `boolean` | Warehouse active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(...)` - Factory method to create new warehouse
- `reconstitute(...)` - Factory method to rebuild warehouse from database
- `updateDetails(WarehouseName, WarehouseLocation)` - Updates name and location
- `activate()` - Activates warehouse
- `deactivate()` - Deactivates warehouse (soft delete)
- `ensureActive()` - Validates warehouse is active, throws if inactive

**Business Rules**:
1. Warehouse code is unique and immutable
2. Warehouse code follows pattern WH-XXX
3. Name is required and must be 3-100 characters
4. Location is required and max 200 characters
5. Only active warehouses can have stock movements
6. Soft delete (marks as `active = false`)
7. Cannot deactivate a warehouse with stock > 0
8. All updates modify the `updatedAt` timestamp

---

#### WarehouseStock (Entity)
**Domain Entity**: `com.lexand.lexand_api.warehouse.stock.domain.entities.WarehouseStock`

**Description**: Represents current stock of a product in a specific warehouse.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `WarehouseStockId` (Long) | Unique identifier | Required, immutable |
| `warehouseId` | `WarehouseId` (Long) | Warehouse reference | Required, immutable |
| `productId` | `ProductId` (Long) | Product reference | Required, immutable |
| `quantity` | `StockQuantity` (Integer) | Current stock quantity | Required, min: 0 |
| `lastMovementAt` | `LocalDateTime` | Last movement timestamp | Auto-updated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(...)` - Factory method to create new stock record
- `addStock(int amount)` - Increases stock quantity
- `removeStock(int amount)` - Decreases stock quantity (validates sufficient stock)
- `adjustStock(StockQuantity newQuantity)` - Sets stock to specific quantity
- `hasEnoughStock(int required)` - Checks if sufficient stock available

**Business Rules**:
1. Quantity can never be negative
2. Combination `(warehouseId, productId)` is unique
3. Auto-create record if doesn't exist with `quantity = 0`
4. `lastMovementAt` updates automatically on every change
5. Remove operation validates sufficient stock before execution

---

#### StockMovement (Audit Entity - Immutable)
**Domain Entity**: `com.lexand.lexand_api.warehouse.movement.domain.entities.StockMovement`

**Description**: Immutable record of each stock movement for auditing. **Cannot be modified after creation.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `StockMovementId` (Long) | Unique identifier | Required, immutable |
| `warehouseId` | `WarehouseId` (Long) | Warehouse reference | Required, immutable |
| `productId` | `ProductId` (Long) | Product reference | Required, immutable |
| `movementType` | `MovementType` (Enum) | Type of movement | Required, immutable |
| `quantity` | `int` | Amount moved | Required, positive, immutable |
| `previousStock` | `int` | Stock before movement | Required, immutable |
| `newStock` | `int` | Stock after movement | Required, immutable |
| `transferBatchId` | `TransferBatchId` (String) | Groups transfer movements | Optional, immutable |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated, immutable |

**Business Methods**:
- `create(...)` - Factory method to create new movement
- `createWithBatch(...)` - Factory method to create movement with transfer batch ID

**Business Rules**:
1. Immutable (cannot be modified after creation)
2. Quantity always positive (direction defined by `movementType`)
3. Consistency validation: `previousStock ± quantity = newStock`
4. `transferBatchId` groups movements from the same transfer
5. Every stock change creates a `StockMovement` record

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `WarehouseId` | Long | Warehouse unique identifier | Positive number |
| `WarehouseCode` | String | Warehouse code | Pattern WH-XXX, unique, immutable |
| `WarehouseName` | String | Warehouse name | 3-100 characters, non-blank |
| `WarehouseLocation` | String | Physical address | Max 200 characters |
| `WarehouseStockId` | Long | Stock record identifier | Positive number |
| `StockQuantity` | Integer | Stock quantity | Non-negative integer (>= 0) |
| `StockMovementId` | Long | Movement identifier | Positive number |
| `TransferBatchId` | String | Transfer batch identifier | UUID or timestamp-based (e.g., TRF-20251217-143022) |
| `ProductId` | Long | Product reference | Positive number |

---

### Enums

#### MovementType
```java
public enum MovementType {
    ENTRY,       // Entry: purchases, returns, inbound transfer
    EXIT,        // Exit: sales, damages, outbound transfer
    ADJUSTMENT   // Manual adjustment: inventory correction
}
```

---

## API Endpoints

### 1. Create Warehouse
**Endpoint**: `POST /api/warehouses`

**Authentication**: Required (JWT Bearer Token)

**Description**: Creates a new warehouse in the system. Uses `CreateWarehouseRequest`.

**Request Body** (`CreateWarehouseRequest`):
```json
{
  "code": "WH-001",
  "name": "Main Warehouse",
  "location": "123 Main St, City"
}
```

**Response** (201 Created) - `WarehouseDetailResponse`:
```json
{
  "id": 1,
  "code": "WH-001",
  "name": "Main Warehouse",
  "location": "123 Main St, City",
  "active": true,
  "deletable": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T10:00:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `409 Conflict`: Duplicate warehouse code

---

### 2. Get Warehouse by ID
**Endpoint**: `GET /api/warehouses/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific warehouse by ID. Returns `WarehouseDetailResponse`.

**Path Parameters**:
- `id` (Long) - Warehouse unique identifier

**Response** (200 OK) - `WarehouseDetailResponse`:
```json
{
  "id": 1,
  "code": "WH-001",
  "name": "Main Warehouse",
  "location": "123 Main St, City",
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T10:00:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse not found

---

### 3. List All Warehouses
**Endpoint**: `GET /api/warehouses`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all warehouses in the system (minimal data for list view). Returns `List<WarehouseListResponse>`.

**Response** (200 OK) - `List<WarehouseListResponse>`:
```json
[
  {
    "id": 1,
    "code": "WH-001",
    "name": "Main Warehouse",
    "location": "123 Main St, City",
    "active": true
  },
  {
    "id": 2,
    "code": "WH-002",
    "name": "East Branch Warehouse",
    "location": "456 East Ave, Town",
    "active": true
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

**Note**: Returns WarehouseListResponse (minimal data, no timestamps).

---

### 4. Update Warehouse
**Endpoint**: `PUT /api/warehouses/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates an existing warehouse. Uses `UpdateWarehouseRequest`.

**Path Parameters**:
- `id` (Long) - Warehouse unique identifier

**Request Body** (`UpdateWarehouseRequest`):
```json
{
  "name": "Central Warehouse",
  "location": "789 Central Blvd, City"
}
```

**Response** (200 OK) - `WarehouseDetailResponse`:
```json
{
  "id": 1,
  "code": "WH-001",
  "name": "Central Warehouse",
  "location": "789 Central Blvd, City",
  "active": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T11:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse not found

**Note**: Warehouse code cannot be changed after creation.

---

### 5. Delete Warehouse
**Endpoint**: `DELETE /api/warehouses/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Soft deletes a warehouse (marks as inactive).

**Path Parameters**:
- `id` (Long) - Warehouse unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse not found
- `409 Conflict`: Cannot deactivate warehouse with existing stock

---

### 6. Add Stock (Entry)
**Endpoint**: `POST /api/warehouses/{id}/stock/entry`

**Authentication**: Required (JWT Bearer Token)

**Description**: Adds stock to a warehouse. Creates ENTRY movement records.

**Path Parameters**:
- `id` (Long) - Warehouse unique identifier

**Request Body** (`StockEntryRequest`):
```json
{
  "products": [
    {"productId": 10, "quantity": 100},
    {"productId": 11, "quantity": 50}
  ]
}
```

**Response** (204 No Content): Empty body

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse or product not found
- `422 Unprocessable Entity`: Warehouse inactive

**Note**: Creates 1 StockMovement with movementType=ENTRY per product.

---

### 7. Remove Stock (Exit)
**Endpoint**: `POST /api/warehouses/{id}/stock/exit`

**Authentication**: Required (JWT Bearer Token)

**Description**: Removes stock from a warehouse. Creates EXIT movement records.

**Path Parameters**:
- `id` (Long) - Warehouse unique identifier

**Request Body** (`StockExitRequest`):
```json
{
  "products": [
    {"productId": 10, "quantity": 20},
    {"productId": 11, "quantity": 10}
  ]
}
```

**Response** (204 No Content): Empty body

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse or product not found
- `422 Unprocessable Entity`: Insufficient stock or warehouse inactive

**Note**: Creates 1 StockMovement with movementType=EXIT per product.

---

### 8. Transfer Stock Between Warehouses
**Endpoint**: `POST /api/warehouses/transfer`

**Authentication**: Required (JWT Bearer Token)

**Description**: Transfers stock between warehouses. Creates EXIT at source + ENTRY at destination.

**Request Body** (`TransferStockRequest`):
```json
{
  "fromWarehouseId": 1,
  "toWarehouseId": 2,
  "products": [
    {"productId": 10, "quantity": 50},
    {"productId": 11, "quantity": 30},
    {"productId": 12, "quantity": 20}
  ]
}
```

**Response** (201 Created) - `TransferStockResponse`:
```json
{
  "transferBatchId": "TRF-20251217-143022",
  "fromWarehouseId": 1,
  "fromWarehouseCode": "WH-001",
  "toWarehouseId": 2,
  "toWarehouseCode": "WH-002",
  "movements": [
    {
      "id": 101,
      "warehouseId": 1,
      "warehouseCode": "WH-001",
      "productId": 10,
      "movementType": "EXIT",
      "quantity": 50,
      "previousStock": 200,
      "newStock": 150,
      "transferBatchId": "TRF-20251217-143022",
      "createdAt": "2025-12-17T14:30:22"
    },
    {
      "id": 102,
      "warehouseId": 2,
      "warehouseCode": "WH-002",
      "productId": 10,
      "movementType": "ENTRY",
      "quantity": 50,
      "previousStock": 80,
      "newStock": 130,
      "transferBatchId": "TRF-20251217-143022",
      "createdAt": "2025-12-17T14:30:22"
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Warehouse or product not found
- `422 Unprocessable Entity`: Insufficient stock or warehouse inactive

**Note**: Creates 2 movements per product (EXIT source + ENTRY destination). All share the same `transferBatchId`. Atomic operation (all or nothing).

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
application/dto/
├── request/                    # Input DTOs for API requests
│   ├── CreateWarehouseRequest.java
│   ├── UpdateWarehouseRequest.java
│   ├── StockEntryRequest.java
│   ├── StockExitRequest.java
│   ├── TransferStockRequest.java
│   └── ProductQuantityRequest.java
└── response/                   # Output DTOs for API responses
    ├── WarehouseListResponse.java
    ├── WarehouseDetailResponse.java
    ├── WarehouseCreateResponse.java
    ├── TransferStockResponse.java
    └── StockMovementResponse.java
```

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateWarehouseRequest
**Usage**: POST /api/warehouses (Create new warehouse)

```java
public record CreateWarehouseRequest(
    @NotBlank String code,                      // Required, pattern WH-XXX
    @NotBlank String name,                      // Required, 3-100 chars
    @NotBlank String location                   // Required, max 200 chars
)
```

**JSON Example**:
```json
{
  "code": "WH-003",
  "name": "South Warehouse",
  "location": "789 South Ave, City"
}
```

---

#### UpdateWarehouseRequest
**Usage**: PUT /api/warehouses/{id} (Update existing warehouse)

```java
public record UpdateWarehouseRequest(
    @NotBlank String name,                      // Required, 3-100 chars
    @NotBlank String location                   // Required, max 200 chars
)
```

**JSON Example**:
```json
{
  "name": "Updated Warehouse Name",
  "location": "New Address, City"
}
```

**Note**: Warehouse code cannot be changed.

---

#### StockEntryRequest
**Usage**: POST /api/warehouses/{id}/stock/entry (Add stock)

```java
public record StockEntryRequest(
    @NotEmpty List<ProductQuantityRequest> products   // At least 1 required
)
```

---

#### StockExitRequest
**Usage**: POST /api/warehouses/{id}/stock/exit (Remove stock)

```java
public record StockExitRequest(
    @NotEmpty List<ProductQuantityRequest> products   // At least 1 required
)
```

---

#### TransferStockRequest
**Usage**: POST /api/warehouses/transfer (Transfer between warehouses)

```java
public record TransferStockRequest(
    @NotNull Long fromWarehouseId,              // Required, source warehouse
    @NotNull Long toWarehouseId,                // Required, destination warehouse
    @NotEmpty List<ProductQuantityRequest> products   // At least 1 required
)
```

---

#### ProductQuantityRequest
**Usage**: Nested in stock operation requests

```java
public record ProductQuantityRequest(
    @NotNull Long productId,                    // Required, product ID
    @Positive int quantity                      // Required, positive integer
)
```

---

### Response DTOs

Located in: `application/dto/response/`

#### 1. WarehouseListResponse
**Usage**: GET /api/warehouses (List view)

**Purpose**: Minimal data for table display in list views.

```java
public record WarehouseListResponse(
    Long id,                                    // Warehouse ID
    String code,                                // Warehouse code
    String name,                                // Warehouse name
    String location,                            // Physical address
    boolean active                              // Active status
)
```

**JSON Example**:
```json
{
  "id": 1,
  "code": "WH-001",
  "name": "Main Warehouse",
  "location": "123 Main St, City",
  "active": true
}
```

---

#### 2. WarehouseDetailResponse
**Usage**: GET /api/warehouses/{id} (Detail view)

**Purpose**: Complete warehouse data for detail/view pages.

```java
public record WarehouseDetailResponse(
    Long id,                                    // Warehouse ID
    String code,                                // Warehouse code
    String name,                                // Warehouse name
    String location,                            // Physical address
    boolean active,                             // Active status
    boolean deletable,                          // False when active stock prevents deletion
    LocalDateTime createdAt,                    // Creation timestamp
    LocalDateTime updatedAt                     // Last update timestamp
)
```

**JSON Example**:
```json
{
  "id": 1,
  "code": "WH-001",
  "name": "Main Warehouse",
  "location": "123 Main St, City",
  "active": true,
  "deletable": true,
  "createdAt": "2025-12-17T10:00:00",
  "updatedAt": "2025-12-17T11:30:00"
}
```

---

#### 3. WarehouseCreateResponse
**Usage**: POST /api/warehouses response (After creation)

**Purpose**: Complete warehouse data returned after successful creation.

```java
public record WarehouseCreateResponse(
    Long id,                                    // Warehouse ID (generated)
    String code,                                // Warehouse code
    String name,                                // Warehouse name
    String location,                            // Physical address
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp (generated)
    LocalDateTime updatedAt                     // Update timestamp (generated)
)
```

---

#### 4. TransferStockResponse
**Usage**: POST /api/warehouses/transfer response

**Purpose**: Complete transfer data with all movements.

```java
public record TransferStockResponse(
    String transferBatchId,                     // Batch ID grouping movements
    Long fromWarehouseId,                       // Source warehouse ID
    String fromWarehouseCode,                   // Source warehouse code
    Long toWarehouseId,                         // Destination warehouse ID
    String toWarehouseCode,                     // Destination warehouse code
    List<StockMovementResponse> movements       // All movements created
)
```

---

#### 5. StockMovementResponse
**Usage**: Nested in TransferStockResponse

```java
public record StockMovementResponse(
    Long id,                                    // Movement ID
    Long warehouseId,                           // Warehouse ID
    String warehouseCode,                       // Warehouse code
    Long productId,                             // Product ID
    String movementType,                        // ENTRY, EXIT, or ADJUSTMENT
    int quantity,                               // Amount moved
    int previousStock,                          // Stock before
    int newStock,                               // Stock after
    String transferBatchId,                     // Batch ID (if transfer)
    LocalDateTime createdAt                     // Movement timestamp
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/warehouses | None | List\<WarehouseListResponse\> |
| GET /api/warehouses/{id} | None | WarehouseDetailResponse |
| GET /api/warehouses/{id}/stock | `page`, `size` | PageResponse\<WarehouseStockResponse\> |
| GET /api/warehouses/{id}/movements | `productId`, `movementType`, `page`, `size` | PageResponse\<StockMovementResponse\> |
| POST /api/warehouses | CreateWarehouseRequest | WarehouseCreateResponse |
| PUT /api/warehouses/{id} | UpdateWarehouseRequest | WarehouseDetailResponse |
| DELETE /api/warehouses/{id} | None | void (204 No Content) |
| POST /api/warehouses/{id}/stock/entry | StockEntryRequest | void (204 No Content) |
| POST /api/warehouses/{id}/stock/exit | StockExitRequest | void (204 No Content) |
| POST /api/warehouses/transfer | TransferStockRequest | TransferStockResponse |

`WarehouseStockResponse` includes `composite`, allowing clients to distinguish simple and
composite products without issuing an additional product request. Registering an entry for a
composite product consumes `component quantity × entry quantity` from every configured component
and records a `COMPONENT_EXIT` movement before recording the composite `ENTRY` movement.

---

## Use Cases

### Application Layer Use Cases

#### Warehouse CRUD
1. **CreateWarehouseUseCase** - Creates a new warehouse
2. **GetWarehouseByIdUseCase** - Retrieves warehouse by ID
3. **GetAllWarehousesUseCase** - Lists all warehouses
4. **UpdateWarehouseUseCase** - Updates warehouse information
5. **DeleteWarehouseUseCase** - Deactivates warehouse (soft delete)

#### Stock Operations
6. **AddStockUseCase** - Adds stock (ENTRY) - batch of products
7. **RemoveStockUseCase** - Removes stock (EXIT) - batch of products
8. **TransferStockUseCase** - Transfers between warehouses - multiple products

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface WarehouseRepository {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findById(WarehouseId id);
    List<Warehouse> findAll();
    void delete(WarehouseId id);
    boolean existsById(WarehouseId id);
    boolean existsByCode(WarehouseCode code);
}
```

```java
public interface WarehouseStockRepository {
    WarehouseStock save(WarehouseStock stock);
    Optional<WarehouseStock> findByWarehouseIdAndProductId(WarehouseId warehouseId, ProductId productId);
    List<WarehouseStock> findByWarehouseId(WarehouseId warehouseId);
    List<WarehouseStock> findByProductId(ProductId productId);
}
```

```java
public interface StockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByWarehouseId(WarehouseId warehouseId);
    List<StockMovement> findByTransferBatchId(TransferBatchId batchId);
}
```

---

## Business Rules Summary

### Warehouse Management
1. **Unique Code**: Warehouse code is unique and cannot be changed
2. **Code Format**: Must follow pattern WH-XXX
3. **Soft Delete**: DELETE marks as `active = false`, does not physically delete
4. **Active Only**: Only active warehouses can have stock movements
5. **Cannot Delete with Stock**: Cannot deactivate a warehouse with stock > 0

### Stock Management
6. **Non-Negative Stock**: Quantity can never be < 0
7. **Uniqueness**: Only 1 record per combination `(warehouseId, productId)`
8. **Auto-Create**: If record doesn't exist, creates with `quantity = 0`
9. **Automatic Timestamp**: `lastMovementAt` updates on every change
10. **Validation**: Validates sufficient stock before EXIT operations

### Movement Auditing
11. **Immutability**: Once created, movements cannot be modified
12. **Mandatory Recording**: Every stock change creates a `StockMovement`
13. **Consistency Validation**: `previousStock ± quantity = newStock`
14. **Transfer Grouping**: Transfers use shared `transferBatchId`

### Transfer Operations
15. **Atomicity**: Entire transfer executes or rolls back completely
16. **Double Movement**: Creates EXIT at source + ENTRY at destination
17. **Batch ID**: All movements share `transferBatchId`
18. **Prior Validation**: Validates sufficient stock before executing

---

## Database Schema

### warehouses Table
```sql
CREATE TABLE warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_warehouse_code (code),
    INDEX idx_warehouse_active (active),
    INDEX idx_warehouse_name (name)
);
```

### warehouse_stock Table
```sql
CREATE TABLE warehouse_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    last_movement_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_warehouse_product (warehouse_id, product_id),
    INDEX idx_warehouse_stock_warehouse (warehouse_id),
    INDEX idx_warehouse_stock_product (product_id),
    INDEX idx_warehouse_stock_quantity (quantity),
    
    CONSTRAINT chk_stock_quantity_positive CHECK (quantity >= 0)
);
```

### stock_movements Table
```sql
CREATE TABLE stock_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    movement_type ENUM('ENTRY', 'EXIT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    transfer_batch_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_movement_warehouse (warehouse_id),
    INDEX idx_movement_product (product_id),
    INDEX idx_movement_type (movement_type),
    INDEX idx_movement_transfer_batch (transfer_batch_id),
    INDEX idx_movement_created (created_at),
    INDEX idx_movement_warehouse_product (warehouse_id, product_id),
    
    CONSTRAINT chk_movement_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_movement_stocks_positive CHECK (previous_stock >= 0 AND new_stock >= 0)
);
```

---

## Exception Handling

### Custom Exceptions
- `WarehouseNotFoundException` - Warehouse not found by ID (404 Not Found)
- `DuplicateWarehouseCodeException` - Duplicate warehouse code (409 Conflict)
- `WarehouseInactiveException` - Operation on inactive warehouse (422 Unprocessable Entity)
- `InsufficientStockException` - Insufficient stock for operation (422 Unprocessable Entity)

---

## Notes for Developers

### When Creating New Features
1. **Aggregate Root**: Warehouse is the aggregate root, validate warehouse existence and active status for stock operations
2. **Value Objects**: Use value objects for all domain fields
3. **DTOs**: Keep them simple, use Java records with validation annotations
4. **Immutable Movements**: StockMovement records are immutable once created
5. **Atomic Transfers**: Ensure transfers are atomic (all or nothing)
6. **Warehouse Code**: Never modify warehouse code after creation
7. **Soft Delete**: Use deactivation instead of physical deletion
8. **Cascading Validation**: Always validate warehouse is active before stock operations

### API Design Rules
- ✅ DO: Validate warehouse existence and active status before stock operations
- ✅ DO: Create movement records for every stock change
- ✅ DO: Use batch IDs to group related transfer movements
- ✅ DO: Return complete transfer details including all movements
- ❌ DON'T: Allow stock operations on inactive warehouses
- ❌ DON'T: Modify or delete movement records
- ❌ DON'T: Allow negative stock quantities

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entities (Warehouse, WarehouseStock, StockMovement)
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test warehouse code uniqueness
- Test soft delete behavior
- Test stock operations create correct movements
- Test transfer atomicity (all succeed or all fail)
- Test insufficient stock validation
- Test inactive warehouse validation
