# Capability-first backend restructuring

Status: implemented and verified locally; ready for review. This change covers every backend owner and all test artifacts, not only Warehouses and Customers.

## Read the change

1. [Proposal](proposal.md): scope and behavior preservation.
2. [Module ownership](module-map.md): all 21 business modules, shared/global infrastructure and composition exceptions.
3. [Complete current structure](trees/before.md) and [complete implemented structure](trees/after.md): every production/test/resource file.
4. [Design](design.md), [review units](work-units.md) and [implementation tasks](tasks.md).
5. [Verification plan](verification-plan.md) and [actual evidence report](verify-report.md).

## Exact inventories

| Artifact | Existing files |
|---|---:|
| [Production Java destinations](source-moves.json) | 1,248 |
| [Java test/helper destinations](test-moves.json) | 573 |
| [Resources and noncompiled backups](resource-inventory.json) | 41 |
| Total unique artifacts | 1,862 |

[Test discovery](test-discovery-inventory.json) distinguishes 559 ordinary executable classes, seven IT, six helpers and one empty class, plus nine backups and two test resources. File counts are not passed-test counts. [Dependency evidence](dependency-baseline.json) identifies existing imports without claiming module isolation is repaired.

## Validation so far

- Official OpenSpec 1.14.2 strict change validation passed without issues.
- The preparation validator reconciled every manifest/hash/destination/tree and passed four negative checks.
- Complete pre-migration baseline: 3,160 tests passed, zero skipped, including all seven MySQL IT.
- Implemented integrity: all 1,862 baseline artifacts reconciled, zero unexpected semantic changes.
- 21 current architecture/boundary tests pass; 3,231 post-migration tests pass with zero failures/errors/skips, all eight IT discovered, all coverage gates passing. Identical 63-case startup checks passed before and after.

The staged specs are [modular architecture](specs/modular-architecture/spec.md) and [backend regression verification](specs/backend-regression-verification/spec.md). Main specs and archived changes are untouched. Production/test relocation is applied under subsequent user authorization. No commits or PRs were requested. Existing dependency debt is recorded in dependency-debt.json.

## Subsequent ownership change

The user subsequently approved [module-owned-reporting](../module-owned-reporting/proposal.md), which supersedes this change’s global reports package destinations. This change’s manifests and evidence remain the historical verified snapshot; use the follow-up manifest and current complete tree to resolve report paths.
