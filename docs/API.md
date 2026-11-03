# 🔌 API Documentation

## 📖 Interactive API Documentation

The Cashly API provides comprehensive interactive documentation through **OpenAPI 3.0** specification with **Swagger UI**.

### 🌐 Access Documentation

#### Local Development
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

#### Testing Environment
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (when using test container)

### ✨ Features

- **Interactive Testing**: Test all endpoints directly from the browser
- **Request/Response Examples**: See real data structures
- **Authentication Support**: Built-in JWT token management
- **Schema Validation**: Automatic request/response validation
- **Complete Documentation**: All endpoints with detailed descriptions

## 🧪 Testing Options

### 1. Direct Testing from OpenAPI/Swagger
- Navigate to Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Authorize with JWT token using the "Authorize" button
- Test any endpoint directly from the interface
- View request/response examples and schemas

## 🚀 Getting Started

### 1. Start the Application
```bash
docker compose up --build
```

### 2. Access Documentation
- Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Start testing endpoints directly from the browser!

### 3. Health Check
```bash
curl http://localhost:8080/actuator/health
# Response: {"status":"UP"}
```

## 📋 API Overview

The Cashly API provides comprehensive enterprise invoicing and inventory management capabilities through RESTful endpoints.

### Implemented Modules ✅

- **🔐 [Authentication](modules/AUTH.md)** - JWT-based user authentication with role-based access control
  - User registration and login
  - Access token and refresh token management
  - Roles: USER (admin), MERCHANT, DISPATCHER

- **👥 [Customers](modules/CUSTOMERS.md)** - Complete customer management with DDD aggregate pattern
  - Customer CRUD operations
  - Nested entities: Emails, Phones, Addresses (managed through customer endpoints)
  - Customer types: INDIVIDUAL, BUSINESS, GOVERNMENT, NON_PROFIT

- **🏪 [Merchants](modules/MERCHANTS.md)** - Merchant account management
  - Merchant CRUD with commission percentage
  - Merchant authentication (MERCHANT role)
  - Merchant self-profile endpoint

- **📦 [Products](modules/PRODUCTS.md)** - Product catalog management
  - SIMPLE and COMPOSITE product types
  - Product compositions (multi-warehouse sourcing)
  - Category management and stock availability queries

- **🏭 [Warehouse](modules/WAREHOUSE.md)** - Multi-warehouse inventory control
  - Stock tracking per product per warehouse
  - Stock movements with audit trail (ENTRY, EXIT, TRANSFER, ADJUSTMENT)
  - Stock reservation and release during order/invoice lifecycle

- **🛒 [Orders](modules/ORDERS.md)** - Sales order management
  - Order creation with or without merchant
  - Status flow: PENDING → INVOICED or CANCELLED
  - Convert order to invoice

- **🧾 [Invoices](modules/INVOICES.md)** - Invoice generation and payment tracking
  - Direct and order-conversion invoice creation
  - Differential pricing per customer/product rules
  - Partial and full payment registration
  - PDF generation and delivery proof upload

- **📋 [Collections](modules/COLLECTIONS.md)** - Commission collection records
  - Auto-generated when invoice payment is registered (if merchant exists)
  - Commission calculation: paymentAmount × (commissionRate / 100)
  - Immutable audit records

- **💰 [Payables](modules/PAYABLES.md)** - Merchant commission payment management
  - Auto-generated from Collections
  - Single, bulk-all, and partial-budget payment flows
  - Full payment audit history per payable

- **📥 [Receivables](modules/RECEIVABLES.md)** - Customer debt management
  - Customer-centric debt view
  - Merchant payment flow with admin approval
  - Payment proof file upload (required for merchants, optional for USER/ADMIN)
  - Merchant self-view of own payments (`GET /payments/my`)
  - Payment validation statuses (PENDING_APPROVAL, APPROVED, REJECTED)

- **🚚 [Dispatchers](modules/DISPATCHERS.md)** - Delivery management
  - Dispatcher CRUD and self-profile
  - Pending delivery invoice queue
  - Mark invoices as delivered

- **📊 [Reports](modules/REPORTS.md)** - Business intelligence and reporting
  - 15 report types: sales, customers, merchants, products, warehouse, dashboard
  - PDF and Excel export
  - Dashboard KPIs with period comparison

