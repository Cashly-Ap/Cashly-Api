# Reports Module

## Overview

The Reports module provides a comprehensive set of printable and exportable reports across all major business areas: **Customers**, **Products**, **Sales**, and **Sales Representatives**. All reports are designed to be generated as PDF or structured data exports.

Reports are grouped into four main categories:

| Category | Reports |
|----------|---------|
| **Customers** | Customer List, Customer Statement (Summary & Detailed), Aging Analysis, Accounts Receivable |
| **Products** | Product Labels, Product Lists (by department, brand, general), Product Catalog, Stock Inventory, Inventory Movements, Price List |
| **Sales** | Daily/Weekly Cash Reconciliation, Sales Breakdown (by product, currency, date range, warehouse, department, etc.) |
| **Sales Representatives** | Representative List, Sales by Representative, Collections by Representative |

### Report Format Customization

All report layouts (fonts, sizes, margins, column positions) are **fully customizable** through a dedicated **Tools / Report Designer** section, similar to the Valery ERP approach. Users can adjust:

- Font size and font family
- Column widths and order
- Element vertical/horizontal positioning
- Header and footer content
- Logo placement

---

## 1. Customer Reports

### 1.1 Customer List (`GET /api/reports/customers/list`)

**Description**: Paginated, filterable list of all customers in the system.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `active` | Boolean | No | Filter by active status |
| `type` | String | No | Filter by customer type (`INDIVIDUAL`, `BUSINESS`, `GOVERNMENT`, `NON_PROFIT`) |
| `search` | String | No | Search by legal name, alias, or RIF |
| `format` | String | No | Output format: `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "totalRecords": 3,
  "customers": [
    {
      "code": 1001,
      "legalName": "Acme Corporation C.A.",
      "alias": "ACME Corp",
      "rif": "J-12345678-9",
      "type": "BUSINESS",
      "creditDays": 30,
      "creditLimit": 50000.00,
      "phone": "0212-1234567",
      "email": "contacto@acme.com",
      "address": "Av. Principal, Edificio Acme, Piso 5, Caracas",
      "active": true
    }
  ]
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Customer sequential code |
| 2 | Legal Name | Full legal name |
| 3 | Alias | Short name |
| 4 | RIF | Tax identification number |
| 5 | Type | Customer type |
| 6 | Credit Days | Payment terms |
| 7 | Credit Limit | Maximum credit amount |
| 8 | Phone | Primary phone |
| 9 | Status | Active / Inactive |

**Business Rules**:
- Sorted by customer code ascending by default
- Active customers listed first
- Inactive customers shown only if explicitly requested

---

### 1.2 Customer Account Statement (`GET /api/reports/customers/{customerId}/statement`)

Generates a customer's account statement showing all invoices and their payment status within a date range.

**Authentication**: Required (JWT Bearer Token)

**Path Parameters**:
| Parameter | Type | Description |
|-----------|------|-------------|
| `customerId` | UUID | Customer unique identifier |

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date (`YYYY-MM-DD`) |
| `to` | Date | Yes | End date (`YYYY-MM-DD`) |
| `mode` | String | No | `summary` (default) or `detailed` |
| `format` | String | No | `json` (default), `pdf`, `excel` |

---

#### 1.2.1 Summary Statement (`mode=summary`)

**Description**: Shows one row per invoice with total, payments, and balance.

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "customer": {
    "code": 1001,
    "legalName": "Acme Corporation C.A.",
    "rif": "J-12345678-9",
    "creditDays": 30
  },
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "openingBalance": 5000.00,
  "invoices": [
    {
      "invoiceNumber": "INV-2026-00001",
      "invoiceDate": "2026-01-05",
      "dueDate": "2026-02-04",
      "totalAmount": 3500.00,
      "totalPaid": 3500.00,
      "balance": 0.00,
      "status": "PAID",
      "overdue": false
    },
    {
      "invoiceNumber": "INV-2026-00012",
      "invoiceDate": "2026-02-15",
      "dueDate": "2026-03-17",
      "totalAmount": 7200.00,
      "totalPaid": 2000.00,
      "balance": 5200.00,
      "status": "PARTIAL",
      "overdue": false
    }
  ],
  "totals": {
    "totalInvoiced": 10700.00,
    "totalPaid": 5500.00,
    "closingBalance": 10200.00
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Invoice # | Invoice number |
| 2 | Date | Invoice date |
| 3 | Due Date | Payment due date |
| 4 | Total | Invoice total |
| 5 | Paid | Amount paid |
| 6 | Balance | Outstanding balance |
| 7 | Status | PAID / PARTIAL / DELIVERED / OVERDUE |

---

#### 1.2.2 Detailed Statement (`mode=detailed`)

**Description**: Expands each invoice to show all individual payment transactions.

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "customer": {
    "code": 1001,
    "legalName": "Acme Corporation C.A.",
    "rif": "J-12345678-9",
    "creditDays": 30
  },
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "openingBalance": 5000.00,
  "invoices": [
    {
      "invoiceNumber": "INV-2026-00001",
      "invoiceDate": "2026-01-05",
      "dueDate": "2026-02-04",
      "totalAmount": 3500.00,
      "status": "PAID",
      "payments": [
        {
          "paymentDate": "2026-01-20",
          "method": "TRANSFER",
          "reference": "TRF-001",
          "amount": 1500.00,
          "runningBalance": 2000.00
        },
        {
          "paymentDate": "2026-02-01",
          "method": "CASH",
          "reference": null,
          "amount": 2000.00,
          "runningBalance": 0.00
        }
      ],
      "invoiceBalance": 0.00
    }
  ],
  "totals": {
    "totalInvoiced": 3500.00,
    "totalPaid": 3500.00,
    "closingBalance": 5000.00
  }
}
```

