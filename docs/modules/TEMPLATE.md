# [MODULE_NAME] Module

## Overview
Brief description of what this module does and its purpose in the system. Explain the key functionality and what business problem it solves.

**Important**: Explain if this module uses an **Aggregate Root** pattern. Describe which entity is the root and which are internal entities that can ONLY be managed through the aggregate.

---

## Aggregate Pattern

### [MainEntity] Aggregate Root
- **Root Entity**: [MainEntity]
- **Internal Entities**: [NestedEntity1], [NestedEntity2] (if applicable)
- **Access Rule**: Describe how internal entities are accessed (only through root or separately)
- **API Design**: Describe the endpoint structure

### Why This Matters
1. **Consistency**: Explain consistency benefits
2. **Business Rules**: Explain business rule enforcement
3. **Transactional Integrity**: Explain transaction boundaries
4. **Simpler API**: Explain API design benefits

---

## Domain Model

### Entities

#### [MainEntity] (Aggregate Root)
**Domain Entity**: `com.lexand.lexand_api.[module].[submodule].domain.entities.[MainEntity]`

**Description**: Describe what this entity represents and its role in the system.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `[EntityId]` (UUID/Long) | Unique identifier | Required, immutable |
| `code` | `[EntityCode]` (String/Long) | Business code | Required, immutable, auto-generated |
| `field1` | `ValueObject1` | Description | Required, validation rules |
| `field2` | `ValueObject2` | Description | Optional, validation rules |
| `active` | `boolean` | Active status | Default: true |
| `createdAt` | `LocalDateTime` | Creation timestamp | Auto-generated |
| `updatedAt` | `LocalDateTime` | Last update timestamp | Auto-updated |
| `nestedEntities` | `List<NestedEntity>` | Collection of nested entities | Collection |

**Business Methods**:
- `create(...)` - Factory method to create new entity
- `reconstitute(...)` - Factory method to rebuild entity from database
- `updateField1(ValueObject1)` - Updates field1
- `updateField2(ValueObject2)` - Updates field2
- `addNestedEntity(...)` - Adds new nested entity
- `clearNestedEntities()` - Removes all nested entities
- `activate()` - Activates entity
- `deactivate()` - Deactivates entity

**Business Rules**:
1. Business rule 1
2. Business rule 2
3. Business rule 3
4. All updates modify the `updatedAt` timestamp
5. Entity creation triggers `[Entity]CreatedEvent` (if applicable)

---

#### [NestedEntity] (Internal Entity)
**Domain Entity**: `com.lexand.lexand_api.[module].[submodule].domain.entities.[NestedEntity]`

**Description**: Describe what this entity represents. **ALWAYS managed through [MainEntity] aggregate, never accessed directly.**

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `[NestedEntityId]` (UUID/Long) | Unique identifier | Required, immutable |
| `mainEntityId` | `[MainEntityId]` (UUID/Long) | Owner entity ID | Required, immutable |
| `field1` | `ValueObject` | Description | Required, validation rules |

**Business Rules**:
1. Business rule for nested entity
2. **NO direct API endpoints** - managed only through [MainEntity] CRUD operations
3. Cannot exist independently outside [MainEntity] aggregate

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `[MainEntityId]` | UUID/Long | Unique identifier | Non-null |
| `[EntityCode]` | String/Long | Business code | Pattern/format, auto-generated |
| `[Field1ValueObject]` | String | Description | Min-max characters, non-blank |
| `[Field2ValueObject]` | BigDecimal | Description | Non-negative, scale: 2 |
| `[NestedEntityId]` | UUID/Long | Nested entity identifier | Non-null |

---

### Enums

#### [EnumName]
```java
public enum [EnumName] {
    VALUE_1,     // Description of value 1
    VALUE_2,     // Description of value 2
    VALUE_3      // Description of value 3
}
```

---

## API Endpoints

**IMPORTANT**: If using aggregate pattern, document which endpoints exist and which DON'T:
- ✅ `/api/[main-entities]` - Main entity endpoints exist
- ❌ `/api/[nested-entities]` - No separate endpoints for nested entities

**All nested entity management is done through [MainEntity] create/update operations.**

---

### 1. Create [MainEntity]
**Endpoint**: `POST /api/[main-entities]`

