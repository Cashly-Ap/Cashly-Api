# CATEGORY Module

## Overview
The CATEGORY module manages product categories for the invoicing system. Categories provide a way to organize and group products, enabling consistent categorization, filtering, and reporting across the product catalog.

**Important**: Category is an **independent entity** with its own lifecycle (create, read, update, delete). Products reference categories by ID (foreign key relationship). A category cannot be deleted if it has associated products.

---

## Domain Model

### Entities

#### Category (Independent Entity)
**Domain Entity**: `com.lexand.lexand_api.category.domain.entities.Category`

**Description**: Represents a product category for organizing and grouping products. Categories have their own lifecycle and are referenced by products via foreign key.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `CategoryId` (Long) | Unique identifier | Required, immutable, auto-generated |
| `code` | `CategoryCode` (String) | Category code | Required, unique, immutable, auto-generated, pattern: CAT-XXXXX |
| `name` | `CategoryName` (String) | Category name | Required, 2-100 characters, unique |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |

**Business Methods**:
- `create(CategoryName)` - Factory method to create new category
- `reconstitute(CategoryId, CategoryCode, CategoryName, LocalDateTime, LocalDateTime)` - Factory method to rebuild from database
- `updateName(CategoryName)` - Updates category name

**Business Rules**:
1. Category code is auto-generated sequentially (CAT-00001, CAT-00002...)
2. Category name must be unique (case-insensitive)
3. Name is required and must be 2-100 characters
4. Cannot delete a category that has associated products
5. All updates modify the `updatedAt` timestamp

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `CategoryId` | Long | Category unique identifier | Positive number |
| `CategoryCode` | String | Category code | Pattern CAT-XXXXX, unique, auto-generated |
| `CategoryName` | String | Category name | 2-100 characters, non-blank, unique |

---

## API Endpoints

### 1. Create Category
**Endpoint**: `POST /api/categories`

**Authentication**: Required (JWT Bearer Token)

**Description**: Creates a new product category.

**Request Body** (`CreateCategoryRequest`):
```json
{
  "name": "Electronics"
}
```

**Response** (201 Created) - `CategoryResponse`:
```json
{
  "id": 1,
  "code": "CAT-00001",
  "name": "Electronics",
  "createdAt": "2025-12-19T10:00:00",
  "updatedAt": "2025-12-19T10:00:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `409 Conflict`: Category name already exists

---

### 2. Get Category by ID
**Endpoint**: `GET /api/categories/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific category by ID.

**Path Parameters**:
- `id` (Long) - Category unique identifier

