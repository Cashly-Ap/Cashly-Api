# Organize Warehouses and Customers around business capabilities

Status: proposed architecture and migration plan. No production or test implementation has been moved. Source inspection date: 2026-10-16.

Organize `warehouses` into `management` and `stock`, and `customers` into `profile`, `balance`, `credit`, and `warehouse_access`. Keep hexagonal layers inside each capability. These are internal submodules of the existing modular monolith, not new services, Maven modules, databases, or independently deployable bounded contexts.

The target design also reserves `warehouses.overview` and `customers.overview` for combined detail views. These are proposed new read-composition capabilities, not existing sources to relocate. Both appear explicitly in the proposed structure document; implementation is optional and separate from the behavior-preserving package migration.

## Review scope and complete inventory

| Module | Production Java files | Source lines | Java files in the module test directory |
|---|---:|---:|---:|
| Warehouses | 86 | 3,121 | 39 |
| Customers | 130 | 4,505 | 62 |
| Total | 216 | 7,626 | 101 |

The review read all 216 production sources, relevant unit/controller/boundary/concurrency tests, integration adapters in consuming modules, the global exception handler, architecture tests, and the frontend detail/account-statement consumers. Test-directory counts include test helpers; they are not counts of executed tests. No runtime suite was executed for this planning-only deliverable.

See [the complete current and proposed trees and file-by-file mapping](warehouses-customers-screaming-structure.md) and [the machine-readable production move manifest](warehouses-customers-screaming-moves.json). The manifest is a proposal, not an executed migration script. Every existing production class has exactly one destination; the initial migration deletes and merges none.

## Ownership decisions

| Capability | Owns | Does not own |
|---|---|---|
| `warehouses.management` | Warehouse identity, code, address, active state, CRUD and directory queries | Product quantities, customer warehouse permissions |
| `warehouses.stock` | Quantities, reservations, movements, manual entry/exit, composite-product assembly, adjustment, transfer, invoice/purchase/return inventory operations, stock queries and product references | Customer profile, pricing policy, financial balance |
| `warehouses.overview` (proposed new) | A combined warehouse detail response assembled from management and stock queries, including a bounded recent-movement summary if needed | Stock mutations, another stock repository or movement aggregate |
| `customers.profile` | Customer aggregate, legal/tax identity, code generation, contacts and addresses, active state, configured credit days/limit and price-type ID, profile/pricing/terms snapshots | Current debt calculation, cash register, stock |
| `customers.balance` | Available customer credit balance, balance transactions, advances, refunds, invoice consumption, return and credit-note adjustments | Invoice debt, full customer account statement |
| `customers.credit` | Credit status/eligibility facade, overdue invoice queries, mapping the existing financial provider's response | A second debt formula, a new credit entity or credit repository |
| `customers.warehouse_access` | Grant/revoke/list customer-to-warehouse permissions and the accessible-ID query | Warehouse existence/state, warehouse stock |
| `customers.overview` (proposed new) | A combined customer detail response assembled from authorized profile, balance and warehouse-access queries | Balance mutations, debt calculation or the full financial account statement |

### Keep stock and movements together

`InvoiceInventoryService`, `PurchaseInventoryService`, `ReturnInventoryService`, and manual stock use cases update stock and, where applicable, record movements. `WarehouseStock` owns reservation behavior. Separate movement and reservation submodules would introduce repeated coordination around the same operations. Keep `StockMovement`, its repository and controller under `stock`; use `stock.application.movements` for the history use case.

This does not assert that every existing mutation records a movement. For example, reservation-only operations and `StockDeductionService` have their own existing behavior. Preserve it during relocation and characterize any later consolidation before changing it.

### Keep customer contacts and configured terms with the aggregate

`Customer` contains emails, phones, addresses, `CreditDays`, `CreditLimit`, and price-type ID. Registration/update persist these together. Keep them under `profile.domain`; optionally group domain files into `entities`, `valueobjects`, `repositories`, and `services` as today. A `contacts` or standalone `credit.domain` aggregate is not justified by the current code.

`RepositoryCustomerCreditContextQuery` combines profile and available balance for the financial provider. It belongs to `credit.application.queries`, while the configured credit-terms query remains under profile. The credit capability has application and infrastructure packages only because it currently delegates calculations to Payments. Empty domain directories are unnecessary.

### Preserve module composition roots

The existing `WarehouseConfig` wires warehouse CRUD, stock use cases, and invoice inventory. `CustomerConfig` wires profile, access, balance, and credit operations. Keep these mixed composition roots at `warehouses.infrastructure.config` and `customers.infrastructure.config` during the initial move. Other, narrowly scoped config classes move with their capability. Do not claim that putting a mixed config file inside one capability makes it independent.

