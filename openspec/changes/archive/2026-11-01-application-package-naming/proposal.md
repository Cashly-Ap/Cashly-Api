# Application Package Naming

## Intent

Make application package names describe ownership and responsibility consistently across Cashly modules without changing HTTP contracts, database schema, or business behavior.

## Scope

- Move existing `application/api` types into `application/ports/in` for capabilities offered by a module and their public neutral models.
- Replace consumer-owned `application/contracts` and singular `application/port` with `application/ports/out`.
- Place standalone models exchanged through consumer ports in `application/ports/out/model`.
- Move report query input records from `application/query` to `application/dto/query`.
- Keep `application/queries` for concrete implementations of public read capabilities.
- Move infrastructure adapters into `infrastructure/adapters/in` and `infrastructure/adapters/out` according to call direction.
- Update imports, existing test references, current documentation, and architecture guards.

## Acceptance

Given existing application packages, when package naming is normalized, then no production `contracts`, `port`, or `query` package remains directly under application.

Given existing HTTP consumers, when the application is rebuilt, then endpoints, permissions, response values, and persistence behavior remain equivalent.

Given future package additions, when architecture tests run, then ambiguous legacy names and framework dependencies in API or port packages fail validation.

## Exclusions

No dependency upgrades, business rule changes, domain events, schema migrations, frontend changes, or commits.


## Persistence Package Responsibilities

Persistence contains only populated responsibility folders: entities (58 JPA entities), repositories (42 Spring Data interfaces), adapters (29 concrete repository implementations), queries (10 database read implementations), mappers (12 transformations), and projections (4 database result interfaces). Concrete classes implementing repository ports belong in adapters, regardless of a Repository suffix. No loose production Java types remain directly in persistence. Input HTTP adapters use controllers.


## Cash Register Module Naming

The backend cash register module uses the cash_register directory and Java package segment. Production and test paths, imports and fully qualified references follow this name. Class names and the /api/cash-register HTTP route remain unchanged.
