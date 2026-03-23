# REPORTS_MERCHANTS Module

## Overview

The REPORTS_MERCHANTS module provides **merchant performance reports** — a directory of all merchants with lifetime stats, a period-based sales summary per merchant, and a detailed commission collections breakdown. All endpoints support JSON and PDF output.

**Pattern**: Pure query module — no write operations. Follows the same hexagonal structure as other report modules under `reports/`.

---

## Module Location

```
src/main/java/com/lexand/lexand_api/reports/merchants/
├── application/
│   ├── dto/
│   │   └── response/
│   │       ├── MerchantListReportResponse.java
│   │       ├── MerchantSalesReportResponse.java
│   │       └── MerchantCollectionsReportResponse.java
│   ├── query/
│   │   ├── MerchantListReportQuery.java
│   │   ├── MerchantSalesReportQuery.java
│   │   └── MerchantCollectionsReportQuery.java
│   └── usecases/
│       ├── MerchantListReportUseCase.java
│       ├── MerchantSalesReportUseCase.java
│       └── MerchantCollectionsReportUseCase.java
└── infrastructure/
    ├── config/
    │   └── MerchantReportConfig.java
    ├── persistence/
    │   ├── JpaMerchantListReportQuery.java
    │   ├── JpaMerchantSalesReportQuery.java
    │   └── JpaMerchantCollectionsReportQuery.java
    ├── services/
    │   ├── ITextMerchantListPdfGenerator.java
    │   ├── ITextMerchantSalesPdfGenerator.java
    │   └── ITextMerchantCollectionsPdfGenerator.java
    └── web/
        └── MerchantReportController.java
```

---

## API Endpoints

### 1. Merchant List Report

**Endpoint**: `GET /api/reports/merchants/list`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a directory of all merchants with their commission percentage and lifetime aggregated sales and collections totals. Supports optional active status filter.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `active` | `Boolean` | No | Filter by active status; omit for all merchants |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `MerchantListReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "totalRecords": 2,
  "merchants": [
    {
      "code": 1,
      "name": "Juan Perez",
      "email": "juan.perez@merchant.com",
      "phone": "+58-424-1234567",
      "commissionPercentage": 5.00,
      "active": true,
      "totalSalesAllTime": 850000.00,
      "totalCollectionsAllTime": 62500.00
    },
    {
      "code": 2,
      "name": "Maria Lopez",
      "email": "maria.lopez@merchant.com",
      "phone": "+58-414-9876543",
      "commissionPercentage": 7.50,
      "active": true,
      "totalSalesAllTime": 520000.00,
      "totalCollectionsAllTime": 39000.00
    }
  ]
}
```