**PDF Columns** (grouped by invoice):
| # | Column | Description |
|---|--------|-------------|
| 1 | Invoice # | Invoice number |
| 2 | Date | Invoice or payment date |
| 3 | Description | Invoice creation or payment detail |
| 4 | Debit | Invoice amount charged |
| 5 | Credit | Payment amount applied |
| 6 | Balance | Running balance after each movement |

**Business Rules**:
- Opening balance = sum of all unpaid invoice balances before `from` date
- Closing balance = opening balance + total invoiced - total paid in period
- Overdue flag = `dueDate < today AND balance > 0`

---

### 1.3 Customer Aging Analysis (`GET /api/reports/customers/aging`)

**Description**: Analyzes outstanding customer balances by aging buckets (current, 1-30 days, 31-60 days, 61-90 days, 90+ days overdue).

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `asOf` | Date | No | Reference date for aging calculation (default: today) |
| `customerId` | UUID | No | Filter for a specific customer |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "asOf": "2026-03-01",
  "customers": [
    {
      "code": 1001,
      "legalName": "Acme Corporation C.A.",
      "rif": "J-12345678-9",
      "current": 7200.00,
      "days1to30": 0.00,
      "days31to60": 3500.00,
      "days61to90": 0.00,
      "over90days": 1200.00,
      "totalBalance": 11900.00
    }
  ],
  "totals": {
    "current": 7200.00,
    "days1to30": 0.00,
    "days31to60": 3500.00,
    "days61to90": 0.00,
    "over90days": 1200.00,
    "grandTotal": 11900.00
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Customer code |
| 2 | Customer | Legal name |
| 3 | RIF | Tax ID |
| 4 | Current | Balance not yet due |
| 5 | 1-30 Days | 1 to 30 days overdue |
| 6 | 31-60 Days | 31 to 60 days overdue |
| 7 | 61-90 Days | 61 to 90 days overdue |
| 8 | 90+ Days | More than 90 days overdue |
| 9 | Total | Total outstanding balance |

**Business Rules**:
- Aging buckets based on `dueDate` vs `asOf` date
- Only invoices with balance > 0 are included
- `current` = invoices where `dueDate >= asOf`

---

### 1.4 Accounts Receivable (`GET /api/reports/customers/receivables`)

**Description**: Consolidated list of all outstanding invoice balances across all customers. Shows total amount owed to the company.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `asOf` | Date | No | Balance as of this date (default: today) |
| `overdueOnly` | Boolean | No | Show only overdue invoices |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "asOf": "2026-03-01",
  "totalReceivable": 45200.00,
  "totalOverdue": 12300.00,
  "invoices": [
    {
      "invoiceNumber": "INV-2026-00012",
      "invoiceDate": "2026-02-15",
      "dueDate": "2026-03-17",
      "customerCode": 1001,
      "customerName": "Acme Corporation C.A.",
      "totalAmount": 7200.00,
      "totalPaid": 2000.00,
      "balance": 5200.00,
      "daysOverdue": 0,
      "overdue": false
    }
  ]
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Invoice # | Invoice number |
| 2 | Date | Invoice date |
| 3 | Due Date | Payment due date |
| 4 | Customer | Customer name |
| 5 | Total | Invoice total |
| 6 | Paid | Total paid |
| 7 | Balance | Outstanding balance |
| 8 | Days Overdue | Days past due date (0 if not overdue) |

---

## 2. Product Reports

### 2.1 Product Labels (`GET /api/reports/products/labels`)

**Description**: Generates printable labels for products. Each label includes product name, code, barcode (optional), price, and unit of measure. Designed for shelf labels or sticker printing.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `productIds` | List\<Long\> | No | Specific product IDs to print labels for |
| `categoryId` | Long | No | Print labels for all products in a category |
| `activeOnly` | Boolean | No | Only active products (default: `true`) |
| `labelSize` | String | No | `small`, `medium` (default), `large` |
| `showBarcode` | Boolean | No | Include barcode (default: `true`) |
| `showCostPrice` | Boolean | No | Include cost price on label (default: `false`) |
| `differentialId` | Long | No | Apply differential pricing for displayed price |
| `format` | String | No | `pdf` only |

**Label Content**:
```
┌─────────────────────────┐
│  [COMPANY LOGO]          │
│  PRD-00001               │
│  ─────────────────────── │
│  Laptop Dell XPS 15      │
│  ─────────────────────── │
│  Price:  $1,200.00       │
│  Unit:   UNIT            │
│  ─────────────────────── │
│  ||||||||||||||||||||    │
│  PRD-00001               │
└─────────────────────────┘
```

**Business Rules**:
- Labels are grouped in a grid layout optimized for standard label sheets
- If `differentialId` is provided, the displayed price uses the differential formula
- `showCostPrice` is restricted to ADMIN role only
- Barcode encodes the product code (Code-128 format)

---

### 2.2 Product List (`GET /api/reports/products/list`)

**Description**: Generates a product list with filters for department (category), brand, or a general full listing.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryId` | Long | No | Filter by category (department) |
| `productType` | String | No | `SIMPLE`, `COMPOSITE` |
| `activeOnly` | Boolean | No | Only active products (default: `true`) |
| `groupBy` | String | No | `category` (default), `type`, `none` |
| `showCostPrice` | Boolean | No | Show cost price (ADMIN only, default: `false`) |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "filters": {
    "categoryId": null,
    "productType": null,
    "activeOnly": true,
    "groupBy": "category"
  },
  "groups": [
    {
      "groupName": "Electronics",
      "groupCode": "CAT-00001",
      "products": [
        {
          "id": 1,
          "code": "PRD-00001",
          "name": "Laptop Dell XPS 15",
          "category": "Electronics",
          "costPrice": 800.00,
          "salePrice": 1200.00,
          "unit": "UNIT",
          "bulkQuantity": null,
          "type": "SIMPLE",
          "active": true
        }
      ],
      "subtotal": 1200.00
    }
  ],
  "totalProducts": 1
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Product code |
| 2 | Name | Product name |
| 3 | Category | Category / Department |
| 4 | Unit | Unit of measure |
| 5 | Cost Price | Cost price (ADMIN only) |
| 6 | Sale Price | Sale price |
| 7 | Type | SIMPLE / COMPOSITE |
| 8 | Status | Active / Inactive |

---

### 2.3 Product Catalog (`GET /api/reports/products/catalog`)

**Description**: Professional product catalog for customer distribution. Does **not** include stock, cost prices, or internal data. Supports differential pricing for custom price lists.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `differentialId` | Long | No | Differential to apply for price calculation |
| `categoryId` | Long | No | Limit catalog to a specific category |
| `format` | String | No | `pdf` (default), `excel` |

**Price Calculation**:
```
catalogPrice = salePrice × (1 + differential.percentage / 100)
```

**PDF Content**:
- Company header with logo
- Generation date
- Columns: Code, Name, Category, Price, Unit
- Grouped by category
- Page numbers
- Professional styling with alternating row colors

> **Note**: This report is already implemented. See [PRODUCTS.md](./modules/PRODUCTS.md) for full technical details (`GET /api/products/pdf/catalog`).

---

### 2.4 Stock Inventory (`GET /api/reports/products/stock`)

**Description**: Current stock levels for all products as of a given date, with their values at cost price and sale price. Can be filtered by warehouse or product.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `asOf` | Date | No | Stock as of this date (default: today) |
| `warehouseId` | Long | No | Filter by warehouse |
| `categoryId` | Long | No | Filter by category |
| `productId` | Long | No | Filter for a specific product |
| `includeZeroStock` | Boolean | No | Include products with 0 stock (default: `false`) |
| `showPrices` | String | No | `sale` (default), `cost`, `both` |
| `differentialId` | Long | No | Apply differential for additional price column |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "asOf": "2026-03-01",
  "warehouse": null,
  "items": [
    {
      "productCode": "PRD-00001",
      "productName": "Laptop Dell XPS 15",
      "category": "Electronics",
      "unit": "UNIT",
      "warehouse": "Main Warehouse",
      "quantity": 25,
      "costPrice": 800.00,
      "salePrice": 1200.00,
      "totalCostValue": 20000.00,
      "totalSaleValue": 30000.00
    }
  ],
  "totals": {
    "totalUnits": 25,
    "totalCostValue": 20000.00,
    "totalSaleValue": 30000.00
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Product code |
| 2 | Name | Product name |
| 3 | Category | Product category |
| 4 | Warehouse | Warehouse name |
| 5 | Unit | Unit of measure |
| 6 | Quantity | Current stock |
| 7 | Cost Price | Unit cost price |
| 8 | Sale Price | Unit sale price |
| 9 | Total Cost | quantity × cost price |
| 10 | Total Sale | quantity × sale price |

**Business Rules**:
- Products with multiple warehouses show one row per warehouse
- If `warehouseId` is provided, only that warehouse is shown
- Zero-stock products excluded by default
- All prices include optional differential column

---

### 2.5 Inventory Movements (`GET /api/reports/products/movements`)

**Description**: Audit trail of all stock movements (entries, exits, adjustments, transfers) within a date range.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date (`YYYY-MM-DD`) |
| `to` | Date | Yes | End date (`YYYY-MM-DD`) |
| `warehouseId` | Long | No | Filter by warehouse |
| `productId` | Long | No | Filter by product |
| `movementType` | String | No | `ENTRY`, `EXIT`, `ADJUSTMENT`, `TRANSFER` |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "movements": [
    {
      "date": "2026-01-10T09:30:00",
      "type": "ENTRY",
      "productCode": "PRD-00001",
      "productName": "Laptop Dell XPS 15",
      "warehouse": "Main Warehouse",
      "quantity": 50,
      "previousStock": 10,
      "newStock": 60,
      "reference": "PO-2026-001",
      "notes": "Purchase order delivery",
      "registeredBy": "admin@lexand.com"
    },
    {
      "date": "2026-02-05T14:00:00",
      "type": "EXIT",
      "productCode": "PRD-00001",
      "productName": "Laptop Dell XPS 15",
      "warehouse": "Main Warehouse",
      "quantity": 5,
      "previousStock": 60,
      "newStock": 55,
      "reference": "INV-2026-00005",
      "notes": "Invoice stock deduction",
      "registeredBy": "system"
    }
  ],
  "totals": {
    "totalEntries": 50,
    "totalExits": 5,
    "totalAdjustments": 0
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Date | Movement date and time |
| 2 | Type | ENTRY / EXIT / ADJUSTMENT / TRANSFER |
| 3 | Product | Product code and name |
| 4 | Warehouse | Warehouse name |
| 5 | Quantity | Units moved (+ or -) |
| 6 | Previous Stock | Stock before movement |
| 7 | New Stock | Stock after movement |
| 8 | Reference | Invoice, order, or PO reference |
| 9 | Notes | Reason or notes |

---

### 2.6 Price List (`GET /api/reports/products/price-list`)

**Description**: Generates a price list for products. Supports multiple differentials to display multiple price columns simultaneously (e.g., Retail Price, Wholesale Price, VIP Price).

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryId` | Long | No | Filter by category |
| `differentialIds` | List\<Long\> | No | Up to 5 differentials for additional price columns |
| `showCostPrice` | Boolean | No | Show cost price (ADMIN only) |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "differentials": [
    { "id": 1, "name": "Retail", "percentage": 25 },
    { "id": 2, "name": "Wholesale", "percentage": 10 }
  ],
  "products": [
    {
      "code": "PRD-00001",
      "name": "Laptop Dell XPS 15",
      "category": "Electronics",
      "unit": "UNIT",
      "basePrice": 1200.00,
      "prices": [
        { "differentialName": "Retail", "price": 1500.00 },
        { "differentialName": "Wholesale", "price": 1320.00 }
      ]
    }
  ]
}
```

**PDF Columns** (dynamic based on differentials requested):
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Product code |
| 2 | Name | Product name |
| 3 | Category | Product category |
| 4 | Unit | Unit of measure |
| 5 | Base Price | Sale price without differential |
| 6+ | [Differential Name] | Price with differential applied |

---

## 3. Sales Reports

### 3.1 Daily/Weekly Cash Reconciliation (`GET /api/reports/sales/cash-reconciliation`)

**Description**: Summary of all payments received in a given period, grouped by payment method. Used for daily or weekly cash drawer closing. Shows total cash, transfers, and other payment methods collected.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | DateTime | Yes | Period start (`YYYY-MM-DDTHH:mm:ss`) |
| `to` | DateTime | Yes | Period end (`YYYY-MM-DDTHH:mm:ss`) |
| `period` | String | No | Shortcut: `today`, `yesterday`, `this_week`, `last_week` |
| `userId` | UUID | No | Filter by cashier/user who registered the payments |
| `format` | String | No | `json` (default), `pdf` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T18:00:00",
  "period": {
    "from": "2026-03-01T00:00:00",
    "to": "2026-03-01T23:59:59"
  },
  "cashier": null,
  "summary": {
    "CASH": 12500.00,
    "TRANSFER": 34200.00,
    "CHECK": 0.00,
    "OTHER": 500.00,
    "totalCollected": 47200.00
  },
  "invoicesCount": 18,
  "payments": [
    {
      "paymentDate": "2026-03-01T09:15:00",
      "invoiceNumber": "INV-2026-00045",
      "customerName": "Acme Corporation C.A.",
      "method": "TRANSFER",
      "reference": "TRF-00821",
      "amount": 3500.00
    }
  ]
}
```

