# Design: User interface preferences

## Technical approach

Introduce a signal-based UserPreferencesService shared by settings, ThemeService, layout, charts, and shortcuts. Keep personal settings at `/platform/auth/preferences`; retain `/platform/auth/configuration` as Perfil. Account storage is authoritative; user-scoped cache supplies initial rendering.

## Architecture decisions

| Decision | Alternatives | Rationale |
|---|---|---|
| Authenticated GET/PUT `/api/auth/preferences` | Global browser settings | User requested synchronization across devices; self-only endpoints prevent preference access to another account |
| One complete typed preference object | Independent unrelated keys | Atomic validation, reliable reset and rollback, straightforward cache versioning |
| Preserve ThemeService.isDarkMode and toggleTheme | Replace every consumer | Existing charts and toggle tests already use this compatibility contract |
| Explicit semantic module tokens | Broad CSS recoloring | Group consistency without changing status colors or shadow/layout geometry |
| Explicit action registration directive | Scan buttons by labels | Safe across translations, permissions, disabled states, dialogs, and teardown |
| Filtered menu as module shortcut source | Separate unrestricted routes | Existing role/module checks remain authoritative |

## Data flow

Authenticated identity → per-user cache/defaults → preference signals → theme, accents, shortcuts.

Authenticated identity → GET account preferences → validated signals/cache.

Settings selection → optimistic signals → serialized PUT → saved cache; failure → last saved signals and visible error/retry.

OS media-query change → resolved theme only when themeMode is system. Sign-out/account switch invalidates in-flight work and resets active state before loading the next user's preferences.

## Interfaces / contracts

```typescript
interface UserPreferences {
  themeMode: 'system' | 'light' | 'dark';
  colorMode: 'minimal' | 'informative';
  shortcutsEnabled: boolean;
}
```

Defaults: system, minimal, false. GET/PUT `/api/auth/preferences` operates exclusively on the authenticated principal; no user id is accepted. A normalized per-user row includes installation-company ownership. New accounts with no saved record return defaults. Validate enum values and required booleans on PUT; invalid requests return 400. Cache keys include user id and schema version. Legacy `theme` light/dark remains compatible as an initial migration hint, without overwriting an account's saved preference.

The shortcut service manages registrations with action id, key combination, scope, enabled predicate, and handler. A directive attaches registration to an explicit actionable element and unregisters on destruction. Scope must exclude hidden/inert/detached/disabled actions and background actions while a modal is active. Navigation uses currently filtered menu routes. Ignore defaultPrevented, composition, repeat and native-reserved combinations. While editing, only explicitly registered save/cancel actions belonging to the same form or active dialog may execute; navigation/new/filter/search remain blocked. Cached shortcut opt-in stays inactive until account preferences are ready. Advertise actual bindings in settings and action hints; avoid browser-save/new-tab combinations.

## File changes

| File/area | Change |
|---|---|
| `cashly-api/src/main/java/com/cashly/cashly_api/platform/auth/` | Self-only preference endpoint, use cases, validation and storage |
| `cashly-api/src/main/resources/db/migration/V32__*.sql` | User preference table with user/company ownership |
| `cashly-app/src/app/core/preferences/` | Model, account/cache service, tests |
| `cashly-app/src/app/core/theme/components/theme-toggle/theme.service.ts` | System listener and compatibility adapter |
| `cashly-app/src/app/core/layouts/main-layout.component.{ts,html}` | User-menu settings entry and authorized shortcut navigation |
| `cashly-app/src/app/core/layouts/config/menu.config.ts` | Group color metadata if required |
| `cashly-app/src/app/features/platform/auth/preferences/` | Shared-card settings screen and shortcut reference |
| `cashly-app/src/app/app.routes.ts` | Authenticated settings route |
| `cashly-app/src/app/core/shortcuts/` | Explicit registration directive and dispatcher |
| `cashly-app/src/app/core/forms/components/headers/form-actions/` | Save/cancel action registration |
| Shared list/filter/detail controls | New/filter/accept registrations at owning control |
| `cashly-app/src/app/features/analytics/dashboard/` | Reactive informative chart palette |
| `cashly-app/src/styles.css` | Scoped accent tokens using existing selectors |

## Testing strategy

Backend: self-only access, persistence/defaults, validation, ownership and cross-user isolation. Frontend unit: OS changes, invalid cache, account switching, stale response protection, optimistic rollback and serialized updates. Shortcuts: off state, editable/repeat/composition, modal priority, disabled/hidden and unauthorized actions, destruction cleanup. Browser: settings on mobile/desktop; six theme/color combinations; reload and account synchronization; actual module and common-action bindings. Build with existing Docker workflow.

## Migration / rollout

Add V32 only after confirming the latest migration number. Preserve existing user/company data. Minimal mode and shortcuts off retain current behavior. No package dependencies required.

## Open questions

- None. Bindings: Alt+Shift+M opens the authorized module navigator; N creates, F filters, B focuses search, Enter saves/accepts, Backspace cancels/returns. KeyboardEvent.code keeps physical positions stable under macOS Option. Navigation, creation, filtering and search ignore editable targets; save/cancel can act only on the same form or active dialog.
