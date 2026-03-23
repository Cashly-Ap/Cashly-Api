# PRODUCTS Module

## Overview
The PRODUCTS module manages the product catalog for the invoicing system. It handles simple products with direct stock tracking and composite products assembled from component materials. Supports multiple units of measure (units, kilograms, bulk packages) and automatic stock deduction when assembling composite products.

**Important**: Product is an **Aggregate Root** in DDD terms. ProductComposition is an **internal entity** that can ONLY be managed through the Product aggregate. There are NO separate API endpoints for compositions - they are managed through Product create/update operations.

**Category Management**: Categories are managed in a **separate module**. See [CATEGORY.md](./CATEGORY.md) for complete documentation. Products reference categories by ID (foreign key relationship).

---

## Aggregate Pattern

### Product Aggregate Root
- **Root Entity**: Product
- **Internal Entities**: ProductComposition
- **Access Rule**: ALL operations on compositions MUST go through Product
- **API Design**: Only Product endpoints exist (/api/products)
- **No Direct Access**: No /api/compositions endpoints

### Why This Matters
1. **Consistency**: Ensures all changes to product data are coordinated
2. **Business Rules**: Composition rules enforced at aggregate level
3. **Transactional Integrity**: All changes committed together
4. **Simpler API**: Frontend only interacts with Product endpoints

---

## Domain Model

### Entities

#### Product (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.products.product.domain.entities.Product`

**Description**: Represents a product entity with complete business information, pricing, and composition details for composite products.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `ProductId` (Long) | Unique identifier | Required, immutable |
| `code` | `ProductCode` (String) | Product code | Required, immutable, auto-generated, pattern: PRD-XXXXX |
| `name` | `ProductName` (String) | Product name | Required, 3-200 characters |
| `description` | `ProductDescription` (String) | Product description | Optional, max 1000 characters |
| `categoryId` | `CategoryId` (Long) | Reference to Category entity | Required, must exist |
| `costPrice` | `Money` (BigDecimal) | Cost price | Required, min: 0, scale: 2 |
| `salePrice` | `Money` (BigDecimal) | Sale price | Required, min: 0, scale: 2 |
| `photo` | `String` | Photo URL/path | Optional |
| `active` | `boolean` | Product active status | Default: true |
| `productType` | `ProductType` (Enum) | Product type | Required, SIMPLE or COMPOSITE |
| `primaryUnitOfMeasure` | `UnitOfMeasure` (Enum) | Unit of measure | Required, UNIT, KILOGRAM, or BULK |
| `bulkQuantity` | `Integer` | Units per bulk | Required if BULK, null otherwise |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |
| `compositions` | `List<ProductComposition>` | Component requirements | Collection, required for COMPOSITE |

**Business Methods**:
- `create(...)` - Factory method to create new product
- `reconstitute(...)` - Factory method to rebuild product from database
- `updateName(ProductName)` - Updates product name
- `updateDescription(ProductDescription)` - Updates description
- `updateCategory(CategoryId)` - Updates category reference
- `updateCostPrice(Money)` - Updates cost price
- `updateSalePrice(Money)` - Updates sale price
- `updatePhoto(String)` - Updates photo URL
- `addComposition(ProductComposition)` - Adds new composition
- `clearCompositions()` - Removes all compositions
- `activate()` - Activates product
- `deactivate()` - Deactivates product

**Business Rules**:
1. Product code is auto-generated sequentially (PRD-00001, PRD-00002...)
2. Name is required and must be 3-200 characters
3. Category is a foreign key reference to the Category entity (see [CATEGORY.md](./CATEGORY.md))
4. Cost price and sale price must be non-negative
5. SIMPLE products cannot have compositions
6. COMPOSITE products must have at least 1 composition
7. If BULK unit, bulkQuantity is required and > 0
8. If not BULK unit, bulkQuantity must be null
9. ProductType cannot be changed after creation
10. All updates modify the `updatedAt` timestamp

---

#### ProductComposition (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.products.composition.domain.entities.ProductComposition`

**Description**: Represents the materials required to assemble one unit of a composite product. **ALWAYS managed through Product aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `ProductCompositionId` (Long) | Unique identifier | Required, immutable |
| `parentProductId` | `ProductId` (Long) | Composite product ID | Required, immutable |
| `componentProductId` | `ProductId` (Long) | Component product ID | Required |
| `quantityRequired` | `QuantityRequired` (BigDecimal) | Amount needed per unit | Required, min: 0.001, scale: 3 |
| `componentUnitOfMeasure` | `UnitOfMeasure` (Enum) | Component unit | Required |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Rules**:
1. Only COMPOSITE products can have compositions
2. **Minimum 1 component required** for COMPOSITE products (no maximum limit)
3. Component products can be SIMPLE or COMPOSITE (nested compositions allowed)
4. Cannot create circular dependencies (A requires B, B requires A)
5. Cannot use the same product as its own component (self-reference)
6. quantityRequired can be decimal (e.g., 0.5, 2.75) for flexible ratios
7. **NO direct API endpoints** - managed only through Product CRUD operations
8. Cannot exist independently outside Product aggregate

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `ProductId` | Long | Product unique identifier | Positive number |
| `ProductCode` | String | Product code | Pattern PRD-XXXXX, unique, auto-generated |
| `ProductName` | String | Product name | 3-200 characters, non-blank |
| `ProductDescription` | String | Product description | Max 1000 characters, nullable |
| `Money` | BigDecimal | Price amount | Non-negative, scale: 2 |
| `ProductCompositionId` | Long | Composition identifier | Positive number |
| `QuantityRequired` | BigDecimal | Component quantity | Min 0.001, scale: 3 |
| `BulkQuantity` | Integer | Units per bulk | Min 1, nullable |

