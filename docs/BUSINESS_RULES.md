# 📋 Business Rules - Lexand API

## Overview

This document provides a high-level overview of business rules, constraints, and validations in the Lexand API system. For detailed module-specific rules, refer to the individual module documentation in [`docs/modules/`](modules/).

---

## 🏢 Implemented Module Business Rules

### 🔐 [Authentication Module](modules/AUTH.md)

**Purpose**: JWT-based authentication and role-based access control

**Key Rules**:
- User registration requires: email, username, password, firstName, lastName, role
- Email must be unique across all users
- Password requirements: min 8 characters, uppercase, lowercase, number, special character
- Three roles: USER (admin), MERCHANT, DISPATCHER
- JWT access token expires in 15 minutes
- Refresh token expires in 7 days
- Only one active refresh token per user (new login revokes previous tokens)

**Detailed Documentation**: [AUTH.md](modules/AUTH.md)

---

### 👥 [Customers Module](modules/CUSTOMERS.md)

**Purpose**: Complete customer management with DDD aggregate pattern

**Key Rules**:
- Customer is an Aggregate Root containing nested entities
- Nested entities (Emails, Phones, Addresses) have NO separate API endpoints
- At least one email, one phone, and one address required
- Tax ID (documentNumber) must be unique across all customers
- Customer types: INDIVIDUAL, BUSINESS, GOVERNMENT, NON_PROFIT
- Update operations use "Replace All" strategy for nested entities
- Cannot delete customer with pending orders or unpaid invoices (planned)

**Aggregate Pattern**:
- **Aggregate Root**: Customer
- **Internal Entities**: CustomerEmail, CustomerPhone, CustomerAddress
- **NO endpoints**: `/api/emails`, `/api/phones`, `/api/addresses`
- **All operations** through: `/api/customers`

**Detailed Documentation**: [CUSTOMERS.md](modules/CUSTOMERS.md)

---

### 🏪 [Merchants Module](modules/MERCHANTS.md)

**Purpose**: Merchant (comercial) account management - separate entity from Users

**Status**: ✅ Implemented

**Key Rules**:
- **Separate Entity**: Merchant is NOT a User with special role - it's a separate entity
- Merchants authenticate with role `MERCHANT` through the same login endpoint
- **Limited Access**: Merchants can ONLY create orders and view their own orders
- **Single Commission**: Each merchant has exactly ONE commission percentage (0-100%)
- Merchant email must be unique across merchants AND users (shared auth namespace)
- **Commission on Payment**: Commission is charged when invoice is PAID, not when order is created

**Merchant Entity Fields**:
- id, code (auto-generated), name, email, password, phone, address
- commissionPercentage (0-100%)
- active status

**Commission Calculation** (when invoice is paid):
```
Invoice Total:        $1,000.00
Commission (15%):     $150.00    ← Charged to merchant
Merchant receives:    $850.00    ← Reported via PDF
```

**Authorization**:
| Action | USER | MERCHANT |
|--------|------|----------|
| Create Merchant | ✅ | ❌ |
| Edit Merchant | ✅ | ❌ |
| View All Merchants | ✅ | ❌ |
| View Own Profile | ❌ | ✅ |
| Create Orders | ✅ | ✅ (auto-assigned) |
| View Orders | ✅ | ✅ (own only) |
| Cancel Orders | ✅ | ✅ (own, while PENDING) |
| Convert to Invoice | ✅ | ❌ |

**Detailed Documentation**: [MERCHANTS.md](modules/MERCHANTS.md)

---

## 📋 Planned Module Business Rules

### 📦 Products Module

**Status**: ✅ **IMPLEMENTED**

#### Product Types

**SIMPLE Products**:
- Standard products with direct stock tracking
- No component requirements
- Stock changes affect only the product itself

**COMPOSITE Products**:
- Products assembled from component materials
- Require at least 1 component definition
- Adding stock automatically deducts components
- Cannot add stock without sufficient components
- Examples: Gift baskets, product kits, assembled items

#### Units of Measure

**UNIT** (Individual Items):
- Sold as discrete units (1, 2, 3...)
- Examples: Laptop, phone, bottle, shirt
- bulkQuantity must be NULL

