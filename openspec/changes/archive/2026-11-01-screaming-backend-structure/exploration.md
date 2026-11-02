# Exploration of the whole backend structure

Inspected the current source inventory, aggregate ownership, application/controller/configuration patterns, consumer references, test discovery, resources, CI and active architecture specifications. Warehouses and Customers were previously read in full; the additional modules received focused inspection of aggregates, operations, transaction-critical flows and wiring. This is structural planning, not a claim of exhaustive behavioral code review or full regression execution.

## Findings

- The backend has 1,248 production Java files across 21 business modules, shared infrastructure, global infrastructure and bootstrap. There are 573 Java test sources, nine noncompiled backups and two test resources. Thirty production resources remain explicitly inventoried.
- Existing Auth and Reports packages already have meaningful capability subdivisions. Keep those instead of renaming them arbitrarily.
- Warehouses supports management and stock; stock owns physical quantities, reservations and movements. Composite entry consumes components, transfer locks both stocks and records linked movements, and inventory operations participate in document transactions.
- Customers supports profile, available balance, credit queries and warehouse access. Profile owns contact children and configured credit terms. Available balance is not the full account statement. Credit currently delegates financial calculations to Payments.
- Payments has a distinct credit policy/read-composition capability and payment collection lifecycle. Cash register separates session operations from export/read reporting, while mixed persistence and query implementations remain explicitly owned at module composition level initially.
- Orders, Quotes, Invoices, Purchases, Returns and both note modules each have a coherent aggregate lifecycle. Splitting lines, allocations or document effects into independent domains would add artificial boundaries. Group operations under application instead.
- Small catalog/administration modules and Suppliers retain coherent aggregate ownership, including supplier contacts. Products keeps its Product aggregate with components/prices/photos and groups application photo, stock-view and transaction-view operations.
- Existing infrastructure adapters include foreign-internal dependencies. Moving packages must update their references without claiming those dependencies are fixed. The dependency inventory and module map retain this evidence and required disposition.

## Migration risks requiring explicit treatment

1. Wildcard imports, static imports, fully qualified names, formerly same-package references and nested exception types do not all migrate with a simple package-prefix substitution.
2. Several suites have identical simple class names in different packages. Their destinations must remain distinct; no test may be merged or discarded.
3. Shared test fixtures include package-private methods. Distributed suites need explicit test-only fixture access changes or maintained package groups; production visibility must not be widened.
4. Source-scanning tests can pass vacuously if they scan old paths. New checks must assert nonempty coverage of every intended owner.
5. Spring scans, configuration references, entity names and repository discovery must remain valid after capability nesting.
6. Default Maven discovery omits seven MySQL/concurrency IT suites. An obsolete exclusion names a nonexistent StockReservationIntegrationTest. Existing CashlyApplicationTests is empty and provides no startup proof.
7. Inventory IT uses create-drop; financial/persistence IT uses Flyway. Running both against one database can invalidate the other suite. A dedicated two-database test harness is required.
8. The existing test runner tears down its Compose project with volumes, and the Returns test Compose file declares global names. Neither is suitable as-is for safe all-module baseline execution beside development services.

## Relationship to current changes

`application-package-naming` is still unarchived but its current code and verification describe the existing layout. This new change is explicitly requested and leaves that evidence intact. It replaces only the infrastructure wrapper-path decision, retaining provider/consumer port direction, persistence responsibility groups and all main-spec behavior. No automatic archive, base-spec rewrite or other active-change overwrite occurs during preparation.

## Evidence and readiness

Architecture baseline: 11 tests passed, no failures/errors/skips, inside the running app container. This scoped check does not start a database context or establish whole-backend correctness. Full isolated baseline, migrated suites, seven IT, runtime/API evidence, coverage and CI remain pending. Official OpenSpec CLI availability and artifact validation are reported separately from runtime tests.
