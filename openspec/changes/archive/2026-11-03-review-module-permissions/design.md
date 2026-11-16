# Design — Grouped permission catalog and role permission matrix

## Context
Enforcement today is `module:action` (`PermissionRegistry` → `@PreAuthorize` → `hasPermission`). Grants are stored in `auth_role_permissions(role_id, module, action)`. There is no group concept, no catalog endpoint with metadata, and no screen that shows denied permissions.

## Decision 1 — Catalog lives in code
`PermissionCatalog` (domain services, next to `PermissionRegistry`) is the single description of the catalog. It is validated against `PermissionRegistry.modules()` in a static initialiser: if a registered `(module, action)` is missing or an entry is not registered, the application fails to start.

Rationale: the catalog must be the same truth the guards use. Drift becomes a boot failure instead of a silent UI lie. No migration, no seed step.

Rejected: a catalog table (`auth_permission_catalog` + groups). It would need a migration, a seed and enum synchronisation, and no requirement asks for runtime editing.

## Decision 2 — Groups classify grants, they are not new grants
Each `(module, action)` pair belongs to exactly one group inside its module. Groups carry `key`, `labelKey`, `sortOrder`. The grant model is untouched: selecting a group never grants anything by itself.

Consequence documented for the user: `FINANCES:EXECUTE` covers both billing transitions and treasury operations because the grant is module-wide. Per-group grants would require sub-modules (out of scope).

Group map (each pair exactly once):

| Module | Group | Actions |
|---|---|---|
| COMPANY | company | READ, UPDATE |
| USERS | users | READ, CREATE, UPDATE, DELETE |
| ROLES | roles | READ, CREATE, UPDATE, DELETE |
| DEPARTMENTS | departments | READ, CREATE, UPDATE, DELETE |
| CATALOG | products | READ, CREATE, UPDATE, DELETE |
| CATALOG | reports | EXPORT |
| INVENTORY | warehouses | READ, CREATE, UPDATE, DELETE |
| INVENTORY | operations | EXECUTE |
| INVENTORY | reports | EXPORT |
| SALES | documents | READ, CREATE, UPDATE, DELETE |
| SALES | transitions | EXECUTE |
| SALES | reports | EXPORT |
| FINANCES | billing | READ, CREATE, UPDATE, DELETE |
| FINANCES | treasury | EXECUTE |
| FINANCES | reports | EXPORT |
| PURCHASES | purchases | READ, CREATE, UPDATE, DELETE |
| PURCHASES | transitions | EXECUTE |
| PURCHASES | reports | EXPORT |
| DOCUMENT_ADMIN | documents | READ |
| DOCUMENT_ADMIN | reports | EXPORT |
| DASHBOARD | dashboard | READ |

## Decision 3 — Contract
`GET /api/permissions/catalog` (`ROLES:READ`)

```json
[
  {
    "module": "FINANCES",
    "sortOrder": 60,
    "groups": [
      {
        "key": "billing",
        "labelKey": "platform.auth.groups.finances.billing",
        "sortOrder": 10,
        "permissions": [
          {
            "action": "CREATE",
            "labelKey": "platform.auth.permissions.finances.billing.create",
            "descriptionKey": "platform.auth.permissions.finances.billing.create.description",
            "sortOrder": 20
          }
        ]
      }
    ]
  }
]
```

Ordering is by `sortOrder`, then enum name, so responses are deterministic.

No new "effective permissions for a role" endpoint is needed: `GET /api/roles` and `GET /api/roles/{id}` already return `permissions` (expanded for the protected system role by `Role.permissions()`).

## Decision 4 — Frontend
- The grouped catalog is rendered **inside the existing role editor** (`Roles → Edit → Permissions`), not in a standalone screen. A separate `/platform/permissions` page was built during the first pass and removed: permissions are edited where roles are edited.
- `PermissionEditorComponent` receives the catalog and renders, per module, the existing presets (No access / Read-only / Customize / All available) plus one section per group with one row per permission: business label, description and the allowed/denied state.
- `PermissionCatalogService` feeds the editor from `GET /api/permissions/catalog`; `AccessManagementService.registry()` keeps driving presets and delegation rules. When the catalog is unavailable the editor falls back to the flat registry with generic action labels.
- No menu entry, route or `moduleForUrl` mapping is added.
- UI colours follow the design system: success only for allowed, neutral for denied.

## Decision 5 — Audit included
While building the catalog, reconcile registry gaps the UI already evaluates (`DASHBOARD:EXECUTE` was evaluated by a dialog and did not exist) and require an explicit `appPermissionModule` for layout-hosted dialogs. Any addition goes through `PermissionRegistry` + a catalog entry, keeping the startup validation green.

## Testing
- Unit: catalog covers the registry exactly (bijection), deterministic ordering, no duplicate classification.
- Web: `GET /api/permissions/catalog` returns 200 with `ROLES:READ`, 403 without it.
- Frontend: page renders allowed and denied rows for a role, system role shows everything allowed, menu entry visibility by permission, guard blocks without `ROLES:READ`.