**KILOGRAM** (Weight-Based):
- Sold by weight with decimal precision (1.5 kg, 2.75 kg)
- 3 decimal precision for storage
- Examples: Rice, flour, coffee, meat
- bulkQuantity must be NULL

**BULK** (Packages/Bundles):
- Sold as packages containing multiple units
- Requires bulkQuantity (units per package)
- Stock tracked in number of bulks
- Examples:
  - Case of 12 sodas (bulkQuantity = 12)
  - Box of 50 pens (bulkQuantity = 50)
  - Pallet of 24 water bottles (bulkQuantity = 24)

#### Core Business Rules

1. **Product Code**: Auto-generated sequentially (PRD-00001, PRD-00002...)
2. **Product Type Immutability**: Cannot change productType after creation
3. **UOM Immutability**: Cannot change primaryUnitOfMeasure after creation
4. **Price Validation**: salePrice >= costPrice (warning, not strict)
5. **Category Required**: Every product must have a category
6. **Active Status**: Only active products can be used in orders

#### Composition Rules (COMPOSITE Products)

1. **Component Requirement**: COMPOSITE products MUST have at least 1 component
2. **Component Validation**: All components must exist and be active
3. **No Self-Reference**: Product cannot be its own component
4. **No Circular Dependencies**:
   - ✅ Valid: A requires B, B requires C
   - ❌ Invalid: A requires B, B requires A
   - ❌ Invalid: A requires B, B requires C, C requires A
5. **Quantity Required**: Must be > 0, can be decimal (e.g., 0.5, 2.25)
6. **UOM Match**: Component UOM must match component product's primary UOM
7. **Nested Compositions**: COMPOSITE products can use other COMPOSITE products as components

#### Stock Management Rules

**SIMPLE Products - Stock Operations**:
- Add stock: Increases product quantity directly
- Remove stock: Decreases product quantity directly
- No component considerations

**COMPOSITE Products - Stock Operations**:
- **Adding Stock** (Assembly Process):
  1. Calculate required components: quantity × quantityRequired for each component
  2. Validate all components have sufficient stock
  3. If any component insufficient, entire operation fails
  4. Deduct all components atomically (transaction)
  5. Add composite product stock
  6. Create stock movements for all changes
  
- **Removing Stock** (Sales):
  - Only affects composite product quantity
  - Components are NOT added back
  - Components were consumed during assembly

**Example**: Gift Basket (COMPOSITE)
```
Components:
  - 2 × Wine Bottle (UNIT)
  - 1 × Greeting Card (UNIT)
  - 0.5 × Chocolates (KILOGRAM)

Add 10 Gift Baskets:
  ✅ Basket stock: +10 units
  ✅ Wine deducted: -20 units (10 × 2)
  ✅ Card deducted: -10 units (10 × 1)
  ✅ Chocolate deducted: -5 kg (10 × 0.5)

Sell 5 Gift Baskets:
  ✅ Basket stock: -5 units
  ❌ Components NOT affected (already consumed during assembly)
```

#### Deletion Rules

Cannot delete product if:
1. Product has existing stock in any warehouse
2. Product is used as a component in other products
3. Product is referenced in existing orders (planned)

Deletion strategy: Soft delete (mark as inactive)

#### Validation Rules

**Product Creation**:
- name: 3-200 characters, required
- category: required
- costPrice: >= 0, required
- salePrice: >= 0, required
- productType: required (SIMPLE or COMPOSITE)
- primaryUnitOfMeasure: required (UNIT, KILOGRAM, or BULK)
- If BULK: bulkQuantity required and > 0
- If COMPOSITE: components required with at least 1 component
- If SIMPLE: components must be null or empty

**Composition Validation**:
- quantityRequired: > 0
- componentUnitOfMeasure: must match component's primaryUnitOfMeasure
- No duplicate components in same product
- No circular dependencies

**Stock Operations**:
- quantity: must be > 0
- For COMPOSITE products:
  - Validate all components exist and are active
  - Validate sufficient component stock
  - All deductions atomic (all succeed or none)

