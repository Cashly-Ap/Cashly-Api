# REPORTS_CUSTOMERS Module

## Overview

The REPORTS_CUSTOMERS module provides **customer-focused reports** — customer directory listing, account statement, aging analysis, and open receivables snapshot. All endpoints support JSON and PDF output.

**Pattern**: Pure query module — no write operations. Follows the same hexagonal structure as other report modules under `reports/`.

---

## Module Location

```
src/main/java/com/lexand/lexand_api/reports/customers/
├── application/
│   ├── dto/
│   │   └── response/
│   │       ├── CustomerListReportResponse.java
│   │       ├── CustomerStatementReportResponse.java
│   │       ├── CustomerAgingReportResponse.java
│   │       └── CustomerReceivablesReportResponse.java
│   ├── query/
│   │   ├── CustomerListReportQuery.java
│   │   ├── CustomerStatementReportQuery.java
│   │   ├── CustomerAgingReportQuery.java
│   │   └── CustomerReceivablesReportQuery.java
│   └── usecases/
│       ├── CustomerListReportUseCase.java
│       ├── CustomerStatementReportUseCase.java
│       ├── CustomerAgingReportUseCase.java
│       └── CustomerReceivablesReportUseCase.java
└── infrastructure/
    ├── config/
    │   └── CustomerReportConfig.java
    ├── persistence/
    │   ├── JpaCustomerListReportQuery.java
    │   ├── JpaCustomerStatementReportQuery.java
    │   ├── JpaCustomerAgingReportQuery.java
    │   └── JpaCustomerReceivablesReportQuery.java
    ├── services/
    │   ├── ITextCustomerListPdfGenerator.java
    │   ├── ITextCustomerStatementPdfGenerator.java
    │   ├── ITextCustomerAgingPdfGenerator.java
    │   └── ITextCustomerReceivablesPdfGenerator.java
    └── web/
        └── CustomerReportController.java
```

---

## API Endpoints

### 1. Customer List Report

**Endpoint**: `GET /api/reports/customers/list`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a full customer directory with contact information and credit settings. Supports optional filters by active status, customer type, and search text.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `active` | `Boolean` | No | Filter by active status; omit for all customers |
| `type` | `String` | No | Filter by customer type (e.g., `REGULAR`, `WHOLESALE`) |
| `search` | `String` | No | Search by name, alias, or RIF |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `CustomerListReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "totalRecords": 3,
  "customers": [
    {
      "code": 1001,
      "legalName": "ABC Company S.A.",
      "alias": "ABC",
      "rif": "J-12345678-9",
      "type": "WHOLESALE",
      "creditDays": 30,
      "creditLimit": 50000.00,
      "phone": "+58-212-1234567",
      "email": "contacto@abc.com",
      "address": "Av. Principal, Local 5, Caracas",
      "active": true
    }
  ]
}
```