**Response DTO**:
```java
public record MerchantListReportResponse(
    LocalDateTime generatedAt,
    int totalRecords,
    List<MerchantItem> merchants
) {
    public record MerchantItem(
        Long code,
        String name,
        String email,
        String phone,
        BigDecimal commissionPercentage,
        boolean active,
        BigDecimal totalSalesAllTime,
        BigDecimal totalCollectionsAllTime
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=merchant-list-{date}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 2. Merchant Sales Report

**Endpoint**: `GET /api/reports/merchants/sales`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns sales performance per merchant for a date range. Shows invoice count, revenue, amount collected, outstanding balance, and estimated commission earned. Supports optional single-merchant filter.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | `Date` (yyyy-MM-dd) | **Yes** | Start date |
| `to` | `Date` (yyyy-MM-dd) | **Yes** | End date |
| `merchantId` | `UUID` | No | Filter to a single merchant |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `MerchantSalesReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-05"
  },
  "merchants": [
    {
      "merchantCode": 1,
      "merchantName": "Juan Perez",
      "commissionPercentage": 5.00,
      "invoiceCount": 12,
      "totalRevenue": 180000.00,
      "totalPaid": 120000.00,
      "totalBalance": 60000.00,
      "estimatedCommission": 6000.00
    }
  ],
  "totals": {
    "totalInvoices": 12,
    "totalRevenue": 180000.00,
    "totalEstimatedCommission": 6000.00
  }
}
```

**Response DTO**:
```java
public record MerchantSalesReportResponse(
    LocalDateTime generatedAt,
    Period period,
    List<MerchantSalesItem> merchants,
    Totals totals
) {
    public record Period(LocalDate from, LocalDate to) {}
    public record MerchantSalesItem(
        Long merchantCode,
        String merchantName,
        BigDecimal commissionPercentage,
        long invoiceCount,
        BigDecimal totalRevenue,
        BigDecimal totalPaid,
        BigDecimal totalBalance,
        BigDecimal estimatedCommission
    ) {}
    public record Totals(
        long totalInvoices,
        BigDecimal totalRevenue,
        BigDecimal totalEstimatedCommission
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=merchant-sales-{from}.pdf`

**Errors**:
- `400 Bad Request`: Missing `from` or `to`, or invalid date format
- `401 Unauthorized`: Missing or invalid JWT token

---

### 3. Merchant Collections Report

**Endpoint**: `GET /api/reports/merchants/collections`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a detailed breakdown of commissions (collections) earned by merchants for a date range. Groups by merchant, then lists each individual collection record with the associated payment and invoice.

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `from` | `Date` (yyyy-MM-dd) | **Yes** | Start date |
| `to` | `Date` (yyyy-MM-dd) | **Yes** | End date |
| `merchantId` | `UUID` | No | Filter to a single merchant |
| `format` | `json \| pdf` | No | Output format (default: `json`) |

**Response** (200 OK) — `MerchantCollectionsReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-05"
  },
  "merchants": [
    {
      "merchantCode": 1,
      "merchantName": "Juan Perez",
      "commissionPercentage": 5.00,
      "collectionsCount": 3,
      "totalPaymentsAmount": 45000.00,
      "totalCommissionEarned": 2250.00,
      "collections": [
        {
          "collectionDate": "2026-03-02",
          "invoiceNumber": "FAC-2026-00101",
          "customerName": "ABC Company S.A.",
          "paymentAmount": 15000.00,
          "commissionRate": 5.00,
          "commissionAmount": 750.00
        },
        {
          "collectionDate": "2026-03-04",
          "invoiceNumber": "FAC-2026-00115",
          "customerName": "XYZ Corp",
          "paymentAmount": 30000.00,
          "commissionRate": 5.00,
          "commissionAmount": 1500.00
        }
      ]
    }
  ],
  "totals": {
    "totalCollections": 3,
    "totalCommissionEarned": 2250.00
  }
}
```

**Response DTO**:
```java
public record MerchantCollectionsReportResponse(
    LocalDateTime generatedAt,
    Period period,
    List<MerchantCollectionGroup> merchants,
    Totals totals
) {
    public record Period(LocalDate from, LocalDate to) {}
    public record MerchantCollectionGroup(
        Long merchantCode,
        String merchantName,
        BigDecimal commissionPercentage,
        int collectionsCount,
        BigDecimal totalPaymentsAmount,
        BigDecimal totalCommissionEarned,
        List<CollectionItem> collections
    ) {}
    public record CollectionItem(
        LocalDate collectionDate,
        String invoiceNumber,
        String customerName,
        BigDecimal paymentAmount,
        BigDecimal commissionRate,
        BigDecimal commissionAmount
    ) {}
    public record Totals(
        int totalCollections,
        BigDecimal totalCommissionEarned
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=merchant-collections-{from}.pdf`

**Errors**:
- `400 Bad Request`: Missing `from` or `to`, or invalid date format
- `401 Unauthorized`: Missing or invalid JWT token

---

## Use Cases

| Use Case | Description |
|---|---|
| `MerchantListReportUseCase` | Returns merchant directory with lifetime aggregates |
| `MerchantSalesReportUseCase` | Returns per-merchant sales summary for a date range |
| `MerchantCollectionsReportUseCase` | Returns commission collections grouped by merchant |

All use cases expose `executeJson(...)` and `executePdf(...)` methods.

---

## Query Ports

```java
public interface MerchantListReportQuery {
    MerchantListReportResponse query(Boolean active);
}

public interface MerchantSalesReportQuery {
    MerchantSalesReportResponse query(LocalDate from, LocalDate to, UUID merchantId);
}

public interface MerchantCollectionsReportQuery {
    MerchantCollectionsReportResponse query(LocalDate from, LocalDate to, UUID merchantId);
}
```

---

## Business Rules

1. **`estimatedCommission`** is calculated from `invoice.subtotal × merchant.commissionPercentage / 100` — it reflects invoiced amounts, not actual payments.
2. **`totalCommissionEarned`** in collections report reflects actual commissions from real `collections` records — it is based on payment amounts, not invoice amounts.
3. **CANCELLED invoices are excluded** from all sales aggregations.
4. **`totalSalesAllTime`** sums all non-cancelled invoice subtotals ever linked to the merchant.
5. **`totalCollectionsAllTime`** sums all `collections.commission_amount` ever recorded for the merchant.
6. **Collections report is filtered by `collection.collection_date`**, not invoice date.
7. **Sales report is filtered by `invoice.created_at`** (invoice creation date).

---

## PDF Generators

| Class | Output |
|---|---|
| `ITextMerchantListPdfGenerator` | Merchant directory table with commission and lifetime stats |
| `ITextMerchantSalesPdfGenerator` | Sales summary per merchant with totals row |
| `ITextMerchantCollectionsPdfGenerator` | Grouped collection listing with per-merchant sub-totals |

---

## Error Handling

| HTTP Code | Condition |
|---|---|
| `400 Bad Request` | Missing `from` or `to` for sales/collections endpoints; invalid date format |
| `401 Unauthorized` | Missing or invalid JWT token |

---

**Last Updated**: 2026-03-05

**Module Status**: ✅ Implemented