- **🔍 [Audit](modules/AUDIT.md)** - Audit log module
  - Append-only audit log table
  - Filterable by user, entity type, action, date range

- **💲 [Differential](modules/DIFFERENTIAL.md)** - Differential pricing rules
  - Per customer/product pricing overrides
  - Applied at invoice line creation

### Module Documentation

Each module has comprehensive documentation in the [`docs/modules/`](modules/) directory:

- **[AUTH.md](modules/AUTH.md)** - Authentication module endpoints, DTOs, and business rules
- **[CUSTOMERS.md](modules/CUSTOMERS.md)** - Customer module with aggregate pattern implementation
- **[MERCHANTS.md](modules/MERCHANTS.md)** - Merchant management
- **[PRODUCTS.md](modules/PRODUCTS.md)** - Product catalog
- **[WAREHOUSE.md](modules/WAREHOUSE.md)** - Warehouse and stock management
- **[ORDERS.md](modules/ORDERS.md)** - Sales order management
- **[INVOICES.md](modules/INVOICES.md)** - Invoice lifecycle
- **[COLLECTIONS.md](modules/COLLECTIONS.md)** - Commission collection records
- **[PAYABLES.md](modules/PAYABLES.md)** - Merchant commission payables
- **[RECEIVABLES.md](modules/RECEIVABLES.md)** - Customer receivables
- **[DISPATCHERS.md](modules/DISPATCHERS.md)** - Delivery management
- **[REPORTS.md](modules/REPORTS.md)** - Reports and analytics
- **[AUDIT.md](modules/AUDIT.md)** - Audit logging
- **[DIFFERENTIAL.md](modules/DIFFERENTIAL.md)** - Differential pricing
- **[TEMPLATE.md](modules/TEMPLATE.md)** - Template for creating new module documentation

For detailed API specifications, request/response examples, and business rules, refer to the individual module documentation files.

### Authentication Flow
1. **Register** new user account with role assignment
2. **Login** to receive JWT access token and refresh token
3. **Use Bearer token** in Authorization header for all authenticated endpoints
4. **Refresh** access token when expired using refresh token
5. **Logout** to revoke refresh token

All endpoints are fully documented in the OpenAPI specification with example requests and responses.

---

[← Back to Main README](../README.md)
## Installation company

Cashly manages one company per installation. `GET /api/company` is available to every authenticated user for navigation branding. `PUT /api/company` requires `COMPANY:UPDATE` and accepts `legalName`, `tradeName`, `taxId`, `email`, `phone`, `website` and `address` (`street`, `city`, `region`, `country`, `postalCode`). The legal name and address object are required; optional values may be empty. Responses additionally include the immutable `id` and nullable `logoPath`.

`POST /api/company/logo` accepts multipart `file`; `DELETE /api/company/logo` removes it. Both require `COMPANY:UPDATE` and return the updated company. `GET /api/company/logo` returns the authenticated PNG image, or 404 when absent. PNG and JPEG uploads are limited to 5 MiB and 4096 × 4096 pixels and are decoded and reencoded as PNG; uploaded SVG files and corrupt images are rejected.

Migration V31 backfills all existing persistent application tables with a required `company_id` foreign key. Its database default also links future records inserted through existing repositories. The companies table enforces one immutable installation ID, so clients cannot switch data ownership. System administrators receive company permissions through the permission registry; existing roles with role-update access receive company read/update grants. Other roles can be granted those permissions through role administration.

### User photos

Authenticated users manage their own photo with `POST /api/auth/profile/photo` (multipart `file`) and `DELETE /api/auth/profile/photo`. `GET /api/auth/profile/photo` returns the PNG or 404 when no image exists. Identity comes from the authenticated principal, never a request-supplied user ID.

Administrators read photos through `GET /api/users/{id}/photo` with `USERS:READ`, and upload/remove through `POST`/`DELETE` with `USERS:UPDATE`. Mutation responses return the updated user including nullable `photoPath`; login and profile responses also include that field. Migration V30 adds the photo reference.

Photos use the same raster limits and normalization as company logos. Image reads return `no-store` and `nosniff`. The generic catalog file route excludes identity media so its catalog permission cannot bypass user-photo authorization. Replaced files are retired after commit, and newly stored files are cleaned up on transaction rollback.
