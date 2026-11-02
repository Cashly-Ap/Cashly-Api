## ADDED Requirements

### Requirement: Shared ERP area ownership
Both projects SHALL organize features by the same business areas while keeping technology-specific implementation responsibilities.

#### Scenario: Complete relocation
- **GIVEN** all current backend modules and Angular features
- **WHEN** navigating their new source trees
- **THEN** sales, procurement, catalog, inventory, finance, partners, analytics and platform group their corresponding capabilities
- **AND** production and test identities are retained or explicitly documented as a responsibility-preserving split

#### Scenario: Invoice returns
- **GIVEN** returns operate on invoices
- **WHEN** locating return source and tests
- **THEN** they belong to finance/billing/returns in both projects
- **AND** sales contains quotes and orders

### Requirement: Preserve public navigation and contracts
Package relocation SHALL preserve all browser/API paths, permissions and business behavior.

#### Scenario: Lazy navigation
- **GIVEN** any existing authenticated browser route or report URL
- **WHEN** Angular resolves its lazy route
- **THEN** it loads the relocated feature with the existing guards and permission identifiers

#### Scenario: Transactional conversion regression
- **GIVEN** an existing quote/order conversion or manual invoice confirmation
- **WHEN** executed after package relocation
- **THEN** invoice, payment, balance, source state and inventory behavior match the existing verified implementation
- **AND** failures retain the same rollback guarantees

### Requirement: Owner-local reports
Report implementations SHALL live under their owning feature's reporting capability.

#### Scenario: Report routing composition
- **GIVEN** the existing public reports navigation
- **WHEN** a user opens a report
- **THEN** thin routing composition resolves the owner-local implementation
- **AND** no global reports feature owns business report generation

### Requirement: Enforced module responsibility boundaries
Existing dependency and responsibility checks SHALL continue to distinguish concrete modules beneath their areas.

#### Scenario: Sibling-module boundary
- **GIVEN** quotes and orders share the sales area
- **WHEN** a cross-module reference is evaluated
- **THEN** it remains subject to module boundary rules despite the shared area parent

#### Scenario: Regression completeness
- **GIVEN** the pre-move executable test inventory
- **WHEN** clean complete suites execute
- **THEN** every original executable test class remains discoverable with no skipped tests
- **AND** relocated-class and module/layer coverage nonregression checks pass