**Authentication**: Required (JWT Bearer Token)

**Description**: Creates a new [main entity] in the system. Uses `Create[MainEntity]Request`.

**Request Body** (`Create[MainEntity]Request`):
```json
{
  "field1": "value",
  "field2": 100.00,
  "active": true,
  "nestedEntities": [
    { "nestedField1": "value1" },
    { "nestedField2": "value2" }
  ]
}
```

**Response** (201 Created) - `[MainEntity]CreateResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "CODE-001",
  "field1": "value",
  "field2": 100.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "nestedEntities": [
    {
      "id": "nested-001",
      "mainEntityId": "550e8400-e29b-41d4-a716-446655440000",
      "nestedField1": "value1"
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `409 Conflict`: Duplicate code/unique field

---

### 2. Get [MainEntity] by ID
**Endpoint**: `GET /api/[main-entities]/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves a specific [main entity] by ID. Returns `[MainEntity]DetailResponse`.

**Path Parameters**:
- `id` (UUID/Long) - Entity unique identifier

**Response** (200 OK) - `[MainEntity]DetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "CODE-001",
  "field1": "value",
  "field2": 100.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "nestedEntities": [
    {
      "id": "nested-001",
      "mainEntityId": "550e8400-e29b-41d4-a716-446655440000",
      "nestedField1": "value1"
    }
  ]
}
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Entity not found

---

### 3. List All [MainEntities]
**Endpoint**: `GET /api/[main-entities]`

**Authentication**: Required (JWT Bearer Token)

**Description**: Retrieves all [main entities] in the system (minimal data for list view). Returns `List<[MainEntity]ListResponse>`.

**Response** (200 OK) - `List<[MainEntity]ListResponse>`:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": "CODE-001",
    "field1": "value",
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440111",
    "code": "CODE-002",
    "field1": "another value",
    "active": true
  }
]
```

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

**Note**: Returns [MainEntity]ListResponse (minimal data, no nested entities).

---

### 4. Update [MainEntity]
**Endpoint**: `PUT /api/[main-entities]/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Updates an existing [main entity]. Uses `Update[MainEntity]Request`.

**Path Parameters**:
- `id` (UUID/Long) - Entity unique identifier

**Request Body** (`Update[MainEntity]Request`):
```json
{
  "field1": "updated value",
  "field2": 150.00,
  "active": true,
  "nestedEntities": [
    { "nestedField1": "new-value" }
  ]
}
```

**Response** (200 OK) - `[MainEntity]DetailResponse`:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "code": "CODE-001",
  "field1": "updated value",
  "field2": 150.00,
  "active": true,
  "createdAt": "2025-12-15T10:30:00",
  "nestedEntities": [
    {
      "id": "nested-new-001",
      "mainEntityId": "550e8400-e29b-41d4-a716-446655440000",
      "nestedField1": "new-value"
    }
  ]
}
```

**Errors**:
- `400 Bad Request`: Invalid request data or validation error
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Entity not found

**Note**: Update replaces all nested entities. Send complete collections.

---

### 5. Delete [MainEntity]
**Endpoint**: `DELETE /api/[main-entities]/{id}`

**Authentication**: Required (JWT Bearer Token)

**Description**: Deletes a [main entity] from the system.

**Path Parameters**:
- `id` (UUID/Long) - Entity unique identifier

**Response** (204 No Content): Empty body

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Entity not found
- `409 Conflict`: Cannot delete (has dependencies)

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

DTOs are organized in two main categories within `application/dto/`:

```
application/dto/
├── request/                    # Input DTOs for API requests
│   ├── Create[MainEntity]Request.java
│   ├── Update[MainEntity]Request.java
│   └── [NestedEntity]Request.java
└── response/                   # Output DTOs for API responses
    ├── [MainEntity]ListResponse.java
    ├── [MainEntity]DetailResponse.java
    ├── [MainEntity]CreateResponse.java
    └── [NestedEntity]Response.java
```

---

### Request DTOs

Located in: `application/dto/request/`

#### Create[MainEntity]Request
**Usage**: POST /api/[main-entities] (Create new entity)

```java
public record Create[MainEntity]Request(
    @NotBlank String field1,                    // Required, validation rules
    @PositiveOrZero BigDecimal field2,          // Required, >= 0
    @NotNull Boolean active,                    // Required, default: true
    @Valid @NotEmpty List<[NestedEntity]Request> nestedEntities  // At least 1 required
)
```