**Note**: Category value objects (`CategoryId`, `CategoryCode`, `CategoryName`) are defined in the [CATEGORY module](./CATEGORY.md).

---

### Enums

#### ProductType
```java
public enum ProductType {
    SIMPLE,      // Standard product with direct stock tracking
    COMPOSITE    // Product assembled from components (auto-deducts materials)
}
```

#### UnitOfMeasure
```java
public enum UnitOfMeasure {
    UNIT,        // Individual units (e.g., 1 bottle, 1 item)
    KILOGRAM,    // Weight-based (e.g., 2.5 kg of flour)
    BULK         // Packages/Bundles (e.g., 1 bulk = 12 units)
}
```

**Examples**:
- **UNIT**: Laptop, Phone, T-shirt (sold individually)
- **KILOGRAM**: Rice, Flour, Coffee (sold by weight)
- **BULK**: Case of 12 sodas, Box of 50 pens (sold by package)

---

## API Endpoints

**IMPORTANT**: Only Product endpoints exist for compositions. There are NO separate endpoints for:
- ❌ `/api/products/{id}/compositions`
- ❌ `/api/compositions/{id}`

**All composition management is done through Product create/update operations.**

**Category endpoints** are documented in [CATEGORY.md](./CATEGORY.md).

---

## Product Endpoints

### 1. Create Product
**Endpoint**: `POST /api/products`

**Authentication**: Required (JWT Bearer Token)

**Content-Type**: `multipart/form-data`

**Description**: Creates a new product in the system. The request uses multipart/form-data to support file upload for the product photo.

**Request Parts**:
| Part Name | Type | Required | Description |
|-----------|------|----------|-------------|
| `product` | JSON | Yes | Product data as JSON string |
| `photo` | Binary | No | Product image file (JPEG, PNG, WebP) |

**Request Body** (`product` part) - Simple Product:
```json
{
  "name": "Laptop Dell XPS 15",
  "description": "15-inch professional laptop",
  "categoryId": 1,
  "costPrice": 800.00,
  "salePrice": 1200.00,
  "productType": "SIMPLE",
  "primaryUnitOfMeasure": "UNIT"
}
```

**Request Body** (`product` part) - Composite Product:
```json
{
  "name": "Gift Basket Premium",
  "description": "Luxury gift basket with wine and chocolates",
  "categoryId": 2,
  "costPrice": 45.00,
  "salePrice": 75.00,
  "productType": "COMPOSITE",
  "primaryUnitOfMeasure": "UNIT",
  "components": [
    {
      "componentProductId": 10,
      "quantityRequired": 2.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "componentProductId": 11,
      "quantityRequired": 1.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "componentProductId": 12,
      "quantityRequired": 0.5,
      "componentUnitOfMeasure": "KILOGRAM"
    }
  ]
}
```

**Request Body** (`product` part) - Bulk Product:
```json
{
  "name": "Coca-Cola Case",
  "description": "Case of 12 Coca-Cola cans",
  "categoryId": 3,
  "costPrice": 8.00,
  "salePrice": 12.00,
  "productType": "SIMPLE",
  "primaryUnitOfMeasure": "BULK",
  "bulkQuantity": 12
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer <token>" \
  -F 'product={"name":"Laptop Dell XPS 15","description":"15-inch professional laptop","categoryId":1,"costPrice":800.00,"salePrice":1200.00,"productType":"SIMPLE","primaryUnitOfMeasure":"UNIT"};type=application/json' \
  -F 'photo=@/path/to/laptop.jpg'
```

**Response** (201 Created) - `ProductCreateResponse`:
```json
{
  "id": 1,
  "code": "PRD-00001",
  "name": "Laptop Dell XPS 15",
  "description": "15-inch professional laptop",
  "categoryId": 1,
  "categoryCode": "CAT-00001",
  "categoryName": "Electronics",
  "costPrice": 800.00,
  "salePrice": 1200.00,
  "productType": "SIMPLE",
  "primaryUnitOfMeasure": "UNIT",
  "bulkQuantity": null,
  "photo": "/uploads/products/PRD-00001_1734567890.jpg",
  "active": true,
  "createdAt": "2025-12-17T10:00:00"
}
```

