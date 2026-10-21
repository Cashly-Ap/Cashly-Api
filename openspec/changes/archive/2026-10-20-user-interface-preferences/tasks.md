# Tasks: User interface preferences

## Review workload forecast

| Field | Value |
|---|---|
| Estimated changed lines | 900–1,600 including tests |
| Delivery strategy | auto-chain: local reviewable work units only |
| Suggested split | Foundation → appearance/settings → shortcuts/integration |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

No PRs or commits are requested. The chain recommendation applies if publication is later requested; current authorization covers local implementation in the units below without a publication decision.

### Suggested work units

| Unit | Deliverable | Verification |
|---|---|---|
| 1 | Account preferences and reactive/cache foundation | Persistence, self-only access, validation, user isolation and rollback tests |
| 2 | Settings page, system theme and informative palette | Appearance units, browser responsive checks and chart palette verification |
| 3 | Explicit safe shortcuts and integration | Dispatch safety units, authorized navigation and action browser tests |

## Phase 1: Foundation

- [x] 1.1 Add backend preference DTO/domain/storage and self-only GET/PUT `/api/auth/preferences`; validate enum and boolean values.
- [x] 1.2 Add next available Flyway migration for user-owned preferences with company foreign key and uniqueness.
- [x] 1.3 Test defaults, persisted updates, rejected invalid values, unauthenticated access and cross-user isolation.
- [x] 1.4 Create `core/preferences/` typed service with per-user versioned cache, account loading, serialized optimistic updates and rollback/error state.
- [x] 1.5 Test invalid cache, legacy theme handling, stale account responses, account switching and failed-save restoration.

## Phase 2: Appearance and settings

- [x] 2.1 Adapt existing ThemeService to system/light/dark while preserving isDarkMode/toggleTheme; test live OS changes and listener cleanup.
- [x] 2.2 Add authenticated `/platform/auth/preferences` and user-menu-only Configuración entry in both layouts.
- [x] 2.3 Build settings with shared form/card components, selectors, persistence feedback, shortcut toggle and discoverable reference.
- [x] 2.4 Add centralized module/group accents and scoped informative tokens to navigation and shared section headings without changing shadows or dimensions.
- [x] 2.5 Adapt dashboard chart computed datasets to informative palettes; preserve semantic statuses and minimal appearance.

## Phase 3: Shortcuts

- [x] 3.1 Add `core/shortcuts/` registration service/directive with explicit actions and automatic lifecycle cleanup.
- [x] 3.2 Guard editable/composing/repeating/default-prevented events, disabled/hidden actions and active dialog scope.
- [x] 3.3 Register authorized module navigation from filtered menu and documented safe key bindings.
- [x] 3.4 Integrate Nuevo, Filtrar, Guardar/Aceptar and Cancelar in owning shared controls and actual dialog actions.
- [x] 3.5 Test shortcut opt-in, permission filtering, dialog precedence, native-key protection and registration teardown.

## Phase 4: Verification

- [x] 4.1 Run targeted backend/frontend tests and frontend build in Docker; record commands and results.
- [x] 4.2 Verify desktop/mobile settings, six theme/color combinations, retained geometry/shadows, reload and account synchronization in browser.
- [x] 4.3 Verify navigation/new/filter/save/cancel in browser and accept/modal/input safety in targeted dispatcher/dialog tests.
- [x] 4.4 Record verify-report.md and update this checklist/state with actual outcomes and any limitations.
