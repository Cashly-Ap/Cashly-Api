# DASHBOARD Module

## Overview

The DASHBOARD module provides aggregated read-only data for the main Panel de Control page. It is a **reporting module** with no write operations — it queries across `invoices`, `invoice_lines`, `invoice_payments`, `customers`, and `categories` to produce KPIs, chart data, and summary tables.

**Pattern**: Same hexagonal structure as other report modules (`reports/`). Pure query side — no domain entities, no write ports.

---

## Module Location

```
src/main/java/com/lexand/lexand_api/reports/dashboard/
├── application/
│   ├── dto/
│   │   └── response/
│   │       ├── DashboardSummaryResponse.java       # KPI cards
│   │       ├── DashboardChartsResponse.java        # All chart data
│   │       ├── DashboardRecentOrdersResponse.java  # Recent invoices table
│   │       └── DashboardTopProductsResponse.java   # Top products table
│   └── usecases/
│       ├── DashboardSummaryUseCase.java
│       ├── DashboardChartsUseCase.java
│       ├── DashboardRecentOrdersUseCase.java
│       └── DashboardTopProductsUseCase.java
├── domain/
│   └── ports/
│       ├── DashboardSummaryQuery.java
│       ├── DashboardChartsQuery.java
│       ├── DashboardRecentOrdersQuery.java
│       └── DashboardTopProductsQuery.java
└── infrastructure/
    ├── config/
    │   └── DashboardConfig.java
    ├── persistence/
    │   ├── JpaDashboardSummaryQuery.java
    │   ├── JpaDashboardChartsQuery.java
    │   ├── JpaDashboardRecentOrdersQuery.java
    │   └── JpaDashboardTopProductsQuery.java
    └── web/
        └── DashboardController.java
```

---

## `?period=` Parameter

All 4 endpoints share a single query parameter that controls the date range and granularities returned.

| Value | Date Range | % Change Comparison | Chart Granularity |
|---|---|---|---|
| `weekly` | Current week (Mon–Sun) | Previous week (same 7 days) | Day (Mon–Sun, 7 points) |
| `monthly` | Current calendar month | Previous calendar month | Week (4 points) |
| `yearly` | Current calendar year | Previous calendar year | Month (12 points) |
| `all_time` | All records, no date filter | N/A — no % shown | Month (12 points, current year) |

**Default**: `monthly` if omitted.

**Backend responsibility**: resolve `period` → `from` / `to` `LocalDate` range. The frontend never sends explicit dates.

---

## Cross-Module Data Sources

```
DashboardModule (read-only)
    │
    ├── invoices            → KPI totals, recent invoices, ventas mensuales, rendimiento anual
    ├── invoice_lines       → top products, ventas por categoría, productos vendidos KPI
    ├── invoice_payments    → ingresos (cash collected), ingresos semanales
    ├── customers           → nuevos clientes KPI
    └── categories          → category names for donut chart
```

**Excluded from invoices queries**: status = `CANCELLED`

---

## API Endpoints

### 1. KPI Summary Cards

**Endpoint**: `GET /api/dashboard/summary`

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `period` | `weekly \| monthly \| yearly \| all_time` | No | Defaults to `monthly` |

**Response** (200 OK):
```json
{
  "period": "monthly",
  "periodLabel": "Marzo 2026",
  "kpis": {
    "totalSales": {
      "value": 3628250.00,
      "changePercent": 12.5,
      "showChange": true
    },
    "newCustomers": {
      "value": 1245,
      "changePercent": 8.3,
      "showChange": true
    },
    "productsSold": {
      "value": 1033,
      "changePercent": 15.7,
      "showChange": true
    },
    "periodRevenue": {
      "value": 892400.00,
      "changePercent": 23.1,
      "showChange": true
    }
  }
}
```

**Field descriptions**:
| Field | Source | Calculation |
|---|---|---|
| `totalSales` | `invoices.subtotal` | `SUM(subtotal)` for period, excluding CANCELLED |
| `newCustomers` | `customers.created_at` | `COUNT(*)` where `createdAt` in period |
| `productsSold` | `invoice_lines.quantity` | `SUM(quantity)` for invoices in period |
| `periodRevenue` | `invoice_payments.amount` | `SUM(amount)` where `paymentDate` in period |
| `changePercent` | Calculated | `((current - previous) / previous) * 100`, rounded 1 decimal |
| `showChange` | Logic | `false` when `period = all_time` |

