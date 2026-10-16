## ADDED Requirements

### Requirement: Reports belong to their business owner
The backend MUST place all report-specific production and test code under the responsible business module's reporting submodule and MUST NOT retain a top-level reports package. Neutral PDF presentation utilities MAY reside under shared/infrastructure/pdf.

#### Scenario: Report implementation is discovered
- **GIVEN** an invoice, order, quote, purchase, product, warehouse, payment or receivable report
- **WHEN** its source and tests are inspected
- **THEN** application and infrastructure responsibilities belong to that owner's reporting submodule

#### Scenario: A global report package is introduced
- **GIVEN** the module ownership architecture guards
- **WHEN** a production or test source uses a top-level reports package
- **THEN** verification fails

### Requirement: Report contracts and behavior remain equivalent
The relocation MUST preserve existing report routes, permissions, request/response contracts, file contents, SQL and business behavior; mixed wiring and controllers MUST be split without duplicate mappings or lost operations.

#### Scenario: Existing frontend downloads a report
- **GIVEN** the frontend's current report URL and an authorized user
- **WHEN** the user downloads a report after relocation
- **THEN** the same route, authorization, media type and download behavior remain available

#### Scenario: Existing placeholder report is relocated
- **GIVEN** payment history or collections currently returns an empty data model
- **WHEN** that implementation moves into its owner
- **THEN** the relocation preserves that behavior without introducing an unrelated data feature

### Requirement: Reporting migration is completely verified
Verification MUST reconcile every original report source and test, execute the complete clean suite including all MySQL IT without hidden skips, and enforce equivalent class/family and module/layer coverage nonregression with global domain greater than 90%, application greater than 80% and infrastructure greater than 60%.

#### Scenario: A coverage owner changes
- **GIVEN** baseline report classes and explicit relocation or split mappings
- **WHEN** coverage is compared after the move
- **THEN** every baseline class is accounted for under its final owner or explicit split family and a missing or regressed coverage group fails verification
