# Verification report

Status: PASS — implementation and local verification complete.

## Verified implementation

- All 1,248 production Java files and 573 original Java test/helper files reconcile with the move manifests. All 41 resources/backups retain their original bytes. Independent Git HEAD baseline hashes match every artifact; no unexpected semantic changes. See [implemented integrity](evidence/implemented-integrity.json).
- Production bodies, annotations and contracts are preserved after canonicalizing package/import/type references. Four existing structural test guards were updated for the new packages; one shared test helper gained test-only visibility. Two tests were added: capability architecture guards and real HTTP startup verification.
- Production and test compilation passed. Seven relocation-tool regression tests passed, including wildcard import precedence and nested/binary type references.
- 21 focused architecture/boundary checks passed. See [architecture evidence](evidence/architecture-green/summary.json).
- All 190 cross-module import edges are preserved after canonicalizing type names: zero new/removed edges. Existing 83 foreign-internal accesses remain debt; they are not newly isolated by a package move. See [dependency evidence](evidence/dependency-integrity.json) and [accountability and closure gates](dependency-debt.json).

## Behavioral baseline and HTTP equivalence

The complete frozen pre-migration baseline passed **3,160 tests**, with zero failures, errors or skips, including all seven original MySQL integration suites. Discovery reconciled 559 ordinary executable classes and seven IT classes. Coverage: domain 94.1834%, application 90.9054%, infrastructure 75.8905%. See [baseline](evidence/full-baseline/summary.md).

The identical new BackendStartupMySqlIT also ran on an independently extracted original Git tree and on the migrated tree: **63 HTTP cases passed on each**. It starts the full application with Tomcat, MySQL/Flyway, real password hashing and JWT login. It checks authenticated JSON reads across all 21 modules, anonymous rejection, module permission rejection and a persisted missing-resource response. The test fails before database startup unless the URL names the isolated verification database. See [startup baseline](evidence/startup-baseline.json).

Existing unit/controller/integration cases provide validation, response shape, pagination, transaction, concurrency, rollback, reports, storage and notification coverage. The new smoke test complements those cases; it is not an exhaustive real-world UI test.

## Execution attempts and final gates

The first final run found an old package prefix assembled by string concatenation in CustomerCreditQueryBoundaryTest; the assertion was updated to the relocated credit port package. That run later exited 137 while the separate baseline startup suite was running concurrently on the 8 GB Docker host. It is **incomplete**, not a passing run: 2,660 cases reported, one failed assertion, 100 executable classes still missing. Its local ignored raw evidence remains under target/structure-verification/cashly-screaming-final-20261016-run1/.

The corrected tree passed clean verify and executable JAR packaging in cashly-screaming-final-20261016-run2: **3,231 tests, zero failures/errors/skips**, all 560 ordinary executable classes and eight IT reconciled. The total is the 3,160-case baseline plus eight additional architecture cases and 63 startup cases. Domain coverage remains 94.1834%; application improves to 91.046%; infrastructure improves to 76.993%. All required aggregate thresholds and every module/layer nonregression comparison pass. See [final summary](evidence/full-final/summary.md), [exact coverage comparison](evidence/full-final/coverage-comparison.json) and [execution provenance](evidence/full-final/provenance.json).

## CI and operational scope

GitHub CI, PR Check and GitLab test jobs use the same isolated verification harness and explicit IT selection. The harness exports reports even after Maven failure, generates sanitized summaries and enforces coverage/discovery gates. Compose configuration, shell syntax and diff whitespace checks passed. The analyzer executed successfully during run 1. Remote CI execution has not been requested or observed; local execution uses the existing Java 24 image, while CI defaults to the pinned Maven Java 24 image.

Only dedicated verification projects are cleaned up. Development services/data are preserved. No SQL migrations, business rules, endpoint contracts, commits, pushes or deployments were introduced. Main OpenSpec specifications and other changes remain untouched; this change stays available for review rather than automatically archiving unrelated work.