**Response** (201 Created) - Composite Product:
```json
{
  "id": 2,
  "code": "PRD-00002",
  "name": "Gift Basket Premium",
  "description": "Luxury gift basket with wine and chocolates",
  "categoryId": 2,
  "categoryCode": "CAT-00002",
  "categoryName": "Gifts",
  "costPrice": 45.00,
  "salePrice": 75.00,
  "productType": "COMPOSITE",
  "primaryUnitOfMeasure": "UNIT",
  "bulkQuantity": null,
  "photo": null,
  "active": true,
  "compositions": [
    {
      "id": 1,
      "componentProductId": 10,
      "componentProductCode": "PRD-00010",
      "componentProductName": "Red Wine Bottle",
      "quantityRequired": 2.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "id": 2,
      "componentProductId": 11,
      "componentProductCode": "PRD-00011",
      "componentProductName": "Greeting Card",
      "quantityRequired": 1.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "id": 3,
      "componentProductId": 12,
      "componentProductCode": "PRD-00012",
      "componentProductName": "Premium Chocolates",
      "quantityRequired": 0.5,
      "componentUnitOfMeasure": "KILOGRAM"
    }
  ],
  "createdAt": "2025-12-17T10:05:00"
}
```

**Photo Upload Details**:
- **Supported formats**: JPEG, PNG, WebP
- **Max file size**: 5MB (configurable)
- **Storage location**: Local folder (configurable, future: dedicated file server)
- **Filename format**: `{productCode}_{timestamp}.{extension}`
- **Database storage**: Path string stored in `photo` column
- **Configuration**: `lexand.storage.products.path` property

**Errors**:
- `400 Bad Request`: Invalid request data, validation error, or unsupported file format
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Category not found
- `413 Payload Too Large`: Photo file exceeds maximum size
- `422 Unprocessable Entity`: Circular dependency or invalid composition

---