**PDF Layout**:
- **Header**: Period, generated by, generation date/time
- **Summary Box**: Total per payment method + grand total
- **Detail Table**: All individual payments in the period
- **Footer**: Cashier signature field

**Business Rules**:
- `period` shortcut takes precedence over `from`/`to` if both provided
- Only `DELIVERED`, `PARTIAL`, and `PAID` invoice payments are included
- Cancelled invoices are excluded

---

### 3.2 Sales Breakdown (`GET /api/reports/sales/breakdown`)

**Description**: Detailed sales analysis with flexible grouping and filtering. Supports multiple dimensions: by product, by currency, by date (daily/weekly/monthly/annual), by warehouse, by department, by color, and custom date ranges.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date (`YYYY-MM-DD`) |
| `to` | Date | Yes | End date (`YYYY-MM-DD`) |
| `groupBy` | String | No | `product`, `category`, `warehouse`, `day`, `week`, `month`, `year`, `currency`, `customer` |
| `warehouseId` | Long | No | Filter by warehouse (depot) |
| `categoryId` | Long | No | Filter by category / department |
| `productId` | Long | No | Filter by specific product |
| `customerId` | UUID | No | Filter by customer |
| `merchantId` | UUID | No | Filter by sales representative |
| `period` | String | No | `daily`, `weekly`, `monthly`, `annual` (sets groupBy automatically) |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON, `groupBy=product`):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "groupBy": "product",
  "rows": [
    {
      "groupKey": "PRD-00001",
      "groupLabel": "Laptop Dell XPS 15",
      "category": "Electronics",
      "quantitySold": 42,
      "totalRevenue": 50400.00,
      "averagePrice": 1200.00,
      "invoiceCount": 15
    },
    {
      "groupKey": "PRD-00002",
      "groupLabel": "Gift Basket Premium",
      "category": "Gifts",
      "quantitySold": 8,
      "totalRevenue": 600.00,
      "averagePrice": 75.00,
      "invoiceCount": 5
    }
  ],
  "totals": {
    "totalQuantitySold": 50,
    "totalRevenue": 51000.00,
    "totalInvoices": 20
  }
}
```

**Response** (200 OK - JSON, `groupBy=month`):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "groupBy": "month",
  "rows": [
    {
      "groupKey": "2026-01",
      "groupLabel": "January 2026",
      "invoiceCount": 45,
      "totalRevenue": 125000.00,
      "totalPaid": 98000.00,
      "totalBalance": 27000.00
    },
    {
      "groupKey": "2026-02",
      "groupLabel": "February 2026",
      "invoiceCount": 38,
      "totalRevenue": 110000.00,
      "totalPaid": 110000.00,
      "totalBalance": 0.00
    }
  ],
  "totals": {
    "totalInvoices": 83,
    "totalRevenue": 235000.00,
    "totalPaid": 208000.00,
    "totalBalance": 27000.00
  }
}
```

