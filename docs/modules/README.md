Current reporting ownership is defined in [backend architecture](../ARCHITECTURE.md): each owner has a `reporting` submodule; legacy examples below do not override it.

# Module Documentation

## Purpose
This directory contains **module-specific documentation** that serves as the **single source of truth** for each module in the Cashly API system.

Each module documentation file is a comprehensive reference used by:
- **Developers** - To understand how to use and extend the module
- **Domain Agent** - As template for creating domain entities and value objects
- **Application Agent** - As template for creating DTOs and use cases
- **Infrastructure Agent** - As template for implementing REST controllers and repositories
- **Security Agent** - As reference for endpoint authentication requirements

---

## File Naming Convention
- File name: `[MODULE_NAME].md` (uppercase)
- Examples: `AUTH.md`, `CUSTOMERS.md`, `ORDERS.md`, `INVOICES.md`
- Module name matches the package: `com.cashly.cashly_api.[module_name]`

---

## Current package ownership

Use [ARCHITECTURE.md](../ARCHITECTURE.md) for current module/capability ownership and short infrastructure paths. Older module guides include planning examples and legacy Lexand package names; verify those examples against the current source before using them as a file location or implementation template. The complete migration tree is in [trees/after.md](../../openspec/changes/screaming-backend-structure/trees/after.md).

## Required Sections

Each module documentation file MUST include:

### 1. Overview
- Module purpose and responsibilities
- Brief description of what the module does

### 2. Domain Model
- **Entities**: Complete documentation of all domain entities with:
  - Attributes table (field, type, description, validation)
  - Business methods
  - Business rules
- **Value Objects**: All value objects with validation rules
- **Enums**: All enumerations with possible values

### 3. API Endpoints
- All REST endpoints with:
  - HTTP method and URL
  - Authentication requirements
  - Request body with JSON example
  - Response with JSON example
  - Error codes and descriptions

### 4. DTOs (Data Transfer Objects)
- **Request DTOs**: All request objects with validation annotations
- **Response DTOs**: All response objects with field descriptions

### 5. Use Cases
- List of all application layer use cases
- Brief description of each use case

### 6. Repository Interfaces
- Domain repository ports (interfaces)
- Method signatures

### 7. Domain Services
- Domain services with responsibilities
- Implementation details

### 8. Business Rules Summary
- Comprehensive list of business rules
- Organized by functionality

### 9. Database Schema
- SQL CREATE TABLE statements
- Indexes and foreign keys
- Migration file references

### 10. Exception Handling
- Custom exceptions
- HTTP status codes

### 11. Notes for Developers
- Implementation guidelines
- Testing requirements
- Common patterns

---

## Existing Modules

## Complete module map

The implementation also includes these module references:

| Module | Documentation |
|---|---|
| Cash register | [CASH_REGISTER.md](./CASH_REGISTER.md) |
| Credit notes | [CREDIT_NOTES.md](./CREDIT_NOTES.md) |
| Debit notes | [DEBIT_NOTES.md](./DEBIT_NOTES.md) |
| Notifications | [NOTIFICATIONS.md](./NOTIFICATIONS.md) |
| Payment methods | [PAYMENT_METHODS.md](./PAYMENT_METHODS.md) |
| Payments | [PAYMENTS.md](./PAYMENTS.md) |
| Price types | [PRICE_TYPES.md](./PRICE_TYPES.md) |
| Purchases | [PURCHASES.md](./PURCHASES.md) |
| Quotes | [QUOTES.md](./QUOTES.md) |
| Returns | [RETURNS.md](./RETURNS.md) |
| Suppliers | [SUPPLIERS.md](./SUPPLIERS.md) |

Existing references for customers, invoices, orders, products, receivables, reports, and warehouses remain the detailed source of truth below.

### CASH REGISTER Module
**File**: [CASH_REGISTER.md](./CASH_REGISTER.md)

**Purpose**: Session-based ledger for all monetary income, expenses, advances, refunds, withdrawals, and adjustments.

**Features**:
- Explicit opening and closing with opening, expected, declared, and difference balances
- One open session at a time, multiple sessions per day, and sessions spanning dates
- Mandatory open-session guard for monetary operations
- Simple and detailed history plus PDF and Excel reports per session

