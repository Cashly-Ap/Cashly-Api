# REPORTS_SALES Module

## Overview

The REPORTS_SALES module provides **sales analysis reports** with both JSON and PDF output formats. It covers cash payment reconciliation and detailed sales breakdown by configurable grouping dimensions.

**Pattern**: Pure query module — no write operations. Follows the same hexagonal structure as other report modules under `reports/`.

**Common behavior**: All endpoints accept `format=json` (default) or `format=pdf`. The PDF is generated via iText and returned as a file download.

---

## Module Location

```
src/main/java/com/lexand/lexand_api/reports/sales/
├── application/
│   ├── dto/
│   │   └── response/
│   │       ├── CashReconciliationReportResponse.java
│   │       └── SalesBreakdownReportResponse.java
│   ├── query/
│   │   ├── CashReconciliationReportQuery.java
│   │   └── SalesBreakdownReportQuery.java
│   └── usecases/
│       ├── CashReconciliationReportUseCase.java
│       └── SalesBreakdownReportUseCase.java
└── infrastructure/
    ├── config/
    │   └── SalesReportConfig.java
    ├── persistence/
    │   ├── JpaCashReconciliationReportQuery.java
    │   └── JpaSalesBreakdownReportQuery.java
    ├── services/
    │   ├── ITextCashReconciliationPdfGenerator.java
    │   └── ITextSalesBreakdownPdfGenerator.java
    └── web/
        └── SalesReportController.java
```

---

## API Endpoints

### 1. Cash Reconciliation Report

**Endpoint**: `GET /api/reports/sales/cash-reconciliation`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns all invoice payments collected within a date/time range, grouped by payment method. Useful for end-of-day cash reconciliation. Supports an optional user (cashier) filter.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | `DateTime` (yyyy-MM-dd HH:mm:ss) | No | Start of range (defaults to start of today) |
| `to` | `DateTime` (yyyy-MM-dd HH:mm:ss) | No | End of range (defaults to now) |
| `period` | `String` | No | Shortcut: `today`, `week`, `month` — overrides `from`/`to` |
| `userId` | `UUID` | No | Filter by cashier (user who registered the payment) |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `CashReconciliationReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "period": {
    "from": "2026-03-05T00:00:00",
    "to": "2026-03-05T10:30:00"
  },
  "cashier": "Admin User",
  "summary": {
    "byMethod": {
      "CASH":     12500.00,
      "TRANSFER": 45000.00,
      "CHECK":     8000.00
    },
    "totalCollected": 65500.00
  },
  "invoicesCount": 18,
  "payments": [
    {
      "paymentDate": "2026-03-05",
      "invoiceNumber": "FAC-2026-00123",
      "customerName": "ABC Company S.A.",
      "method": "TRANSFER",
      "reference": "TRF-00456",
      "amount": 3500.00
    }
  ]
}
```