### 2. Get Product by ID
**Endpoint**: `GET /api/products/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific product by ID. Returns `ProductDetailResponse`.

**Path Parameters**:
- `id` (Long) - Product unique identifier

**Response** (200 OK) - `ProductDetailResponse`:
```json
{
  "id": 2,
  "code": "PRD-00002",
  "name": "Gift Basket Premium",
  "description": "Luxury gift basket with wine and chocolates",
  "categoryId": 2,
  "categoryCode": "CAT-00002",
  "categoryName": "Gifts",
  "costPrice": 45.00,
  "salePrice": 75.00,
  "productType": "COMPOSITE",
  "primaryUnitOfMeasure": "UNIT",
  "bulkQuantity": null,
  "photo": null,
  "active": true,
  "compositions": [
    {
      "id": 1,
      "componentProductId": 10,
      "componentProductCode": "PRD-00010",
      "componentProductName": "Red Wine Bottle",
      "quantityRequired": 2.0,
      "componentUnitOfMeasure": "UNIT",
      "currentStock": 150
    },
    {
      "id": 2,
      "componentProductId": 11,
      "componentProductCode": "PRD-00011",
      "componentProductName": "Greeting Card",
      "quantityRequired": 1.0,
      "componentUnitOfMeasure": "UNIT",
      "currentStock": 80
    },
    {
      "id": 3,
      "componentProductId": 12,
      "componentProductCode": "PRD-00012",
      "componentProductName": "Premium Chocolates",
      "quantityRequired": 0.5,
      "componentUnitOfMeasure": "KILOGRAM",
      "currentStock": 25.500
    }
  ],
  "maxAssemblableUnits": 51,
  "createdAt": "2025-12-17T10:05:00",
  "updatedAt": "2025-12-17T10:05:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Product not found

**Note**: `maxAssemblableUnits` shows how many units can be assembled with current component stock.

---

### 3. List All Products
**Endpoint**: `GET /api/products`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all products in the system (minimal data for list view). Returns `List<ProductListResponse>`.

**Query Parameters** (optional):
- `categoryId` (Long): Filter by category ID
- `productType` (String): Filter by type (SIMPLE, COMPOSITE)
- `page` (int): Page number (default: 0)
- `size` (int): Page size (default: 20)

**Response** (200 OK) - `List<ProductListResponse>`:
```json
[
  {
    "id": 1,
    "code": "PRD-00001",
    "name": "Laptop Dell XPS 15",
    "categoryId": 1,
    "categoryCode": "CAT-00001",
    "categoryName": "Electronics",
    "salePrice": 1200.00,
    "productType": "SIMPLE",
    "primaryUnitOfMeasure": "UNIT",
    "bulkQuantity": null,
    "active": true,
    "photo": "/uploads/products/PRD-00001_1734567890.jpg"
  },
  {
    "id": 2,
    "code": "PRD-00002",
    "name": "Gift Basket Premium",
    "categoryId": 2,
    "categoryCode": "CAT-00002",
    "categoryName": "Gifts",
    "salePrice": 75.00,
    "productType": "COMPOSITE",
    "primaryUnitOfMeasure": "UNIT",
    "bulkQuantity": null,
    "active": true,
    "photo": null
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

**Note**: Returns ProductListResponse (minimal data, no compositions).

---

### 4. Update Product
**Endpoint**: `PUT /api/products/{id}`

**Authentication**: Required (JWT Bearer Token)

**Content-Type**: `multipart/form-data`

**Description**: Updates an existing product. Supports optional photo update.

**Path Parameters**:
- `id` (Long) - Product unique identifier

**Request Parts**:
| Part Name | Type | Required | Description |
|-----------|------|----------|-------------|
| `product` | JSON | Yes | Product data as JSON string |
| `photo` | Binary | No | New product image file (JPEG, PNG, WebP) |

**Request Body** (`product` part):
```json
{
  "name": "Gift Basket Premium Deluxe",
  "description": "Updated description",
  "categoryId": 2,
  "costPrice": 50.00,
  "salePrice": 85.00
}
```

**cURL Example** (with photo update):
```bash
curl -X PUT http://localhost:8080/api/products/2 \
  -H "Authorization: Bearer <token>" \
  -F 'product={"name":"Gift Basket Premium Deluxe","description":"Updated description","categoryId":2,"costPrice":50.00,"salePrice":85.00};type=application/json' \
  -F 'photo=@/path/to/new-basket.jpg'
```

**cURL Example** (without photo update):
```bash
curl -X PUT http://localhost:8080/api/products/2 \
  -H "Authorization: Bearer <token>" \
  -F 'product={"name":"Gift Basket Premium Deluxe","description":"Updated description","categoryId":2,"costPrice":50.00,"salePrice":85.00};type=application/json'
```

**Response** (200 OK) - `ProductDetailResponse`:
```json
{
  "id": 2,
  "code": "PRD-00002",
  "name": "Gift Basket Premium Deluxe",
  "description": "Updated description",
  "categoryId": 2,
  "categoryCode": "CAT-00002",
  "categoryName": "Gifts",
  "costPrice": 50.00,
  "salePrice": 85.00,
  "productType": "COMPOSITE",
  "primaryUnitOfMeasure": "UNIT",
  "bulkQuantity": null,
  "photo": "/uploads/products/PRD-00002_1734567890.jpg",
  "active": true,
  "compositions": [...],
  "createdAt": "2025-12-17T10:05:00",
  "updatedAt": "2025-12-17T11:30:00"
}
```

**Photo Update Behavior**:
- If `photo` part is provided: Old photo is deleted, new photo is saved
- If `photo` part is omitted: Existing photo is preserved
- To remove photo without replacement: Send empty `photo` part or use dedicated endpoint

**Errors**:
- `400 Bad Request`: Invalid request data, validation error, or unsupported file format
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Product or Category not found
- `413 Payload Too Large`: Photo file exceeds maximum size

**Note**: Cannot change `productType`, `primaryUnitOfMeasure`, or `bulkQuantity` after creation.

---

### 5. Update Product Composition
**Endpoint**: `PUT /api/products/{id}/composition`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates the composition of a composite product. Uses `UpdateCompositionRequest`.

**Path Parameters**:
- `id` (Long) - Product unique identifier

**Request Body** (`UpdateCompositionRequest`):
```json
{
  "components": [
    {
      "componentProductId": 10,
      "quantityRequired": 3.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "componentProductId": 11,
      "quantityRequired": 1.0,
      "componentUnitOfMeasure": "UNIT"
    },
    {
      "componentProductId": 12,
      "quantityRequired": 0.75,
      "componentUnitOfMeasure": "KILOGRAM"
    }
  ]
}
```

**Response** (200 OK) - `ProductDetailResponse`: Updated product with new composition

**Errors**:
- `400 Bad Request`: Invalid request data, circular dependency, or SIMPLE product
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Product or component not found
- `422 Unprocessable Entity`: Business rule violation

**Note**: Update replaces all compositions. Send complete collections. Uses "Replace All" strategy.

---

### 6. Delete Product
**Endpoint**: `DELETE /api/products/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Deletes a product from the system (soft delete - marks as inactive).

**Path Parameters**:
- `id` (Long) - Product unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Product not found
- `409 Conflict`: Cannot delete (has existing stock or used as component)

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
products/product/application/dto/
├── request/                    # Input DTOs for API requests
│   ├── CreateProductRequest.java
│   ├── UpdateProductRequest.java
│   ├── UpdateCompositionRequest.java
│   └── ProductCompositionRequest.java
└── response/                   # Output DTOs for API responses
    ├── ProductListResponse.java
    ├── ProductDetailResponse.java
    ├── ProductCreateResponse.java
    └── ProductCompositionResponse.java
```

**Note**: Category DTOs are in the [CATEGORY module](./CATEGORY.md).

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateProductRequest
**Usage**: POST /api/products (Create new product) - JSON part in multipart request

```java
public record CreateProductRequest(
    @NotBlank String name,                      // Required, 3-200 chars
    String description,                         // Optional, max 1000 chars
    @NotNull Long categoryId,                   // Required, must exist
    @NotNull @PositiveOrZero BigDecimal costPrice,    // Required, >= 0
    @NotNull @PositiveOrZero BigDecimal salePrice,    // Required, >= 0
    @NotNull ProductType productType,           // Required, SIMPLE or COMPOSITE
    @NotNull UnitOfMeasure primaryUnitOfMeasure,// Required
    Integer bulkQuantity,                       // Required if BULK
    List<ProductCompositionRequest> components  // Required if COMPOSITE
)
```

**Note**: Photo is sent as a separate binary part in multipart/form-data request.

**JSON Example** (Simple):
```json
{
  "name": "Laptop Dell XPS 15",
  "description": "15-inch professional laptop",
  "categoryId": 1,
  "costPrice": 800.00,
  "salePrice": 1200.00,
  "productType": "SIMPLE",
  "primaryUnitOfMeasure": "UNIT"
}
```

**JSON Example** (Composite):
```json
{
  "name": "Gift Basket Premium",
  "categoryId": 2,
  "costPrice": 45.00,
  "salePrice": 75.00,
  "productType": "COMPOSITE",
  "primaryUnitOfMeasure": "UNIT",
  "components": [
    { "componentProductId": 10, "quantityRequired": 2.0, "componentUnitOfMeasure": "UNIT" }
  ]
}
```

---

#### UpdateProductRequest
**Usage**: PUT /api/products/{id} (Update existing product) - JSON part in multipart request

```java
public record UpdateProductRequest(
    @NotBlank String name,                      // Required, 3-200 chars
    String description,                         // Optional
    @NotNull Long categoryId,                   // Required, must exist
    @NotNull @PositiveOrZero BigDecimal costPrice,    // Required
    @NotNull @PositiveOrZero BigDecimal salePrice     // Required
)
```

**Note**: Photo is sent as a separate binary part in multipart/form-data request (optional).
Cannot change productType, primaryUnitOfMeasure, or bulkQuantity after creation.

---

#### UpdateCompositionRequest
**Usage**: PUT /api/products/{id}/composition (Update composition)

```java
public record UpdateCompositionRequest(
    @NotNull @NotEmpty List<ProductCompositionRequest> components  // At least 1 required
)
```

---

#### ProductCompositionRequest
**Usage**: Nested in Create/Update requests

```java
public record ProductCompositionRequest(
    @NotNull Long componentProductId,           // Required, component product ID
    @NotNull @Positive BigDecimal quantityRequired,   // Required, > 0
    @NotNull UnitOfMeasure componentUnitOfMeasure     // Required
)
```

---

### Response DTOs

Located in: `application/dto/response/`

#### 1. ProductListResponse
**Usage**: GET /api/products (List view)

**Purpose**: Minimal data for table display in list views.

```java
public record ProductListResponse(
    Long id,                                    // Product ID
    String code,                                // Product code
    String name,                                // Product name
    Long categoryId,                            // Category ID
    String categoryCode,                        // Category code
    String categoryName,                        // Category name
    BigDecimal salePrice,                       // Sale price
    ProductType productType,                    // SIMPLE or COMPOSITE
    UnitOfMeasure primaryUnitOfMeasure,        // Unit of measure
    Integer bulkQuantity,                       // Bulk quantity (if BULK)
    boolean active,                             // Active status
    String photo                                // Photo path/URL
)
```

**JSON Example**:
```json
{
  "id": 1,
  "code": "PRD-00001",
  "name": "Laptop Dell XPS 15",
  "categoryId": 1,
  "categoryCode": "CAT-00001",
  "categoryName": "Electronics",
  "salePrice": 1200.00,
  "productType": "SIMPLE",
  "primaryUnitOfMeasure": "UNIT",
  "bulkQuantity": null,
  "active": true,
  "photo": "/uploads/products/PRD-00001_1734567890.jpg"
}
```

---

#### 2. ProductDetailResponse
**Usage**: GET /api/products/{id} (Detail view)

**Purpose**: Complete product data for detail/view pages.

```java
public record ProductDetailResponse(
    Long id,                                    // Product ID
    String code,                                // Product code
    String name,                                // Product name
    String description,                         // Description
    Long categoryId,                            // Category ID
    String categoryCode,                        // Category code
    String categoryName,                        // Category name
    BigDecimal costPrice,                       // Cost price
    BigDecimal salePrice,                       // Sale price
    String photo,                               // Photo path/URL
    ProductType productType,                    // SIMPLE or COMPOSITE
    UnitOfMeasure primaryUnitOfMeasure,        // Unit of measure
    Integer bulkQuantity,                       // Bulk quantity
    boolean active,                             // Active status
    List<ProductCompositionResponse> compositions,  // Compositions (null for SIMPLE)
    Integer maxAssemblableUnits,               // Max assemblable (COMPOSITE only)
    LocalDateTime createdAt,                    // Creation timestamp
    LocalDateTime updatedAt                     // Last update timestamp
)
```

---

#### 3. ProductCreateResponse
**Usage**: POST /api/products response (After creation)

**Purpose**: Complete product data returned after successful creation.

```java
public record ProductCreateResponse(
    Long id,                                    // Product ID (generated)
    String code,                                // Product code (generated)
    String name,                                // Product name
    String description,                         // Description
    Long categoryId,                            // Category ID
    String categoryCode,                        // Category code
    String categoryName,                        // Category name
    BigDecimal costPrice,                       // Cost price
    BigDecimal salePrice,                       // Sale price
    String photo,                               // Photo path/URL (generated)
    ProductType productType,                    // SIMPLE or COMPOSITE
    UnitOfMeasure primaryUnitOfMeasure,        // Unit of measure
    Integer bulkQuantity,                       // Bulk quantity
    boolean active,                             // Active status
    List<ProductCompositionResponse> compositions,  // Created compositions with IDs
    LocalDateTime createdAt                     // Creation timestamp (generated)
)
```

---

#### ProductCompositionResponse
**Usage**: Nested in responses

```java
public record ProductCompositionResponse(
    Long id,                                    // Composition ID
    Long componentProductId,                    // Component product ID
    String componentProductCode,               // Component product code
    String componentProductName,               // Component product name
    BigDecimal quantityRequired,               // Quantity needed
    UnitOfMeasure componentUnitOfMeasure,     // Component unit
    BigDecimal currentStock                    // Current stock (optional)
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/products | None | List\<ProductListResponse\> |
| GET /api/products/{id} | None | ProductDetailResponse |
| POST /api/products | CreateProductRequest + photo (multipart) | ProductCreateResponse |
| PUT /api/products/{id} | UpdateProductRequest + photo (multipart) | ProductDetailResponse |
| PUT /api/products/{id}/composition | UpdateCompositionRequest | ProductDetailResponse |
| DELETE /api/products/{id} | None | void (204 No Content) |

---

### 7. Generate Product Catalog PDF
**Endpoint**: `GET /api/products/pdf/catalog`

**Authentication**: Required (JWT Bearer Token)

**Description**: Generates a professional PDF catalog containing all active products with their catalog prices. The catalog is designed for external distribution and does **NOT include stock information** (internal/sensitive data). Prices are calculated using the sale price plus an optional differential (profit margin).

**Query Parameters** (optional):
| Parameter | Type | Description |
|-----------|------|-------------|
| `differentialId` | Long | ID of the differential to apply for price calculation. If omitted, products show their base sale price. |

**Price Calculation Formula**:
```
catalogPrice = salePrice × (1 + differential.percentage / 100)
```

**Examples**:
- Sale price: $100, No differential → Catalog price: $100.00
- Sale price: $100, Differential 25% → Catalog price: $125.00
- Sale price: $100, Differential 50% → Catalog price: $150.00

**Response** (200 OK): 
- **Content-Type**: `application/pdf`
- **Content-Disposition**: `attachment; filename="product-catalog-YYYY-MM-DD.pdf"`
- **Body**: Binary PDF file

**PDF Content**:
The generated PDF includes:
- **Header**: Company logo area and generation date
- **Table columns**: 
  - Code (product code)
  - Name (product name)
  - Category (category name)
  - Price (calculated catalog price)
  - Unit (unit of measure + bulk quantity if applicable)
- **Footer**: Page number (e.g., "Page 1 of 3")
- **Styling**: Professional table with alternating row colors

**What is NOT included** (intentionally):
- ❌ Stock quantities (internal data)
- ❌ Cost prices (confidential)
- ❌ Product photos
- ❌ Inactive products

**cURL Examples**:
```bash
# Generate catalog without differential (base prices)
curl -X GET "http://localhost:8080/api/products/pdf/catalog" \
  -H "Authorization: Bearer <token>" \
  -o "product-catalog.pdf"

# Generate catalog with 25% profit margin (differentialId=1)
curl -X GET "http://localhost:8080/api/products/pdf/catalog?differentialId=1" \
  -H "Authorization: Bearer <token>" \
  -o "product-catalog-with-margin.pdf"
```

**Use Cases**:
1. **Sales Representatives**: Download catalog to share with potential customers
2. **Print Distribution**: Generate PDF for printing physical catalogs
3. **Price Lists**: Create price lists with different profit margins for different customer segments

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Differential not found (if `differentialId` provided but doesn't exist, uses base prices)

**Technical Notes**:
- Uses iText 8.0.5 for PDF generation
- Products are filtered to show only `active = true`
- Categories are fetched and mapped to show category names
- If a product has no category (edge case), category columns show empty strings
- PDF is generated in-memory and streamed directly (no temporary files)

**Related**: See [DIFFERENTIAL.md](./DIFFERENTIAL.md) for managing differentials.

---

## Managing Compositions

### Creation Pattern
When creating a composite product, components are sent WITHOUT IDs:

```json
{
  "name": "Gift Basket",
  "productType": "COMPOSITE",
  "components": [
    { "componentProductId": 10, "quantityRequired": 2.0, "componentUnitOfMeasure": "UNIT" }
  ]
}
```

Backend generates IDs and returns them in the response.

---

### Update Pattern (Replace All)
When updating compositions, the backend **replaces ALL compositions**:

```json
{
  "components": [
    { "componentProductId": 10, "quantityRequired": 3.0, "componentUnitOfMeasure": "UNIT" }
  ]
}
```

**Important**:
- ❌ Backend does NOT merge old + new
- ❌ Backend does NOT check for IDs to update vs create
- ✅ Backend DELETES all old compositions and CREATES new ones
- ✅ Frontend must send complete arrays with ALL desired components

---

### Why Replace All?
1. **Simplicity**: No complex merge logic
2. **Consistency**: Clear state after update
3. **Frontend Control**: Frontend decides what to keep/remove
4. **Transactional**: All changes in one atomic operation

---

## Use Cases

### Application Layer Use Cases

#### Product CRUD
1. **CreateProductUseCase** - Creates a new product (simple or composite) with photo upload
2. **GetProductByIdUseCase** - Retrieves product by ID
3. **GetAllProductsUseCase** - Lists all products
4. **UpdateProductUseCase** - Updates product information with optional photo update
5. **UpdateProductCompositionUseCase** - Updates composition for composite products
6. **DeleteProductUseCase** - Soft deletes a product

#### PDF Generation
7. **GenerateCatalogPdfUseCase** - Generates a professional PDF catalog with all active products and optional differential pricing

---

## Repository Interfaces

### Domain Layer Ports

```java
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    Optional<Product> findByCode(ProductCode code);
    List<Product> findAll();
    List<Product> findByCategoryId(CategoryId categoryId);
    void delete(ProductId id);
    boolean existsById(ProductId id);
    boolean existsByCode(ProductCode code);
    boolean existsByCategoryId(CategoryId categoryId);
    long count();
}
```

```java
public interface ProductCompositionRepository {
    ProductComposition save(ProductComposition composition);
    List<ProductComposition> findByParentProductId(ProductId parentId);
    void deleteByParentProductId(ProductId parentId);
}
```

**Note**: `CategoryRepository` is defined in the [CATEGORY module](./CATEGORY.md).

---

## Domain Services

### ProductCodeGenerator
**Responsibilities**:
- Generate sequential product codes (PRD-00001, PRD-00002...)
- Ensure code uniqueness

**Implementation**: `SequentialProductCodeGenerator`
- Queries database for max code
- Increments by 1
- Starts from PRD-00001 if no products exist

### ProductPhotoStorageService
**Responsibilities**:
- Save product photos to storage
- Generate unique filenames
- Delete old photos when replaced
- Return storage path for database

**Implementation**: `LocalProductPhotoStorageService`
- Saves to configurable local folder
- Filename format: `{productCode}_{timestamp}.{extension}`
- Future: Can be replaced with cloud storage implementation

### CatalogPriceCalculator
**Responsibilities**:
- Calculate catalog prices with optional differential markup
- Apply percentage-based profit margins to sale prices

**Implementation**: `DefaultCatalogPriceCalculator`
- Formula: `catalogPrice = salePrice × (1 + percentage / 100)`
- If no differential provided, returns the original sale price
- Handles null differentials gracefully

### CatalogPdfGenerator
**Responsibilities**:
- Generate professional PDF catalogs
- Format product data into styled tables
- Handle pagination and headers/footers

**Implementation**: `ITextCatalogPdfGenerator`
- Uses iText 8.0.5 library
- Professional table styling with alternating row colors
- Includes page numbers and generation date
- Filters only active products

### CompositionValidator
**Responsibilities**:
- Validate composition rules
- Detect circular dependencies
- Validate component products exist and are active

---

## Business Rules Summary

### Aggregate Root Pattern
1. **Product is the Aggregate Root** - All operations go through Product
2. **No direct access to compositions** - Cannot CRUD compositions independently
3. **Transactional boundary** - All changes to Product + compositions in one transaction
4. **Consistency guarantee** - Aggregate ensures all business rules are enforced

### Product Type Rules
**SIMPLE Products**:
1. Cannot have compositions (no component requirements)
2. Stock tracked directly in warehouse
3. Adding/removing stock affects quantity directly

**COMPOSITE Products**:
1. **MUST have at least 1 composition** (no maximum limit)
2. Stock tracked in warehouse
3. **Adding stock triggers automatic component deduction**
4. Cannot add stock if insufficient components available
5. All component deductions are atomic (all succeed or none)

### Unit of Measure Rules
1. **UNIT**: bulkQuantity must be NULL
2. **KILOGRAM**: bulkQuantity must be NULL, quantity can be decimal
3. **BULK**: bulkQuantity REQUIRED and > 0

### Composition Rules
1. Component products must exist and be active
2. quantityRequired must be > 0
3. No self-reference (product cannot require itself)
4. No circular dependencies
5. componentUnitOfMeasure must match component's primaryUnitOfMeasure
6. **Update replaces all** - Send complete arrays on update

### Deletion Rules
1. Cannot delete product with existing stock
2. Cannot delete product used as component in other products
3. Soft delete: mark as inactive instead

### Category Relationship
1. Products must reference a valid category ID
2. Category must exist before product creation
3. Products include `categoryId`, `categoryCode`, and `categoryName` in responses
4. See [CATEGORY.md](./CATEGORY.md) for category management

---

## Database Schema

### products Table
```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    category_id BIGINT NOT NULL,
    cost_price DECIMAL(15, 2) NOT NULL,
    sale_price DECIMAL(15, 2) NOT NULL,
    photo VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    product_type VARCHAR(20) NOT NULL,
    primary_unit_of_measure VARCHAR(20) NOT NULL,
    bulk_quantity INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    
    INDEX idx_product_code (code),
    INDEX idx_product_category (category_id),
    INDEX idx_product_type (product_type),
    INDEX idx_product_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### product_compositions Table
```sql
CREATE TABLE product_compositions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_product_id BIGINT NOT NULL,
    component_product_id BIGINT NOT NULL,
    quantity_required DECIMAL(10, 3) NOT NULL,
    component_unit_of_measure VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (component_product_id) REFERENCES products(id) ON DELETE RESTRICT,
    
    UNIQUE KEY uk_parent_component (parent_product_id, component_product_id),
    INDEX idx_parent_product (parent_product_id),
    INDEX idx_component_product (component_product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Notes**:
- `ON DELETE RESTRICT` for category: cannot delete a category with associated products (see [CATEGORY.md](./CATEGORY.md))
- `ON DELETE CASCADE` for parent product: deleting a composite product removes its compositions
- `ON DELETE RESTRICT` for component product: cannot delete a product used as a component
- Unique constraint prevents duplicate components in same product
- Photo column stores the path/URL string (actual file stored externally)

---

## Exception Handling

### Custom Exceptions

#### Product Exceptions
- `ProductNotFoundException` - Product not found by ID (404 Not Found)
- `InsufficientComponentStockException` - Not enough component stock (422 Unprocessable Entity)
- `CircularCompositionException` - Circular dependency detected (400 Bad Request)
- `InvalidBulkQuantityException` - BULK product without bulkQuantity (400 Bad Request)
- `InvalidCompositionException` - COMPOSITE without components or SIMPLE with components (400 Bad Request)
- `CannotDeleteProductException` - Product has stock or is used as component (409 Conflict)

#### Photo Exceptions
- `InvalidPhotoFormatException` - Unsupported image format (400 Bad Request)
- `PhotoTooLargeException` - Photo exceeds max file size (413 Payload Too Large)
- `PhotoStorageException` - Error saving photo to storage (500 Internal Server Error)

**Note**: Category exceptions are in [CATEGORY.md](./CATEGORY.md).

---

## Notes for Developers

### When Creating New Features
1. **Aggregate Root**: Product is the aggregate root, always access compositions through Product
2. **Value Objects**: Use value objects for all domain fields
3. **DTOs**: Keep them simple, use Java records with validation annotations
4. **No Nested Endpoints**: NEVER create endpoints like `/api/compositions/{id}`
5. **Replace Pattern**: Update operations replace entire composition collections
6. **Product Code**: Never modify product code after creation
7. **ProductType**: Never modify product type after creation
8. **Cascading**: Deleting a product deletes all related compositions
9. **Transactional**: All Product + composition changes in one transaction

### API Design Rules
- ✅ DO: Manage compositions through Product endpoints
- ✅ DO: Use POST/PUT on `/api/products` with full composition data
- ✅ DO: Return complete Product with all compositions
- ❌ DON'T: Create separate endpoints for compositions
- ❌ DON'T: Allow partial updates of composition collections
- ❌ DON'T: Try to update/delete compositions independently

### DTO Structure Rules
- **Request DTOs**: No IDs for compositions (except componentProductId)
- **Response DTOs**: Include IDs for all compositions
- **Update Logic**: Backend deletes all old + creates all new compositions
- **Frontend**: Must send complete arrays on update

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entities (Product, ProductComposition)
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test product code generation uniqueness
- Test circular dependency detection
- Test cascade deletion of compositions
- **Test aggregate pattern**: Verify no direct access to compositions
- **Test replace logic**: Verify update deletes old + creates new compositions
- Test SIMPLE vs COMPOSITE validation rules
- Test BULK vs non-BULK validation rules
- **Test photo upload**: Verify file storage and path generation
- **Test photo update**: Verify old photo deletion on replacement
- **Test photo validation**: Verify format and size restrictions

---

## Configuration

### Photo Storage Configuration

Configure product photo storage in `application.properties`:

```properties
# Photo storage settings
lexand.storage.products.path=/uploads/products
lexand.storage.products.max-size=5MB
lexand.storage.products.allowed-formats=jpg,jpeg,png,webp

# Future: External storage server
# lexand.storage.products.type=local|s3|external
# lexand.storage.products.external-url=https://files.example.com
```

**Configuration Properties**:
| Property | Default | Description |
|----------|---------|-------------|
| `lexand.storage.products.path` | `/uploads/products` | Local folder for photo storage |
| `lexand.storage.products.max-size` | `5MB` | Maximum file size allowed |
| `lexand.storage.products.allowed-formats` | `jpg,jpeg,png,webp` | Accepted image formats |

**Future Enhancement**:
The photo storage can be migrated to a dedicated file server or cloud storage (S3, Azure Blob, etc.) by implementing the `ProductPhotoStorageService` interface with a different adapter.

---

**Related Modules**:
- [CATEGORY.md](./CATEGORY.md) - Category management (required by Products)
- [WAREHOUSE.md](./WAREHOUSE.md) - Stock management for products

---

**Last Updated**: 2025-12-26

**Module Status**: ✅ Implemented
