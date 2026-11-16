# Seed implementation progress

Change: user-profiles-roles-departments
Mode: Standard (no project config.yaml/strict TDD seed runner found).
Delivery: assigned independent seed work unit in the resolved auto-chain / feature-branch-chain plan; no branches, commits or PRs created.
Skills: gabriel-backend and sdd-apply, loaded from filesystem, plus shared OpenSpec persistence protocol.

## Implemented

- Inventoried all 17 historical `db/seeds/*.sql` files and master loader; preserved six existing user IDs/usernames used by documents, balances and cash registers.
- Added required role/department references, names, structured addresses and single phone value. Seeded protected system administrator, editable management/operational roles and independent READ-only example user. Matrix uses current API PermissionRegistry capabilities; custom roles confer no special authority by name.
- Added deterministic payment method IDs and fixed malformed 31-character payment allocation UUID literals.
- Removed competing obsolete `06_price_types.sql`; 05b owns price types/prices. Removed per-file deletions and disabled FK checks; a single guarded master performs a child-before-parent clean reset and parent-before-child reload.
- Reset clears business documents and non-seeded dependents (notifications, cash movements, source links, category snapshots, refresh tokens and sequences). Preserves Flyway schema history and V26 administration lock singleton.
- Corrected existing invoice and quote header totals, return source price/total, debit-note customer ownership, missing invoice/quote sequence rows and advances incorrectly marked applied without allocations.
- Replaced mutually inconsistent inventory snapshots with deterministic opening stock, actual confirmed invoice/purchase and return movements, cumulative movement arithmetic and active order reservations. Customer balances derive from actual return credits and unapplied approved payment advances.
- Historical June register examples now have real zero activity instead of invented summaries; their period has no business receipts in this fixture.
- Added target-guarded reset script and db/README.md describing destructive scope, dependency order, retries and required two-reset/login/access smoke.
- Added runtime MySQL integrity assertions covering declared FK orphans and 29 data invariants (profiles, grants, document totals/arithmetic, returns, notes, payment ownership/allocation, advances, stock/source references, balances, cash totals and sequences).

## Verification evidence

- `bash -n scripts/reset-beta-data.sh`: passed.
- `scripts/reset-beta-data.sh production cashly`: rejected before Docker, exit 2.
- `scripts/reset-beta-data.sh dev production`: rejected before Docker, exit 2.
- Literal seed UUID length audit: all UNHEX literal UUIDs are exactly 32 hexadecimal characters.
- Static loader simulation using temporary `/tmp/check_cashly_seeds.py`: adapted MySQL fixture statements to in-memory SQLite, loaded 18 data sources / 848 rows, passed all 29 invariant expressions and all declared FK orphan checks. This is an offline aid, **not** evidence of MySQL syntax, Flyway startup, Hibernate mapping, actual reset repeatability or application login.
- `git diff --check`: passed at seed handoff.
- Docker inspection `docker compose --env-file .env ps --status running`: blocked by permission denied connecting to `/Users/gabrielvargas/.docker/run/docker.sock`. No database reset or destructive execution performed.

## Task ownership / remaining

Seed portions of tasks 1.2, 4.1 and 4.2 are implemented. Broader fixture/mocks inventory and global task checkboxes belong to the orchestrator because they span API/app owners. Task 4.3 remains pending: execute fresh V26 startup, complete guarded reset twice, integrity SQL on actual MySQL, admin/demo login and API/UI revocation checks in an environment with Docker access. Task 4.4 reset documentation is implemented; remaining module/API docs and coverage inventory are outside this slice.

## Risks / limitations

The full-data fixture intentionally replaces beta data and uses generous stock opening quantities. Reset is not atomic; leave API traffic stopped and rerun the complete master after any error. SQL stored procedures and MySQL window/update syntax still require real Docker verification. No legacy migration promise, no archive, no production data operations.

## Final registry reconciliation

The API registry narrowed DOCUMENT_ADMIN to READ/EXPORT after the initial seed handoff. Removed the four unsupported CREATE/UPDATE/DELETE/EXECUTE rows. Added a fail-closed SQL assertion for every supported module/action combination, including unknown modules and unsupported IMPORT cells. Parsed the final Java PermissionRegistry offline and compared every seed grant: all match. The new SQL expression passes the actual fixture and rejects four injected invalid cells (DOCUMENT_ADMIN CREATE, DASHBOARD UPDATE, CATALOG IMPORT, UNKNOWN READ). The complete adapted fixture now has 848 rows and 29 passing assertions; this remains offline evidence only. Final V26 review confirms roles/departments/users column contracts and preservation of administration-lock row 1; actual MySQL/Flyway/reset/login checks remain pending Docker access.

## Cross-module Sales seed dependencies

After final backend owner checks introduced explicit inventory dependencies, operational Sales role 04 now has INVENTORY READ/EXECUTE in addition to SALES operations and CATALOG READ. Its description documents stock reservation execution and stock consultation. Demo role 06 retains no inventory permissions: permitted own-module lists/catalog consultation remain available, while stock-rich detail views require INVENTORY READ and stock-mutating sales actions require INVENTORY EXECUTE. Supported-cell whitelist is unchanged. Offline adapted fixture is now 850 rows and still passes 29 assertions plus FK/registry checks; no SQL reset or runtime MySQL verification was executed.