Splitting or grouping config beans is a separate optional cleanup. It must preserve bean names, transaction proxies, and wiring; fewer files alone is not an acceptance criterion.

## Package and dependency rules

1. Source layout is `<module>/<capability>/<layer>/<responsibility-or-operation>`. Existing externally consumed contracts remain explicit `application/ports/in`; required external capabilities remain `application/ports/out`.
2. Group use cases with their operation-specific input DTOs: for example `stock.application.transfer` and `profile.application.register`. Shared input/output DTOs stay under the owning capability's `application.dto`.
3. Shorten HTTP and integration paths to `infrastructure/web` and `infrastructure/integration`; these roles already express their responsibility. Keep persistence role directories (`persistence/entities|repositories|adapters|queries|mappers`), which the project explicitly tests. Tiny domain packages can place entities and repository contracts directly under `domain`, retaining `valueobjects` where useful. The complete manifest fixes these choices consistently; this is organization, not a behavior change.
4. Domain imports no application, persistence, HTTP, or framework implementation. Application imports no infrastructure. Preserve the existing accepted transaction-annotation convention.
5. Other top-level modules should consume offered application ports through their own adapters. A public Java class is not automatically part of a module's supported interface.
6. Submodules inside the same top-level module are organizational boundaries. Existing application coordination across their domain repositories is explicitly allowed initially; no port and adapter is required merely because a directory was introduced.
7. Keep domains independently understandable. Do not make `CustomerId` or customer domain entities a universal shared kernel. Access and balance currently use UUIDs where appropriate; preserve those representations.
8. The root composition package may wire all its submodules. It must contain no business workflows.

### Existing internal coordination that remains explicit

- Warehouse stock use cases read the management repository to validate warehouse existence/active state. `RepositoryPurchaseWarehouseQuery` combines warehouse directory and quantities, so it belongs to stock, not a purportedly stock-independent directory capability.
- Warehouse management deletion/listing currently uses stock-presence methods on `WarehouseRepository`; the JPA implementation reads the stock table. Preserve that documented read coupling in the relocation. Introducing opposite `WarehouseStatus` and `StockPresence` ports does not automatically remove the conceptual cycle.
- Customer registration creates profile plus initial warehouse access in one transaction. `GrantWarehouseAccessUseCase` and listing validate customer existence. Credit context reads profile plus balance.
- If stronger independently enforced submodule boundaries are later required, design that as a separate change with explicit orchestration and one-way contracts. Do not label the initial move as complete dependency isolation.

## External integration findings and follow-up design

| Current integration | Evidence and consequence | Planned action |
|---|---|---|
| Orders writes Warehouses internals | `orders/.../LocalOrderInventoryGateway` injects stock and movement repositories through its Spring-selected constructor, implements reserve/release/deduct/restore, and has an alternate constructor with different behavior | After relocation, move the owned operations into a new `warehouses.stock.application.ports.in.OrderInventoryApi` and implementation. Keep Orders' adapter as a mapper/delegator. Characterize all five operations, movement types and references before changing wiring. |
| Payments consumes a concrete Customers use case | `payments/.../LocalPaymentCustomerGateway` imports `ConsumeCustomerBalanceUseCase` and `BalanceType` | Adapt it to the existing `InvoiceBalanceConsumption` contract if its semantics match; verify its implementation's guards and response first. Do not create a redundant balance facade by default. |
| Reports reads warehouse JPA entities | Stock, inventory catalog, and movements report JPA repositories import `WarehouseStockEntity` or `StockMovementEntity` | Update these imports during relocation. Later choose typed read projections/query ports or explicitly document reporting as a privileged read adapter. Do not pretend physical package moves remove this coupling. |
| Integration adapters import foreign exceptions | Examples include `LocalWarehouseDirectoryGateway`, `LocalInventoryGatewayAdapter`, product/receivables customer adapters | Initially update imports and preserve HTTP error behavior. Later expose stable errors or translate at the consumer boundary, with exception-handler contract tests. |
| Customers and Warehouses depend on each other's offered capabilities | Access grants query the warehouse directory; warehouse availability queries customer access | Document both call paths. They are separate operations; the inspected calls do not recurse. Ports do not remove the module-level dependency cycle. Keep access ownership in Customers. |
| Customers credit and Payments depend on each other | Customers delegates status to `CustomerCreditQuery`; Payments obtains customer credit context and uses balance | Preserve the existing financial provider and DTO mapping. Reassigning debt calculation to Customers/Receivables requires a separate financial ownership decision and regression fixtures. |

