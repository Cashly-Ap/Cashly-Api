# Backend Regression Verification

## ADDED Requirements

### Requirement: Reproducible baseline and inventory evidence (VERIFY-01)

Verification MUST freeze source/resource inventories and record baseline versus migrated results using identical configuration and fixtures. Reports MUST distinguish source-file counts, discovered tests, executed tests, assertions, failures and skipped tests. The current 11 passing architecture checks MUST NOT be represented as a full-suite baseline; the full suite remains pending until executed. Commands, configuration and evidence MUST follow `verification-plan.md`.

#### Scenario: VERIFY-01.1 — Baseline comparison

- GIVEN frozen inventories and an unchanged baseline checkout
- WHEN baseline and migrated checks complete
- THEN reports reconcile all 1,248 production and 573 Java test sources plus non-Java artifacts/resources and explain every result-count difference.

### Requirement: Complete executable test discovery (VERIFY-02)

Verification MUST compile every production/test source and discover all executable suites, including every `*IT` and nondefault test name identified by the inventory. No blanket skip, exclusion or disabled assertion MAY hide a regression. Nine `.bak` artifacts MUST remain accounted for without being misreported as executable suites. Every skipped/nonexecutable item MUST have an explicit disposition; required checks MUST execute before completion.

#### Scenario: VERIFY-02.1 — Default discovery omits integration tests

- GIVEN an inventoried `*IT` omitted by the default runner
- WHEN verification selects its suites
- THEN it executes that integration suite explicitly and records its results separately.

### Requirement: Coverage thresholds and nonregression (VERIFY-03)

Migrated aggregate layer coverage MUST exceed 90% for domain, 80% for application and 60% for infrastructure, and MUST NOT regress by module or layer relative to baseline. Coverage MUST classify every source under its real layer despite changed package depth; denominators and exclusions MUST be comparable and explicit. Below-threshold baselines MUST trigger remediation, not an invented pass or weakened threshold.

#### Scenario: VERIFY-03.1 — Package depth changes

- GIVEN equivalent instrumented sources before and after relocation
- WHEN coverage is compared by layer
- THEN classification includes all nested capabilities and satisfies both absolute thresholds and nonregression.

### Requirement: Isolated real-database transaction verification (VERIFY-04)

MySQL concurrency and rollback checks MUST use an isolated test database and reproducible test-only configuration, never a live business database. Verification MUST prove persisted stock/movement and financial state rollback after required downstream failure, preserve existing lock/uniqueness behavior and include all inventoried database suites. Mock-only success MUST NOT substitute for real persisted rollback evidence.

#### Scenario: VERIFY-04.1 — Required downstream write fails

- GIVEN an isolated MySQL workflow with prior local writes
- WHEN a required inventory or financial participant fails
- THEN the database retains no partial transactional changes and the failure is reported.

### Requirement: All-module contract and startup equivalence (VERIFY-05)

Verification MUST cover every backend module's existing contracts, permissions/security, DTOs, errors, fixtures and pagination/filtering semantics, including historical categories. Migrated application startup MUST resolve the expected beans, entity/repository scans and configuration. Existing cross-module consumers and fully qualified, reflective and implicit same-package references MUST remain valid.

#### Scenario: VERIFY-05.1 — Startup and contract smoke

- GIVEN baseline and migrated applications using equivalent safe test configuration
- WHEN startup and representative authorized/denied requests run across all modules
- THEN wiring succeeds and existing response/error/permission outcomes remain equivalent.

### Requirement: Evidence-backed completion (VERIFY-06)

Completion MUST require reconciled inventories, executed required suites, coverage evidence and an explicit report of baseline failures and known architecture debt. Missing tools, credentials, infrastructure or results MUST be reported as pending or blocked checks, never passes.

#### Scenario: VERIFY-06.1 — Required environment is unavailable

- GIVEN a required check cannot execute
- WHEN verification summarizes readiness
- THEN it identifies the missing prerequisite and leaves completion unconfirmed.
