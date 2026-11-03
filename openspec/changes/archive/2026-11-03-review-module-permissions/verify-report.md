# Verify report — review-module-permissions

Date: 2026-11-01
Scope delivered: grouped permission catalog (backend) + detailed grouped permissions inside the role editor (frontend).

## Spec compliance

| Requirement | Scenario | Verdict | Evidence |
|---|---|---|---|
| Grouped permission catalog | Every registered permission is classified | COMPLIANT | `PermissionCatalogTest` bijection test (46 pairs, 11 modules, 21 groups) |
| Grouped permission catalog | Startup fails on drift | COMPLIANT | static validation + drift tests (missing entry, unknown entry, duplicate classification) |
| Catalog endpoint | Authorised read | COMPLIANT | `PermissionCatalogControllerTest` 200 with `ROLES:READ` |
| Catalog endpoint | Denied read | COMPLIANT | 403 with another module authority, 401 anonymous |
| Catalog endpoint | Entry metadata | COMPLIANT | JSON assertions on `action`, `labelKey`, `descriptionKey`, `sortOrder`, `key` |
| Detailed permissions in the role editor | Grouped detailed permissions | COMPLIANT | editor + page specs; manual: Sales shows Documents/Transitions/Reports with allowed/denied per row |
| Detailed permissions in the role editor | Toggling a permission | COMPLIANT | manual: checking `COMPANY:READ` moved the module preset from No access to Read-only and the row to Allowed |
| Detailed permissions in the role editor | Delegation ceiling | COMPLIANT | editor specs (`outsideYourScope`) |
| Detailed permissions in the role editor | System administrator role | COMPLIANT | editor specs; tab stays read-only |
| No standalone permission screen | No permission route | COMPLIANT | `/platform/permissions` falls back to the dashboard; Platform menu lists only Users, Roles, Departments |

## Automated tests

| Suite | Command | Result |
|---|---|---|
| Backend full | `docker compose --profile test run --rm app-test ./mvnw clean test` | 4094 tests, 0 failures, 0 errors, 3 skipped |
| Backend focused | `./mvnw test -Dtest='PermissionCatalogTest,PermissionCatalogControllerTest'` | 13 tests, 0 failures |
| Frontend full | `./node_modules/.bin/vitest run` (in `cashly-app-dev`) | 310 files, 3201 tests passed |
| Frontend build | `./node_modules/.bin/ng build --configuration development` | success (only the pre-existing dashboard NG8107 warning) |
| Formatting | `prettier --check` on touched files | clean |

## Manual verification (Playwright, dev stack)

| Case | Steps | Expected | Actual | Verdict |
|---|---|---|---|---|
| Operator row actions | Roles list → Operator row menu | Edit and Delete offered | `View details / Edit / Delete` | PASS |
| System administrator row | Roles list → System administrator row menu | Only detail | `View details` | PASS |
| Grouped detailed permissions | Roles → Operator → Edit → Permissions → Customize a module | Groups with label and allowed/denied rows | Sales shows Documents/Transitions/Reports, `Read sales documents` allowed, rest denied | PASS |
| Permission toggle | Check `COMPANY:READ` | Preset switches to read-only, row shows allowed | Preset changed to Read-only, row Allowed | PASS |
| Standalone screen removed | Open `/platform/permissions` | Falls back to dashboard | Redirected to `/analytics/dashboard` | PASS |
| Menu entry removed | Open Platform menu | No Permissions entry | `Users Roles Departments` | PASS |
| API contract | `GET /api/permissions/catalog` with bearer token | 200 + grouped JSON | 200 with `module`, `groups[].key`, `permissions[].action` | PASS |
| API guard | Same request without token | 401 | 401 `AUTHENTICATION_REQUIRED` | PASS |

## Known gaps (not blocking this delivery)

1. Audit tasks 2.1 and 2.2 of `tasks.md` remain open: reconciling every `@PreAuthorize` action against the registry and adding actions the UI evaluates but the registry lacks (`DASHBOARD:EXECUTE`). The catalog is intentionally bijective with the current registry, so it cannot expose an action that enforcement does not support yet.
2. Groups classify existing module-level grants; they are not independent grants. Per-group grants would require sub-modules.
3. Unrelated console noise on the dev stack: `GET /api/auth/preferences` returns 401 for the browser session used during manual checks; it is independent of this change.

## Verdict

APPROVED for archive once tasks 2.1/2.2 are either completed or explicitly deferred.
