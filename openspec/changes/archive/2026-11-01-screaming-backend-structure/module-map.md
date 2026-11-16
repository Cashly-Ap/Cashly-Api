# Whole-backend ownership and exact mapping

Status: proposed only. The manifests are exhaustive and authoritative; full trees are in [trees/before.md](trees/before.md) and [trees/after.md](trees/after.md). Every existing production source has a single move or retain disposition. No class is omitted because its package is already suitable.

## All owners

| Owner | Production Java | Java test files | Target organization | Responsibility |
|---|---:|---:|---|---|
| `auth` | 72 | 45 | authentication, token, user | Preserve existing capabilities; login/refresh/logout and user operations group beneath application. |
| `bootstrap` | 1 | 4 | CashlyApplication unchanged | Retain the Spring application root and component scanning reach. |
| `cash_register` | 67 | 32 | sessions, reporting | Session domain includes cash movements; exports have application/infrastructure only. |
| `categories` | 26 | 12 | single aggregate; operation packages | CRUD plus usage/directory queries; preserve soft-deactivation semantics. |
| `credit_notes` | 29 | 15 | single aggregate; operation packages | Keep note creation/deletion and existing financial effects together. |
| `customers` | 130 | 62 | profile, balance, credit, warehouse_access | Customer owns contacts/configured terms; balance owns available funds; credit is a read facade. |
| `dashboard` | 8 | 3 | read capability; summary application | Keep existing combined dashboard reads and their query gateway. |
| `debit_notes` | 27 | 13 | single aggregate; operation packages | Keep note lifecycle and current financial effects together. |
| `infrastructure` | 6 | 1 | global web and config | Keep exception handling, CORS, OpenAPI, multipart and JPA auditing global. |
| `invoices` | 106 | 43 | single aggregate; operation packages | Keep invoice lines/returns/payment states together; application composes foreign facts. |
| `notifications` | 19 | 2 | single aggregate; application services/quotes | Preserve recipient ownership, reference metadata and after-commit delivery. |
| `orders` | 72 | 26 | single aggregate; operation packages | Keep document lines, allocations and historical categories with Order. |
| `payment_methods` | 22 | 14 | single aggregate; operation packages | Create/update/delete/toggle and details/usage capabilities. |
| `payments` | 82 | 36 | collection, credit | Collection owns payment/allocation lifecycle; credit owns current financial policy and read composition. |
| `price_types` | 25 | 10 | single aggregate; operation packages | CRUD and usage queries; preserve current guarded physical deletion. |
| `products` | 68 | 32 | single aggregate; operation packages | Keep prices/components/photos in Product; application groups photos, stock views and transaction views. |
| `purchases` | 57 | 19 | single aggregate; operation packages | Keep purchase lines/allocations and confirmation/cancellation lifecycle together. |
| `quotes` | 71 | 31 | single aggregate; operation packages | Keep quote lines/allocations and conversion links with Quote. |
| `receivables` | 22 | 11 | read capability; operation packages | No new domain; group summary, customer debt, invoices and payment history reads. |
| `reports` | 148 | 79 | finance, inventory, invoices, movements, orders, purchases, quotes, stock | Preserve existing report families and move interface/implementation pairs together by operation. |
| `returns` | 45 | 23 | single aggregate; operation packages | Keep lines and cancelled payment allocation snapshot with Return. |
| `shared` | 9 | 4 | existing storage capability and neutral pagination | Preserve storage APIs, neutral PageResponse and infrastructure page conversion. |
| `suppliers` | 50 | 13 | single aggregate; operation packages | Keep supplier contacts and billing-address rules with Supplier. |
| `warehouses` | 86 | 39 | management, stock | Stock owns quantities, reservations, movement history and document inventory effects. |

Global test-only owners also remain represented: architecture, config and infrastructure. Test counts include helpers and the empty application test; they are not executed-test counts.

## Inventory contract

- [source-moves.json](source-moves.json): all 1,248 production Java classes, their old/new paths, owners, reasons and SHA-256 source snapshots.
- [test-moves.json](test-moves.json): all 573 Java test/helper sources with unique destinations and reasons.
- [resource-inventory.json](resource-inventory.json): 30 production resources plus nine noncompiled test backups and two test resources. Preserve every resource/backup byte.
- [inventory-summary.json](inventory-summary.json): module counts and explicit move/retain totals.
- [dependency-baseline.json](dependency-baseline.json): existing cross-module source imports; this is evidence of dependencies, not an isolation compliance verdict.
- [test-discovery-inventory.json](test-discovery-inventory.json): executable-suite classification, helpers, empty class, backups and resources.

The initial source proposal moves 854 files and explicitly retains 394. The test proposal moves 461 Java files and retains 112. Existing files only: no overview implementations or new endpoints are included. New guards, migration/verification tooling and genuine missing regression checks may be added during implementation without pretending they were part of the frozen baseline.

