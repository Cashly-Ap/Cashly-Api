# Cashly backend architecture

The backend is a modular Spring Boot application. Business modules and their real capabilities precede technical layers in package names. The application remains a single deployable service and database; these packages do not imply separate services or transactions.

## Module and capability ownership

| Module | Existing capability packages |
| --- | --- |
| `auth` | `authentication`, `token`, `user` |
| `warehouses` | `management`, `stock`, `reporting` |
| `customers` | `profile`, `balance`, `credit`, `warehouse_access` |
| `payments` | `collection`, `credit`, `reporting` |
| `cash_register` | `sessions`, `reporting` |
| `invoices`, `orders`, `quotes`, `purchases`, `products`, `receivables` | Existing business layers plus a dedicated `reporting/{application,infrastructure}` submodule |
| `categories`, `credit_notes`, `dashboard`, `debit_notes`, `notifications`, `payment_methods`, `price_types`, `returns`, `suppliers` | One cohesive module; layers remain directly below the module |

Warehouse configuration and lifecycle belong to `management`; quantities, reservations and movements belong to `stock`. Customer identity, contacts and configured commercial terms belong to `profile`; balances belong to `balance`; credit eligibility and credit-status composition belong to `credit`; warehouse entitlements belong to `warehouse_access`. Customer credit is a read/orchestration capability and has no separate domain aggregate. Aggregate lines, contacts and allocations stay with their parent aggregate.

## Package structure

This abbreviated tree shows actual package roots. The complete file-level target is recorded in [the migration tree](../openspec/changes/module-owned-reporting/trees/after.md).

```text
com/cashly/cashly_api/
├── warehouses/
│   ├── management/{domain,application,infrastructure}/
│   ├── stock/{domain,application,infrastructure}/
│   ├── reporting/{application,infrastructure}/
│   └── infrastructure/config/WarehouseConfig.java
├── customers/
│   ├── profile/{domain,application,infrastructure}/
│   ├── balance/{domain,application,infrastructure}/
│   ├── credit/{application,infrastructure}/
│   ├── warehouse_access/{domain,application,infrastructure}/
│   └── infrastructure/config/CustomerConfig.java
├── payments/
│   ├── collection/{domain,application,infrastructure}/
│   ├── credit/{domain,application,infrastructure}/
│   ├── reporting/{application,infrastructure}/
│   └── infrastructure/config/PaymentConfig.java
├── cash_register/
│   ├── sessions/{domain,application,infrastructure}/
│   ├── reporting/{application,infrastructure}/
│   ├── application/ports/out/CashRegisterQueryGateway.java
│   └── infrastructure/
│       ├── config/CashRegisterConfig.java
│       └── persistence/adapters/CashRegisterRepositoryAdapter.java
├── products/
│   ├── {domain,application,infrastructure}/
│   └── reporting/{application,infrastructure}/
├── invoices, orders, quotes, purchases, receivables/
│   ├── {domain,application,infrastructure}/
│   └── reporting/{application,infrastructure}/
├── … other business modules listed above
├── shared/
├── infrastructure/
└── CashlyApplication.java
```

Braced paths are a compact description of populated layers, not a requirement to create empty folders. Tests follow the package of the production responsibility they exercise. Shared customer fixtures live in `customers/testing`; architecture guards live in `architecture`.

### Domain

`domain/entities`, `domain/valueobjects`, `domain/services`, `domain/events` and `domain/repositories` contain business types and persistence contracts. Domain code does not depend on application or infrastructure. Repository implementations and JPA entities belong to infrastructure. Only existing layers are retained; package restructuring does not create aggregates or change consistency boundaries.

### Application

Operations use named packages such as `application/create`, `register`, `update`, `delete`, `get`, `list` or a more specific business action. Operation-specific requests, results and failures live with that action. Shared application DTOs, mappers, exceptions and query implementations retain explicit shared roles when multiple operations use them.

