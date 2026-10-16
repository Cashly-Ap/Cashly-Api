## ADDED Requirements

### Requirement: Common atomic issuance
All invoice origins SHALL use the same internal issuance coordinator and retain transactional rollback across invoice, payment, balance, inventory and source status.

#### Scenario: Cash allocation refers to a persisted invoice
- **GIVEN** a valid draft CASH invoice
- **WHEN** issuance settles its remainder
- **THEN** the invoice is persisted and flushed before payment allocation writes
- **AND** a later failure rolls back all issuance effects

#### Scenario: Validate before payment effects
- **GIVEN** invalid authorization or missing required payment options
- **WHEN** issuance is requested
- **THEN** payment and balance writes do not occur

### Requirement: Origin inventory policies
Issuance SHALL preserve each source's existing inventory responsibility.

#### Scenario: Quote conversion
- **GIVEN** a draft quote with allocations
- **WHEN** converted to an invoice
- **THEN** allocated stock is deducted once

#### Scenario: Confirmed order conversion
- **GIVEN** an order whose confirmation already deducted inventory
- **WHEN** converted to an invoice
- **THEN** issuance does not deduct inventory again

#### Scenario: Invalid order state
- **GIVEN** an order outside CONFIRMED
- **WHEN** conversion is requested
- **THEN** it is rejected before invoice creation or payment effects

#### Scenario: Manual draft confirmation
- **GIVEN** an existing draft invoice with reservations
- **WHEN** confirmed
- **THEN** reservations are confirmed and existing accounting behavior is retained

### Requirement: Existing public contracts
Quote and order creation APIs SHALL retain neutral request and result contracts without exposing the Invoice aggregate across module boundaries.

#### Scenario: Standalone creation
- **GIVEN** invoice creation without a primary order
- **WHEN** the request succeeds
- **THEN** a DRAFT is saved without issuance or settlement
