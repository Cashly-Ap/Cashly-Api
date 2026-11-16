# Verification — 2026-10-19

## API

550 focused Docker tests passed: 516 method-authorization cases across 172 mapped protected operations, 17 access-service tests, 9 JWT-filter tests, 3 endpoint-coverage tests, 4 role-management tests and 1 department test. Spring PreAuthorize expressions are exercised for no grants, READ-only grants and each missing required grant. Updated selectors enforce USERS READ plus CREATE or UPDATE; role updates validate existing and requested grants to prevent stripping a role outside the caller's scope.

The live API resolved persisted role permissions on each request. A disposable CATALOG READ role/user could GET categories and own profile; warehouses, customers, users, roles and departments returned 403. Valid category POST/PUT/DELETE and commercial catalog export returned 403. Removing READ from the fixture role caused the same previously issued token to receive 403 immediately. The disposable user and role were removed through the API afterward (204 each). Existing assignments and business data were preserved.

## Frontend

The complete Docker frontend run passed 269 files / 2800 tests. Final shared-detail refinements were additionally checked with 77 tests across 9 suites. Route regression tests exercise real route configuration through RouterTestingHarness, covering CRUD areas, dashboard and reports; access tests cover absent grants, legacy-label bypass denial, READ prerequisites and authoritative refresh failure/revocation. Shared tests cover stale action menus, event capture denial, navigation reuse, confirmation-time checks, empty menu triggers and draft visibility/reopening.

Final production build passed. Existing STOMP CommonJS optimization warning remains. Repository whitespace checks passed.

## Browser

Verified actual API data with a temporary CATALOG READ account: only Catalog and own-account navigation remained; product list and detail worked, only View detail was offered and no create/edit/delete/export action appeared. Empty detail action trigger was removed. After removing READ, navigating to the catalog redirected to own profile and left only Platform > Configuration; no business modules or draft badge remained. Captured desktop evidence in the parent workspace:

- artifacts/design-check/role-read-only-enforcement.png
- artifacts/design-check/role-no-module-enforcement.png

Restored viewport and closed the temporary browser tab after fixture cleanup. No credential changes or changes to existing users' permissions were performed.

## Review

Independent frontend and backend reviews confirmed persisted permission enforcement, exact cross-owner export requirements, fail-closed route metadata and ancestor checks. The backend reviewer found an inventory catalog report owner mismatch in navigation; it was corrected to INVENTORY and covered with an explicit mapping regression. No remaining blocking finding was identified. No commit or push was performed for this follow-up.