#### Calculated Fields

**Max Assemblable Units** (COMPOSITE only):
- Calculates maximum units that can be assembled with current component stock
- Formula: MIN(componentStock / quantityRequired) for all components
- Example:
  - Wine: 150 units / 2 = 75 baskets
  - Card: 80 units / 1 = 80 baskets
  - Chocolate: 25.5 kg / 0.5 = 51 baskets
  - **Max Assemblable = 51** (limited by chocolate)

**Detailed Documentation**: [PRODUCTS.md](modules/PRODUCTS.md)

---

### 🛒 [Orders Module](modules/ORDERS.md)

**Status**: ✅ Implemented

**Purpose**: Sales order management with optional merchant association for commission tracking

#### Core Rules
1. **Customer Required**: Every order MUST have a customer associated
2. **Merchant Optional**: Orders MAY have a merchant associated (for commission tracking)
3. **Delivery Date Required**: Orders must specify a delivery date (today or future)
4. **No Validation Workflow**: Orders go directly from PENDING to INVOICED
5. **Invoice Generation**: USER/ADMIN manually converts orders to invoices

#### Who Can Create Orders
| Role | Can Create | Merchant Association |
|------|------------|---------------------|
| USER | ✅ | Optional (can assign any merchant) |
| MERCHANT | ✅ | Automatic (self-assigned) |

#### Order Status Flow
```
PENDING ──┬──► INVOICED (USER converts to invoice)
          │
          └──► CANCELLED (USER/MERCHANT cancels)
```

#### Access Control
| Action | USER | MERCHANT |
|--------|------|----------|
| Create Order | ✅ | ✅ (auto-assigned) |
| View All Orders | ✅ | ❌ |
| View Own Orders | ✅ | ✅ |
| Update Order | ✅ | ✅ (own, PENDING only) |
| Cancel Order | ✅ | ✅ (own, PENDING only) |
| Convert to Invoice | ✅ | ❌ |

#### Commission Rules
- If order has merchant → commission charged when invoice is PAID
- If order has NO merchant → no commission
- Commission = Invoice Total × (Merchant Commission % / 100)

**Detailed Documentation**: [ORDERS.md](modules/ORDERS.md)

---

### 🧾 Invoices Module

**Status**: ✅ Implemented

#### Core Rules
1. **Sequential Numbers**: Invoice numbers auto-generated and sequential
2. **Tax Calculation**: Automatic tax calculation by line and total
3. **Payment Terms**: Due dates calculated from credit days
4. **Balance Tracking**: Outstanding balance calculation
5. **Stock Impact**: Stock deducted at creation, restored on cancellation
6. **Status Flow**: `PENDING` → `PARTIALLY_PAID` → `PAID`, `OVERDUE`, `CANCELLED`

---

### 🏭 Warehouses Module

**Status**: ✅ Implemented

#### Core Rules
1. **Warehouse Locations**: Multiple physical warehouse locations
2. **Stock Per Product**: Current inventory per product per warehouse
3. **Stock Movements**: All stock changes tracked with audit trail (ENTRY, EXIT, TRANSFER, ADJUSTMENT)
4. **Stock Transfers**: Inter-warehouse product transfers
5. **Reservation System**: Stock reserved for pending orders, released on cancellation

---

## 🔄 Cross-Module Business Rules (Planned)

### Order → Invoice Flow
1. Order approved → Invoice automatically created
2. Invoice lines mirror order lines
3. Customer balance updated
4. Stock reservation converted to stock reduction
5. Payment terms from order settings

### Stock Management Rules
1. **Stock Reservation**: Pending orders reserve stock
2. **Negative Stock Prevention**: Cannot approve order without stock (unless override)
3. **Stock Transfer**: Origin decremented, destination incremented atomically
4. **Audit Trail**: Every stock change logged with user and timestamp

### Payment Application Rules
1. **FIFO Method**: Payments apply to oldest due dates first
2. **Partial Payments**: Allowed and tracked per due date
3. **Overpayment**: Generates credit balance for customer
4. **Late Payment**: Due dates tracked, overdue status automatic