Use the move manifest to update both imports and fully qualified references throughout production, tests, configuration and documentation. The impact is larger than the two moved folders. In particular, update `GlobalExceptionHandler` and report repository imports.

## Transactions and concurrency

### Warehouses

- Keep transactions on entry, exit, adjustment, transfer, and inventory service operations.
- Preserve `PESSIMISTIC_WRITE` stock reads, the unique warehouse/product key, the current insert-if-absent creation strategy, and transfer lock acquisition in sorted warehouse order.
- Keep stock writes and their required movement writes in the same transaction. A failure in a later batch item must roll back prior writes.
- Do not move transaction logic into private methods or introduce self-invocation that bypasses Spring proxies.
- Existing manual batch and composite-component paths do not uniformly order all stock keys. Treat a universal lock-order policy as a separate concurrency improvement with opposing-operation tests; do not claim it is already implemented.

### Customers

- Registration and initial permission grants remain atomic.
- Advances/refunds retain their open-register check, actor lookup, balance transaction, and corresponding cash movement in one transaction using the existing shared transaction manager.
- Balance mutations currently load via an unlocked `findByCustomerId`; the inspected entity has no `@Version`. Relocation does not fix concurrent lost updates or first-balance creation races.
- Plan a separate hardening change: serialize updates per customer with a tested lock/atomic update strategy, handle the missing-row initialization race, and verify simultaneous consumers cannot spend the same available balance. Inspect existing SQL uniqueness constraints before deciding whether any new migration is necessary.
- Do not add `REQUIRES_NEW` to nested financial mutations: it would break rollback across the surrounding business operation.

## Screens that combine information

No new overview endpoint is required for the package migration. Current HTTP routes, JSON, permissions and pagination stay intact.

### Where combined-view controllers belong

An operation with a clear owner keeps its controller under that owner's submodule. For example, `customers.profile.infrastructure.web.CustomerController` calls `RegisterCustomerUseCase`, which coordinates profile creation and initial warehouse grants in one transaction. The controller does not invoke other controllers. Registration does not move to overview just because it writes two areas.

A combined read view with no single existing capability owner belongs under the module's `overview` capability. The proposed new sources are:

```text
warehouses/overview/
├── application/
│   ├── GetWarehouseOverviewUseCase.java
│   └── WarehouseOverviewResponse.java
└── infrastructure/web/
    └── WarehouseOverviewController.java

customers/overview/
├── application/
│   ├── GetCustomerOverviewUseCase.java
│   └── CustomerOverviewResponse.java
└── infrastructure/web/
    └── CustomerOverviewController.java
```

These six files are additions for an optional overview implementation. They are excluded from the 216-file relocation manifest and current source counts. Wire the use cases through each module's existing root config when implemented. Neither overview needs its own domain, entities, tables or repositories merely to compose read results.

Proposed routes, if this feature is implemented, are `GET /api/warehouses/{id}/overview` and `GET /api/customers/{id}/overview`. They are not current endpoints or replacements for existing routes. Controllers deserialize/authorize requests and call a single overview use case; that use case composes application query capabilities. Add or extend narrow query contracts where the current ones lack a required field, rather than importing another capability's JPA implementation. Source capabilities do not depend on overview.

For a customer view containing financial fields, enforce the relevant finance authorization in the backend in addition to profile/access permissions. Define omission/error behavior explicitly before implementation; hiding a field in the frontend is insufficient. Bound summaries and keep large stock, transaction and movement collections paginated. Do not promise a point-in-time snapshot across multiple queries without deliberately selecting and testing transaction/isolation behavior.

| Existing screen | Actual behavior | Architectural treatment |
|---|---|---|
| Warehouse details | Loads warehouse details and navigates separately to stock and movements pages | Keep current endpoints; stock/movement screens remain owned by stock. A future overview read use case can compose directory and stock summaries only if the screen needs it. |
| Customer details | Loads customer profile, warehouse access and price-type data separately | Keep current composition. A future customer overview can combine profile/access snapshots without importing foreign JPA entities. |
| Customer balance/account statement | Frontend combines seven sources: available balance, balance transactions, invoices, payment history, credit notes, debit notes, and returns; it retains only advances/refunds from the independent balance ledger to avoid counting other sources twice | Keep the available balance aggregate under Customers. A future paginated account-statement read model can live under `receivables.account_statement`, with explicit source, signs, cancellation, permissions, pagination and failure semantics. This is an optional endpoint/frontend change, not a class move. |