**JSON Example**:
```json
{
  "field1": "value",
  "field2": 100.00,
  "active": true,
  "nestedEntities": [
    { "nestedField1": "value" }
  ]
}
```

---

#### Update[MainEntity]Request
**Usage**: PUT /api/[main-entities]/{id} (Update existing entity)

```java
public record Update[MainEntity]Request(
    @NotBlank String field1,                    // Required, validation rules
    @PositiveOrZero BigDecimal field2,          // Required, >= 0
    @NotNull Boolean active,                    // Required
    @Valid @NotEmpty List<[NestedEntity]Request> nestedEntities  // Replaces all
)
```

**Note**: Update replaces all nested collections. Send complete arrays.

---

#### [NestedEntity]Request
**Usage**: Nested in Create/Update requests

```java
public record [NestedEntity]Request(
    @NotBlank String nestedField1               // Required, validation rules
)
```

---

### Response DTOs

Located in: `application/dto/response/`

#### 1. [MainEntity]ListResponse
**Usage**: GET /api/[main-entities] (List view)

**Purpose**: Minimal data for table display in list views.

```java
public record [MainEntity]ListResponse(
    String id,                                  // ID as string
    String code,                                // Business code
    String field1,                              // Main field
    boolean active                              // Active status
)
```

**JSON Example**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "code": "CODE-001",
  "field1": "value",
  "active": true
}
```

---

#### 2. [MainEntity]DetailResponse
**Usage**: GET /api/[main-entities]/{id} (Detail view)

**Purpose**: Complete entity data for detail/view pages.

```java
public record [MainEntity]DetailResponse(
    String id,                                  // ID as string
    String code,                                // Business code
    String field1,                              // Main field
    BigDecimal field2,                          // Another field
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp
    List<[NestedEntity]Response> nestedEntities // Nested entities with IDs
)
```

---

#### 3. [MainEntity]CreateResponse
**Usage**: POST /api/[main-entities] response (After creation)

**Purpose**: Complete entity data returned after successful creation.

```java
public record [MainEntity]CreateResponse(
    String id,                                  // ID (generated)
    String code,                                // Business code (generated)
    String field1,                              // Main field
    BigDecimal field2,                          // Another field
    boolean active,                             // Active status
    LocalDateTime createdAt,                    // Creation timestamp (generated)
    List<[NestedEntity]Response> nestedEntities // Created nested entities with IDs
)
```

---

#### [NestedEntity]Response
**Usage**: Nested in responses

```java
public record [NestedEntity]Response(
    String id,                                  // ID as string
    String mainEntityId,                        // Owner entity ID
    String nestedField1                         // Nested field
)
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| GET /api/[main-entities] | None | List\<[MainEntity]ListResponse\> |
| GET /api/[main-entities]/{id} | None | [MainEntity]DetailResponse |
| POST /api/[main-entities] | Create[MainEntity]Request | [MainEntity]CreateResponse |
| PUT /api/[main-entities]/{id} | Update[MainEntity]Request | [MainEntity]DetailResponse |
| DELETE /api/[main-entities]/{id} | None | void (204 No Content) |

---

## Managing Nested Entities

### Creation Pattern
When creating an entity, nested entities are sent WITHOUT IDs:

```json
{
  "field1": "New Entity",
  "nestedEntities": [
    { "nestedField1": "value" }  // No ID
  ]
}
```

Backend generates IDs and returns them in the response.

---

### Update Pattern (Replace All)
When updating an entity, the backend **replaces ALL nested entities**:

