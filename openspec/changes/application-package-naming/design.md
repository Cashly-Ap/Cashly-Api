# Application Package Naming Design

## Package Responsibilities

| Package | Responsibility |
| --- | --- |
| `ports/in` | Provider-owned capability interfaces and neutral public values offered to other modules |
| `ports/out` | Consumer-owned interfaces describing required external capabilities |
| `ports/out/model` | Standalone neutral values used by consumer-owned ports |
| `queries` | Concrete read-capability implementations using the module's domain abstractions |
| `dto/query` | Immutable input parameters for application read/report use cases |
| `usecases` | Application operations coordinating business policy |
| `services` | Application coordination shared by operations or implementations of exported capabilities |
| `mappers` | Application/domain value transformations |
| `exceptions` | Application failures |

## Mapping

The prior normalization moved 96 consumer interfaces from `contracts` and 25 reporting ports from `port`. This directional refinement places their 121 interfaces and four standalone models under `ports/out`, and moves 94 provider-owned interface/value types from `api` into `ports/in`. Move the four standalone consumer records (`WarehouseSnapshot`, `CustomerCreditContext`, `InvoiceCreditEntry`, `AccountsReceivableRow`) into `ports/out/model`. Nested records stay with their owning interface. Move five report input records from `query` into `dto/query`. Preserve the twelve concrete implementations in `queries`.

Provider-owned input interfaces move from api into `ports/in`. In particular, Returns' `ProductReferenceQuery` and `InvoiceReturnDetailsQuery` are locally implemented offerings consumed by other modules, rather than external requirements of Returns.

## Compatibility and Verification

Replace package declarations and all imports or fully qualified references atomically. Types and methods retain their names and signatures; no serialization or HTTP mapping changes are needed. Existing historical archived OpenSpec artifacts and architecture audit evidence remain unchanged. New naming guards run red before moves and green afterward. Compile and regression tests run in Docker; runtime readiness and representative existing endpoint requests verify the packaged application.

## Workflow

This is a Light refactor: proposal, design, apply, verify. Leave the change active with actual verification evidence; do not archive without the required review gate.

## Directional Adapters

HTTP and security entry adapters move to infrastructure/adapters/in/controllers and adapters/in/security. Persistence and database query implementations move to adapters/out/persistence. Existing integration, excel, pdf, report, messaging delivery and external dependency services retain their subfolder names under adapters/out. Existing flat adapters move directly under adapters/out. Config and pagination remain unchanged; domain repository interfaces retain domain ownership. Existing test packages follow corresponding production packages.

Internal fully qualified Java type names change; public HTTP contracts and serialization remain unchanged.


## Persistence Package Responsibilities

Persistence contains only populated responsibility folders: entities (58 JPA entities), repositories (42 Spring Data interfaces), adapters (29 concrete repository implementations), queries (10 database read implementations), mappers (12 transformations), and projections (4 database result interfaces). Concrete classes implementing repository ports belong in adapters, regardless of a Repository suffix. No loose production Java types remain directly in persistence. Input HTTP adapters use controllers.


## Cash Register Module Naming

The backend cash register module uses the cash_register directory and Java package segment. Production and test paths, imports and fully qualified references follow this name. Class names and the /api/cash-register HTTP route remain unchanged.
