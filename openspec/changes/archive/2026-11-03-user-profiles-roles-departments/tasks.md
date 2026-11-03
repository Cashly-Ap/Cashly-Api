# Tasks: User profiles, roles and departments

## Review Workload Forecast

| Field | Value |
|---|---|
| Estimated changed lines | 2,000–4,000 across API/app/seeds/tests |
| Delivery strategy | auto-chain; user authorized implementation, local work units only |
| Suggested split | Foundation → policy → management UI → seed/integration verification |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Verification |
|---|---|---|
| 1 | Schema, domains and profile contracts | Domain/profile validation tests |
| 2 | Role policy and endpoint coverage | Direct API denial/revocation tests |
| 3 | Management UI and all client action guards | Forms/guards/action tests |
| 4 | Consistent beta seeds and end-to-end wiring | Fresh/reset integrity and login smoke |

Implementation is organized into the four review units below using a feature-branch chain if PR delivery is subsequently requested. Each unit includes tests and documentation. The user authorized local implementation; no commit, branch publication or PR creation is requested.

## Phase 1: Inventory and Foundation

- [x] 1.1 Inventory every protected controller mapping, frontend route/action and public/self-service exception; record module/action mapping and cross-owner dependencies.
- [x] 1.2 Inventory `db/seeds/*.sql`, `db/seed.sql`, bootstrap migration inserts, `src/test/` fixtures, app mocks and `e2e/` data; trace all user/business foreign keys.
- [x] 1.3 Write failing domain tests for profile limits, role matrix, required references and lifecycle conflicts; add role/department domains and schema under `platform/auth/` and `db/migration/`.
- [x] 1.4 Extend user request/response/mappers/persistence with validated profile and atomic `roleId`/`departmentId` assignment; test invalid references leave no partial changes.

## Phase 2: Authorization

- [x] 2.1 Implement role/department use cases/controllers, minimal selection endpoints, version conflicts and permission matrix validation; test reference-safe deletion/deactivation.
- [x] 2.2 Replace trusted access claims in `JwtAuthenticationFilter` and token/session contracts with current server-side permissions; test revocation using already-issued access/refresh tokens.
- [ ] 2.3 Replace fixed-role/direct-module checks across all mapped endpoints; retire user direct-grant writes; parameterize denial, READ-only and selected-action tests for every module, including bulk/report/import/export/transitions without side effects.
- [ ] 2.4 Test custom-name non-bypass, delegation ceiling, system-role assignment and concurrent last-admin protection.

## Phase 3: Angular

- [x] 3.1 Update `users-form-page`, details/models/services with profile controls and required searchable selects; remove direct permission tabs and fixed role enum editing.
- [ ] 3.2 Add Roles matrix and Departments CRUD pages under `features/platform/auth/`; test invalid/stale selection and referenced record conflicts.
- [x] 3.3 Update auth state, `modulePermissionGuard`, `app.routes.ts`, layouts and every feature action to module/action access; refresh permissions on session restore/403.

## Phase 4: Disposable Beta Data and Verification

- [x] 4.1 Update every inventoried seed/fixture/mock; reconcile duplicate price-type seeds and master loader; seed deterministic system admin, departments and inventory-denied/catalog-read-only demo role/user.
- [ ] 4.2 Document target-guarded beta/dev/test clean-reset order/repeatability; validate orphan references, document totals, stock, payments, returns and balances after complete load.
- [ ] 4.3 Run Docker API/app checks plus fresh startup and repeated-reset smoke; verify administrator login and demo navigation/direct API restrictions.
- [x] 4.4 Update module/API/reset documentation, reconcile coverage inventory and record verification evidence before archive. Do not archive during planning.
