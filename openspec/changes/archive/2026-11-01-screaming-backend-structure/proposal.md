# Proposal: Business-oriented backend structure

## Intent

Make the whole backend navigable by business module and justified submodule while preserving current ERP behavior. Extend the inspected Warehouses/Customers plan to every production and test package; a directory change must not conceal lost tests or claim existing dependency leaks are fixed.

## Scope

### In Scope

- Account for all 1,248 production Java files, 573 Java test sources, and inventoried non-Java test artifacts/resources, including nine `.bak` files and two test resources.
- Define one destination per existing artifact in `module-map.md` and its inventories; retain classes, signatures, fixtures and business ownership.
- Organize real capabilities before layers; keep aggregate children together, operation inputs beside their use cases, and public directional ports explicit.
- Specify architecture guards, complete regression discovery, isolated MySQL verification, coverage gates, startup and all-module contracts in `verification-plan.md`.

### Out of Scope

Implementation during planning; new endpoints, overview classes, domain events, database/schema/SQL changes, dependencies, financial formulas, authorization changes, concurrency fixes, source deletion or test suppression. Overview locations are reserved extension points only.

## Capabilities

### New Capabilities

- `backend-regression-verification`: complete inventory, reproducible baseline comparison and regression evidence for structural migrations.

### Modified Capabilities

- `modular-architecture`: add package ownership, relocation completeness and compatibility requirements; retain all existing requirements unchanged.

## Approach

Use the prior Warehouses/Customers plan (retired after the migration), `module-map.md`, `design.md` and `verification-plan.md`. Relocate packages and references in compiling review units. Preserve mixed composition roots and existing synchronous transactions.

This change supersedes **only infrastructure layout** in active `application-package-naming`: use `infrastructure/web`, `integration` and persistence role directories. Directional `application/ports/in|out` and existing capability contracts remain. Do not edit or archive that change or main specs here.

## Affected Areas

| Area | Impact |
|---|---|
| `src/main/java/com/cashly/cashly_api` | Proposed complete package mapping |
| `src/test`, test artifacts and build verification | Proposed mirrored ownership and executable evidence |
| OpenSpec/current documentation | New change artifacts and explicit convention reconciliation |

## Risks

Stale implicit/FQN references, Spring/JPA wiring, undiscovered `*IT`, and silent coverage loss require manifest checks and baseline comparison. Existing cross-module leaks remain identified debt with owner and closure milestone; relocation does not establish their compliance.

## Rollback Plan

Revert each migration unit using its reverse manifest, including consumer references/tests/configuration; rebuild and repeat the baseline checks. No data migration is needed.

## Dependencies

Docker, isolated MySQL, safe test configuration and the complete source/resource inventories.

## Success Criteria

- [ ] Every artifact accounted for without deletion or destination collisions.
- [ ] All-module behavior, contracts and transactions preserved.
- [ ] Complete tests/`*IT`, coverage gates and startup evidence pass against baseline.

Current evidence: 11 architecture checks passed; the full suite has not run.
