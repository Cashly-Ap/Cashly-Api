## ADDED Requirements

### Requirement: Standalone warehouse reassignment is unavailable
The application MUST remove post-creation warehouse reassignment dialogs/actions and their exclusive backend operations while preserving initial document allocations.

#### Scenario: User opens a sales document action menu
- **GIVEN** a quote, order or invoice
- **WHEN** its list or detail actions are shown
- **THEN** no standalone assign-warehouses action or modal is available

#### Scenario: A client invokes a removed allocation route
- **GIVEN** the obsolete quote or order reassignment endpoint
- **WHEN** an authorized client invokes it
- **THEN** the endpoint is unavailable and no allocation is mutated

### Requirement: Conversions preserve managed snapshot identity
Quote-to-order, quote-to-invoice and order-to-invoice conversion MUST persist their source status and historical category snapshots without shared-primary-key identity conflicts.

#### Scenario: A categorized document is converted in one transaction
- **GIVEN** an existing document with categorized lines loaded in the current persistence context
- **WHEN** it is converted and its source is saved
- **THEN** the converted source persists successfully without a duplicate snapshot instance conflict

#### Scenario: A legitimate conversion constraint fails
- **GIVEN** insufficient stock, invalid source state or a required provider failure
- **WHEN** conversion is attempted
- **THEN** the existing constraint is enforced and the transaction leaves no partial business effects

### Requirement: Cash conversion persists invoice references atomically
Cash invoice creation from a quote or order MUST persist the invoice before inserting invoice payment allocations and MUST keep invoice, payment, balance, cash and inventory effects in one transaction.

#### Scenario: A cash invoice is created from a sales document
- **GIVEN** a quote or order converted using a valid payment method and an open cash register
- **WHEN** payment allocation records are inserted
- **THEN** the referenced invoice exists and the conversion commits without a foreign-key conflict

#### Scenario: Inventory fails after cash authorization
- **GIVEN** invoice and financial writes performed within the conversion transaction
- **WHEN** a required inventory operation fails
- **THEN** invoice, payment, allocation, cash and balance writes roll back together and the existing source document remains unchanged
