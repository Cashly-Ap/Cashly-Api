# Proposal: Simple shortcuts and screen hints

## Intent
Make enabled shortcuts easier to remember and show the actions available on the current screen without requiring multi-key combinations.

## Scope
- Single-key N (new), F (filter), B (search), G (save/accept), Escape (cancel/back), M (module navigator).
- A personal, account-synchronized showShortcutHints boolean, default true.
- Contextual help limited to visible, authorized and enabled actions in the active screen/dialog; shown only when shortcuts and hints are enabled.
- Protect typing, browser combinations, active dialogs, semantic permissions and component design.

Out of scope: remapping, company preferences, destructive-action shortcuts, commits and PRs.

## Capabilities
### New capabilities
- `screen-shortcut-help`: contextual keyboard help and its saved visibility preference.
### Modified capabilities
- `user-interface-preferences`: simple bindings and typing safety for unmodified keys.

## Approach
Reuse explicit action registrations and active-scope safety. Extend the existing account preference contract with required showShortcutHints. V33 adds the column with true default for existing rows. Keep shared UI geometry/shadows.

## Affected areas
Backend platform/auth/preferences and V33; frontend core/shortcuts, core/preferences, personal settings, contextual shared help and runtime tests.

## Risks
Typing accidentally triggers actions: ignore single letters in editable controls. Help advertises unavailable actions: derive it from the same live registration/scope/permission predicates as dispatch. Old caches lack the new field: ignore the old cache schema and restore saved choices from the authoritative account API.

## Rollback
Restore previous bindings/help UI; retain the harmless database column. Revert preference contract only together with callers. Preserve prior archived changes and existing user data.

## Success criteria
- All six bindings execute only available actions and preserve native/typing behavior.
- Users can save hints off/on and recover the choice in another session.
- Help accurately follows page/dialog scope and honors both preference toggles.
