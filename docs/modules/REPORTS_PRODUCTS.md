# REPORTS_PRODUCTS Module

## Overview

The REPORTS_PRODUCTS module provides **product, stock, and pricing reports** with both JSON and PDF output formats. It covers the full product information surface: a filterable product list, current stock levels, stock movement history, a visual product catalog PDF, a multi-differential price list, and printable product labels.

**Pattern**: Pure query module — no write operations. Follows the same hexagonal structure as other report modules under `reports/`.

**Common behavior**: Most endpoints accept `format=json` (default) or `format=pdf`. The `/catalog` and `/labels` endpoints are **PDF only** — they always return `application/pdf` and do not accept a `format` parameter. PDFs are generated via iText and returned as file downloads.

**Special note**: The `/catalog` endpoint delegates to `GenerateCatalogPdfUseCase` from the `products/pdf/` submodule, not from `reports/products/`.

---

## Module Location

```
src/main/java/com/lexand/lexand_api/reports/products/
├── application/
│   ├── dto/
│   │   └── response/
│   │       ├── ProductListReportResponse.java
│   │       ├── ProductStockReportResponse.java
│   │       ├── ProductMovementsReportResponse.java
│   │       └── PriceListReportResponse.java
│   ├── query/
│   │   ├── ProductListReportQuery.java
│   │   ├── ProductStockReportQuery.java
│   │   ├── ProductMovementsReportQuery.java
│   │   ├── PriceListReportQuery.java
│   │   └── ProductLabelsReportQuery.java
│   └── usecases/
│       ├── ProductListReportUseCase.java
│       ├── ProductStockReportUseCase.java
│       ├── ProductMovementsReportUseCase.java
│       ├── PriceListReportUseCase.java
│       └── ProductLabelsReportUseCase.java
└── infrastructure/
    ├── config/
    │   └── ProductReportConfig.java
    ├── persistence/
    │   ├── JpaProductListReportQuery.java
    │   ├── JpaProductStockReportQuery.java
    │   ├── JpaProductMovementsReportQuery.java
    │   ├── JpaPriceListReportQuery.java
    │   └── JpaProductLabelsReportQuery.java
    ├── services/
    │   ├── ITextProductListPdfGenerator.java
    │   ├── ITextProductStockPdfGenerator.java
    │   ├── ITextProductMovementsPdfGenerator.java
    │   ├── ITextPriceListPdfGenerator.java
    │   └── ITextProductLabelsPdfGenerator.java
    └── web/
        └── ProductReportController.java

# Catalog PDF use case lives in the products module:
src/main/java/com/lexand/lexand_api/products/pdf/application/usecases/GenerateCatalogPdfUseCase.java
```

---

## API Endpoints

### 1. Product List Report

**Endpoint**: `GET /api/reports/products/list`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns the full product catalog grouped by a configurable dimension (category, type, or flat). Optionally filters by category, product type, and active status. Cost price can be hidden for non-admin use cases.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `categoryId` | `UUID` | No | — | Filter by category |
| `productType` | `String` | No | — | Filter by product type (e.g. `BULK`, `UNIT`) |
| `activeOnly` | `boolean` | No | `false` | If `true`, exclude inactive products |
| `groupBy` | `String` | No | `category` | Grouping: `category`, `type`, or `none` |
| `showCostPrice` | `boolean` | No | `false` | If `true`, include `costPrice` in each product row |
| `format` | `json \| pdf` | No | `json` | Output format |

**Response** (200 OK) — `ProductListReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "filters": {
    "categoryId": null,
    "productType": null,
    "activeOnly": false,
    "groupBy": "category"
  },
  "groups": [
    {
      "groupName": "Electronics",
      "groupCode": "ELEC",
      "products": [
        {
          "id": "uuid-001",
          "code": "PROD-001",
          "name": "Smart TV 55\"",
          "categoryName": "Electronics",
          "costPrice": null,
          "salePrice": 850.00,
          "productType": "UNIT",
          "unit": "PCS",
          "bulkQuantity": null,
          "active": true
        }
      ]
    }
  ],
  "totalProducts": 42
}
```

