# Angular implementation progress

Mode: Standard. Delivery: auto-chain logical units locally, no commits or PRs.

Implemented UI units:

- User profile creation/edit with required Unicode names, structured address, searchable current role/department options and one editable phone number. Profile/reference creation and update use one atomic request. Direct user grants and enum role editing removed.
- User list/detail include custom role and department identity and profile fields; role filters use identifier options.
- Roles and Departments array-backed CRUD screens, optimistic version propagation, conflicts surfaced, protected system-role matrix disabled. Role matrix uses the server registry and disables unsupported/delegation-exceeding cells; READ prerequisite maintained.
- Authoritative access fetched from `/api/auth/access`; legacy names/module claims cannot grant access. Login waits for current access; session restoration reloads it; 403 refresh replaces grants and failed refresh clears permissions.
- Every existing protected child create/edit route checks CREATE/UPDATE using parent module data. Custom roles can access user administration by grants. Catalog routes/menu use CATALOG independently from INVENTORY.
- Explicit action directives applied across feature buttons and explicit `requiredAction` constants across list menus. Unknown list actions fail closed. Reports use explicit owner override, owner EXPORT and DOCUMENT_ADMIN EXPORT; no runtime label-based permission inference.
- Global navigation and initial destinations now include roles/departments, custom users and separated catalog permissions.
- Meaningful tests added for every module's deny/read/create matrix, custom-name non-bypass, current permission revocation, fail closed refresh, route action checks, required profile references, atomic payload and phone validation. Affected user fixtures include new required fields.

Static verification: no unresolved relative TypeScript imports; no permission directive template/import mismatches. API contracts coordinated with backend executor.

Execution limitation: Docker compose inspection failed with permission denied to Docker socket. Per Docker workflow, no host compilation/tests/formatter executed without user authorization. Runtime verification, full existing-suite reconciliation and formatted-source review remain pending until an authorized runner is available. No test pass or completed verification is claimed.

Risks requiring runner checks: Angular strict template/type compilation, fixture mocks for changed auth access async contracts, PrimeNG controls and matrix interaction, all seeds/login full-stack smoke. Existing historical tests contain pre-existing stale non-paged user responses and obsolete role types; targeted contract tests cover new behavior but whole suite has not run.

Additional static reconciliation completed: historical paged user API tests, exact create/update DTO projections, updated typed fixtures, changed route module/guard expectations, custom-role navigation, directive dependency mocks and shared action menu tests. Added Roles matrix/version-conflict tests and silent-refresh continuity test. Silent/manual token refresh now awaits fresh current access, access-fetch requests skip recursive interceptor refresh, and each guarded navigation reloads effective grants. Invoice/return owner corrected to FINANCES and additional stock/conversion grants propagated. Catalog-only product detail no longer queries or renders inventory stock. Protected administrator matrix displays registry-derived effective grants. Action headers are Spanish. Updated app module documentation.

Final static evidence: all relative application imports resolve, and every feature template using the permission directive imports it. Updated affected component test policy/router dependencies; user list options now use a signal for OnPush updates. Added Playwright current-access fixtures and coherent role/department selection fixtures; legacy cached roles no longer drive those browser mocks. User HTTP adapter tests assert exact accepted create/update keys to prevent strict DTO unknown-field failures. Token restoration checks stored expiry before access lookup and reschedules proactive refresh; expired access is renewed through the valid refresh token before permission lookup.

Remaining checks (not executed): install/resolve frontend dependencies in the authorized environment; run Angular production/development strict compilation; run permission/guard/form/role/payload unit tests then affected/full Vitest suites; apply repository formatter; run browser module/navigation/auth smoke, catalog-only and department/role assignment flows against fresh API seeds. No concrete unresolved runtime defect is currently known from static review, but strict-template compilation and existing-suite compatibility cannot be guaranteed without those checks. No task verification checkbox is claimed on static evidence alone.

Final contract boundary correction: optional nullable address fields and phone labels round-trip null-safely. Phone labels remain optional, and number validation/submission share the API normalization of whitespace, dots, parentheses and hyphens before validating the optional plus and 7–15 digits. Added a nullable optional-field round-trip acceptance test. Initial account creation excludes unsupported active state; edits explicitly omit immutable ID/email and credentials from strict update DTOs.

Final backend stock-boundary alignment: inspected current Order/Invoice/Quote/Purchase/Return controller authorization declarations. Added INVENTORY READ to stock-rich detail routes and quote/purchase forms; INVENTORY EXECUTE to order/invoice forms and mutations, quote conversions, purchase confirmation/cancellation and return creation/deletion. Lists remain independently readable. Mirrored dependencies in explicit action/button grants, retained destination CREATE intersections, added route dependency acceptance assertions, and explained inventory dependencies in the matrix UI and module documentation. No runtime commands executed.

Final list-role integration: sorting/global filters now use API-supported `roleName`. USERS READ-only administrators do not request the assignment-only role options endpoint; their role filter is hidden. Assignment-capable administrators retain the searchable role options filter, with handled option-load errors. Added an acceptance assertion for the read-only list avoiding that request. Conversion dialogs now explicitly require both destination CREATE and INVENTORY EXECUTE. Local draft line-removal controls do not incorrectly require resource DELETE.


## Customers-based presentation correction — 2026-10-19

The user rejected the earlier approximation across lists, details and forms. The reference is now explicitly the current customers list/details/form templates and customer general section, not a generic styling interpretation.

- Roles/departments have separate list, new, detail and edit URLs with module child guards and explicit READ/CREATE/UPDATE actions. Detail navigation no longer opens the editor.
- Lists follow customers' toolbar/search/filter popover, desktop table and external paginator, mobile cards, shared action popover, loading/empty/filtered states and confirmation dialog. Both use array-backed filtering/sorting/pagination because the existing API returns arrays.
- Details use the same page header, details card/header, back button, actions menu and information grid. Role permissions are read-only on detail routes.
- Forms use the actual form container/header/card/actions, TabContent spacing, shared text inputs and the same status and textarea treatment as customer-general-section. Role General and Permissions remain separate tabs.
- User detail separates Contact and Access cards; user forms use TabContent and the customer status-field treatment, and retain one scalar phone.
- Added role atomic form/matrix/conflict tests and department detail/navigation/validation/conflict tests. These tests are authored, not executed.

Validation: HTML tag nesting, relative import resolution and git diff whitespace checks passed. Docker compose inspection still fails with permission denied to the Docker socket; node_modules is empty. Angular compilation, test execution and actual browser comparison remain unverified. Do not describe this correction as visually verified or ready for delivery until that comparison runs.

## Runtime correction and executed checks — 2026-10-19

Docker access is now available. The running frontend was retaining its previous successful bundle because UsersFormPage had two strict-template errors: Tabs valueChange can be undefined, and submitTooltip does not exist on this form. Corrected both; the dev server rebuilt successfully and sent client reloads. Production `bun run build` passed. Focused Vitest run passed 3 files / 16 tests (roles, departments and user form); shared reactive-form disabled warnings remain.

Browser checks executed against the actual running API using the development administrator: department desktop and mobile lists, shared action menu, separate read-only department detail, department creation form, role list, role form General/Permissions tab switching, and user form General/Contact tabs with one phone. Customers list was opened in the same browser at 1440px to compare the shared presentation. No records or permissions were created, changed or deleted during browser checks. Screenshot: `artifacts/design-check/departments-desktop.png` in the parent workspace. This supersedes the prior Docker/visual-check blocker for these frontend checks; backend integration and broader verification are not claimed.
