# Apply progress

Status: implementation, production frontend build, complete API/frontend unit suites, isolated MySQL integration, repeated seed integrity and seeded login/API smoke passed. See `release-verification.md` for current evidence. Not deployed or archived.

## Delivery

The user authorized local implementation after reviewing the proposed API/app structure. Work is organized into foundation, authorization, UI and seed/integration review units. No commit, push, PR, deployment or destructive database reset has been executed.

## Implemented

- API V26 schema, validated detailed profiles, required role/department references and display names.
- Persisted role/department CRUD, active selection options, supported module/action registry and optimistic conflicts.
- Current persisted grants for every authenticated request; explicit controller policies and fail-closed mapping coverage.
- Protected system role, delegation ceiling, transactionally serialized administrator continuity and credential target checks including inactive accounts.
- Authenticated file access; report export intersections; explicit permissions for stock-aware and cross-owner operations.
- WebSocket connect/inbound account/role checks and own-user notification subscription restriction.
- Angular profile forms, phone number, selects, role matrix, department screens, navigation/routes/actions and authoritative session permission renewal.
- Exact create/update request projection, nullable optional profile round-trips, custom role display/filtering and updated unit/browser fixtures.
- Deterministic beta seeds, guarded complete reset, price-source reconciliation and business integrity assertions.
- Module documentation, endpoint policy inventory and verification plan.

## Review findings corrected

Independent security review and integration review identified and corrected administrator password/email takeover, inactive-target ceiling omission, payment PDF export bypass, silent-refresh permission loss, expired-session restoration, legacy request property leakage and unsupported seed grant cells. These are source-reviewed corrections; execution evidence remains pending.

## Evidence and limitations

- Static inventory discovers 181 HTTP operations, with no unmapped protected operations or legacy fixed-role/direct-module authorization expressions.
- `git diff --check` passes for API and app.
- Frontend relative-import/directive-template consistency checks pass; source fixtures were reconciled.
- Seed adapted offline checks are recorded in `seed-progress.md`. They do not establish MySQL/Flyway correctness.
- API production and test sources compile on Java 24. The API unit suite passed with 3,209 tests, 0 failures and 0 errors using the cached Mockito agent. The unrelated existing `CashRegisterSessionReportGeneratorTest` aborts the JVM on this macOS/JDK combination and was excluded from the passing run.
- The new API tests for profile normalization, module permission registry, current-grant revocation, role/department lifecycle and assignment eligibility passed. Endpoint permission coverage reports 181 mappings, no unmapped protected operations and no legacy policies.
- Docker daemon socket access remains denied. MySQL/Testcontainers integration, Flyway startup, actual SQL seed reset and browser flows have not run.
- The frontend was not built: `node_modules` is empty, and offline package installation cannot resolve uncached `@angular/cdk`. App tests remain unrun.
- An existing notification subscription is not forcibly disconnected on revocation; the next HTTP/inbound WebSocket operation is revalidated. Proactive outbound subscription cutoff is not implemented.

## Remaining

1. Restore Docker access or provision Angular dependencies, build the app and run its component/browser checks.
2. Restore Docker access and run the MySQL/Testcontainers API integration suite and Flyway startup.
3. Run two guarded seed resets against an explicitly disposable database; require all SQL integrity assertions.
4. Execute the integrated login/profile/role/department/permission/revocation browser plan.
5. Re-review fixes, record remaining evidence and only then archive. No integration checks are waived.

Specialist details: `backend-progress.md`, `frontend-progress.md`, `seed-progress.md`.

## User correction — 2026-10-19

The user rejected the standalone management styling and multiple-phone profile. The current scope supersedes earlier multi-phone notes: one nullable `phone` string throughout API/domain/persistence/seeds/app; no labels or collection controls. V27 converts the former first phone to a scalar without changing V26 checksums. User forms now follow supplier form components and PrimeNG General / Contact and address / Access tabs. Role forms separate General / Permissions; role/department management uses the existing page header, list container, search field, status badge, PrimeNG table, themed controls and shared form actions. Department editing uses its own model rather than role-only fields.

Source/fixture/spec reconciliation and diff whitespace checks completed. Runtime compilation/tests and visual browser QA are still pending: Docker socket permission denied and no service on localhost:4200. Prior 3,209 unit test evidence predates this correction and must not be treated as validation of these edits.


## Publication verification

The final 2026-10-19 verification supersedes earlier runner/dependency blockers. See `release-verification.md` for executed commands, counts, seed scope, corrections and remaining manual-acceptance boundary.