**Response DTO**:
```java
public record ProductListReportResponse(
    LocalDateTime generatedAt,
    Filters filters,
    List<ProductGroup> groups,
    int totalProducts
) {
    public record Filters(UUID categoryId, String productType, boolean activeOnly, String groupBy) {}
    public record ProductGroup(String groupName, String groupCode, List<ProductRow> products) {}
    public record ProductRow(
        UUID id, String code, String name, String categoryName,
        BigDecimal costPrice, BigDecimal salePrice,
        String productType, String unit, Integer bulkQuantity, boolean active
    ) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=product-list-{date}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 2. Product Stock Report

**Endpoint**: `GET /api/reports/products/stock`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns current stock levels per product per warehouse as of a given date. Calculates total cost value and total sale value for each line and as aggregated totals. If `asOf` is omitted, defaults to today.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `asOf` | `Date` (yyyy-MM-dd) | No | Today | Snapshot date for stock levels |
| `warehouseId` | `UUID` | No | — | Filter by warehouse |
| `categoryId` | `UUID` | No | — | Filter by category |
| `productId` | `UUID` | No | — | Filter by specific product |
| `includeZeroStock` | `boolean` | No | `false` | If `true`, include products with 0 stock |
| `format` | `json \| pdf` | No | `json` | Output format |

**Response** (200 OK) — `ProductStockReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "asOf": "2026-03-05",
  "items": [
    {
      "productCode": "PROD-001",
      "productName": "Smart TV 55\"",
      "category": "Electronics",
      "warehouse": "Main Warehouse",
      "unit": "PCS",
      "quantity": 15,
      "costPrice": 600.00,
      "salePrice": 850.00,
      "totalCostValue": 9000.00,
      "totalSaleValue": 12750.00
    }
  ],
  "totals": {
    "totalUnits": 320,
    "totalCostValue": 185000.00,
    "totalSaleValue": 264000.00
  }
}
```

**Response DTO**:
```java
public record ProductStockReportResponse(
    LocalDateTime generatedAt,
    LocalDate asOf,
    List<StockItem> items,
    Totals totals
) {
    public record StockItem(
        String productCode, String productName, String category, String warehouse,
        String unit, int quantity,
        BigDecimal costPrice, BigDecimal salePrice,
        BigDecimal totalCostValue, BigDecimal totalSaleValue
    ) {}
    public record Totals(int totalUnits, BigDecimal totalCostValue, BigDecimal totalSaleValue) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=product-stock-{asOf}.pdf`

**Errors**:
- `400 Bad Request`: Invalid date format for `asOf`
- `401 Unauthorized`: Missing or invalid JWT token

---

### 3. Product Movements Report

**Endpoint**: `GET /api/reports/products/movements`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns all stock movement records within a mandatory date range. Shows before/after stock levels for each movement and summarises total entries, exits, and adjustments.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `from` | `Date` (yyyy-MM-dd) | **Yes** | — | Start date (inclusive) |
| `to` | `Date` (yyyy-MM-dd) | **Yes** | — | End date (inclusive) |
| `warehouseId` | `UUID` | No | — | Filter by warehouse |
| `productId` | `UUID` | No | — | Filter by specific product |
| `movementType` | `String` | No | — | Filter by movement type (e.g. `ENTRY`, `EXIT`, `ADJUSTMENT`) |
| `format` | `json \| pdf` | No | `json` | Output format |

**Response** (200 OK) — `ProductMovementsReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "period": {
    "from": "2026-03-01",
    "to": "2026-03-05"
  },
  "movements": [
    {
      "date": "2026-03-03T14:20:00",
      "movementType": "ENTRY",
      "productCode": "PROD-001",
      "productName": "Smart TV 55\"",
      "warehouse": "Main Warehouse",
      "quantity": 10,
      "previousStock": 5,
      "newStock": 15,
      "reference": "PO-2026-00045",
      "notes": "Purchase order delivery"
    }
  ],
  "totals": {
    "totalEntries": 10,
    "totalExits": 3,
    "totalAdjustments": 1
  }
}
```

**Response DTO**:
```java
public record ProductMovementsReportResponse(
    LocalDateTime generatedAt,
    Period period,
    List<MovementItem> movements,
    Totals totals
) {
    public record Period(LocalDate from, LocalDate to) {}
    public record MovementItem(
        LocalDateTime date, String movementType,
        String productCode, String productName, String warehouse,
        int quantity, int previousStock, int newStock,
        String reference, String notes
    ) {}
    public record Totals(int totalEntries, int totalExits, int totalAdjustments) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=product-movements-{from}.pdf`

**Errors**:
- `400 Bad Request`: Missing `from` or `to`, or invalid date format
- `401 Unauthorized`: Missing or invalid JWT token

---

### 4. Product Catalog (PDF Only)

**Endpoint**: `GET /api/reports/products/catalog`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Generates a formatted product catalog PDF. Optionally applies a differential pricing tier so that displayed prices reflect the chosen differential. Delegates to `GenerateCatalogPdfUseCase` from the `products/pdf/` submodule.

**This endpoint is PDF only — there is no JSON format.**

**Query Parameters**:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `differentialId` | `Long` | No | Apply a differential pricing tier to all displayed prices |

**Response** (200 OK):
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename=product-catalog-{date}.pdf`
- Body: PDF binary

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 5. Price List Report

**Endpoint**: `GET /api/reports/products/price-list`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Returns a price list showing the base sale price alongside calculated prices for up to 5 differential pricing tiers. Each differential price is computed as `salePrice × (1 + percentage / 100)`. Optionally filters by category and can include cost price for internal use.

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `categoryId` | `UUID` | No | — | Filter by category |
| `differentialIds` | `List<Long>` | No | — | Up to 5 differential IDs to include as price columns |
| `showCostPrice` | `boolean` | No | `false` | If `true`, include `costPrice` in each row |
| `format` | `json \| pdf` | No | `json` | Output format |

**Response** (200 OK) — `PriceListReportResponse`:
```json
{
  "generatedAt": "2026-03-05T10:30:00",
  "differentials": [
    { "id": 1, "percentage": 10.00 },
    { "id": 2, "percentage": 15.00 }
  ],
  "products": [
    {
      "code": "PROD-001",
      "name": "Smart TV 55\"",
      "category": "Electronics",
      "unit": "PCS",
      "bulkQuantity": null,
      "costPrice": null,
      "basePrice": 850.00,
      "prices": [
        { "differentialId": 1, "percentage": 10.00, "price": 935.00 },
        { "differentialId": 2, "percentage": 15.00, "price": 977.50 }
      ]
    }
  ]
}
```

**Response DTO**:
```java
public record PriceListReportResponse(
    LocalDateTime generatedAt,
    List<DifferentialInfo> differentials,
    List<ProductPriceRow> products
) {
    public record DifferentialInfo(Long id, BigDecimal percentage) {}
    public record ProductPriceRow(
        String code, String name, String category, String unit, Integer bulkQuantity,
        BigDecimal costPrice, BigDecimal basePrice,
        List<DifferentialPrice> prices
    ) {}
    public record DifferentialPrice(Long differentialId, BigDecimal percentage, BigDecimal price) {}
}
```

**PDF**: Returns `application/pdf` with `Content-Disposition: attachment; filename=price-list-{date}.pdf`

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

### 6. Product Labels (PDF Only)

**Endpoint**: `GET /api/reports/products/labels`

**Authentication**: Required (JWT Bearer Token)

**Roles**: Any authenticated user

**Description**: Generates a PDF sheet of printable product labels. Products are selected by explicit UUID list and/or category filter. Optionally applies a differential pricing tier so label prices reflect the chosen differential. Label size, barcode display, and cost price visibility are configurable.

**This endpoint is PDF only — there is no JSON format.**

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `productIds` | `List<UUID>` | No | — | Explicit list of product UUIDs to include |
| `categoryId` | `UUID` | No | — | Include all products in this category |
| `activeOnly` | `boolean` | No | `true` | If `true`, exclude inactive products |
| `labelSize` | `String` | No | `medium` | Label size: `small`, `medium`, `large` |
| `showBarcode` | `boolean` | No | `true` | If `true`, print a barcode on each label |
| `showCostPrice` | `boolean` | No | `false` | If `true`, include cost price on the label |
| `differentialId` | `Long` | No | — | Apply a differential pricing tier to displayed prices |

**Response** (200 OK):
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename=product-labels-{date}.pdf`
- Body: PDF binary

**Errors**:
- `401 Unauthorized`: Missing or invalid JWT token

---

## Use Cases

| Use Case | Description |
|---|---|
| `ProductListReportUseCase` | Queries all products, groups them by the chosen dimension, and optionally masks cost price |
| `ProductStockReportUseCase` | Queries stock rows as of a given date, computes line totals and aggregate totals |
| `ProductMovementsReportUseCase` | Queries movement history for a date range and tallies entries, exits, and adjustments |
| `GenerateCatalogPdfUseCase` | Generates the visual product catalog PDF (lives in `products/pdf/`) |
| `PriceListReportUseCase` | Builds a price list with up to 5 differential price columns per product |
| `ProductLabelsReportUseCase` | Resolves products and differential percentage, then generates printable label PDF |

All use cases (except `GenerateCatalogPdfUseCase` and `ProductLabelsReportUseCase`) expose `executeJson(...)` and `executePdf(...)` methods.

---

## Query Ports

```java
// ProductListReportQuery.java
public interface ProductListReportQuery {
    List<ProductListRow> findAll(UUID categoryId, String productType, boolean activeOnly, String groupBy);

    record ProductListRow(
        UUID id, String code, String name, String categoryName, String categoryCode,
        BigDecimal costPrice, BigDecimal salePrice,
        String productType, String unit, Integer bulkQuantity, boolean active
    ) {}
}

// ProductStockReportQuery.java
public interface ProductStockReportQuery {
    List<ProductStockRow> findAll(LocalDate asOf, UUID warehouseId, UUID categoryId, UUID productId, boolean includeZeroStock);

    record ProductStockRow(
        String productCode, String productName, String category, String warehouse,
        String unit, int quantity, BigDecimal costPrice, BigDecimal salePrice
    ) {}
}

// ProductMovementsReportQuery.java
public interface ProductMovementsReportQuery {
    List<MovementRow> findAll(LocalDate from, LocalDate to, UUID warehouseId, UUID productId, String movementType);

    record MovementRow(
        LocalDateTime date, String movementType,
        String productCode, String productName, String warehouse,
        int quantity, int previousStock, int newStock,
        String reference, String notes
    ) {}
}

// PriceListReportQuery.java
public interface PriceListReportQuery {
    List<ProductRow> findProducts(UUID categoryId);
    List<DifferentialRow> findDifferentials(List<Long> differentialIds);

    record ProductRow(UUID id, String code, String name, String categoryName,
                      String unit, Integer bulkQuantity, BigDecimal costPrice, BigDecimal salePrice) {}
    record DifferentialRow(Long id, BigDecimal percentage) {}
}

// ProductLabelsReportQuery.java
public interface ProductLabelsReportQuery {
    List<LabelRow> findProducts(List<UUID> productIds, UUID categoryId, boolean activeOnly);

    record LabelRow(UUID id, String code, String name, String unit,
                    Integer bulkQuantity, BigDecimal costPrice, BigDecimal salePrice) {}
}
```

---

## Business Rules

1. **`groupBy` defaults to `category`** when the parameter is absent or set to an invalid value. Valid values are `category`, `type`, and `none`.
2. **`groupBy=none`** places all products in a single group labelled "All Products" with no group code.
3. **`costPrice` is `null` in responses when `showCostPrice=false`** — it is intentionally masked, not missing data.
4. **Stock report `asOf` defaults to today** when the parameter is omitted; the effective date is always included in the response.
5. **Stock totals** (`totalCostValue`, `totalSaleValue`) are computed by the use case as `costPrice × quantity` and `salePrice × quantity` respectively — they are not stored in the database.
6. **`includeZeroStock=false` (default)** excludes products with 0 units on hand; set to `true` to see the full catalog regardless of stock.
7. **Movements `from` and `to` are required** — the endpoint returns `400 Bad Request` if either is missing.
8. **Catalog PDF** delegates to `GenerateCatalogPdfUseCase` (from `products/pdf/`). When `differentialId` is provided, all catalog prices are adjusted by the differential percentage.
9. **Price list limits differentials to 5** — if more than 5 `differentialIds` are supplied, only the first 5 are processed.
10. **Differential price formula**: `price = salePrice × (1 + percentage / 100)`, rounded to 2 decimal places (HALF_UP).
11. **Labels `activeOnly` defaults to `true`** — unlike the product list endpoint (which defaults to `false`).
12. **Labels `showBarcode` defaults to `true`** and `labelSize` defaults to `medium`.
13. **Labels differential pricing** applies the same formula as the price list: `salePrice × (1 + percentage / 100)`.

---

## PDF Generators

| Class | Output |
|---|---|
| `ITextProductListPdfGenerator` | Multi-section PDF with products grouped by chosen dimension |
| `ITextProductStockPdfGenerator` | Tabular PDF of stock levels with totals row |
| `ITextProductMovementsPdfGenerator` | Chronological movement log with entry/exit/adjustment summary |
| `ITextPriceListPdfGenerator` | Price grid with base price and one column per differential |
| `ITextProductLabelsPdfGenerator` | Label sheet PDF with configurable size, barcode, and cost price |

---

## Error Handling

| HTTP Code | Condition |
|---|---|
| `400 Bad Request` | `from` or `to` missing for movements; invalid date format |
| `401 Unauthorized` | Missing or invalid JWT token |

---

**Last Updated**: 2026-03-05

**Module Status**: ✅ Implemented
