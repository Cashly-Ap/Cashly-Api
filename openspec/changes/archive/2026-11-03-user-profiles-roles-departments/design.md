# Design: User profiles, roles and departments

## Technical Approach

Follow existing domain/application/infrastructure boundaries under `platform/auth`; add role and department owners with public application contracts. Angular remains standalone, OnPush, reactive forms and existing shared select/form components. This is specification only.

## Observed Current State

`user/domain/entity/User.java` stores one `UserRole` enum (SUPER_ADMIN, ADMIN, OPERATOR) and direct `BusinessModule` grants, not multiple roles. `UserController` uses fixed role checks and `/module-permissions`. `JwtAuthenticationFilter` trusts token access claims; account status alone is refreshed. `BusinessModule` has no CATALOG. Angular `users-form-page` saves profile and grants separately; `modulePermissionGuard` checks only modules. No tenant model was found: preserve the single deployment scope and all existing resource ownership checks.

## Proposed Decisions

| Choice | Alternatives / rationale |
|---|---|
| One required `roleId` and `departmentId` per user | Multiple roles deferred; selects keep assignment explicit. Departments never grant access. |
| Required `firstName`, `lastName`; optional address object and a nullable `phone` string | Keep username separate; validation limits are in user-profiles spec. Fresh seeds must be complete. |
| Persist Role, RolePermission, Department; deny omitted grants | No implicit permission from custom role names. Add CATALOG, ROLES, DEPARTMENTS independently of INVENTORY. |
| READ prerequisite; CREATE/UPDATE/DELETE/EXPORT/IMPORT/EXECUTE explicit | Explicit actions avoid HTTP-method guessing, especially report POST and status transitions. Unsupported cells disabled/rejected. |
| Protected system administrator role, identified by immutable system key | Name “SUPER_ADMIN” on a custom role grants nothing. System role cannot be renamed, deleted, disabled or have its matrix edited. Server-defined full grants bootstrap recovery; only an existing protected administrator may assign it. |
| Delegation restricted to actor grants | Role mutation/user assignment cannot escalate rights; lock affected administrator state transactionally to protect concurrent last-admin changes. |
| Current access resolved on every authenticated request | Replace trusted JWT role/module claims; avoids stale grants and cross-instance cache invalidation. Fail closed on lookup failure; refresh tokens issue current access. |

## Data Flow and Contracts

User form → profile use case → validate selectable role/department → atomic user save.

JWT subject → active account/current role lookup → permission evaluator → endpoint/use case → business owner. Existing tokens with obsolete grants confer no authority. Client restores current access, refreshes on 403/session restoration, and recalculates menus/actions; requests remain server-authorized.

- Users: extend create/update/detail DTOs with `firstName`, `lastName`, `address`, `phone`, `roleId`, `departmentId`; remove writable enum/direct grants and retire `/api/users/{id}/module-permissions` with no fallback bypass.
- `/api/roles`, `/api/departments`: list/detail/create/update/delete; role matrix saved atomically with optimistic version conflict detection. Referenced records cannot be disabled/deleted (409).
- Minimal active selection endpoints require authorized USERS administration, independent of management READ. Role options additionally filter assignable privilege scope.
- Current-access contract returns role identity and effective module/action grants, never editable from the client.
- Validation 400, missing references 404, authorization 401/403, lifecycle/version conflicts 409.

## Endpoint Coverage

The permission registry covers every application module. Each module independently supports no access, READ only, or selected supported actions; the inventory/catalog example is not hardcoded. Matrix tests cover every registered module and its applicable actions.

Inventory every controller mapping and client route/action before implementation. Map catalog products/categories/price types and commercial catalog to CATALOG; stock, warehouses, movements and inventory catalog stay INVENTORY. Map cross-owner operations explicitly to every required permission. Include exports/downloads, uploads/imports, bulk changes, status transitions, conversions, reports, lookups and asynchronous entry points. Unknown protected mappings deny. Preserve narrowly documented authentication/self-service exceptions; do not accidentally restrict login/logout/refresh.

## File Changes

| Paths (relative to API unless prefixed) | Changes |
|---|---|
| `src/main/java/com/cashly/cashly_api/platform/auth/{user,authentication,token}/` | Profile/reference DTOs, policy integration, JWT/session refresh |
| `src/main/java/com/cashly/cashly_api/platform/auth/{role,department}/` | New domain/use cases/adapters/controllers |
| `src/main/resources/db/migration/`, `db/seed.sql`, `db/seeds/*.sql` | Fresh beta schema and fully reconciled seed dependency graph |
| `src/test/`, `../cashly-app/src/`, `../cashly-app/e2e/` | Fixtures/mocks, API/action/guard/form tests, demo flow |
| `../cashly-app/src/app/{features/platform/auth,core/layouts}/` | New pages/selects, permission state/navigation |

## Beta Reset and Verification

No legacy-data migration required: user authorizes disposable beta data replacement. Do not execute a reset during planning. Future reset documentation must identify beta/dev/test target explicitly and reject production. Review every SQL seed, loader, bootstrap migration, test fixture and frontend mock. Load deterministic roles/departments before users, then dependent business records. Reconcile unused `06_price_types.sql` against loaded `05b_price_types_and_product_prices.sql`. Document clean-reset repeatability rather than assuming repeated INSERT safety. Avoid disabled FK checks where possible; always run orphan queries because reenabling checks does not validate earlier rows. Reconcile stock movements, document totals, payments, balances and returns. Verify empty-database startup, repeat reset, admin/demo login, navigation and catalog-only denial scenarios in Docker. Docker socket access is unavailable in this planning session.