### Customer Balance Rules
1. **Invoice Impact**: Invoices increase customer debt
2. **Payment Impact**: Payments decrease customer debt
3. **Credit Limit**: Optional credit limit enforcement
4. **Balance Calculation**: Real-time balance = total invoiced - total paid

---

## 🔐 Security Rules

### Authentication (Implemented ✅)

See [AUTH.md](modules/AUTH.md) for complete authentication rules.

**Summary**:
- JWT token required for all endpoints except login/register
- Access token expiration: 15 minutes
- Refresh token expiration: 7 days
- Automatic token refresh mechanism

### Authorization (Implemented ✅)

**Role Permissions**:

| Action | USER | MERCHANT | DISPATCHER |
|--------|------|----------|------------|
| Create/Edit Users | ✅ | ❌ | ❌ |
| View All Customers | ✅ | ❌ | ❌ |
| Create/Edit Customers | ✅ | ❌ | ❌ |
| Delete Customers | ✅ | ❌ | ❌ |
| Create Orders | ✅ | ✅ (auto-assigned) | ❌ |
| Generate Invoices | ✅ | ❌ | ❌ |
| View Products | ✅ | ✅ | ✅ |
| Edit Products | ✅ | ❌ | ❌ |
| Manage Stock | ✅ | ❌ | ❌ |
| Register Payments | ✅ | ❌ | ❌ |
| Mark Invoice Delivered | ❌ | ❌ | ✅ |
| View Reports | ✅ | ❌ | ❌ |
| View Own Payables | ❌ | ✅ | ❌ |
| View Own Orders | ✅ | ✅ | ❌ |

### Data Privacy (Planned)
- Personal data encryption at rest
- PII (Personally Identifiable Information) access logged
- Customer data isolated by access permissions
- GDPR compliance for data deletion requests

---

## 📊 Common Validation Rules

### Monetary Values
- All prices and amounts must be ≥ 0
- Currency format: 2 decimal places
- Tax rates: 0-100%
- Discounts: 0-100%

### Dates
- Creation dates cannot be in the future
- Due dates must be after creation date
- Date range queries: start ≤ end

### Text Fields
- Names: 2-200 characters
- Descriptions: max 1000 characters
- Codes/SKUs: alphanumeric, 2-50 characters
- Email: valid email format (RFC 5322)
- Phone: valid international format

### Numeric Fields
- IDs: positive integers (Long)
- Quantities: positive numbers, up to 2 decimal places
- Percentages: 0-100, up to 2 decimal places

### Required Fields

**Customers**:
- firstName, lastName (for INDIVIDUAL)
- companyName (for BUSINESS, GOVERNMENT, NON_PROFIT)
- documentNumber (tax ID)
- At least 1 email, 1 phone, 1 address

**Products** (planned):
- SKU, name, price, category

**Orders** (planned):
- customer, at least one line item

**Users** (implemented):
- username, email, password, firstName, lastName, role

---

## 🚫 Deletion Rules

### Cannot Delete (Implemented ✅)

- **Customers**: with pending orders or unpaid invoices (backend validation ready)

### Cannot Delete (Planned)

- **Products**: with existing stock or referenced in orders
- **Warehouses**: with existing stock
- **Users**: with associated transactions
- **Orders/Invoices**: with payments (must cancel instead)

### Soft Delete Strategy

- Deleted records can be marked as inactive (to be implemented)
- Historical data preserved for reporting
- Cascade rules prevent orphaned records (implemented in JPA entities)

---

## 🎯 DDD Aggregate Pattern Rules

### What is an Aggregate?

An **Aggregate** is a cluster of domain objects (entities and value objects) treated as a single unit for data changes.

**Key Principles**:
1. **Aggregate Root**: Single entity that serves as the entry point
2. **Internal Entities**: Entities inside the aggregate, accessible only through the root
3. **NO Separate Endpoints**: Internal entities have no dedicated API endpoints
4. **Consistency Boundary**: All changes go through the aggregate root to ensure consistency

### Implemented Aggregates ✅

#### Customer Aggregate

