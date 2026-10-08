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
