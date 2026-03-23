# AUDIT Module

## Overview
The AUDIT module provides a centralized, append-only audit trail for the entire Lexand API. It records every significant action performed in the system — including entity creation, updates, deletions, authentication events, and status changes — along with the acting user, the affected entity, the IP address, and a human-readable description.

**Important**: Audit logs are **created programmatically only**. No external caller can `POST` an audit log through the REST API. All other modules interact with this module exclusively through the `AuditService` facade. The only public REST endpoint exposes a paginated, filterable read-only list for administrators.

---

## Append-Only Pattern

### Why Append-Only?
- **Immutability**: Audit records represent historical facts and must never change
- **Legal Compliance**: Tamper-proof trail of every action in the system
- **Forensic Integrity**: Deleted or modified records would undermine the purpose of auditing

### Consequences
1. **No `UPDATE` operations** — the repository exposes no update method
2. **No `DELETE` operations** — the repository exposes no delete method
3. **No `PUT` / `PATCH` / `DELETE` REST endpoints** — only `GET /api/audit`
4. **No `updatedAt` column** — records have only `created_at`
5. **All writes go through `AuditService`** — never call the use case or repository directly from outside this module

---

## Domain Model

### Entities

#### AuditLog
**Domain Entity**: `com.lexand.lexand_api.audit.domain.entities.AuditLog`

**Description**: Represents a single immutable audit event. Records who performed an action, on which entity, from which IP address, and when.

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | `AuditLogId` (UUID) | Unique identifier | Required, auto-generated, immutable |
| `userId` | `String` | ID of the acting user | Optional (nullable for system events) |
| `userEmail` | `String` | Email of the acting user | Optional (nullable for system events) |
| `entityType` | `AuditEntityType` | Type of the affected entity (enum) | Required |
| `entityId` | `String` | ID of the affected entity | Required, non-blank |
| `action` | `AuditAction` | The action performed (enum) | Required |
| `details` | `String` | Human-readable description | Optional, max 1000 characters |
| `ipAddress` | `String` | Client IP address | Optional, max 45 characters (IPv6 support) |
| `createdAt` | `LocalDateTime` | Timestamp of the event | Auto-generated, immutable |

**Factory Methods**:
- `create(userId, userEmail, entityType, entityId, action, details, ipAddress)` — Creates a new audit log entry. Generates a new `AuditLogId` and sets `createdAt` to `LocalDateTime.now()`
- `reconstitute(id, userId, userEmail, entityType, entityId, action, details, ipAddress, createdAt)` — Rebuilds an existing audit log from persistent storage

**Validation Rules in `create()`**:
1. `entityType` cannot be `null`
2. `entityId` cannot be `null` or blank
3. `action` cannot be `null`

**No setters — all fields are `final`**. The entity is fully immutable after construction.

---

### Value Objects

| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| `AuditLogId` | String (UUID) | Audit log unique identifier | Non-null, non-blank UUID string |

#### AuditLogId
```java
public record AuditLogId(String value) {
    // Validates non-null, non-blank on construction
    public static AuditLogId generate()          // Creates new random UUID
    public static AuditLogId of(String value)    // Wraps existing UUID string
}
```

---

### Enums

#### AuditAction
Represents the type of action performed on an entity.

```java
public enum AuditAction {
    CREATE,         // A new entity was created
    UPDATE,         // An existing entity was modified
    DELETE,         // An entity was deleted
    READ,           // An entity was accessed/viewed
    LOGIN,          // A user logged in
    LOGOUT,         // A user logged out
    PAYMENT,        // A payment was processed
    STATUS_CHANGE   // An entity's status was changed
}
```

#### AuditEntityType
Represents the type of entity that was affected by the action.

```java
public enum AuditEntityType {
    INVOICE,        // Invoice entity
    ORDER,          // Order entity
    CUSTOMER,       // Customer entity
    PRODUCT,        // Product entity
    MERCHANT,       // Merchant entity
    PAYABLE,        // Payable entity
    COLLECTION,     // Collection entity
    RECEIVABLE,     // Receivable entity
    DISPATCHER,     // Dispatcher entity
    AUTH,           // Authentication events
    WAREHOUSE       // Warehouse entity
}
```

