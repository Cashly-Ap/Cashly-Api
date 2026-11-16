# Review module permissions

## Intent
Make the permission model correct, predictable and **visible**: every module must expose the actions it actually needs, every gate must evaluate the module that owns the action, and an authorised user must be able to open a screen that shows, **for a selected role**, every permission grouped by **module** and by **group**, stating explicitly whether it is **allowed or denied**.

## Scope

### In scope
- A code-owned permission catalog (`module → group → permission`) that is validated against `PermissionRegistry` at startup, so the catalog can never drift from enforcement.
- `GET /api/permissions/catalog` exposing modules, groups and permission entries with i18n keys and ordering.
- A `/platform/permissions` screen with a **role selector** and, per module → group, one row per permission showing **Allowed / Denied**.
- Navbar entry under **Platform**, route, guard and i18n (es/en).
- Audit of catalog gaps that today break the UI: actions evaluated by the frontend but absent from the catalog, and `[appPermission]` usages resolved with the wrong module.
- Documentation update (`docs/modules/ACCESS_CONTROL.md`).

### Out of scope
- Fine-grained grants per group (sub-modules). Grants stay `module:action`; groups are a classification of those grants.
- Direct user grants (retired by design: users inherit from their role).
- Making the catalog editable at runtime (decision: catalog lives in code — option A).
- The remaining backend/frontend audit items that belong to `user-profiles-roles-departments`.

## Capabilities
- `permission-catalog` (new): a stable, grouped description of every permission plus a target-scoped view of what a role may do.

## Risks
| Risk | Mitigation |
|---|---|
| Catalog drifts from enforcement | Startup validation fails the boot if any registry permission is unclassified or any catalog entry is not registered |
| Group split breaks the "READ is prerequisite" rule | Groups classify existing grants only; `PermissionRegistry.validate` is untouched |
| New route invisible or wrongly gated by URL inference | Explicit `appPermissionModule`/route `data.module` + `moduleForUrl` mapping |
| Role with masked permissions leaks information | The screen only lists roles the actor can already read (`GET /api/roles`), and reuses the existing delegation filter for options |

## Rollback
Revert the catalog controller, catalog classes, menu/route entry, screen and i18n keys. No migration, no grant writes and no document/customer data are touched.