**Response** (200 OK) - `CategoryResponse`:
```json
{
  "id": 1,
  "code": "CAT-00001",
  "name": "Electronics",
  "createdAt": "2025-12-19T10:00:00",
  "updatedAt": "2025-12-19T10:00:00"
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Category not found

---

### 3. List All Categories
**Endpoint**: `GET /api/categories`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all categories in the system.

**Response** (200 OK) - `List<CategoryResponse>`:
```json
[
  {
    "id": 1,
    "code": "CAT-00001",
    "name": "Electronics",
    "createdAt": "2025-12-19T10:00:00",
    "updatedAt": "2025-12-19T10:00:00"
  },
  {
    "id": 2,
    "code": "CAT-00002",
    "name": "Gifts",
    "createdAt": "2025-12-19T10:05:00",
    "updatedAt": "2025-12-19T10:05:00"
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 4. Update Category
**Endpoint**: `PUT /api/categories/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates an existing category.

**Path Parameters**:
- `id` (Long) - Category unique identifier

**Request Body** (`UpdateCategoryRequest`):
```json
{
  "name": "Electronics & Gadgets"
}
```

**Response** (200 OK) - `CategoryResponse`:
```json
{
  "id": 1,
  "code": "CAT-00001",
  "name": "Electronics & Gadgets",
  "createdAt": "2025-12-19T10:00:00",
  "updatedAt": "2025-12-19T11:30:00"
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Category not found
- `409 Conflict`: Category name already exists

---

### 5. Delete Category
**Endpoint**: `DELETE /api/categories/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Deletes a category from the system.

**Path Parameters**:
- `id` (Long) - Category unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Category not found
- `409 Conflict`: Cannot delete category with associated products

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
category/application/dto/
├── request/                    # Input DTOs for API requests
│   ├── CreateCategoryRequest.java
│   └── UpdateCategoryRequest.java
└── response/                   # Output DTOs for API responses
    └── CategoryResponse.java
```

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateCategoryRequest
**Usage**: POST /api/categories (Create new category)

```java
public record CreateCategoryRequest(
    @NotBlank String name                       // Required, 2-100 chars, unique
)
```

**JSON Example**:
```json
{
  "name": "Electronics"
}
```

---

#### UpdateCategoryRequest
**Usage**: PUT /api/categories/{id} (Update existing category)

```java
public record UpdateCategoryRequest(
    @NotBlank String name                       // Required, 2-100 chars, unique
)
```

**JSON Example**:
```json
{
  "name": "Electronics & Gadgets"
}
```

---

### Response DTOs

Located in: `application/dto/response/`

#### CategoryResponse
**Usage**: All category endpoints (Create, Get, List, Update)

```java
public record CategoryResponse(
    Long id,                                    // Category ID
    String code,                                // Category code (CAT-XXXXX)
    String name,                                // Category name
    LocalDateTime createdAt,                    // Creation timestamp
    LocalDateTime updatedAt                     // Last update timestamp
)
```

**JSON Example**:
```json
{
  "id": 1,
  "code": "CAT-00001",
  "name": "Electronics",
  "createdAt": "2025-12-19T10:00:00",
  "updatedAt": "2025-12-19T10:00:00"
}
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/categories | None | List\<CategoryResponse\> |
| GET /api/categories/{id} | None | CategoryResponse |
| POST /api/categories | CreateCategoryRequest | CategoryResponse |
| PUT /api/categories/{id} | UpdateCategoryRequest | CategoryResponse |
| DELETE /api/categories/{id} | None | void (204 No Content) |

---

## Use Cases

### Application Layer Use Cases

1. **CreateCategoryUseCase** - Creates a new category with auto-generated code
2. **GetCategoryByIdUseCase** - Retrieves category by ID
3. **GetAllCategoriesUseCase** - Lists all categories
4. **UpdateCategoryUseCase** - Updates category name
5. **DeleteCategoryUseCase** - Deletes a category (if no associated products)

---

## Repository Interfaces

### Domain Layer Port

```java
public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    Optional<Category> findByCode(CategoryCode code);
    Optional<Category> findByName(CategoryName name);
    List<Category> findAll();
    void deleteById(CategoryId id);
    boolean existsById(CategoryId id);
    boolean existsByName(CategoryName name);
    boolean existsByNameAndIdNot(CategoryName name, CategoryId id);
    long count();
}
```

---

## Domain Services

### CategoryCodeGenerator
**Responsibilities**:
- Generate sequential category codes (CAT-00001, CAT-00002...)
- Ensure code uniqueness

**Interface**: `CategoryCodeGenerator`
```java
public interface CategoryCodeGenerator {
    CategoryCode generateNext();
}
```

**Implementation**: `SequentialCategoryCodeGenerator`
- Queries database for category count
- Generates next sequential code
- Format: CAT-XXXXX (5-digit zero-padded)
- Starts from CAT-00001 if no categories exist

---

## Business Rules Summary

### Category Lifecycle
1. **Independent Entity**: Category has its own CRUD lifecycle
2. **Referenced by Products**: Products contain `categoryId` foreign key
3. **Deletion Constraint**: Cannot delete if products reference this category

### Code Generation
1. Code format: `CAT-XXXXX` (e.g., CAT-00001, CAT-00002)
2. Auto-generated on creation
3. Immutable after creation
4. Unique across all categories

### Name Validation
1. Required field (cannot be null or blank)
2. Length: 2-100 characters
3. Must be unique (case-insensitive validation recommended)
4. Can be updated after creation

---

## Database Schema

### categories Table
```sql
CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_category_code (code),
    INDEX idx_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Notes**:
- Primary key is auto-incremented Long
- Both `code` and `name` have unique constraints
- Indexes on `code` and `name` for fast lookups
- Timestamps auto-managed by database

---

## Exception Handling

### Custom Exceptions

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `CategoryNotFoundException` | 404 Not Found | Category not found by ID |
| `DuplicateCategoryNameException` | 409 Conflict | Category name already exists |
| `CategoryHasProductsException` | 409 Conflict | Cannot delete category with associated products |

---

## Relationship with Products Module

### How Categories are Used

1. **Product Creation**: Products require a valid `categoryId`
2. **Product Response**: Product responses include `categoryId`, `categoryCode`, and `categoryName`
3. **Validation**: When creating/updating a product, the category must exist
4. **Deletion Protection**: Categories with products cannot be deleted

### Cross-Module Communication

The Products module depends on the Category module:

```
┌─────────────────┐          ┌─────────────────┐
│   PRODUCTS      │          │   CATEGORY      │
│   Module        │ ──────── │   Module        │
│                 │  uses    │                 │
│  Product has    │          │  Category       │
│  categoryId FK  │          │  (id, code,     │
│                 │          │   name)         │
└─────────────────┘          └─────────────────┘
```

**Import in Products Module**:
```java
import com.lexand.lexand_api.category.domain.entities.Category;
import com.lexand.lexand_api.category.domain.valueobjects.CategoryId;
import com.lexand.lexand_api.category.domain.repositories.CategoryRepository;
import com.lexand.lexand_api.category.application.exceptions.CategoryNotFoundException;
```

---

## Notes for Developers

### When Working with Categories

1. **Standalone Module**: Category is its own bounded context
2. **Value Objects**: Use `CategoryId`, `CategoryCode`, `CategoryName` value objects
3. **DTOs**: Use Java records with validation annotations
4. **Code Immutability**: Category code cannot be changed after creation
5. **Name Uniqueness**: Always validate name uniqueness before save
6. **Deletion Check**: Always check for associated products before deletion

### API Design Rules
- All category endpoints return `CategoryResponse`
- Create returns 201 with created resource
- Update returns 200 with updated resource
- Delete returns 204 with no content
- Not found returns 404
- Conflicts (duplicate name, has products) return 409

### Testing Requirements
- Unit tests for `CategoryId`, `CategoryCode`, `CategoryName` value objects
- Unit tests for `Category` entity
- Unit tests for all use cases with mocked repository
- Unit tests for `CategoryMapper`
- Integration tests for `CategoryController`
- Test code generation (CAT-00001, CAT-00002...)
- Test name uniqueness validation
- Test deletion constraint (category with products)
- Test concurrent code generation (if applicable)

---

## Project Structure

```
src/main/java/com/lexand/lexand_api/category/
├── application/
│   ├── dto/
│   │   ├── request/
│   │   │   ├── CreateCategoryRequest.java
│   │   │   └── UpdateCategoryRequest.java
│   │   └── response/
│   │       └── CategoryResponse.java
│   ├── exceptions/
│   │   ├── CategoryNotFoundException.java
│   │   ├── DuplicateCategoryNameException.java
│   │   └── CategoryHasProductsException.java
│   ├── mappers/
│   │   └── CategoryMapper.java
│   └── usecases/
│       ├── CreateCategoryUseCase.java
│       ├── GetCategoryByIdUseCase.java
│       ├── GetAllCategoriesUseCase.java
│       ├── UpdateCategoryUseCase.java
│       └── DeleteCategoryUseCase.java
├── domain/
│   ├── entities/
│   │   └── Category.java
│   ├── repositories/
│   │   └── CategoryRepository.java
│   ├── services/
│   │   └── CategoryCodeGenerator.java
│   └── valueobjects/
│       ├── CategoryId.java
│       ├── CategoryCode.java
│       └── CategoryName.java
└── infrastructure/
    ├── persistence/
    │   ├── entities/
    │   │   └── CategoryEntity.java
    │   ├── mappers/
    │   │   └── CategoryEntityMapper.java
    │   └── repositories/
    │       ├── JpaCategoryRepositoryAdapter.java
    │       └── SpringDataCategoryRepository.java
    ├── services/
    │   └── SequentialCategoryCodeGenerator.java
    └── web/
        ├── CategoryController.java
        └── CategoryExceptionHandler.java
```

---

**Last Updated**: 2025-12-19

**Module Status**: ✅ Implemented