The frontend currently fetches up to 1,000 entries from some sources and paginates the assembled history locally. A future statement must paginate the unified result, not independently paginate each source and pretend the combined page is complete. Preserve source identities and define overlap/deduplication rules. No debt/available-credit formula is redefined by this plan.

## Migration sequence and review units

| Step | Concrete work | Completion evidence |
|---|---|---|
| 1. Freeze the inventory and baseline | Record the move manifest, current HTTP/JSON contracts, inventory and financial operation behavior; run current architecture and targeted tests in Docker | Baseline results with failures distinguished from relocation failures; no production changes |
| 2. Relocate Warehouses | Move all 86 source files according to the manifest; move corresponding tests; update all consumers, imports, fully qualified references, report entity imports and exception handlers | Complete 86-to-86 mapping, no stale references, compile and warehouse/consumer/architecture tests pass |
| 3. Relocate Customers | Move all 130 source files and corresponding tests; update consumers including Payments, credit notes, Returns, Warehouses, Receivables and exception handlers | Complete 130-to-130 mapping, compile and customer/consumer/architecture tests pass; current HTTP contract unchanged |
| 4. Close real external write leaks | Introduce and wire the Orders inventory contract; use the existing customer invoice-balance contract from Payments after semantic comparison; characterize and translate boundary errors | External modules no longer write warehouse repositories or call concrete customer balance use cases; exact prior movements/rollback behavior retained |
| 5. Simplify proven duplication | Extract only shared operations that retain differing purchase/invoice/return semantics; optionally split composition roots and make pure mappers explicit | Fewer duplicated rules with operation-level tests; no generic inventory engine or new event bus |
| 6. Separate optional functional changes | Combined-detail controllers/use cases under `warehouses.overview` and `customers.overview`; balance concurrency hardening; universal stock lock order; account-statement query/API and frontend adoption; reporting projection isolation | Each change has its own scenarios and validation; overview additions have explicit payload/permission/query contracts and do not alter the 216-file relocation manifest |

Steps 2 and 3 each include all imports in consumers of the moved module so each review unit can compile independently. They are not one commit per folder with broken intermediate states. Implementation, commits, PRs and schema migrations have not been performed by this planning deliverable.

## Verification required during implementation

- Migrate the 101 module test source files with their owning capability. Preserve integration helpers and update imported config classes and entity/repository scanning.
- Preserve existing architecture checks rather than disabling them. The current `moduleOf` only recognizes the top-level module; add explicit allowed internal-submodule rules if enforcing submodule boundaries. Existing checks do not prohibit all infrastructure-to-foreign-internal imports, so passing them alone does not establish isolation.
- Update hardcoded source paths and string assertions, especially `CustomerWarehouseBoundaryTest`, and reflection-based package lookups. Keep assertion intent unchanged.
- Run `LayerDependencyTest`, `ApplicationPackageConventionTest`, module unit/controller/boundary tests and the affected consumer tests in Docker. Compile all production and test sources.
- Explicitly run `InvoiceInventoryConcurrencyIT`, `StockDeductionConcurrencyIT`, `WarehouseStockConcurrencyIT`, and `ManualStockConcurrencyIT` against an isolated test database. Default Maven Surefire naming does not automatically include `*IT`; do not count a plain `test` run as concurrency validation. Some existing concurrency tests mock movement persistence, so add a focused integration scenario before claiming persisted stock and movement rollback is proven together.
- Verify concurrent reservation contention and batch rollback; transfer rollback; purchase confirmation/cancellation; invoice/return/order stock references; initial customer access rollback; advance/refund cash linkage; balance consumption.
- Verify the seven existing controllers' routes, response fields, permissions and error contracts remain unchanged in the relocation. No frontend changes should be required for source-package relocation.
- Treat tests proposed for subsequent behavior changes as future work, not executed evidence. This document reports source analysis and manifest validation only.

## Acceptance criteria for the structural migration

- [ ] Each of the 216 existing production files has one valid destination and no destination collides.
- [ ] All existing entities, mappings, SQL table names, repository queries and public HTTP contracts retain behavior.
- [ ] Warehouse movements/reservations remain with stock; customer contacts/configured terms remain with Customer.
- [ ] Credit has no invented domain/persistence layer; root configs contain wiring only.
- [ ] Tests and external consumers use the new packages; source-scanning tests still examine the intended files.
- [ ] Required build, contract and explicitly selected concurrency checks pass in Docker.
- [ ] External dependency exceptions and follow-up work are documented honestly; folder moves are not presented as complete modular isolation.
