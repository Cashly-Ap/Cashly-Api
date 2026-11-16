# U01 baseline and verification harness progress

## Scope

Implementation authorized by the user's instruction to proceed. This work unit changes only isolated test infrastructure/scripts and evidence; it does not alter production/test sources. Delivery: local verified foundation slice, no branch/commit/PR creation. Standard implementation mode; no strict-TDD project configuration was present for this harness slice.

## Completed

- Froze `src`, `pom.xml`, `.mvn`, and `mvnw` before source migration at `/tmp/cashly-screaming-baseline-20261016-immutable`.
- Snapshot: 1,248 production Java, 573 test Java, 584 total test artifacts, 1,865 files in the checksum manifest.
- Snapshot manifest SHA-256: `21ffe2e4756ae038e4761c74e96cc7f65e9caa28e9631e4175dbc341665967d8`.
- Created `compose.screaming-test.yaml` with separate suite/Flyway and inventory/create-drop MySQL, project-scoped network/cache and no published host ports or development volumes.
- Created `scripts/run-structure-verification.sh`: unique project guard, read-only source input, immutable per-run copy into writable runner workspace, complete explicit Surefire selector including IT, restricted ignored evidence export and no automatic cleanup.
- Created `scripts/summarize-structure-verification.py`: sanitized suite/case identities, counts, discovery reconciliation, original XML hashes and line coverage by module/layer; no XML environment properties or arbitrary error messages are published.
- `docker compose ... config --quiet` and `bash -n` passed.

## Current execution

Project: `cashly-screaming-baseline-20261016-frozen`.

```sh
SCREAMING_TEST_IMAGE=cashly-api-app:latest SCREAMING_PROJECT=cashly-screaming-baseline-20261016-frozen scripts/run-structure-verification.sh baseline /tmp/cashly-screaming-baseline-20261016-immutable
```

The locally available Docker image runs Corretto 24.0.1 and Maven wrapper 3.9.11. Its entrypoint is overridden; the development application is not started. Source is copied from the frozen snapshot, never the live tree under migration. The baseline includes all ordinary classes and all seven IT with explicit inventory and RETURNS datasource properties.

Raw restricted evidence: `target/structure-verification/cashly-screaming-baseline-20261016-frozen/` (already gitignored). The databases and runner remain available after success/failure; cleanup is explicit and scoped to this unique project after report export.

Status: BASELINE PASS. `clean verify` completed at 2026-10-16 09:58:53 UTC in 1 minute 22 seconds: **3,160 tests, 0 failures, 0 errors, 0 skipped**. All 559 ordinary test owners and seven IT owners were reconciled against 755 XML reports; no executable owner was missing. This is baseline evidence only, not a migrated-tree pass. Final migrated execution awaits the root agent's readiness signal.

Sanitized evidence: `evidence/full-baseline/summary.json` and `summary.md`. They contain original-report SHA-256 hashes, per-suite results, discovery reconciliation and coverage; raw XML and arbitrary logs remain only under the restricted ignored output directory.

Baseline JaCoCo line coverage (counter sums, not averaged percentages):

| Layer | Covered | Missed | Line coverage |
|---|---:|---:|---:|
| Domain | 2,947 | 182 | 94.1834% |
| Application | 4,528 | 453 | 90.9054% |
| Infrastructure | 6,264 | 1,990 | 75.8905% |

The global layer thresholds are satisfied by the baseline. Per-module/layer counters are recorded for the final no-regression comparison. Unknown startup/API-specific acceptance requirements remain pending independently of a full existing-suite pass.

## Design adjustment

The proposed named volume directly at `/workspace/target` was replaced by an ordinary directory in the writable runner filesystem. This lets `mvn clean` remove/recreate target safely. The source is mounted read-only at `/source` and copied once to `/workspace`; evidence is exported with Compose before any teardown.