**Response DTO**:
```java
public record CustomerListReportResponse(
    LocalDateTime generatedAt,
    int totalRecords,
    List<CustomerRow> customers
) {
    public record CustomerRow(
        Long code,
        String legalName,
        String alias,
        String rif,
        String type,
        Integer creditDays,
        BigDecimal creditLimit,
        String phone,
        String email,
        String address,
        boolean active
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=customer-list-{date}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 2. Customer Statement Report

**Endpoint**: `GET /api/reports/customers/{customerId}/statement`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns an account statement for a single customer — invoices, payments, and running balance for the requested period. Supports `summary` mode (invoice totals only) and `detail` mode (includes individual payments per invoice).

**Path Parameters**:
- `customerId` (UUID) — Customer unique identifier

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `mode` | `summary \| detail` | No | Level of detail (default: `summary`) |
| `from` | `Date` (yyyy-MM-dd) | No | Start date (defaults to first day of current month) |
| `to` | `Date` (yyyy-MM-dd) | No | End date (defaults to today) |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `CustomerStatementReportResponse`:
```json
{
  "customerId": "customer-uuid-001",
  "customerName": "ABC Company S.A.",
  "customerRif": "J-12345678-9",
  "from": "2026-03-01",
  "to": "2026-03-05",
  "mode": "detail",
  "openingBalance": 1500.00,
  "invoices": [
    {
      "invoiceId": "invoice-uuid-001",
      "invoiceNumber": "FAC-2026-00101",
      "invoiceDate": "2026-03-02",
      "dueDate": "2026-04-01",
      "status": "DELIVERED",
      "subtotal": 3000.00,
      "totalPaid": 0.00,
      "balance": 3000.00,
      "payments": []
    },
    {
      "invoiceId": "invoice-uuid-002",
      "invoiceNumber": "FAC-2026-00098",
      "invoiceDate": "2026-02-15",
      "dueDate": "2026-03-17",
      "status": "PARTIALLY_PAID",
      "subtotal": 2000.00,
      "totalPaid": 500.00,
      "balance": 1500.00,
      "payments": [
        {
          "paymentId": "payment-uuid-001",
          "paymentDate": "2026-02-20",
          "paymentMethod": "TRANSFER",
          "reference": "TRF-00123",
          "amount": 500.00
        }
      ]
    }
  ],
  "totalInvoiced": 5000.00,
  "totalPaid": 500.00,
  "closingBalance": 4500.00
}
```

**Response DTO**:
```java
public record CustomerStatementReportResponse(
    UUID customerId,
    String customerName,
    String customerRif,
    LocalDate from,
    LocalDate to,
    String mode,
    BigDecimal openingBalance,
    List<StatementInvoiceEntry> invoices,
    BigDecimal totalInvoiced,
    BigDecimal totalPaid,
    BigDecimal closingBalance
) {
    public record StatementInvoiceEntry(
        UUID invoiceId,
        String invoiceNumber,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String status,
        BigDecimal subtotal,
        BigDecimal totalPaid,
        BigDecimal balance,
        List<StatementPaymentEntry> payments   // empty list in summary mode
    ) {}
    public record StatementPaymentEntry(
        UUID paymentId,
        LocalDate paymentDate,
        String paymentMethod,
        String reference,
        BigDecimal amount
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=statement-{customerId}-{from}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `404 Not Found`: Customer not found

---

### 3. Customer Aging Report

**Endpoint**: `GET /api/reports/customers/aging`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a classic accounts-receivable aging report. Each customer row shows debt broken down into aging buckets: current (not yet due), 1–30 days, 31–60 days, 61–90 days, and 90+ days overdue. Includes column totals.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `asOf` | `Date` (yyyy-MM-dd) | No | Reference date for aging calculation (defaults to today) |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `CustomerAgingReportResponse`:
```json
{
  "asOf": "2026-03-05",
  "customers": [
    {
      "customerId": "customer-uuid-001",
      "customerCode": 1001,
      "customerName": "ABC Company S.A.",
      "customerRif": "J-12345678-9",
      "current":    3000.00,
      "days1to30":  1500.00,
      "days31to60":    0.00,
      "days61to90":    0.00,
      "days90plus":    0.00,
      "total":      4500.00
    }
  ],
  "totals": {
    "current":    3000.00,
    "days1to30":  1500.00,
    "days31to60":    0.00,
    "days61to90":    0.00,
    "days90plus":    0.00,
    "total":      4500.00
  }
}
```

**Response DTO**:
```java
public record CustomerAgingReportResponse(
    LocalDate asOf,
    List<AgingCustomerEntry> customers,
    AgingTotals totals
) {
    public record AgingCustomerEntry(
        UUID customerId,
        Long customerCode,
        String customerName,
        String customerRif,
        BigDecimal current,
        BigDecimal days1to30,
        BigDecimal days31to60,
        BigDecimal days61to90,
        BigDecimal days90plus,
        BigDecimal total
    ) {}
    public record AgingTotals(
        BigDecimal current,
        BigDecimal days1to30,
        BigDecimal days31to60,
        BigDecimal days61to90,
        BigDecimal days90plus,
        BigDecimal total
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=aging-{asOf}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 4. Customer Receivables Report

**Endpoint**: `GET /api/reports/customers/receivables`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a flat list of all open invoice balances as of a reference date. Optionally filtered to only overdue invoices. Each row shows invoice-level detail including days overdue.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `asOf` | `Date` (yyyy-MM-dd) | No | Reference date (defaults to today) |
| `overdueOnly` | `Boolean` | No | `true` to return only past-due invoices (default: `false`) |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `CustomerReceivablesReportResponse`:
```json
{
  "asOf": "2026-03-05",
  "overdueOnly": false,
  "receivables": [
    {
      "customerId": "customer-uuid-001",
      "customerCode": 1001,
      "customerName": "ABC Company S.A.",
      "customerRif": "J-12345678-9",
      "invoiceNumber": "FAC-2026-00098",
      "invoiceDate": "2026-02-15",
      "dueDate": "2026-03-17",
      "status": "PARTIALLY_PAID",
      "subtotal": 2000.00,
      "totalPaid": 500.00,
      "balance": 1500.00,
      "daysOverdue": 0
    }
  ],
  "totalBalance": 4500.00
}
```

**Response DTO**:
```java
public record CustomerReceivablesReportResponse(
    LocalDate asOf,
    boolean overdueOnly,
    List<ReceivableEntry> receivables,
    BigDecimal totalBalance
) {
    public record ReceivableEntry(
        UUID customerId,
        Long customerCode,
        String customerName,
        String customerRif,
        String invoiceNumber,
        LocalDate invoiceDate,
        LocalDate dueDate,
        String status,
        BigDecimal subtotal,
        BigDecimal totalPaid,
        BigDecimal balance,
        long daysOverdue        // 0 if not yet due
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=receivables-{asOf}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

## Use Cases

| Use Case | Description |
|---|---|
| `CustomerListReportUseCase` | Returns filtered customer directory |
| `CustomerStatementReportUseCase` | Returns account statement for one customer with optional payment detail |
| `CustomerAgingReportUseCase` | Returns aging analysis bucketed by days overdue |
| `CustomerReceivablesReportUseCase` | Returns flat open-balance list with optional overdue filter |

All use cases expose `executeJson(...)` and `executePdf(...)` methods.

---

## Query Ports

```java
public interface CustomerListReportQuery {
    CustomerListReportResponse query(String search, Boolean active, String type);
}

public interface CustomerStatementReportQuery {
    CustomerStatementReportResponse query(UUID customerId, LocalDate from, LocalDate to, String mode);
}

public interface CustomerAgingReportQuery {
    CustomerAgingReportResponse query(LocalDate asOf);
}

public interface CustomerReceivablesReportQuery {
    CustomerReceivablesReportResponse query(LocalDate asOf, boolean overdueOnly);
}
```

---

## Business Rules

1. **Only DELIVERED, PARTIALLY_PAID invoices appear in receivables** — PAID and CANCELLED are excluded.
2. **Aging buckets** are based on `invoice.dueDate` vs `asOf` date: `daysOverdue = asOf - dueDate` (negative = current/not yet due).
3. **Statement `openingBalance`** is the total outstanding balance for the customer as of `from - 1 day`.
4. **Statement `closingBalance`** = `openingBalance` + `totalInvoiced` - `totalPaid` for the period.
5. **`summary` mode** returns an empty `payments` list in each invoice entry.
6. **`detail` mode** returns full payment history per invoice.
7. **`daysOverdue = 0`** for invoices whose `dueDate >= asOf` (current, not yet due).
8. **All report data is read-only** — no invoice statuses are changed by these endpoints.

---

## PDF Generators

| Class | Output |
|---|---|
| `ITextCustomerListPdfGenerator` | Multi-column customer directory table |
| `ITextCustomerStatementPdfGenerator` | Formal account statement with header, invoice table, and totals |
| `ITextCustomerAgingPdfGenerator` | Classic aging grid with colored buckets and column totals |
| `ITextCustomerReceivablesPdfGenerator` | Flat receivables table sorted by customer and invoice date |

---

## Error Handling

| HTTP Code | Condition |
|---|---|
| `401 Unauthorized` | Missing or invalid JWT token |
| `404 Not Found` | Customer not found (statement endpoint only) |

---

**Last Updated**: 2026-03-05

**Module Status**: ✅ Implemented