| Package | Responsibility |
| --- | --- |
| `application/ports/in` | Public capabilities offered by the owner, with neutral contract values |
| `application/ports/out` | Consumer-owned external capabilities required by the application |
| `application/ports/out/model` | Standalone values exchanged through consumer-owned ports |
| `application/queries` | Concrete read-capability implementations using the owner's abstractions |
| `application/dto/query` | Immutable query parameters shared by read/report operations |
| `application/<operation>` | A business operation and its operation-specific types |
| `application/services`, `mappers`, `exceptions`, `dto` | Existing shared application responsibilities |

The provider owns input ports; the consumer owns output ports. For example, Products offers `application/ports/in/StockCatalogQuery`, while Warehouses requires `stock/application/ports/out/StockCatalogGateway`; infrastructure connects them. A `Query` suffix alone does not determine whether a class is a contract or implementation.

### Infrastructure

Adapters use short role-based paths: `infrastructure/web` for HTTP controllers, `integration` for other-module adapters, `security` for authentication adapters, `config` for wiring, and `persistence/{entities,repositories,adapters,queries,mappers,projections}` for storage responsibilities. Existing output adapters use their concrete roles, including `pdf` and `excel`. The existing `shared/storage/infrastructure/adapters` package is an explicit exception.

## Controllers and composition

A controller belongs to the capability responsible for its HTTP operation. `CustomerController` is under `customers/profile/infrastructure/web`; `CustomerCreditController` is under `customers/credit/infrastructure/web`; `CustomerWarehouseAccessController` is under `customers/warehouse_access/infrastructure/web`. A response containing data from several capabilities does not move ownership into a technical catch-all folder.

Existing mixed wiring stays at module root: `WarehouseConfig`, `CustomerConfig`, `PaymentConfig`, and `CashRegisterConfig`. The existing cash-register query gateway and repository adapter also remain at root because they span sessions and reporting. Shared PDF branding helpers live under `shared/infrastructure/pdf`. Every report-specific operation, DTO, port, adapter, generator and controller belongs to its owner’s `reporting` submodule. The global `reports` package has been removed. Public HTTP routes under `/api/reports/...` are retained for frontend compatibility; URL prefixes do not dictate Java package ownership. Mixed finance and inventory wiring is split by owner. Payment-history and collections placeholders remain unchanged.

No `overview` package or endpoint is introduced by this migration. A future overview with its own use case can own `application` composition and `infrastructure/web`, consuming the relevant public capabilities. It should be added only with a real feature and contract.

## Dependency rules and existing debt

Dependencies point inward: infrastructure may use application and domain; application may use domain and its own ports; domain cannot use either outer layer. Cross-module capabilities should be accessed through provider input ports and consumer output ports, connected by infrastructure adapters.

The current source contains pre-existing cross-module internal dependencies. Moving files preserves these dependencies; it does not make every module isolated. Their measured baseline and disposition are recorded in [dependency-baseline.json](../openspec/changes/screaming-backend-structure/dependency-baseline.json). Removing them requires separate behavior-preserving work. The migration preserves transaction annotations, lock behavior, HTTP routes, schema migrations and business logic.

## Verification

`ApplicationPackageConventionTest`, `LayerDependencyTest`, `CapabilityStructureTest` and `ReportingOwnershipTest` enforce package roles, direction and capability placement. Their scans require nonempty source roots and include rejecting fixtures. `CustomerWarehouseBoundaryTest` scans every customer capability's application layer and checks consumer-owned warehouse contracts.

Use [the structure verification plan](../openspec/changes/screaming-backend-structure/verification-plan.md) and `scripts/run-structure-verification.sh` for the isolated full Maven verification, MySQL integration suites and coverage comparison. The final verification report records executed results; this document describes the intended responsibilities and does not assert that a test run has passed.

Reporting ownership and class-aware coverage comparison are specified in [module-owned-reporting](../openspec/changes/module-owned-reporting/design.md). The complete clean verification includes real HTTP startup/security and all existing MySQL transaction/concurrency suites.
