# Screen shortcut help specification

## Purpose
Expose relevant keyboard help without overwhelming each screen.

## Requirements
### Requirement: Saved help visibility
The system MUST save showShortcutHints per authenticated account and synchronize it across sessions/devices. New accounts and existing preference rows MUST default true. PUT MUST reject missing or null values.

#### Scenario: Default visibility
- GIVEN an account has no saved hint choice
- WHEN preferences load
- THEN showShortcutHints is true without changing its other preferences.

#### Scenario: Hide help persistently
- GIVEN a user saves showShortcutHints false
- WHEN they sign in through another session
- THEN the saved preference is false and contextual help is hidden.

#### Scenario: Invalid hint preference
- GIVEN an authenticated update lacks showShortcutHints or supplies null
- WHEN it reaches the preference endpoint
- THEN the system rejects it and retains the saved preferences.

### Requirement: Contextual available-action help
Help MUST be visible only when shortcuts and showShortcutHints are both enabled. It MUST list only currently registered, visible, enabled, authorized actions in the active page/dialog scope and update when scope changes.

#### Scenario: Active screen help
- GIVEN both toggles are on and a screen has available actions
- WHEN help is rendered
- THEN it shows each available action's simple binding without exposing unauthorized actions.

#### Scenario: Disabled preferences
- GIVEN either shortcut activation or showShortcutHints is off
- WHEN the screen renders
- THEN contextual shortcut help is hidden.

#### Scenario: Dialog scope
- GIVEN an active dialog blocks background actions
- WHEN contextual help updates
- THEN it shows only actions executable within that dialog.

#### Scenario: Filters and search retain screen help
- GIVEN shortcut help is enabled
- WHEN a non-modal filter popup, dropdown or search field is opened or focused
- THEN the screen help remains visible in its original scope, including when no registered action is temporarily executable.
