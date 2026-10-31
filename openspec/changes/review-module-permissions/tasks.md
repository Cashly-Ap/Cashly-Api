# Tasks

## Audit
- [ ] Inventory every `BusinessModule` value and the actions the UI and API actually require for it.
- [ ] Diff the catalog (`auth_role_permissions`) against that inventory and list missing or unused actions.
- [ ] List every `[appPermission]` usage without an explicit `[appPermissionModule]` and classify each as safe (module page) or unsafe (layout-hosted dialog/modal).
- [ ] Review seeded roles and confirm the intended grants for administrator, operator and any other seeded role.

## Backend
- [ ] Add the missing catalog actions through a new Flyway migration (never edit committed migrations).
- [ ] Align `@PreAuthorize` checks and endpoint guards with the corrected catalog.
- [ ] Add regression tests that fail when a required action is absent from the catalog.

## Frontend
- [ ] Remove URL-based module inference from gated dialogs; require the owning module explicitly.
- [ ] Fix every unsafe `[appPermission]` usage found in the audit.
- [ ] Add a guard test that fails when a new `[appPermission]` usage has no explicit module outside a module page.

## Verification
- [ ] Run the full backend suite and the full frontend suite.
- [ ] Manually verify each role in the browser: administrator, operator and a read-only account.
- [ ] Record the resulting permission matrix in the module documentation.
