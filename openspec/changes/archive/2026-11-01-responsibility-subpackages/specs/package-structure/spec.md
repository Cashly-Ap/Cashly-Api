## ADDED Requirements

### Requirement: Explicit responsibility folders
Backend modules SHALL place Java source files inside meaningful responsibility packages instead of directly in module, submodule, layer or operation containers.

#### Scenario: Order creation structure
- **GIVEN** standard order creation and quote-based creation
- **WHEN** browsing orders/application/create
- **THEN** it contains responsibility folders such as use_case and request, and a from_quote branch
- **AND** the source-specific branch groups its own implementation responsibility

#### Scenario: Domain separation
- **GIVEN** a module with domain entities and repository contracts
- **WHEN** browsing its domain layer
- **THEN** entities are under entity and repository contracts under repository
- **AND** value objects and exceptions retain their explicit responsibility packages

#### Scenario: Infrastructure organization
- **GIVEN** an infrastructure layer
- **WHEN** browsing its classes
- **THEN** controllers, configuration, integration adapters, persistence and document generators remain in explicit responsibility packages

### Requirement: Tests mirror responsibilities
Tests SHALL follow the responsibility packages of the production classes they exercise. Cross-cutting architecture/integration tests and test fixtures SHALL have explicit responsibility folders.

#### Scenario: Relocated class tests
- **GIVEN** a production class moved to a new responsibility package
- **WHEN** locating its existing tests
- **THEN** their packages and source references match the new structure
- **AND** all previous executable test classes remain discoverable

### Requirement: Behavioral equivalence
Relocation SHALL preserve runtime behavior and existing verification coverage without deleting tests or changing API/database contracts.

#### Scenario: Full regression
- **GIVEN** the verified source before relocation
- **WHEN** the new structure is compiled and tested in isolation
- **THEN** all ordinary and real MySQL integration tests pass without skips
- **AND** class coverage is reconciled using the explicit old-to-new manifest
