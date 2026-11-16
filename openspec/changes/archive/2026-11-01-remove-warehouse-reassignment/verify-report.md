# Verification report

Status: PASS — frontend removal, backend removal, both conversion defects and the final development runtime are verified.

## Delivered behavior

Standalone warehouse reassignment dialogs, list/detail actions, application/API methods, request types and exclusive backend operations are removed for quotes and orders. No equivalent standalone invoice flow existed. Initial creation/editing allocations and normal conversion dialogs remain. Removed backend operations are PUT /api/quotes/{id}/allocations and PUT /api/orders/{id}/allocations. Exclusive domain methods, dead order gateway/adapter and obsolete tests are removed; replacement menu/route checks verify the removed feature stays unavailable.

## Two distinct 409 causes

1. Category snapshot identity: quote and order repository saves rebuilt @MapsId snapshots without retaining persisted IDs. Hibernate treated existing snapshots as new and raised DuplicateKeyException. The repositories restore IDs only for snapshots already present in the database and perform read/save in one transaction. Fresh snapshots keep null IDs so creation still works. Regression tests reproduce all three original conversion failures, then verify conversions, fresh creation, repeated-conversion rejection and rollback of an existing source document.
2. Invoice payment FK: the user-provided error timestamp 2026-10-16T22:18:38.175072459 matches MySQL 1452 on fk_payment_allocations_invoice. Cash quote/order conversion inserted payment allocations before the invoice existed. Cash creation now persists the draft invoice first; InvoiceRepositoryAdapter uses saveAndFlush to guarantee visibility before financial references are inserted. Authorization, confirmation and all financial/stock effects remain in the same surrounding transaction. Credit flow remains unchanged. Flush is not a separate commit.

Real Spring/MySQL financial regressions reproduce the exact FK in three flows before the fix and pass after it. They exercise full payment, partial balance plus payment, full balance, order conversion, and forced failure after actual stock deduction. Rollback verifies the existing quote remains DRAFT and invoice, payment, cash, balance and stock effects leave no partial state.

## Executed checks

| Check | Result |
|---|---|
| Frontend focused quote/order suites | 549 tests passed |
| Frontend complete suite | 258 files, 2,502 tests passed; zero failures/skips |
| Angular production build | Passed; existing stompjs CommonJS warning only |
| Backend route-removal/domain/config checks | 35 passed in an independent source copy |
| Snapshot conversion regression and adapter/mapper checks | 37 passed; five MySQL regression cases |
| Real cash conversion regression and invoice unit checks | 37 passed; five full-context MySQL cases |
| Final Java 24 clean verify and executable JAR | 3,239 tests passed, zero failures/errors/skips |
| Discovery reconciliation | 565 ordinary executable classes and all 10 MySQL IT; zero missing |
| Coverage/relocation tooling | 20 Python tests passed in Docker |
| OpenSpec strict validation | Passed, zero issues |
| Whitespace checks | Passed in both repositories |

Final layer coverage: application 91.811%, domain 94.3244%, infrastructure 77.6797%. All global thresholds and retained-code module/class/family nonregression checks pass. Only explicitly deleted feature classes and uniquely deleted executable lines were subtracted from the historical baseline; no modified class, retained method or threshold was waived. The exact baseline adjustment is recorded in evidence/removed-production-coverage-audit.json and coverage-equivalence.md.

## Evidence and runtime

Sanitized final reports are in evidence/full-final/ with original report hashes and exact command/runtime/source identity. The earlier 3,234-test run passed before the user reported the second financial FK; it is retained in evidence/pre-settlement-fix/ and is not substituted for the final result. Original logs/reports remain ignored under target/. Frontend summary and log hash are in evidence/frontend-verification.json.

The local backend was compiled and restarted after the final source changes. HTTP /actuator/health returned 200/UP; compiled InvoiceRepositoryAdapter contains saveAndFlush; obsolete assignment components are absent from the runtime classpath. Development customer documents and balances were not mutated for testing. All new MySQL tests fail closed outside the isolated verification database. Only task-owned verification containers/volumes are removed after evidence export. No commit or push was requested for this follow-up.
