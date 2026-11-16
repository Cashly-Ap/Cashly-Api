# Release verification — 2026-10-19

## Results

- API: `docker exec cashly-api-app ./mvnw -B test` — 3,212 tests, zero failures/errors/skips.
- Isolated MySQL integration: Maven `test -Dtest=*IT` in project `cashly-screaming-auth-release` — 100 tests, zero failures/errors/skips; all 27 Flyway migrations applied on an empty schema. Includes 73 startup/security HTTP assertions, stock concurrency and financial rollback checks.
- Seeds: the reset script's MySQL client body ran twice against isolated `cashly_test`; both runs reported `Cashly beta seed integrity passed`. All declared foreign keys and business integrity assertions passed. The database was cloned from the independently migrated schema; development data was never reset.
- Seed login/API smoke: administrator and demo login succeeded; current access matched each role; administrator could read users/roles/departments; user details exposed scalar `phone`; demo could read products, was denied inventory and was denied a structurally valid product creation request.
- Frontend: `bun run test:run` — 264 files and 2,583 tests passed. Production `bun run build` passed. The existing STOMP CommonJS optimization warning and shared reactive-form disabled warnings remain non-fatal.
- Changed frontend TypeScript/HTML formatted with Prettier; both repository whitespace checks passed.
- Previous browser evidence covers customers comparison, responsive department list, separate detail/form, role General/Permissions tabs, user tabs/single phone, and full-width trailing detail fields.

## Corrections found during final verification

The login integration fixture now completes authoritative access loading. Menu/module expectations include roles, departments and catalog, and invoices belong to FINANCES. MySQL 8 does not implement `--abort-source-on-error`; the reset script now assembles the master and seed files into one validated temporary batch and stops on SQL error. Conditional Tailwind class bindings were expressed in a form accepted by both Angular and Prettier without changing their behavior. User documentation now describes one optional phone.

## Scope

This records readiness of the source changes for version-control publication. It does not record deployment or completion of every manual acceptance scenario. The change remains unarchived. Reports/logs are retained locally under `cashly-api/target/access-release-verification` and `/tmp/cashly-*.log`; temporary verification containers and volumes were removed after evidence export. Development containers and data were preserved.