```json
{
  "field1": "Updated Entity",
  "nestedEntities": [
    { "nestedField1": "new-value" }  // Old entities deleted, new created
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

## Use Cases

### Application Layer Use Cases
1. **Create[MainEntity]UseCase** - Creates a new entity
2. **Get[MainEntity]UseCase** - Retrieves entity by ID
3. **List[MainEntities]UseCase** - Lists all entities
4. **Update[MainEntity]UseCase** - Updates entity information
5. **Delete[MainEntity]UseCase** - Deletes an entity

---

## Repository Interfaces

### Domain Layer Port
```java
public interface [MainEntity]Repository {
    [MainEntity] save([MainEntity] entity);
    Optional<[MainEntity]> findById([MainEntityId] id);
    List<[MainEntity]> findAll();
    void delete([MainEntityId] id);
    boolean existsById([MainEntityId] id);
}
```

---

## Domain Services

### [EntityCode]Generator (if applicable)
**Responsibilities**:
- Generate sequential/unique codes
- Ensure code uniqueness

**Implementation**: `Sequential[EntityCode]Generator`
- Queries database for max code
- Increments by 1
- Starts from 1 if no entities exist

---

## Domain Events (if applicable)

### [MainEntity]CreatedEvent
**Triggered**: When a new entity is created

**Payload**:
- `entityId` ([MainEntityId])
- `entityCode` ([EntityCode])
- `field1` (ValueObject)
- `occurredOn` (LocalDateTime)

**Purpose**: Notify other modules about new entity creation

---

## Business Rules Summary

### Aggregate Root Pattern
1. **[MainEntity] is the Aggregate Root** - All operations go through it
2. **No direct access to nested entities** - Cannot CRUD nested entities independently
3. **Transactional boundary** - All changes in one transaction
4. **Consistency guarantee** - Aggregate ensures all business rules are enforced

### Entity Validation
1. Rule 1 - Description
2. Rule 2 - Description
3. Rule 3 - Description

### Nested Entity Rules
1. **Managed ONLY through [MainEntity]** - No separate endpoints
2. Rule specific to nested entities
3. **Update replaces all** - Send complete arrays on update

---

## Database Schema

### [main_entities] Table
```sql
CREATE TABLE [main_entities] (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    field1 VARCHAR(200) NOT NULL,
    field2 DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_code (code),
    INDEX idx_active (active)
);
```

### [nested_entities] Table
```sql
CREATE TABLE [nested_entities] (
    id VARCHAR(36) PRIMARY KEY,
    [main_entity]_id VARCHAR(36) NOT NULL,
    nested_field1 VARCHAR(255) NOT NULL,
    FOREIGN KEY ([main_entity]_id) REFERENCES [main_entities](id) ON DELETE CASCADE
);
```

---

## Exception Handling

### Custom Exceptions
- `[MainEntity]NotFoundException` - Entity not found by ID (404 Not Found)
- `Duplicate[MainEntity]CodeException` - Duplicate code (409 Conflict)

---

## Notes for Developers

### When Creating New Features
1. **Aggregate Root**: [MainEntity] is the aggregate root, always access nested entities through it
2. **Value Objects**: Use value objects for all domain fields
3. **DTOs**: Keep them simple, use Java records with validation annotations
4. **No Nested Endpoints**: NEVER create endpoints like `/api/[main-entities]/{id}/[nested-entities]`
5. **Replace Pattern**: Update operations replace entire collections
6. **Entity Code**: Never modify entity code after creation
7. **Cascading**: Deleting an entity deletes all related nested entities
8. **Transactional**: All entity + nested entity changes in one transaction

### API Design Rules
- ✅ DO: Manage nested entities through [MainEntity] endpoints
- ✅ DO: Use POST/PUT on `/api/[main-entities]` with full nested data
- ✅ DO: Return complete entity with all nested entities
- ❌ DON'T: Create separate endpoints for nested entities
- ❌ DON'T: Allow partial updates of nested collections
- ❌ DON'T: Try to update/delete nested entities independently

### DTO Structure Rules
- **Request DTOs**: No IDs for nested entities
- **Response DTOs**: Include IDs for all nested entities
- **Update Logic**: Backend deletes all old + creates all new nested entities
- **Frontend**: Must send complete arrays on update

### Testing Requirements
- Unit tests for all value objects with validation rules
- Unit tests for domain entities
- Unit tests for use cases with mocked repositories
- Integration tests for controllers with test database
- Test code generation uniqueness (if applicable)
- Test business rule constraints
- Test cascade deletion of related entities
- **Test aggregate pattern**: Verify no direct access to nested entities
- **Test replace logic**: Verify update deletes old + creates new nested entities
- **Test validation**: Required fields, minimum constraints

---

**Last Updated**: YYYY-MM-DD

**Module Status**: ⚠️ Planned | 🚧 In Development | ✅ Implemented
