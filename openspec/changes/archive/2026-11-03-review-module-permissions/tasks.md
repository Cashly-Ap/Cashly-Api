# Tasks

## 1. Backend — catalog
- [x] 1.1 `PermissionCatalog` (domain services, next to `PermissionRegistry`): module → group → permission entries with `labelKey`, `descriptionKey`, `sortOrder`; static validation against `PermissionRegistry.modules()` (boot fails on drift). Lives in `domain/services` because the structure guard only allows `entity|repository|valueobjects|services|exceptions|architecture|fixtures` under `domain/`.
- [x] 1.2 Unit test: catalog is a bijection with the registry, no duplicates, deterministic order (9 tests).
- [x] 1.3 `GET /api/permissions/catalog` (`@PreAuthorize("hasAuthority('ROLES:READ')")`) returning modules → groups → permissions sorted by `sortOrder`.
- [x] 1.4 Web test: 200 with `ROLES:READ`, 403 without it; response shape asserted (4 tests).

## 2. Backend — audit
- [ ] 2.1 Inventory every `@PreAuthorize` action per module and diff it against `PermissionRegistry`; list missing/unused actions. (`endpoint-permission-inventory.json` in `user-profiles-roles-departments` is the input; reconciliation not executed here.)
- [ ] 2.2 Add the missing registry actions (e.g. `DASHBOARD:EXECUTE`) with their catalog entry. Blocked by design: `PermissionRegistry` was out of scope for this pass, so the catalog stays bijective with the current registry.
- [x] 2.3 Regression test that fails when a registry permission has no catalog entry (`PermissionCatalogTest` covers missing entry, unknown entry and duplicate classification).

## 3. Frontend — data layer
- [x] 3.1 Models: `PermissionCatalogModule`, `PermissionGroup`, `PermissionEntry`.
- [x] 3.2 `PermissionCatalogService`: loads the catalog from `GET /api/permissions/catalog`.
- [x] 3.3 Service spec: success and error paths.

## 4. Frontend — detailed permissions in the role editor
- [x] 4.1 `PermissionEditorComponent` renders one section per module, one block per group and one row per permission with its business label and description.
- [x] 4.2 Allowed/denied state per row; denied rows always visible; module presets (No access / Read-only / Customize / All available) preserved and kept in sync with the rows.
- [x] 4.3 Delegation rules preserved: rows outside the actor's scope are not editable and are labelled.
- [x] 4.4 `Roles → Edit` offers edit and delete for every non-system role; only `SYSTEM_ADMIN` stays read-only.
- [x] 4.5 i18n es/en: group labels plus one label and description per permission (94 keys per language after removing the screen-only keys).
- [x] 4.6 Editor and page specs updated (31 tests across the role feature).
- [x] 4.7 No standalone `/platform/permissions` page, route, menu entry or `moduleForUrl` mapping: a first pass added them and they were removed on review.

## 5. Verification
- [x] 5.1 Full backend suite green: 4094 tests, 0 failures, 0 errors, 3 skipped; real startup proves the catalog validation passes.
- [x] 5.2 Full frontend suite green (310 files / 3201 tests) and development build clean.
- [x] 5.3 Manual browser check: `Roles → Operator → Edit → Permissions` shows groups with allowed/denied per row (Sales → Documents/Transitions/Reports); toggling `COMPANY:READ` switches the preset to read-only; `/platform/permissions` no longer exists and the Platform menu lists only Users, Roles and Departments.
- [x] 5.4 `docs/modules/ACCESS_CONTROL.md` updated: catalog endpoint row, V26→V2 correction and a new "Permission catalog and matrix" section.
- [x] 5.5 Verification recorded in `verify-report.md`.