---

### AUTH Module
**File**: [AUTH.md](./AUTH.md)

**Purpose**: User authentication and authorization using JWT

**Features**:
- User registration and login
- JWT token generation and validation
- Refresh token management
- Logout functionality

---

### CUSTOMERS Module
**File**: [CUSTOMERS.md](./CUSTOMERS.md)

**Purpose**: Customer management for the invoicing system

**Features**:
- Customer CRUD operations
- Contact information (emails, phones, addresses)
- Credit management (credit days, credit limit)
- Customer classification by type

---

### MERCHANTS Module
**File**: [MERCHANTS.md](./MERCHANTS.md)

**Purpose**: Merchant (comercial) account management - separate entity from Users

**Features**:
- Merchant entity with authentication capabilities (MERCHANT role)
- Limited access: create orders and view own orders only
- Single commission percentage per merchant (applied when invoice is paid)
- Merchant profile management

**Key Concepts**:
- **Separate Entity**: Merchant is NOT a User with special role - it's a separate entity
- **Single Commission**: Each merchant has exactly ONE commission percentage
- **Restricted Access**: Merchants can only see and manage their own orders
- **Order Association**: Orders can optionally have a merchant associated (documented in ORDERS module)

**Note**: Order management, validation, and invoice conversion are documented in the ORDERS module (planned).

---

### ORDERS Module
**File**: [ORDERS.md](./ORDERS.md)

**Purpose**: Sales order management with optional merchant association for commission tracking

**Features**:
- Order CRUD operations (create, read, update, cancel)
- Customer required, merchant optional
- Delivery date tracking
- Order lines with product snapshots
- Convert order to invoice functionality
- Role-based access (MERCHANT sees only own orders)

**Key Concepts**:
- **Customer Required**: Every order must have a customer
- **Merchant Optional**: Orders can optionally have a merchant for commission
- **MERCHANT Auto-Assignment**: When MERCHANT creates order, they are auto-assigned
- **No Validation**: Orders go directly from PENDING to INVOICED
- **Commission on Payment**: If order has merchant, commission is charged when invoice is paid

**Order Status Flow**:
```
PENDING → INVOICED (via convert)
PENDING → CANCELLED (via cancel)
```

---

### INVOICES Module
**File**: [INVOICES.md](./INVOICES.md)

**Purpose**: Sales invoice management with differential pricing, stock management, and payment tracking

**Features**:
- Invoice creation (direct or from Order conversion)
- Customer required, merchant optional (for commission)
- Invoice lines with differential pricing
- Stock deduction from warehouse
- Credit days and due date management
- Payment registration (partial and full)
- Automatic Collection creation when payment registered

**Key Concepts**:
- **Two Creation Paths**: Direct creation OR conversion from Order
- **Differential Pricing**: Applied to each line based on customer/product rules
- **Stock Management**: Deducted at invoice creation, restored on cancellation
- **Credit Days**: Uses `Customer.creditDays` if not specified; calculates due date
- **Payment Tracking**: Supports partial payments, tracks balance
- **Commission Trigger**: When payment registered, Collection created (if merchant exists)

**Invoice Status Flow**:
```
PENDING → DELIVERED (via dispatcher delivery)
DELIVERED → PARTIALLY_PAID (via partial payment)
DELIVERED → PAID (via full payment)
PENDING → CANCELLED (via cancel, not yet delivered)
PARTIALLY_PAID → PAID (via final payment)
```

---

### COLLECTIONS Module
**File**: [COLLECTIONS.md](./COLLECTIONS.md)

**Purpose**: Commission tracking for merchants when invoice payments are received

**Features**:
- Automatic collection creation when payment registered
- Commission calculation based on merchant percentage
- Immutable records for auditing
- Merchant-specific views and summaries
- Date range reporting

**Key Concepts**:
- **Automatic Generation**: Created when payment registered on invoice with merchant
- **Immutable Records**: Cannot be modified or deleted once created
- **Commission Formula**: `commissionAmount = paymentAmount × (merchant.commission% / 100)`
- **Snapshot Pattern**: Commission percentage captured at creation time
- **One-to-One**: Each payment generates exactly one collection (if merchant exists)