---

## API Endpoints

**IMPORTANT**: There is only ONE REST endpoint in this module.

- ✅ `GET /api/audit` — Paginated, filterable list (admin only)
- ❌ `POST /api/audit` — **Does not exist.** Logs are created programmatically via `AuditService`
- ❌ `PUT /api/audit/{id}` — **Does not exist.** Records are immutable
- ❌ `DELETE /api/audit/{id}` — **Does not exist.** Records are append-only

---

### 1. List Audit Logs

**Endpoint**: `GET /api/audit`

**Authentication**: Required (JWT Bearer Token)

**Authorization**: `ROLE_USER` — restricted to admin/privileged users

**Description**: Returns a paginated list of audit log entries, with optional filters by user, entity type, action, and date range. All filters are optional and can be combined freely.

**Query Parameters**:
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `userId` | `String` | No | Filter by the ID of the acting user | `3fa85f64-5717-4562-b3fc-2c963f66afa6` |
| `entityType` | `String` | No | Filter by entity type (case-insensitive) | `CUSTOMER`, `ORDER`, `PRODUCT` |
| `action` | `String` | No | Filter by action type (case-insensitive) | `CREATE`, `UPDATE`, `DELETE` |
| `fromDate` | `LocalDate` (ISO) | No | Filter records from this date (inclusive) | `2025-01-01` |
| `toDate` | `LocalDate` (ISO) | No | Filter records up to this date (inclusive) | `2025-12-31` |
| `page` | `Integer` | No | Page number, zero-indexed (default: `0`) | `0` |
| `size` | `Integer` | No | Page size (default: `20`) | `50` |
| `sort` | `String` | No | Sort field and direction (default: `createdAt`) | `createdAt,desc` |

**Example Request** (filter by entity type and date range):
```
GET /api/audit?entityType=CUSTOMER&action=CREATE&fromDate=2025-01-01&toDate=2025-03-31&page=0&size=20&sort=createdAt,desc
```

**Response** (200 OK) — `Page<AuditLogResponse>`:
```json
{
  "content": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "userEmail": "admin@lexand.com",
      "entityType": "CUSTOMER",
      "entityId": "550e8400-e29b-41d4-a716-446655440000",
      "action": "CREATE",
      "details": "Customer 'ABC Company S.A.' (code: 1) created successfully",
      "ipAddress": "192.168.1.100",
      "createdAt": "2025-03-15T10:30:00"
    },
    {
      "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "userEmail": "admin@lexand.com",
      "entityType": "CUSTOMER",
      "entityId": "660e8400-e29b-41d4-a716-446655440111",
      "action": "CREATE",
      "details": "Customer 'XYZ Solutions C.A.' (code: 2) created successfully",
      "ipAddress": "192.168.1.100",
      "createdAt": "2025-03-14T08:15:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 2,
  "empty": false
}
```

**Empty Result** (200 OK — no matching records):
```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 0,
  "totalPages": 0,
  "last": true,
  "first": true,
  "numberOfElements": 0,
  "empty": true
}
```

**Errors**:
- `400 Bad Request`: Invalid `entityType` or `action` value (not a valid enum member)
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Authenticated user does not have `ROLE_USER`

---

## DTOs (Data Transfer Objects)

### DTO Organization Structure

```
application/dto/
├── request/
│   └── CreateAuditLogRequest.java      # Internal use only — not exposed via REST
└── response/
    └── AuditLogResponse.java           # Used by GET /api/audit
```

---

### Request DTOs

Located in: `application/dto/request/`

#### CreateAuditLogRequest
**Usage**: Internal — used by `AuditService` to pass data to `CreateAuditLogUseCase`. **Never sent over HTTP.**

```java
public record CreateAuditLogRequest(
    String userId,       // ID of the acting user (nullable)
    String userEmail,    // Email of the acting user (nullable)
    String entityType,   // AuditEntityType as String — validated in use case
    String entityId,     // ID of the affected entity (required, non-blank)
    String action,       // AuditAction as String — validated in use case
    String details,      // Human-readable description (nullable)
    String ipAddress     // Client IP address (nullable)
)
```