---

### 2. Chart Data

**Endpoint**: `GET /api/dashboard/charts`

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `period` | `weekly \| monthly \| yearly \| all_time` | No | Defaults to `monthly` |

**Response** (200 OK):
```json
{
  "period": "monthly",
  "salesTrend": {
    "labels": ["Semana 1", "Semana 2", "Semana 3", "Semana 4"],
    "currentPeriod": [45200.00, 62800.00, 71300.00, 58400.00],
    "previousPeriod": [38900.00, 54100.00, 68200.00, 51700.00],
    "currentLabel": "Marzo 2026",
    "previousLabel": "Febrero 2026"
  },
  "salesByCategory": [
    { "category": "Electrónica",   "total": 1250000.00, "percentage": 34.5 },
    { "category": "Computadoras",  "total": 980000.00,  "percentage": 27.0 },
    { "category": "Accesorios",    "total": 720000.00,  "percentage": 19.8 },
    { "category": "Software",      "total": 430000.00,  "percentage": 11.9 },
    { "category": "Otros",         "total": 248250.00,  "percentage": 6.8 }
  ],
  "revenueByGranularity": {
    "labels": ["Semana 1", "Semana 2", "Semana 3", "Semana 4"],
    "values": [82400.00, 110500.00, 143200.00, 98700.00]
  },
  "annualPerformance": {
    "labels": ["Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"],
    "sales":  [65000, 59000, 80000, 81000, 56000, 95000, 88000, 87000, 92000, 77000, 85000, 97000],
    "target": [70000, 70000, 80000, 80000, 80000, 90000, 90000, 90000, 90000, 90000, 95000, 95000]
  }
}
```

#### `salesTrend` — Ventas (line chart, current vs previous)

| `period` | Labels | Points | Source |
|---|---|---|---|
| `weekly` | Mon–Sun day names | 7 | `SUM(subtotal) GROUP BY DAY` |
| `monthly` | "Semana 1"..."Semana 4" | 4 | `SUM(subtotal) GROUP BY WEEK_OF_MONTH` |
| `yearly` | Month names (Ene–Dic) | 12 | `SUM(subtotal) GROUP BY MONTH` |
| `all_time` | Last 5 years | 5 | `SUM(subtotal) GROUP BY YEAR` |

- `currentPeriod`: current period values
- `previousPeriod`: same granularity, shifted back one period (week/month/year)
- `all_time` has no `previousPeriod` (field is `null`)

#### `salesByCategory` — Ventas por Categoría (donut chart)

- Source: `invoice_lines` JOIN `products` JOIN `categories`
- Filter: invoices in period, status ≠ CANCELLED
- `percentage` = category total / grand total × 100, rounded 1 decimal
- All periods change the filter range, chart type stays donut

#### `revenueByGranularity` — Ingresos (bar chart)

| `period` | Labels | Points | Source |
|---|---|---|---|
| `weekly` | Mon–Sun day names | 7 | `SUM(amount) GROUP BY DAY` from `invoice_payments` |
| `monthly` | "Semana 1"..."Semana 4" | 4 | `SUM(amount) GROUP BY WEEK_OF_MONTH` |
| `yearly` | Month names | 12 | `SUM(amount) GROUP BY MONTH` |
| `all_time` | Last 5 years | 5 | `SUM(amount) GROUP BY YEAR` |

#### `annualPerformance` — Rendimiento Anual (bar + line chart)

| `period` | Labels | Points | Source |
|---|---|---|---|
| `weekly` | Mon–Sun day names | 7 | `SUM(subtotal) GROUP BY DAY` |
| `monthly` | "Semana 1"..."Semana 4" | 4 | `SUM(subtotal) GROUP BY WEEK_OF_MONTH` |
| `yearly` | Month names (Ene–Dic) | 12 | `SUM(subtotal) GROUP BY MONTH` — always current year |
| `all_time` | Month names (Ene–Dic) | 12 | Same as `yearly` |