**Collection Flow**:
```
Invoice Payment (with merchant) → Collection Created → Commission Recorded
```

---

### AUDIT Module
**File**: [AUDIT.md](./AUDIT.md)

**Purpose**: Append-only audit log for compliance and traceability

**Features**:
- Immutable audit log records (no updates, no deletes)
- Tracks: user, entity type, entity ID, action, timestamp, IP address
- `AuditService` facade for programmatic logging by other modules
- Admin-only paginated query endpoint with filters

**Key Concepts**:
- **Append-Only**: Records are created once and never modified
- **AuditService**: Other modules call `AuditService.log(...)` to record events
- **Actions**: CREATE, UPDATE, DELETE, READ, LOGIN, LOGOUT, PAYMENT, STATUS_CHANGE
- **Entity Types**: INVOICE, ORDER, CUSTOMER, PRODUCT, MERCHANT, PAYABLE, COLLECTION, RECEIVABLE, DISPATCHER, AUTH, WAREHOUSE

---

### PAYABLES Module
**File**: [PAYABLES.md](./PAYABLES.md)

**Purpose**: Commission payment management — tracks what the company owes to merchants

**Features**:
- Auto-generated when a Collection is created
- Partial and full payment support (PENDING → PARTIALLY_PAID → PAID)
- Admin-only payment registration; merchants have read-only access
- Merchant-scoped view: merchants can only see their own payables

**Key Concepts**:
- **Auto-Generated**: Payables are created automatically from Collections
- **Role-Based Access**: Only USER (admin) role can register payments
- **Merchant View**: Merchants can query their own payables and totals

---

### PRODUCTS Module
**File**: [PRODUCTS.md](./PRODUCTS.md)

**Purpose**: Product catalog management with support for simple and composite products

**Features**:
- Product CRUD with category association
- Composite products assembled from component materials (ProductComposition)
- Multiple units of measure: units, kilograms, bulk packages
- Automatic stock deduction when assembling composite products
- Category-based filtering and organization

**Key Concepts**:
- **Aggregate Root**: Product is the root; ProductComposition is an internal entity
- **No Separate Composition Endpoints**: Compositions are managed through Product create/update
- **Category Reference**: Products reference categories by ID (see CATEGORY module)

---

### CATEGORY Module
**File**: [CATEGORY.md](./CATEGORY.md)

**Purpose**: Product category management — organizes and groups products

**Features**:
- Category CRUD operations
- Used by Products and Reports modules for filtering
- Deletion blocked when category has associated products

**Key Concepts**:
- **Independent Entity**: Categories have their own lifecycle separate from Products
- **Referenced by Products**: Products reference categories via foreign key
- **Deletion Guard**: Cannot delete a category that still has products

---

### DIFFERENTIAL Module
**File**: [DIFFERENTIAL.md](./DIFFERENTIAL.md)

**Purpose**: Price differential (percentage adjustment) management for product pricing

**Features**:
- Create and manage price differentials (positive = markup, negative = discount)
- Applied to invoice lines and price list reports
- Used in catalog and label PDF generation

**Key Concepts**:
- **Simple Aggregate Root**: No internal entities
- **Percentage-Based**: Adjustments expressed as percentage of the base sale price
- **Reusable**: One differential can be applied to multiple contexts (invoices, reports, labels)

---

### WAREHOUSE Module
**File**: [WAREHOUSE.md](./WAREHOUSE.md)

**Purpose**: Multi-warehouse inventory management with full stock movement audit trail

**Features**:
- Warehouse CRUD operations
- Stock levels per product per warehouse
- Stock movements: entries, exits, adjustments, and transfers
- Complete movement history for traceability

**Key Concepts**:
- **Active Warehouse Rule**: Stock operations require an active warehouse
- **Multi-Warehouse**: Each product can have stock in multiple warehouses
- **Audit Trail**: Every stock change is recorded as a movement

---

### DISPATCHERS Module
**File**: [DISPATCHERS.md](./DISPATCHERS.md)