**Note**: String values for `entityType` and `action` are converted to their enum equivalents inside `CreateAuditLogUseCase.execute()`. `AuditService.log()` uses the typed enums directly, converting them to strings before building this DTO.

---

### Response DTOs

Located in: `application/dto/response/`

#### AuditLogResponse
**Usage**: `GET /api/audit` — returned as elements inside a `Page<AuditLogResponse>`

```java
public record AuditLogResponse(
    String id,           // UUID as string
    String userId,       // Acting user ID (may be null)
    String userEmail,    // Acting user email (may be null)
    String entityType,   // AuditEntityType name as string (e.g. "CUSTOMER")
    String entityId,     // Affected entity ID
    String action,       // AuditAction name as string (e.g. "CREATE")
    String details,      // Human-readable description (may be null)
    String ipAddress,    // Client IP address (may be null)
    String createdAt     // ISO-8601 datetime string (from LocalDateTime.toString())
)
```

**JSON Example**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userEmail": "salesrep@lexand.com",
  "entityType": "ORDER",
  "entityId": "770e8400-e29b-41d4-a716-446655440222",
  "action": "UPDATE",
  "details": "Order status changed from PENDING to APPROVED",
  "ipAddress": "10.0.0.55",
  "createdAt": "2025-03-15T14:22:10.543721"
}
```

---

### DTO Mapping Summary

| Endpoint | Request DTO | Response DTO |
|----------|-------------|--------------|
| `GET /api/audit` | None | `Page<AuditLogResponse>` |
| *(internal)* `AuditService.log()` | `CreateAuditLogRequest` | `AuditLogResponse` (discarded) |

---

## Use Cases

### Application Layer Use Cases

#### 1. CreateAuditLogUseCase
**Class**: `com.lexand.lexand_api.audit.application.usecases.CreateAuditLogUseCase`

**Purpose**: Creates and persists a single audit log entry.

**Input**: `CreateAuditLogRequest`

**Output**: `AuditLogResponse` (returned to `AuditService`, typically discarded by callers)

**Execution Flow**:
1. Parses `entityType` string to `AuditEntityType` enum (throws `IllegalArgumentException` on invalid value)
2. Parses `action` string to `AuditAction` enum (throws `IllegalArgumentException` on invalid value)
3. Calls `AuditLog.create(...)` to build the domain entity (validates non-null fields)
4. Persists the entity via `AuditLogRepository.save(...)`
5. Maps the saved domain entity to `AuditLogResponse` and returns it

**Called by**: `AuditService.log(...)` — never called directly from outside the audit module.

---

#### 2. ListAuditLogsUseCase
**Class**: `com.lexand.lexand_api.audit.application.usecases.ListAuditLogsUseCase`

**Purpose**: Retrieves a paginated, filtered page of audit log entries.

**Input**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | `String` | Optional filter by acting user ID |
| `entityType` | `String` | Optional filter by entity type (case-insensitive string) |
| `action` | `String` | Optional filter by action (case-insensitive string) |
| `fromDate` | `LocalDate` | Optional lower bound on `created_at` |
| `toDate` | `LocalDate` | Optional upper bound on `created_at` |
| `pageable` | `Pageable` | Spring pagination and sort parameters |

**Output**: `Page<AuditLogResponse>`

**Execution Flow**:
1. Resolves `entityType` string to `AuditEntityType` enum — returns `null` if blank (means "no filter"), throws `IllegalArgumentException` on invalid value
2. Resolves `action` string to `AuditAction` enum — returns `null` if blank (means "no filter"), throws `IllegalArgumentException` on invalid value
3. Delegates to `AuditLogRepository.findAll(...)` with all resolved filters
4. Maps each `AuditLog` domain entity to `AuditLogResponse`
5. Returns the mapped `Page<AuditLogResponse>`

**Called by**: `AuditController.listAuditLogs(...)`.

---

## Repository Interfaces

### Domain Layer Port
**Interface**: `com.lexand.lexand_api.audit.domain.repositories.AuditLogRepository`

```java
public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Page<AuditLog> findAll(String userId, AuditEntityType entityType, AuditAction action,
                           LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
```

**Method Details**:

| Method | Description |
|--------|-------------|
| `save(AuditLog)` | Persists a new audit log entry. Always inserts — never updates |
| `findAll(...)` | Returns a filtered, paginated page of audit logs. Any `null` parameter is treated as "no filter" |

**Notice**: There is intentionally no `findById`, `update`, or `delete` method. The append-only constraint is enforced at the repository port level — it is architecturally impossible to modify or delete records through this interface.

---

## AuditService Facade

### Overview
**Class**: `com.lexand.lexand_api.audit.application.service.AuditService`

**Package**: `com.lexand.lexand_api.audit.application.service`

**Spring Bean**: `@Component` — injectable across the entire application

`AuditService` is the **single public facade** that all other modules must use to record audit events. It provides a clean, type-safe API that hides the internal DTO construction and use case invocation details.

### Method Signature

```java
public void log(
    String userId,
    String userEmail,
    AuditEntityType entityType,
    String entityId,
    AuditAction action,
    String details,
    String ipAddress
)
```

**Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | `String` | No | ID of the authenticated user performing the action. Pass `null` for system-triggered events |
| `userEmail` | `String` | No | Email of the authenticated user. Pass `null` for system-triggered events |
| `entityType` | `AuditEntityType` | Yes | Type of the entity being acted upon (typed enum) |
| `entityId` | `String` | Yes | ID of the specific entity being acted upon (non-blank) |
| `action` | `AuditAction` | Yes | The action being recorded (typed enum) |
| `details` | `String` | No | Human-readable description of the event. Keep concise and informative |
| `ipAddress` | `String` | No | Client IP address from the HTTP request. Pass `null` if not available |

### How Other Modules Should Use AuditService

#### Step 1 — Inject AuditService
```java
@Component
public class SomeUseCase {

    private final SomeRepository someRepository;
    private final AuditService auditService;

    public SomeUseCase(SomeRepository someRepository, AuditService auditService) {
        this.someRepository = someRepository;
        this.auditService = auditService;
    }
}
```

#### Step 2 — Call `auditService.log()` after a successful operation

**Example: auditing a customer creation**
```java
public CustomerResponse execute(CreateCustomerRequest request, String userId,
                                String userEmail, String ipAddress) {
    // ... create and save the customer ...
    Customer saved = customerRepository.save(customer);

    auditService.log(
        userId,
        userEmail,
        AuditEntityType.CUSTOMER,
        saved.getId().value(),
        AuditAction.CREATE,
        "Customer '" + saved.getLegalName().value() + "' (code: " + saved.getCode().value() + ") created successfully",
        ipAddress
    );

    return toResponse(saved);
}
```

**Example: auditing an authentication login**
```java
auditService.log(
    user.getId(),
    user.getEmail(),
    AuditEntityType.AUTH,
    user.getId(),
    AuditAction.LOGIN,
    "User logged in successfully",
    ipAddress
);
```

**Example: auditing a status change**
```java
auditService.log(
    userId,
    userEmail,
    AuditEntityType.ORDER,
    order.getId().value(),
    AuditAction.STATUS_CHANGE,
    "Order status changed from " + previousStatus + " to " + newStatus,
    ipAddress
);
```

**Example: system-triggered event (no user context)**
```java
auditService.log(
    null,
    null,
    AuditEntityType.INVOICE,
    invoice.getId().value(),
    AuditAction.CREATE,
    "Invoice auto-generated from order " + order.getId().value(),
    null
);
```

#### Best Practices
- Always call `auditService.log()` **after** a successful operation — never before
- Do **not** catch exceptions thrown by `AuditService`; let them propagate so the transaction rolls back if persistence fails
- Pass the actual IP address from the HTTP request when available (extract from `HttpServletRequest.getRemoteAddr()` in the controller)
- Write `details` messages in English, in the past tense, as facts (e.g., `"Customer 'ABC' deleted"`)
- Use the most specific `AuditAction` available (`STATUS_CHANGE` instead of `UPDATE` when only the status changed)

---

## Business Rules

### Append-Only Constraint
1. Audit logs are **never updated** after creation
2. Audit logs are **never deleted** under any circumstances
3. The `created_at` column is set once at insert time and is non-updatable
4. The repository port intentionally exposes no `update` or `delete` methods

### Record Creation
1. `entityType` and `action` are required — creation fails with `IllegalArgumentException` if either is `null`
2. `entityId` is required and must be non-blank — creation fails with `IllegalArgumentException` if blank
3. `userId`, `userEmail`, `details`, and `ipAddress` are all optional (nullable)
4. The audit log `id` is always generated internally via `AuditLogId.generate()` — never provided by callers
5. `createdAt` is always `LocalDateTime.now()` at creation time — never provided by callers

### Access Control
1. `GET /api/audit` requires a valid JWT Bearer Token
2. Only users with `ROLE_USER` may access the audit log list
3. There is no public endpoint to create, modify, or delete audit logs

### Filtering Rules
1. All query filters in `GET /api/audit` are optional and combinable
2. Omitting a filter means "no restriction" on that field
3. An invalid `entityType` or `action` string returns `400 Bad Request`
4. `fromDate` and `toDate` are inclusive bounds on the `created_at` date

### Pagination Defaults
1. Default page size: `20` records per page
2. Default sort: `createdAt` ascending
3. Pages are zero-indexed (`page=0` is the first page)

---

## Database Schema

### Flyway Migration
**File**: `src/main/resources/db/migration/V15__create_audit_logs_table.sql`

### audit_logs Table

```sql
CREATE TABLE audit_logs (
    id          CHAR(36)      NOT NULL,
    user_id     VARCHAR(36)   NULL,
    user_email  VARCHAR(255)  NULL,
    entity_type VARCHAR(50)   NOT NULL,
    entity_id   VARCHAR(36)   NOT NULL,
    action      VARCHAR(50)   NOT NULL,
    details     VARCHAR(1000) NULL,
    ip_address  VARCHAR(45)   NULL,
    created_at  DATETIME      NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_user_id   ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity    ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
```

### Column Reference

| Column | Type | Nullable | Description |
|--------|------|----------|-------------|
| `id` | `CHAR(36)` | NOT NULL | UUID primary key, auto-generated |
| `user_id` | `VARCHAR(36)` | NULL | ID of the acting user (null for system events) |
| `user_email` | `VARCHAR(255)` | NULL | Email of the acting user (null for system events) |
| `entity_type` | `VARCHAR(50)` | NOT NULL | `AuditEntityType` enum name |
| `entity_id` | `VARCHAR(36)` | NOT NULL | ID of the affected entity |
| `action` | `VARCHAR(50)` | NOT NULL | `AuditAction` enum name |
| `details` | `VARCHAR(1000)` | NULL | Human-readable event description |
| `ip_address` | `VARCHAR(45)` | NULL | Client IP address (IPv4 or IPv6) |
| `created_at` | `DATETIME` | NOT NULL | Timestamp of the event (set once, non-updatable) |

### Index Strategy

| Index Name | Columns | Purpose |
|------------|---------|---------|
| `idx_audit_logs_user_id` | `user_id` | Filter/search by the acting user |
| `idx_audit_logs_entity` | `(entity_type, entity_id)` | Filter the full history of a specific entity |
| `idx_audit_logs_created_at` | `created_at` | Filter by date ranges and sort chronologically |

**Design Notes**:
- `ip_address` is `VARCHAR(45)` to support both IPv4 (`15` chars) and full IPv6 addresses (`39` chars) with scope IDs
- There is no `updated_at` column — the append-only constraint makes it meaningless
- `ENGINE=InnoDB` and `DEFAULT CHARSET=utf8mb4` ensure full Unicode support for the `details` field
- No foreign keys to `users` table — audit records must survive user deletion

---

## Exception Handling

### Exceptions Thrown

| Exception | HTTP Status | Trigger |
|-----------|-------------|---------|
| `IllegalArgumentException` | `400 Bad Request` | Invalid `entityType` or `action` string value in query params |
| `org.springframework.security.access.AccessDeniedException` | `403 Forbidden` | Authenticated user lacks `ROLE_USER` |
| `org.springframework.security.core.AuthenticationException` | `401 Unauthorized` | Missing or invalid JWT token |

**Note**: `IllegalArgumentException` thrown by `ListAuditLogsUseCase` (on invalid enum value) should be caught and translated to a `400 Bad Request` response by the global exception handler (`GlobalExceptionHandler`).

---

## Notes for Developers

### How to Integrate AuditService in a New Module

1. **Inject `AuditService`** as a constructor dependency in any use case or service that performs auditable operations
2. **Call `auditService.log()`** immediately after a successful persistence operation
3. **Use the typed enums** — `AuditEntityType` and `AuditAction` — never raw strings
4. **Add `AuditEntityType` values** for any new entity types by editing the `AuditEntityType` enum in the audit domain module
5. **Pass the IP address** from the controller layer through the use case call chain when it is available and relevant

### Where to Call AuditService (Guidelines)

| Scenario | Where to Call | Notes |
|----------|---------------|-------|
| Entity created | After `repository.save()` in use case | Use `AuditAction.CREATE` |
| Entity updated | After `repository.save()` in use case | Use `AuditAction.UPDATE` or `AuditAction.STATUS_CHANGE` |
| Entity deleted | After `repository.delete()` in use case | Use `AuditAction.DELETE` |
| User login | In the authentication use case, after token issued | Use `AuditEntityType.AUTH` + `AuditAction.LOGIN` |
| User logout | In the logout use case, after token invalidated | Use `AuditEntityType.AUTH` + `AuditAction.LOGOUT` |
| Payment processed | After payment persistence | Use `AuditAction.PAYMENT` |

### What NOT to Do

- ❌ **Do NOT** call `CreateAuditLogUseCase` directly from outside the `audit` module — always use `AuditService`
- ❌ **Do NOT** call `AuditLogRepository` directly from outside the `audit` module
- ❌ **Do NOT** create a `POST /api/audit` endpoint — audit log creation is internal only
- ❌ **Do NOT** add `update` or `delete` methods to `AuditLogRepository`
- ❌ **Do NOT** fire the audit log call inside a try/catch that silently swallows errors — audit failures should propagate

### Controller Layer: Extracting IP Address

Controllers should extract the client IP from the HTTP request and pass it down to the use case:

```java
@PostMapping
public ResponseEntity<CustomerCreateResponse> createCustomer(
        @RequestBody @Valid CreateCustomerRequest request,
        HttpServletRequest httpRequest) {

    String ipAddress = httpRequest.getRemoteAddr();
    String userId    = securityContext.getUserId();
    String userEmail = securityContext.getUserEmail();

    CustomerCreateResponse response = registerCustomerUseCase.execute(
        request, userId, userEmail, ipAddress
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Adding a New AuditEntityType

When a new module introduces a new entity that should be audited:

1. Open `com.lexand.lexand_api.audit.domain.valueobjects.AuditEntityType`
2. Add the new enum constant (e.g., `PAYMENT_METHOD`)
3. Use it immediately in the new module's use cases without any further configuration

### Testing Requirements
- Unit tests for `AuditLog.create()` — verify validation of null `entityType`, null `action`, blank `entityId`
- Unit tests for `CreateAuditLogUseCase` — verify mapping, enum parsing, and delegation to repository
- Unit tests for `ListAuditLogsUseCase` — verify null/blank filter resolution and pagination delegation
- Unit tests for `AuditService.log()` — verify correct `CreateAuditLogRequest` construction
- Integration tests for `GET /api/audit` — verify pagination, all filter combinations, and 403 for unauthorized roles
- Integration tests for `GET /api/audit` with invalid enum values — verify `400 Bad Request`
- Verify that no `POST`, `PUT`, or `DELETE` endpoint exists (negative tests)
- Verify `AuditLogId.generate()` produces unique values across concurrent calls
