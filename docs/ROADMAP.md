# Lexand API Roadmap

## Current Status — March 2026

---

## ✅ Completed Modules

### AUTH
- JWT-based authentication (login, logout, refresh token)
- User registration with role assignment
- Roles: `USER` (admin), `MERCHANT`, `DISPATCHER`
- Spring Security filter chain with stateless JWT validation

### CUSTOMERS
- Full CRUD for customers
- Contact information: emails, phones, addresses
- Credit management (credit days, credit limit)
- Customer classification by type (REGULAR, GOVERNMENT, etc.)

### PRODUCTS
- Product catalog CRUD
- Product compositions (multi-warehouse sourcing)
- Category management
- Stock availability queries per warehouse

### WAREHOUSE
- Multi-warehouse management
- Stock tracking per product per warehouse
- Stock movements with audit trail (ENTRY, EXIT, TRANSFER, ADJUSTMENT)
- Stock reservation and release flow during order/invoice lifecycle

### ORDERS
- Sales order creation (with or without merchant)
- Order lines with product snapshots
- Status flow: `PENDING` → `INVOICED` or `CANCELLED`
- Merchant auto-assignment when MERCHANT role creates order
- Convert order to invoice

### INVOICES
- Invoice creation (direct or from order conversion)
- Differential pricing per customer/product rules
- Stock deduction at creation, restoration on cancellation
- Payment registration (partial and full)
- Invoice status: `PENDING` → `PARTIALLY_PAID` → `PAID`, `OVERDUE`, `CANCELLED`
- PDF generation and delivery proof upload
- Credit days and due date calculation

### COLLECTIONS
- Auto-generated when invoice payment registered (if merchant exists)
- Commission calculation: `paymentAmount × (commissionRate / 100)`
- Immutable audit records
- Merchant-specific views and date range summaries

### PAYABLES
- Auto-generated from Collections
- Admin-only payment registration (`POST /api/payables/{id}/pay`)
- Bulk settlement: `POST /api/payables/merchants/{id}/pay-all` (settles all outstanding for a merchant)
- Bulk partial: `POST /api/payables/merchants/{id}/pay-partial` (distributes a budget FIFO by collection date)
- Partial payment support: `PENDING` → `PARTIALLY_PAID` → `PAID`
- `payable_payments` audit table (V15): immutable record per transaction, full history via `GET /api/payables/{id}/payments`
- Merchants have read-only access to their own payables
- `PayableAlreadyPaidException` (409) on overpayment attempt

### RECEIVABLES
- Customer-centric debt view (by customer, showing invoice breakdown)
- Merchant payment flow: registers `PendingPayment` → `PENDING_APPROVAL`
- Admin direct payment: applied immediately to invoice
- Admin approve/reject pending payments
- Payment validation statuses: `PENDING_APPROVAL`, `APPROVED`, `REJECTED`
- Commission collection auto-created on approval (if merchant exists)

### DISPATCHERS
- Dispatcher CRUD (admin-managed)
- Dispatcher self-profile (`GET /api/dispatchers/me`)
- Pending delivery invoice queue
- Mark invoice as delivered (with or without proof file)

### DIFFERENTIAL
- Differential pricing rules per customer/product combination
- Applied at invoice line creation

### REPORTS
- 15 report types: sales, customers, merchants, products, warehouse, dashboard
- PDF generation (A4 portrait, black & white)
- Excel export support
- Dashboard KPIs with period comparison (daily, weekly, monthly, yearly)

### MERCHANTS
- Merchant CRUD with commission percentage
- Merchant authentication (MERCHANT role)
- Commission percentage management

### Integration Tests — Controller Layer
- All controller integration tests completed
- Covers: AUTH, CUSTOMERS, WAREHOUSE, REPORTS, PAYABLES, RECEIVABLES, COLLECTIONS, DISPATCHERS
- Each test suite covers: 401 unauthenticated, 403 wrong role, 200/201 valid request, 400 bad request

### Global Exception Handler Improvements
- Standardized error response shape with `timestamp`, `status`, `error`, `message`, `path`, `traceId`
- Per-field validation errors returned as structured `errors` array
- `AuthorizationDeniedException` correctly returns HTTP 403

### Customer Credit Limit Enforcement
- `CreditLimit` value object enforced during invoice creation
- Outstanding balance queried before saving new invoice
- `CreditLimitExceededException` returns HTTP 422

### Pagination & Search Enhancements
- Free-text `search` param added to Collections list endpoints (searches `invoiceNumber`, `customerName`)
- `fromDate`/`toDate` date range filters added to Payables items endpoints

### Multi-Currency Support
- `CurrencyCode` enum: `USD`, `EUR`, `VES`
- `currency` field added to invoices (optional, defaults to `USD`)
- Included in `V9__create_invoices_tables.sql`

### Audit Log Module
- Append-only `audit_logs` table (Flyway migration `V14`)
- Full hexagonal module: domain, use cases, JPA adapter, REST controller
- `AuditService` facade for future integration by other modules
- `GET /api/audit` — paginated, admin-only, filterable by user, entity type, action, date range

### Payable Payments Audit Table
- `payable_payments` table (Flyway migration `V15`)
- Immutable record per payment transaction on any payable
- Full history accessible via `GET /api/payables/{id}/payments`
- Used by single payment, pay-all, and pay-partial flows

### CI/CD Pipeline
- GitHub Actions `ci.yml`: build + test + Docker image on push to `main`
- GitHub Actions `pr-check.yml`: test + JUnit report on every PR
- Dependabot configured for Maven and GitHub Actions weekly updates

---

## 🚧 In Progress

*Nothing currently in progress.*

---

## 📋 Planned

*No items currently planned.*

---

## 🗑️ Removed

### NOTIFICATIONS Module
**Removed**: March 2026
**Reason**: Feature not needed for current business scope. In-app notifications were replaced by a simpler operational workflow where admins monitor pending payments and overdue invoices directly via the RECEIVABLES and PAYABLES dashboards.

**What was removed**:
- All Java source: domain entity, value objects, repositories, use cases, DTOs, controller, exception handler, bean config
- 3 unit test files
- `docs/modules/NOTIFICATIONS.md`
- All seed data (20 records)

---

## 📊 Module Summary

| Module | Status | Tests | Integration Tests |
|---|---|---|---|
| AUTH | ✅ Complete | ✅ Full | ✅ Controller |
| CUSTOMERS | ✅ Complete | ✅ Full | ✅ Controller |
| PRODUCTS | ✅ Complete | ✅ Full | - |
| WAREHOUSE | ✅ Complete | ✅ Full | ✅ Stock Reservation |
| ORDERS | ✅ Complete | ✅ Partial | - |
| INVOICES | ✅ Complete | ✅ Partial | - |
| COLLECTIONS | ✅ Complete | ✅ Partial | ✅ Controller |
| PAYABLES | ✅ Complete | ✅ Full | ✅ Controller |
| RECEIVABLES | ✅ Complete | ✅ Partial | ✅ Controller |
| DISPATCHERS | ✅ Complete | ✅ Partial | ✅ Controller |
| DIFFERENTIAL | ✅ Complete | ✅ Partial | - |
| REPORTS | ✅ Complete | ✅ Full | ✅ Controller |
| MERCHANTS | ✅ Complete | ✅ Partial | - |
| AUDIT | ✅ Complete | - | - |
| NOTIFICATIONS | 🗑️ Removed | - | - |
