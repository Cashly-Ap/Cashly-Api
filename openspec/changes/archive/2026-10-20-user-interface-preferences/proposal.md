# Proposal: User interface preferences

## Intent

Let each user choose the application's appearance and keyboard interaction from Configuración in their user menu, while keeping Cashly's established layout and shadows.

## Scope

### In scope
- A user-menu-only Configuración page, separate from Perfil and Empresa.
- Light, dark, and system theme preferences, including live operating-system changes.
- Minimalista (current appearance) and Informativo (consistent module groups, sections, and colorful charts).
- Optional, documented keyboard shortcuts for authorized modules and explicit common actions.
- Validated preference persistence, legacy theme compatibility, and user isolation.

### Out of scope
- Changing business permissions, company settings, component geometry, or semantic status meanings.
- Custom palettes, user-defined shortcut remapping, commits, pull requests, and ClickUp work.

## Capabilities

### New capabilities
- `user-interface-preferences`: Personal appearance and keyboard interaction settings.

### Modified capabilities
- None.

## Approach

Extend the existing signal-based ThemeService and shared form/layout components. Add a central preference store, semantic module palette, and explicitly registered keyboard actions. Preserve permission-filtered navigation as the authority for module shortcuts. Persist preferences through an authenticated self-only account API, with a per-user browser cache for initial rendering.

## Affected areas

| Area | Impact | Description |
|---|---|---|
| `cashly-app/src/app/core/theme/` | Modified | Theme preference and palette tokens |
| `cashly-app/src/app/core/layouts/` | Modified | Settings entry and grouped navigation colors |
| `cashly-app/src/app/features/platform/auth/` | New/modified | Preference page and authenticated user identity |
| `cashly-app/src/app/core/forms/` | Modified | Explicit action shortcut integration |
| `cashly-app/src/app/features/analytics/dashboard/` | Modified | Reactive chart colors |

## Risks

| Risk | Mitigation |
|---|---|
| Shortcut conflicts or hidden actions | Opt-in, explicit registration, permission and dialog scope checks |
| Informative appearance alters familiar UI | Colors only; shared geometry and shadows stay intact |
| Preferences leak between accounts | User-keyed storage or self-only account API |

## Rollback plan

Remove the preference route, action registrations, and color tokens; restore ThemeService's existing light/dark behavior. Ignore new preference storage keys. Keep existing user/company changes intact.

## Dependencies

- Authenticated account preference storage and migration.
- Existing Angular, PrimeNG, Chart.js, Vitest, and Playwright infrastructure.

## Success criteria

- [x] All appearance combinations work on desktop and mobile without changing layout.
- [x] System mode follows OS changes and survives reload.
- [x] Shortcuts respect focus, permissions, disabled actions, dialogs, and the off setting.
- [x] Preference changes remain isolated between users.