- **Aggregate Root**: Customer
- **Internal Entities**: CustomerEmail, CustomerPhone, CustomerAddress
- **API Design**: All operations go through `/api/customers`
- **NO Endpoints**: `/api/emails`, `/api/phones`, `/api/addresses`
- **Update Strategy**: "Replace All" - delete old nested entities, create new ones

See [CUSTOMERS.md](modules/CUSTOMERS.md) for complete details.

### Planned Aggregates

#### Order Aggregate
- **Aggregate Root**: Order
- **Internal Entities**: OrderLine
- **API Design**: All operations through `/api/orders`

#### Invoice Aggregate
- **Aggregate Root**: Invoice
- **Internal Entities**: InvoiceLine, PaymentDueDate, Payment
- **API Design**: All operations through `/api/invoices`

---

## 📦 DTO Design Rules

### Request DTOs (Input)

**Location**: `application/dto/request/`

**Characteristics**:
- NO IDs for nested entities
- Used for CREATE and UPDATE operations
- Validation annotations on all fields
- Frontend sends data without entity IDs

**Example**:
```java
public class CreateCustomerRequest {
    private String firstName;
    private List<EmailRequest> emails; // NO ID field in EmailRequest
}
```

### Response DTOs (Output)

**Location**: `application/dto/response/`

**Characteristics**:
- WITH IDs for all entities (root and nested)
- Different DTOs for different use cases:
  - `[Entity]ListResponse`: Minimal data for list views (performance)
  - `[Entity]DetailResponse`: Complete data with all nested entities
  - `[Entity]CreateResponse`: Response after creation

**Example**:
```java
public class CustomerDetailResponse {
    private Long id;
    private String firstName;
    private List<EmailResponse> emails; // EmailResponse HAS id field
}
```

### Update Strategy: "Replace All"

When updating nested entities:

1. Backend **deletes ALL** old nested entities
2. Backend **creates ALL** new nested entities from request
3. NO merge logic, NO partial updates
4. Frontend must send **complete arrays** of nested data

**Why this approach?**
- Simplifies backend logic (no complex merge)
- Ensures data consistency
- Clear transaction boundaries
- Easier to test and maintain

---

## 📚 Module Documentation References

For detailed business rules, validations, and examples, see individual module documentation:

- **[AUTH.md](modules/AUTH.md)** - Authentication and authorization rules
- **[CUSTOMERS.md](modules/CUSTOMERS.md)** - Customer management business rules
- **[MERCHANTS.md](modules/MERCHANTS.md)** - Merchant and commission rules
- **[PRODUCTS.md](modules/PRODUCTS.md)** - Product types, compositions, and stock rules
- **[WAREHOUSE.md](modules/WAREHOUSE.md)** - Warehouse and stock movement rules
- **[ORDERS.md](modules/ORDERS.md)** - Order lifecycle and commission rules
- **[INVOICES.md](modules/INVOICES.md)** - Invoice creation, payments, and status flow
- **[COLLECTIONS.md](modules/COLLECTIONS.md)** - Commission collection rules
- **[PAYABLES.md](modules/PAYABLES.md)** - Merchant payable and payment rules
- **[RECEIVABLES.md](modules/RECEIVABLES.md)** - Customer receivable and pending payment rules
- **[DISPATCHERS.md](modules/DISPATCHERS.md)** - Dispatcher and delivery rules
- **[REPORTS.md](modules/REPORTS.md)** - Report generation rules
- **[AUDIT.md](modules/AUDIT.md)** - Audit log rules
- **[TEMPLATE.md](modules/TEMPLATE.md)** - Template for new module documentation

---

## 🔄 Change Log

| Date | Module | Change |
|------|--------|--------|
| 2025-12-15 | AUTH | Implemented JWT authentication with refresh tokens |
| 2025-12-15 | CUSTOMERS | Implemented customer aggregate with DDD pattern |
| 2025-12-15 | DOCS | Created module-specific documentation structure |
| 2026-03-10 | DOCS | Updated all module statuses to reflect current implementation state |
| 2026-03-10 | DOCS | Fixed role names: USER (admin), MERCHANT, DISPATCHER |

---

[← Back to Documentation](./ARCHITECTURE.md)