**PDF Columns** (vary by `groupBy`):

| groupBy | Columns |
|---------|---------|
| `product` | Code, Name, Category, Qty Sold, Revenue, Avg Price, # Invoices |
| `category` | Category, Qty Sold, Revenue, # Invoices |
| `warehouse` | Warehouse, Qty Sold, Revenue, # Invoices |
| `day` | Date, # Invoices, Revenue, Paid, Balance |
| `week` | Week, # Invoices, Revenue, Paid, Balance |
| `month` | Month, # Invoices, Revenue, Paid, Balance |
| `year` | Year, # Invoices, Revenue, Paid, Balance |
| `customer` | Code, Customer, # Invoices, Revenue, Balance |
| `currency` | Currency, # Invoices, Revenue |

**Business Rules**:
- Only `DELIVERED`, `PARTIAL`, and `PAID` invoices are included
- Cancelled and `PENDING` invoices are excluded from revenue figures
- All monetary amounts in the company's base currency

---

## 4. Sales Representative Reports

### 4.1 Representative List (`GET /api/reports/merchants/list`)

**Description**: List of all registered sales representatives (merchants) with their commission rates and contact information.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `active` | Boolean | No | Filter by active status |
| `format` | String | No | `json` (default), `pdf` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "totalRecords": 5,
  "merchants": [
    {
      "code": 1,
      "name": "Juan Pérez",
      "email": "juan.perez@lexand.com",
      "phone": "0414-1234567",
      "commissionPercentage": 15.00,
      "active": true,
      "totalSalesAllTime": 450000.00,
      "totalCollectionsAllTime": 67500.00
    }
  ]
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Merchant sequential code |
| 2 | Name | Full name |
| 3 | Email | Contact email |
| 4 | Phone | Contact phone |
| 5 | Commission % | Commission percentage |
| 6 | Status | Active / Inactive |