**Response DTO**:
```java
public record CashReconciliationReportResponse(
    LocalDateTime generatedAt,
    Period period,
    String cashier,
    Summary summary,
    int invoicesCount,
    List<PaymentItem> payments
) {
    public record Period(LocalDateTime from, LocalDateTime to) {}
    public record Summary(Map<String, BigDecimal> byMethod, BigDecimal totalCollected) {}
    public record PaymentItem(
        LocalDate paymentDate,
        String invoiceNumber,
        String customerName,
        String method,
        String reference,
        BigDecimal amount
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=cash-reconciliation-{date}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token
- `400 Bad Request`: Invalid date format

---

### 2. Sales Breakdown Report

**Endpoint**: `GET /api/reports/sales/breakdown`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a detailed sales breakdown over an explicit date range. Supports flexible grouping (by product, category, customer, merchant, warehouse) with optional dimension filters.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | `Date` (yyyy-MM-dd) | **Yes** | Start date |
| `to` | `Date` (yyyy-MM-dd) | **Yes** | End date |
| `groupBy` | `String` | No | `product`, `category`, `customer`, `merchant`, `warehouse` (default: `product`) |
| `warehouseId` | `UUID` | No | Filter by warehouse |
| `categoryId` | `UUID` | No | Filter by category |
| `productId` | `UUID` | No | Filter by specific product |
| `customerId` | `UUID` | No | Filter by customer |
| `merchantId` | `UUID` | No | Filter by merchant |
| `period` | `String` | No | Informational period label included in output |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `SalesBreakdownReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-05"
  },
  "groupBy": "category",
  "rows": [
    {
      "groupKey": "cat-uuid-001",
      "groupLabel": "Electrónica",
      "category": "Electrónica",
      "quantitySold": 145.00,
      "totalRevenue": 652500.00,
      "totalPaid": 520000.00,
      "totalBalance": 132500.00,
      "invoiceCount": 28
    },
    {
      "groupKey": "cat-uuid-002",
      "groupLabel": "Accesorios",
      "category": "Accesorios",
      "quantitySold": 89.00,
      "totalRevenue": 178000.00,
      "totalPaid": 178000.00,
      "totalBalance": 0.00,
      "invoiceCount": 15
    }
  ],
  "totals": {
    "totalQuantitySold": 234.00,
    "totalRevenue": 830500.00,
    "totalPaid": 698000.00,
    "totalBalance": 132500.00,
    "totalInvoices": 43
  }
}
```

**Response DTO**:
```java
public record SalesBreakdownReportResponse(
    LocalDateTime generatedAt,
    Period period,
    String groupBy,
    List<BreakdownRow> rows,
    Totals totals
) {
    public record Period(LocalDate from, LocalDate to) {}
    public record BreakdownRow(
        String groupKey,
        String groupLabel,
        String category,
        BigDecimal quantitySold,
        BigDecimal totalRevenue,
        BigDecimal totalPaid,
        BigDecimal totalBalance,
        long invoiceCount
    ) {}
    public record Totals(
        BigDecimal totalQuantitySold,
        BigDecimal totalRevenue,
        BigDecimal totalPaid,
        BigDecimal totalBalance,
        long totalInvoices
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=sales-breakdown-{from}.pdf`

**Errors**:
- `400 Bad Request`: Missing `from` or `to`, or invalid date format
- `401 Unauthorized`: Missing or invalid JWT token

---

## Use Cases

| Use Case | Description |
|---|---|
| `CashReconciliationReportUseCase` | Aggregates payments by method for a datetime range; supports cashier filter |
| `SalesBreakdownReportUseCase` | Aggregates invoice line data grouped by a chosen dimension |

Both use cases expose `executeJson(...)` and `executePdf(...)` methods. The PDF variants call the corresponding iText generator.

---

## Query Ports

```java
// CashReconciliationReportQuery.java
public interface CashReconciliationReportQuery {
    CashReconciliationReportResponse query(LocalDateTime from, LocalDateTime to, UUID userId);
}

// SalesBreakdownReportQuery.java
public interface SalesBreakdownReportQuery {
    SalesBreakdownReportResponse query(
        LocalDate from, LocalDate to, String groupBy,
        UUID warehouseId, UUID categoryId, UUID productId,
        UUID customerId, UUID merchantId
    );
}
```

---

## Business Rules

1. **CANCELLED invoices are excluded** from all sales aggregations.
2. **`totalRevenue`** reflects `invoice_lines.line_total` (quantity × unit price after differential), not the raw price.
3. **`totalPaid`** reflects actual payments collected (`invoice_payments.amount`), not the invoiced amount.
4. **`totalBalance`** = `totalRevenue` - `totalPaid` (outstanding receivable).
5. **`groupBy` defaults to `product`** when the parameter is absent or invalid.
6. **Cash reconciliation uses `invoice_payments.payment_date`** (actual collection date), not invoice date.
7. **`cashier` field** is `null` when no `userId` filter is applied (shows "All users").
8. **Date range is inclusive** on both ends.

---

## PDF Generators

| Class | Output |
|---|---|
| `ITextCashReconciliationPdfGenerator` | Single-page or multi-page PDF with payment table and method summary |
| `ITextSalesBreakdownPdfGenerator` | Multi-page PDF with breakdown rows grouped by dimension |

---

## Error Handling

| HTTP Code | Condition |
|---|---|
| `400 Bad Request` | `from`/`to` missing or invalid for breakdown; invalid date format for reconciliation |
| `401 Unauthorized` | Missing or invalid JWT token |

---

**Last Updated**: 2026-03-05

**Module Status**: ✅ Implemented