## Composition exceptions

Keep WarehouseConfig, CustomerConfig and PaymentConfig as mixed module composition roots. CashRegisterConfig, CashRegisterQueryGateway and CashRegisterRepositoryAdapter remain at module-level composition because they span sessions/reporting. The cash-register adapter is not silently split or assigned to a supposedly independent submodule. These are documented structural exceptions, not duplicate ownership.

Within a top-level module, application workflows may coordinate sibling capabilities. Warehouse management still queries stock presence through existing repository methods; stock validates warehouses. Customer registration writes profile/access atomically, and profile/credit projections read available balance. These internal organizational boundaries do not require an interface for every folder.

## Controllers and combined screens

Owned operations keep their controllers: customer registration remains profile-owned and stock transfers remain stock-owned. Existing AuthController composes registration/login/refresh/logout across Auth capabilities while preserving its current route ownership. Existing shared read projections remain with their owning operation.

Future combined read views may use `<module>/overview/application` and `<module>/overview/infrastructure/web`. Those extension paths need no domain or tables, but the six illustrative files in earlier PDFs are not created by this structural-only change. Financial statement composition remains a separate possible Receivables query.

## Test namespace and fixture decisions

All production-peer tests follow the tested type where practical. Broader boundary suites keep a module-wide architecture location; integration suites retain their full fixture scope. No assertions are dropped.

- Preserve both InvoiceEditingBoundaryTest classes in distinct module-wide/operation-boundary packages.
- Preserve both versions of each LoginRequestUnitTest, LogoutRequestUnitTest and RefreshTokenRequestUnitTest; the current dto/request peer follows its production package, while the other suite receives a distinct validation subpackage.
- CustomerTestHelper moves to `src/test/java/com/cashly/cashly_api/customers/testing/CustomerTestHelper.java`. Make its class and shared factories public only in test scope, update callers/static imports, and preserve all fixture values. This is an explicit access adjustment required by moving callers out of their current shared package.
- The four Warehouses concurrency IT fixtures remain in the same proposed concurrency package so their shared helper access remains valid.
- Nine `.java.bak` files remain noncompiled and byte-identical; move beside a relocated current peer when one exists, otherwise retain their explicit old path. Old package strings in these backups are historical content, not active Java imports.

## Existing external dependency debt

The following are baseline violations of the existing ownership aspiration. Relocation updates references only. This change must not introduce additional cross-module internals or claim it repaired these accesses. The owning module maintainer is responsible for disposition. D0 is the pre-migration baseline review: assign a calendar deadline and follow-up owner before final verification. A missing deadline is a failed debt-accounting gate, not implied approval. The proposed closure milestone for each item is the first boundary-hardening work unit for its owner, before another feature extends the affected API.

| Debt | Current evidence | Owner | Closure milestone |
|---|---|---|---|
| D01 | Orders LocalOrderInventoryGateway injects warehouse stock/movement repositories and implements inventory writes. | Orders and Warehouses maintainers | Warehouses public Orders-inventory operation follow-up; assign deadline at D0. |
| D02 | Payments LocalPaymentCustomerGateway injects ConsumeCustomerBalanceUseCase and BalanceType. | Payments and Customers maintainers | Reuse/validate InvoiceBalanceConsumption in balance-boundary follow-up; assign deadline at D0. |
| D03 | Invoice LocalInvoiceOrderGateway and LocalInvoiceQuoteGateway access other document repositories. | Invoices, Orders and Quotes maintainers | Provider-owned link/query contract follow-up; assign deadline at D0. |
| D04 | Stock, catalog and movement Reports repositories use warehouse JPA types. | Reports and Warehouses maintainers | Reporting projection isolation follow-up; assign deadline at D0. |
| D05 | Cross-module integration adapters import foreign application exceptions. | Consuming module maintainers | Stable error-mapping follow-up preserving HTTP errors; assign deadline at D0. |
| D06 | Notification/WebSocket and document integrations use concrete services or Auth internals. | Notifications and consuming maintainers | Notification capability follow-up preserving after-commit delivery; assign deadline at D0. |

Other import-level dependencies are retained in dependency-baseline.json. Audit fully qualified references as well: the import inventory alone is not a complete Java symbol graph. Existing Customers/Payments, Products/Warehouses and document conversion relationships can be bidirectional with terminating call paths. Ports do not automatically make the package graph acyclic.

## Immutable behavior boundaries

This proposal does not change money/debt formulas, available-stock formulas, manual handling of reserved quantities, code/sequence allocation, duplicate handling, same-warehouse transfer policy, active/deactivation policy, file compensation semantics, notification wording or placeholder report behavior. Existing defects discovered by a complete baseline remain explicit defects; they must not be hidden with exclusions or silently bundled into package moves. Required functional remediation is separately specified and tested before claiming the corresponding check passes.
