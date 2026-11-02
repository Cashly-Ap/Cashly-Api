# Technical design

## Owners

| Former responsibility | Destination |
|---|---|
| reports/invoices | invoices/reporting |
| reports/orders | orders/reporting |
| reports/quotes | quotes/reporting |
| reports/purchases | purchases/reporting |
| reports/stock and reports/movements | warehouses/reporting |
| Inventory catalog by warehouse | warehouses/reporting |
| Commercial product catalog and media | products/reporting |
| Payment receipt and payment history | payments/reporting |
| Accounts receivable and collections | receivables/reporting |
| PdfBranding and PdfStatusColors | shared/infrastructure/pdf |

Each reporting submodule owns application operations, DTOs, output ports, infrastructure wiring, query adapters, persistence projections/repositories, format generators and HTTP controllers. No artificial reporting domain is introduced. Existing provider-owned query contracts remain with their original business capability; existing adapters preserve neutral snapshots and mappings.

FinancesReportController is split into PaymentReportsController and ReceivablesReportsController. Both retain the existing /api/reports/finance prefix with disjoint method mappings and identical authorization. FinancesReportConfig and InventoryReportConfig are split by owner. Individual document controllers remain within owner/reporting/infrastructure/web. Tests mirror the production ownership; mixed controller/configuration assertions are preserved and extended to both configurations.

## Preservation and verification

The baseline is the preceding clean 3,231-case verified run, including 63 real HTTP startup cases and all seven other MySQL IT. The new change captures original source hashes before moving files. Full clean verification uses isolated suite and inventory databases; development data is never used for the complete suite.

Coverage is compared against the same executable classes after canonicalizing their new packages. Split configurations/controllers use explicit before/after families; all counters remain covered by global layer gates and strict family nonregression. Families crossing owners are excluded symmetrically from per-owner aggregates rather than arbitrarily assigning their old coverage. All remaining classes are reassigned to their new owner before comparing module/layer coverage. No missing global reports group is silently ignored.

## Rollback and delivery

Package relocation and consumer references form one coherent architecture deliverable with the earlier uncommitted modular restructure. Commit only after full verification, then push normally to origin/develop. A rollback reverts code, tests, docs and coverage mapping together; no database rollback is required. No PR creation or deployment is requested.