- `target`: static or configurable monthly target values (can be hardcoded initially)
- `sales`: `SUM(invoices.subtotal)` per label granularity

---

### 3. Recent Invoices Table

**Endpoint**: `GET /api/dashboard/recent-orders`

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `period` | `weekly \| monthly \| yearly \| all_time` | No | Defaults to `monthly` |

**Response** (200 OK):
```json
{
  "period": "monthly",
  "items": [
    {
      "invoiceNumber": "FAC-2026-01234",
      "customerName": "Juan Pérez",
      "firstProductName": "Laptop HP Pavilion",
      "subtotal": 45000.00,
      "status": "PAID",
      "statusLabel": "Completado",
      "createdAt": "2026-03-03T14:22:00"
    },
    {
      "invoiceNumber": "FAC-2026-01235",
      "customerName": "María García",
      "firstProductName": "Mouse Logitech MX",
      "subtotal": 850.00,
      "status": "PENDING",
      "statusLabel": "Pendiente",
      "createdAt": "2026-03-03T11:05:00"
    }
  ]
}
```

**Data sources**:
| Field | Source |
|---|---|
| `invoiceNumber` | `invoices.invoice_number` |
| `customerName` | `invoices.customer_name` (denormalized snapshot) |
| `firstProductName` | `invoice_lines.product_name` WHERE `MIN(line order)` for that invoice |
| `subtotal` | `invoices.subtotal` |
| `status` | `invoices.status` |
| `statusLabel` | Backend maps: `PAID`→"Completado", `PENDING`→"Pendiente", `DELIVERED`→"En Proceso", `PARTIALLY_PAID`→"Pago Parcial", `CANCELLED`→"Cancelado" |
| `createdAt` | `invoices.created_at` |

**Query**: `ORDER BY created_at DESC LIMIT 10`, filtered by period. All statuses included (unlike other endpoints, CANCELLED is shown to give full picture).

---

### 4. Top Products Table

**Endpoint**: `GET /api/dashboard/top-products`

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `period` | `weekly \| monthly \| yearly \| all_time` | No | Defaults to `monthly` |

**Response** (200 OK):
```json
{
  "period": "monthly",
  "items": [
    {
      "productName": "Laptop HP Pavilion",
      "unitsSold": 145,
      "totalRevenue": 652500.00
    },
    {
      "productName": "Mouse Logitech MX",
      "unitsSold": 289,
      "totalRevenue": 245650.00
    },
    {
      "productName": "Teclado Mecánico RGB",
      "unitsSold": 198,
      "totalRevenue": 495000.00
    },
    {
      "productName": "Monitor Samsung 27\"",
      "unitsSold": 167,
      "totalRevenue": 1486300.00
    },
    {
      "productName": "SSD Kingston 1TB",
      "unitsSold": 234,
      "totalRevenue": 748800.00
    }
  ]
}
```

**Data sources**:
| Field | Source |
|---|---|
| `productName` | `invoice_lines.product_name` (snapshot) |
| `unitsSold` | `SUM(invoice_lines.quantity)` |
| `totalRevenue` | `SUM(invoice_lines.line_total)` |

**Query**: `GROUP BY product_id, product_name ORDER BY SUM(line_total) DESC LIMIT 5`. Joins to `invoices` to apply period filter and exclude CANCELLED.

---

## Period → Date Range Resolution

The backend resolves `period` to a concrete `from`/`to` range using `LocalDate.now()`:

```
weekly    → from = start of current ISO week (Monday)
             to   = end of current ISO week (Sunday)

monthly   → from = first day of current month
             to   = last day of current month

yearly    → from = January 1 of current year
             to   = December 31 of current year

all_time  → from = null (no date filter applied)
             to   = null
```

For `% change`, the **previous period** range is:

```
weekly    → same 7-day window shifted back 7 days
monthly   → same calendar month, one month back
yearly    → same calendar year, one year back
all_time  → no comparison (changePercent = null, showChange = false)
```

---

## Response DTOs

### `DashboardSummaryResponse`
```java
public record DashboardSummaryResponse(
    String period,
    String periodLabel,
    KpisDto kpis
) {
    public record KpisDto(
        KpiValueDto totalSales,
        KpiValueDto newCustomers,
        KpiValueDto productsSold,
        KpiValueDto periodRevenue
    ) {}

    public record KpiValueDto(
        BigDecimal value,
        Double changePercent,       // null when all_time
        boolean showChange
    ) {}
}
```