**Purpose**: Dispatcher (delivery personnel) account management with highly restricted access

**Features**:
- Dispatcher authentication (separate entity from Users and Merchants)
- Single allowed action: mark an invoice as DELIVERED with a justification note
- View pending invoices assigned for delivery

**Key Concepts**:
- **Separate Entity**: Dispatcher is NOT a User with a special role
- **Restricted Access**: Can ONLY mark invoices as DELIVERED — no other operations
- **Invoice Flow**: Triggers the `PENDING → DELIVERED` invoice status transition

---

### RECEIVABLES Module
**File**: [RECEIVABLES.md](./RECEIVABLES.md)

**Purpose**: Customer-centric view of outstanding debts with payment validation workflow

**Features**:
- Aggregate view of invoices with pending balances per customer
- Single invoice payment, partial payment, or full customer balance payment
- Merchant payment validation: payments from merchants require USER approval
- Commission tracking: each payment generates a payable for the merchant

**Key Concepts**:
- **Customer-Centric**: Primary view groups unpaid invoices by customer
- **Payment Validation**: Merchant-submitted payments are not applied until USER approves
- **Role-Based Scope**: Merchants can only see invoices associated with them
- **Commission Trigger**: Validated payment → Collection → Payable

---

### DASHBOARD Module
**File**: [DASHBOARD.md](./DASHBOARD.md)

**Purpose**: Aggregated read-only KPIs and chart data for the main control panel

**Features**:
- Summary KPIs: total invoiced, total collected, outstanding receivables, overdue invoices
- Chart data: monthly revenue trend, sales by category, payment method breakdown
- Recent invoices table with status
- No write operations — pure query module

**Key Concepts**:
- **Read-Only**: No domain entities or write ports
- **Cross-Module Query**: Aggregates data from invoices, payments, customers, and categories
- **Follows `reports/` pattern**: Hexagonal structure under `reports/dashboard/`

---

### REPORTS_SALES Module
**File**: [REPORTS_SALES.md](./REPORTS_SALES.md)

**Purpose**: Sales analysis reports — cash reconciliation and sales breakdown by configurable dimension

**Features**:
- Cash reconciliation: payments grouped by method for a datetime range, with optional cashier filter
- Sales breakdown: invoice line aggregation grouped by product, category, customer, merchant, or warehouse
- JSON and PDF output for both endpoints

**Key Concepts**:
- **Pure Query Module**: No write operations
- **CANCELLED invoices excluded** from all aggregations
- **`totalBalance` = revenue − paid**: Represents the outstanding receivable per group

---

### REPORTS_CUSTOMERS Module
**File**: [REPORTS_CUSTOMERS.md](./REPORTS_CUSTOMERS.md)

**Purpose**: Customer-focused reports — directory, account statement, aging analysis, and open receivables

**Features**:
- Customer directory with lifetime stats
- Account statement: full invoice and payment history per customer
- Aging report: outstanding balances bucketed by days overdue (0-30, 31-60, 61-90, 90+)
- Open receivables snapshot with days overdue per invoice
- JSON and PDF output for all endpoints

**Key Concepts**:
- **Pure Query Module**: No write operations
- **Aging uses `dueDate < today`**: Overdue detection is a date comparison, not a status

---

### REPORTS_MERCHANTS Module
**File**: [REPORTS_MERCHANTS.md](./REPORTS_MERCHANTS.md)

**Purpose**: Merchant performance reports — directory, period sales, and commission collections

**Features**:
- Merchant directory with lifetime commission totals
- Period-based sales summary per merchant
- Detailed commission collections breakdown
- JSON and PDF output for all endpoints

**Key Concepts**:
- **Pure Query Module**: No write operations
- **Commission traceability**: Links collections back to invoices and payments

---

### REPORTS_PRODUCTS Module
**File**: [REPORTS_PRODUCTS.md](./REPORTS_PRODUCTS.md)

**Purpose**: Product, stock, and pricing reports — list, stock levels, movements, catalog, price list, and labels

