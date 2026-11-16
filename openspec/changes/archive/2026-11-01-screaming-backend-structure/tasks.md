# Tasks: Business-oriented backend structure

## Review Workload Forecast

| Field | Value |
|---|---|
| Estimated changed lines | 5,000–20,000+; measure each slice |
| Delivery strategy | ask-on-risk |
| Suggested split | Foundation → module slices → shared/root → complete verification |
| Chain strategy | deferred until PR creation; local work units verified |

Decision needed before apply: Resolved — user authorized local implementation. Verify local work units; select a PR chain when PR creation is requested.
Chained PRs recommended: Yes
Chain strategy: deferred until PR creation; no PR requested
400-line budget risk: High

Slice boundaries and rollback: [work-units.md](work-units.md). No branches, commits or PRs are requested; local implementation follows the authorized scope.

## Phase 1: Baseline and migration safeguards

- [x] 1.1 Freeze and reconcile `source-moves.json`, `test-moves.json`, `resource-inventory.json`: 1,248 production Java, 573 test Java, all 584 test artifacts, including nine backups and two resources (STRUCT-02).
- [x] 1.2 Establish the isolated Docker/MySQL baseline and evidence capture using `verification-plan.md`; distinguish discovered/executed/skipped suites (VERIFY-01/02).
- [x] 1.3 Prepare symbol-aware relocation tooling and architecture guards; cover wildcard/implicit/FQCN/nested references, destination collisions and nonempty nested-layer scans (STRUCT-03).

## Phase 2: Business owners

Each entry includes `src/main/java` and `src/test` manifest slices, **all consumer references**, wiring, fixtures and verification. Follow `module-map.md` and `work-units.md`; preserve behavior (STRUCT-01/04).

- [x] 2.1 Migrate `auth`; retain authentication/token/user.
- [x] 2.2 Migrate `categories`; preserve usage guards.
- [x] 2.3 Migrate `price_types`; preserve usage contracts.
- [x] 2.4 Migrate `payment_methods`; preserve usage/toggle behavior.
- [x] 2.5 Migrate `suppliers`; retain aggregate contacts.
- [x] 2.6 Migrate `products`; retain photos/stock/transaction composition.
- [x] 2.7 Migrate `warehouses`; retain management/stock transactions and locks.
- [x] 2.8 Migrate `customers`; retain profile/balance/credit/warehouse_access.
- [x] 2.9 Migrate `quotes`; preserve conversions and historical categories.
- [x] 2.10 Migrate `orders`; preserve allocations and conversion boundaries.
- [x] 2.11 Migrate `invoices`; preserve editing and financial effects.
- [x] 2.12 Migrate `purchases`; preserve inventory participation.
- [x] 2.13 Migrate `payments`; retain collection/credit.
- [x] 2.14 Migrate `returns`; preserve nested exception references.
- [x] 2.15 Migrate `credit_notes`; preserve adjustment contracts.
- [x] 2.16 Migrate `debit_notes`; preserve adjustment contracts.
- [x] 2.17 Migrate `cash_register`; retain sessions/reporting exceptions.
- [x] 2.18 Migrate `receivables`; preserve financial queries.
- [x] 2.19 Migrate `reports`; retain existing capability owners.
- [x] 2.20 Migrate `dashboard`; preserve combined reads.
- [x] 2.21 Migrate `notifications`; preserve metadata/after-commit delivery.

## Phase 3: Shared and root ownership

- [x] 3.1 Reconcile `shared` source/test manifests, storage and neutral values; update every consumer.
- [x] 3.2 Reconcile root `infrastructure`, `CashlyApplication.java` and root tests/configuration; preserve bootstrap/security scans and resources.

## Phase 4: Complete verification and closure

- [x] 4.1 Run clean compilation/full suites, all seven current ITs, discovery reconciliation and isolated persisted rollback/concurrency scenarios (VERIFY-02/04).
- [x] 4.2 Compare coverage: domain >90%, application >80%, infrastructure >60%; enforce module/layer nonregression (VERIFY-03).
- [x] 4.3 Verify startup and all-module HTTP/security/JSON/errors/pagination equivalence; execute equivalent CI and ancillary checks (VERIFY-05).
- [x] 4.4 Reconcile every artifact, preserve evidence, document debt owners/closure milestones, update current guides and write verification report (STRUCT-05/VERIFY-06).

Implementation applied and verified: 3,231 Java tests, zero failures/errors/skips, 560 ordinary classes and eight IT reconciled. All layer/module coverage gates pass. The shared CI harness ran locally; remote CI was not executed. No new business endpoints or overview classes were introduced.