---

### 4.2 Sales by Representative (`GET /api/reports/merchants/sales`)

**Description**: Sales volume and revenue breakdown per sales representative within a date range.

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date (`YYYY-MM-DD`) |
| `to` | Date | Yes | End date (`YYYY-MM-DD`) |
| `merchantId` | UUID | No | Filter for a specific representative |
| `groupBy` | String | No | `merchant` (default), `month`, `customer` |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "merchants": [
    {
      "merchantCode": 1,
      "merchantName": "Juan Pérez",
      "commissionPercentage": 15.00,
      "invoiceCount": 28,
      "totalRevenue": 185000.00,
      "totalPaid": 160000.00,
      "totalBalance": 25000.00,
      "estimatedCommission": 24000.00
    }
  ],
  "totals": {
    "totalInvoices": 28,
    "totalRevenue": 185000.00,
    "totalEstimatedCommission": 24000.00
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Code | Merchant code |
| 2 | Name | Merchant name |
| 3 | Commission % | Commission rate |
| 4 | # Invoices | Number of invoices |
| 5 | Total Revenue | Total invoiced amount |
| 6 | Total Paid | Amount paid by customers |
| 7 | Balance | Outstanding amount |
| 8 | Est. Commission | Estimated commission earned |

**Business Rules**:
- `estimatedCommission = totalPaid × (commissionPercentage / 100)`
- Only invoices with `merchantId` assigned are included
- Invoices without a merchant are NOT included in this report

---

### 4.3 Collections by Representative (`GET /api/reports/merchants/collections`)

**Description**: Actual commission collections registered for each sales representative. Shows confirmed earned commissions based on payments received (not estimated).

**Authentication**: Required (JWT Bearer Token)

**Query Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `from` | Date | Yes | Start date (`YYYY-MM-DD`) |
| `to` | Date | Yes | End date (`YYYY-MM-DD`) |
| `merchantId` | UUID | No | Filter for a specific representative |
| `status` | String | No | `PENDING`, `PAID` (default: both) |
| `format` | String | No | `json` (default), `pdf`, `excel` |

**Response** (200 OK - JSON):
```json
{
  "generatedAt": "2026-03-01T10:00:00",
  "period": {
    "from": "2026-01-01",
    "to": "2026-03-01"
  },
  "merchants": [
    {
      "merchantCode": 1,
      "merchantName": "Juan Pérez",
      "commissionPercentage": 15.00,
      "collectionsCount": 12,
      "totalPaymentsAmount": 95000.00,
      "totalCommissionEarned": 14250.00,
      "totalCommissionPaid": 10000.00,
      "totalCommissionPending": 4250.00,
      "collections": [
        {
          "collectionDate": "2026-01-20",
          "invoiceNumber": "INV-2026-00001",
          "customerName": "Acme Corporation C.A.",
          "paymentAmount": 3500.00,
          "commissionRate": 15.00,
          "commissionAmount": 525.00,
          "status": "PAID",
          "settledDate": "2026-02-01"
        }
      ]
    }
  ],
  "totals": {
    "totalCollections": 12,
    "totalCommissionEarned": 14250.00,
    "totalCommissionPaid": 10000.00,
    "totalCommissionPending": 4250.00
  }
}
```

**PDF Columns**:
| # | Column | Description |
|---|--------|-------------|
| 1 | Date | Collection date |
| 2 | Invoice # | Related invoice number |
| 3 | Customer | Customer name |
| 4 | Payment Amount | Payment received from customer |
| 5 | Commission % | Commission rate at time of collection |
| 6 | Commission Amount | Earned commission (`payment × rate`) |
| 7 | Status | PENDING / PAID |
| 8 | Settled Date | Date commission was paid to merchant |

---

## 5. Implementation Task List

The following tasks reflect the current implementation status of the Reports module.

---

### Phase 1 — Foundation ✅ COMPLETE

- [x] **TASK-RPT-001** — Create `reports` module package structure  
  `src/main/java/com/lexand/lexand_api/reports/`  
  Submodules: `customers`, `products`, `sales`, `merchants`, `shared`

- [x] **TASK-RPT-002** — Create `ReportFormat` enum (`JSON`, `PDF`)

- [x] **TASK-RPT-003** — Create `PeriodFilter` value object (from, to date validation)

- [x] **TASK-RPT-005** — Create `PdfReportGenerator` port interface and iText 8 implementation

---

### Phase 2 — Customer Reports ✅ COMPLETE

- [x] **TASK-RPT-011** — Implement `CustomerListReport` use case and controller  
  `GET /api/reports/customers/list`

- [x] **TASK-RPT-012** — Implement `CustomerStatementSummaryReport` use case  
  `GET /api/reports/customers/{customerId}/statement?mode=summary`

- [x] **TASK-RPT-013** — Implement `CustomerStatementDetailedReport` use case  
  `GET /api/reports/customers/{customerId}/statement?mode=detailed`

- [x] **TASK-RPT-014** — Implement `CustomerAgingReport` use case  
  `GET /api/reports/customers/aging`

- [x] **TASK-RPT-015** — Implement `AccountsReceivableReport` use case  
  `GET /api/reports/customers/receivables`

- [x] **TASK-RPT-016** — Add PDF rendering for all customer reports

---

### Phase 3 — Product Reports ✅ COMPLETE

- [x] **TASK-RPT-018** — Implement `ProductLabelsReport` use case  
  `GET /api/reports/products/labels`

- [x] **TASK-RPT-019** — Implement `ProductListReport` use case  
  `GET /api/reports/products/list`

- [x] **TASK-RPT-020** — Implement `ProductCatalogReport` use case  
  `GET /api/reports/products/catalog`

- [x] **TASK-RPT-021** — Implement `StockInventoryReport` use case  
  `GET /api/reports/products/stock`

- [x] **TASK-RPT-022** — Implement `InventoryMovementsReport` use case  
  `GET /api/reports/products/movements`

- [x] **TASK-RPT-023** — Implement `PriceListReport` use case  
  `GET /api/reports/products/price-list`

- [x] **TASK-RPT-024** — Add PDF rendering for all product reports

---

### Phase 4 — Sales Reports ✅ COMPLETE

- [x] **TASK-RPT-026** — Implement `CashReconciliationReport` use case  
  `GET /api/reports/sales/cash-reconciliation`

- [x] **TASK-RPT-027** — Implement `SalesBreakdownReport` use case  
  `GET /api/reports/sales/breakdown`

- [x] **TASK-RPT-030** — Add PDF rendering for all sales reports

---

### Phase 5 — Sales Representative Reports ✅ COMPLETE

- [x] **TASK-RPT-032** — Implement `MerchantListReport` use case  
  `GET /api/reports/merchants/list`

- [x] **TASK-RPT-033** — Implement `SalesByMerchantReport` use case  
  `GET /api/reports/merchants/sales`

- [x] **TASK-RPT-034** — Implement `CollectionsByMerchantReport` use case  
  `GET /api/reports/merchants/collections`

- [x] **TASK-RPT-035** — Add PDF rendering for all merchant reports

---

### Phase 6 — Testing & Polish ✅ COMPLETE

- [x] **TASK-RPT-042** — Unit tests for all report use cases

- [x] **TASK-RPT-043** — Integration tests for all report controllers

---

## 6. Dependencies & Cross-Module Requirements

The Reports module reads data from multiple existing modules. The following ports/repositories must be accessible:

| Data Needed | Source Module | Key Queries |
|------------|---------------|-------------|
| Customer info | `customers` | Find all, find by ID, search |
| Invoice data | `invoices` | By customer, by date range, by status |
| Invoice payments | `invoices` | Payment history per invoice |
| Invoice lines | `invoices` | Product details, quantities, prices |
| Product info | `products` | Find all, find by category |
| Stock levels | `warehouse` | Current stock per product/warehouse |
| Stock movements | `warehouse` | Movements by date range and type |
| Categories | `products` | Find all categories |
| Merchants | `merchants` | Find all merchants |
| Collections | `collections` | Collections by merchant, by date |
| Differentials | `differential` | Find by ID for price calculations |

> All queries should use **read-only repository ports** (`QueryRepository`) — reports never write data.

---

## 7. Technical Notes

- **PDF Generation**: iText 8.0.5 (already in project)
- **Barcode (Labels)**: iText Barcode module (included in iText 8)
- **Report Auth**: All report endpoints require JWT authentication
- **Role Restrictions**:
  - `showCostPrice` fields: ADMIN role only
  - Merchant-scoped reports: MERCHANT role sees only their own data
  - All other reports: ADMIN and standard authenticated users
- **Format Detection**: The `format` query param controls output (`json`, `pdf`)

---

*Last Updated: 2026-03-03*  
*Status: ✅ Complete — All 15 reports implemented with unit and integration tests*