**Features**:
- Product list with flexible grouping (category, type, or flat) — JSON and PDF
- Stock snapshot as of any date with per-line and aggregate totals — JSON and PDF
- Stock movement history for a date range with entry/exit/adjustment summary — JSON and PDF
- Visual product catalog PDF (delegates to `products/pdf/` submodule) — **PDF only**
- Price list with up to 5 differential pricing columns — JSON and PDF
- Printable product labels with configurable size, barcode, and differential pricing — **PDF only**

**Key Concepts**:
- **Pure Query Module**: No write operations
- **Catalog delegates to `GenerateCatalogPdfUseCase`** from `products/pdf/` (not from `reports/products/`)
- **Max 5 differentials** on the price list — additional IDs are silently truncated
- **`costPrice` is masked (`null`) when `showCostPrice=false`**

---

## Creating New Module Documentation

When implementing a new module, follow these steps:

### 1. Create Module File
Create `docs/modules/[MODULE_NAME].md` using existing modules as template

### 2. Define Domain Model
Document all entities, value objects, and enums BEFORE implementation

### 3. Design API Endpoints
Define all endpoints with request/response examples

### 4. Specify DTOs
Document all request and response DTOs with validation rules

### 5. List Use Cases
Document all use cases the module will implement

### 6. Define Repository Interfaces
Specify repository method signatures

### 7. Document Business Rules
List all domain rules and constraints

### 8. Design Database Schema
Write SQL schema and plan migrations

### 9. Review with Team
Ensure all agents understand the module structure

### 10. Implement Code
Agents use the documentation as blueprint for implementation

---

## Module Documentation Template

```markdown
# [MODULE_NAME] Module

## Overview
[Module description and purpose]

## Domain Model

### Entities

#### [EntityName]
**Domain Entity**: `com.lexand.lexand_api.[module].domain.entities.[EntityName]`

**Description**: [Entity description]

**Attributes**:
| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| ... | ... | ... | ... |

**Business Methods**:
- `method()` - Description

**Business Rules**:
1. Rule 1
2. Rule 2

### Value Objects
| Value Object | Type | Description | Validation Rules |
|--------------|------|-------------|------------------|
| ... | ... | ... | ... |

## API Endpoints

### 1. [Endpoint Name]
**Endpoint**: `[METHOD] /api/[path]`
**Authentication**: Required/Not required
**Description**: [Description]

**Request Body**:
```json
{
  "field": "value"
}
```

**Response** ([Status Code]):
```json
{
  "field": "value"
}
```

**Errors**:
- `[Code]`: [Description]

## DTOs

### Request DTOs
[Document all request DTOs]

### Response DTOs
[Document all response DTOs]

## Use Cases
1. **[UseCase1]** - Description
2. **[UseCase2]** - Description

## Repository Interfaces
[Document repository interface]

## Domain Services
[Document domain services]

## Business Rules Summary
[List all business rules]

## Database Schema
[SQL schema]

## Exception Handling
[Custom exceptions]

## Notes for Developers
[Guidelines and best practices]
```

---

## Maintenance

### When to Update
- **Before implementing** new features
- **When adding** new endpoints
- **When modifying** domain entities or DTOs
- **During code reviews** to ensure sync
- **After major refactoring**

### Review Process
1. Developer updates module documentation
2. Master Documentation Agent reviews changes
3. Other agents validate against their responsibilities
4. Documentation merged with code changes

---

## Best Practices

1. **Keep in Sync**: Always update docs when code changes
2. **Examples First**: Show JSON examples before explaining
3. **Complete Validation**: Document all validation rules
4. **Clear Business Rules**: Make domain rules explicit
5. **Real Examples**: Use realistic data in examples
6. **No Code Comments**: Documentation goes here, not in code
7. **English Only**: All documentation in English
8. **Diagrams**: Use Mermaid for complex flows

---

## Questions?

If you're unsure about:
- **Module structure**: Check existing AUTH or CUSTOMERS modules
- **Section format**: Follow the template above
- **Business rules**: Consult with Domain Agent
- **API design**: Consult with Infrastructure Agent
- **Documentation standards**: Consult with Master Documentation Agent


[Complete source and test responsibility structure](../architecture/RESPONSIBILITY-PACKAGES.md) lists the current packages for every backend module.