---

### `DashboardChartsResponse`
```java
public record DashboardChartsResponse(
    String period,
    SalesTrendDto salesTrend,
    List<CategorySalesDto> salesByCategory,
    RevenueGranularityDto revenueByGranularity,
    AnnualPerformanceDto annualPerformance
) {
    public record SalesTrendDto(
        List<String> labels,
        List<BigDecimal> currentPeriod,
        List<BigDecimal> previousPeriod,    // null for all_time
        String currentLabel,
        String previousLabel                 // null for all_time
    ) {}

    public record CategorySalesDto(
        String category,
        BigDecimal total,
        Double percentage
    ) {}

    public record RevenueGranularityDto(
        List<String> labels,
        List<BigDecimal> values
    ) {}

    public record AnnualPerformanceDto(
        List<String> labels,
        List<BigDecimal> sales,
        List<BigDecimal> target
    ) {}
}
```

---

### `DashboardRecentOrdersResponse`
```java
public record DashboardRecentOrdersResponse(
    String period,
    List<RecentInvoiceDto> items
) {
    public record RecentInvoiceDto(
        String invoiceNumber,
        String customerName,
        String firstProductName,
        BigDecimal subtotal,
        String status,
        String statusLabel,
        LocalDateTime createdAt
    ) {}
}
```

---

### `DashboardTopProductsResponse`
```java
public record DashboardTopProductsResponse(
    String period,
    List<TopProductDto> items
) {
    public record TopProductDto(
        String productName,
        BigDecimal unitsSold,
        BigDecimal totalRevenue
    ) {}
}
```

---

## Domain Ports (Query Interfaces)

```java
// DashboardSummaryQuery.java
public interface DashboardSummaryQuery {
    DashboardSummaryResponse query(String period);
}

// DashboardChartsQuery.java
public interface DashboardChartsQuery {
    DashboardChartsResponse query(String period);
}

// DashboardRecentOrdersQuery.java
public interface DashboardRecentOrdersQuery {
    DashboardRecentOrdersResponse query(String period);
}

// DashboardTopProductsQuery.java
public interface DashboardTopProductsQuery {
    DashboardTopProductsResponse query(String period);
}
```

---

## Business Rules

1. **CANCELLED invoices are excluded** from all sales/revenue aggregations (KPIs, charts, top products).
2. **CANCELLED invoices ARE included** in "Pedidos Recientes" for full operational visibility.
3. **`all_time` has no % change** — `changePercent` is `null` and `showChange` is `false`.
4. **`all_time` chart granularity** defaults to the same as `yearly` (monthly, 12 points) for visual consistency.
5. **Top products** are ranked by `totalRevenue` descending, not by units sold.
6. **`firstProductName`** in recent invoices is the first line of the invoice (lowest line ID or insertion order).
7. **`periodRevenue`** is based on `invoice_payments.payment_date` (when cash was actually collected), NOT `invoices.created_at`.
8. **`totalSales`** is based on `invoices.subtotal` (invoiced amount, regardless of payment).
9. **`newCustomers`** counts by `customers.created_at`, not first invoice date.
10. **Period defaults to `monthly`** if the parameter is absent or invalid.

---

## Error Handling

| HTTP Code | Condition |
|---|---|
| `400 Bad Request` | Invalid `period` value (not one of: `weekly`, `monthly`, `yearly`, `all_time`) |
| `401 Unauthorized` | Missing or invalid JWT token |

---

## Notes for Implementation

- All 4 JPQL query classes live in `infrastructure/persistence/` and receive a `PeriodRange` value object (internally resolved from the `period` string).
- The `PeriodRange` value object encapsulates `from`, `to`, `granularity`, `previousFrom`, `previousTo` — computed once and reused across all queries.
- No caching is required initially, but the `GET /api/dashboard/charts` endpoint is the heaviest query (4 aggregations) and is a candidate for a short TTL cache in the future.
- The `annualPerformance.target` values can be hardcoded as constants initially. A future `dashboard_targets` table can be added when configurable targets are needed.
