# Modular Architecture Specification

## Purpose

Define stable behavior and module boundaries for Cashly's modular architecture while preserving the current ERP contracts during internal refactoring.

## Requirements

### Requirement: Existing ERP contracts remain stable
Cashly MUST preserve existing endpoints, permissions, HTTP DTOs, and business rules unless a separately specified correction changes them.

#### Scenario: Internal module refactor
- **WHEN** a module replaces a direct dependency with a public contract
- **THEN** existing endpoint responses, permissions, and business outcomes remain equivalent

### Requirement: Modules own their business data and operations
Each module MUST own its business rules and expose supported capabilities through stable contracts; consumers MUST NOT depend on another module's internal models or persistence.

#### Scenario: Cross-module business operation
- **WHEN** one module requests an operation owned by another module
- **THEN** it uses the owner's public capability and receives neutral input and output values

### Requirement: Dependencies point toward business policy
Domain behavior MUST remain independent of frameworks and persistence, and application behavior MUST depend on abstractions rather than infrastructure implementations.

#### Scenario: Infrastructure technology changes
- **WHEN** an adapter or framework integration is replaced
- **THEN** domain rules and application contracts remain usable without importing the replaced technology

### Requirement: Failed cross-module operations do not leave partial business state
Cashly MUST report failures from required cross-module operations and MUST roll back local transactional changes when the operation cannot complete atomically.

#### Scenario: Required provider operation fails
- **WHEN** a transaction performs local writes and a required provider operation fails
- **THEN** the transaction leaves no partial local business state and the caller receives an explicit failure

### Requirement: Sales documents retain historical product categories
Order and quote lines MUST retain the product category recorded when the line was created, while category filters continue to include matching current categories and preserve pagination and ordering.

#### Scenario: Product category changes after a sale document is created
- **WHEN** an order or quote is queried by category after its product changes category
- **THEN** the line remains discoverable by its historical category and also matches its current category where applicable

### Requirement: Capability-first package ownership (STRUCT-01)
Backend sources MUST follow business module ownership and justified internal capabilities before layers. Single-aggregate modules MAY retain layers directly. Aggregate children, contacts, allocations and their invariants MUST remain with their owner; empty layers and artificial submodules MUST NOT be introduced. `module-map.md` defines the approved ownership and destinations.

#### Scenario: STRUCT-01.1 — Existing capabilities are organized
- GIVEN an inventoried module and its aggregate relationships
- WHEN its package layout is migrated
- THEN every source occupies its approved owner, preserving aggregate boundaries and signatures.

#### Scenario: STRUCT-01.2 — A capability has no independent domain
- GIVEN a read facade or simple module
- WHEN its target packages are generated
- THEN no empty domain, duplicate aggregate or redundant submodule is required.

### Requirement: Lossless package relocation (STRUCT-02)
The migration MUST account for 1,248 existing production Java files, 573 Java test sources, and every inventoried non-Java test artifact/resource, including nine `.bak` files and two test resources. Each MUST have one unique destination or explicit unchanged disposition. Existing artifacts MUST NOT be deleted, merged, reclassified as passing tests or silently excluded.

#### Scenario: STRUCT-02.1 — Manifest reconciliation
- GIVEN frozen production, test and resource inventories
- WHEN the migrated tree is compared with the manifest
- THEN all entries reconcile, destinations are unique and no artifact is lost.

### Requirement: Compatible layout conventions (STRUCT-03)
The new infrastructure layout MUST supersede only the infrastructure path convention of active `application-package-naming`: HTTP uses `infrastructure/web`, cross-module adapters `integration`, and persistence populated role directories. Directional `application/ports/in|out`, neutral public values and inward dependencies MUST remain. Prior changes and main specifications MUST remain untouched during planning.

#### Scenario: STRUCT-03.1 — Naming guards are reconciled
- GIVEN prior naming guards and the approved target layout
- WHEN guards inspect migrated packages
- THEN they enforce the new infrastructure paths and preserve directional-port and persistence-role checks.

### Requirement: Stable runtime and business contracts (STRUCT-04)
Relocation MUST preserve production type names/signatures and existing HTTP, security, persistence, business and transaction contracts. SQL/schema, fixture values, historical categories and locking semantics MUST remain equivalent. Test-only helper access MAY change as specified in design.md without weakening assertions. This structural change MUST NOT add overview classes, endpoints or business workflows.

#### Scenario: STRUCT-04.1 — A coordinated workflow is relocated
- GIVEN existing registration, inventory, conversion or financial behavior
- WHEN its packages and references change
- THEN its observable outcomes, required rollback, stock movements and authorization remain equivalent.

### Requirement: Honest dependency-debt accounting (STRUCT-05)
Known foreign repository/model, concrete-use-case and exception leaks MUST be named in `module-map.md` with evidence, owner and time-bounded closure milestone before verification. No additional leak MAY be introduced. Preserving baseline debt MUST NOT weaken any existing modular requirement or count as proof of dependency compliance; unresolved violations MUST remain visible in verification and closure decisions.

#### Scenario: STRUCT-05.1 — A baseline leak remains
- GIVEN a documented pre-existing cross-module violation
- WHEN only its package references are migrated
- THEN verification reports it as unresolved debt with its closure milestone, without claiming it was repaired.
