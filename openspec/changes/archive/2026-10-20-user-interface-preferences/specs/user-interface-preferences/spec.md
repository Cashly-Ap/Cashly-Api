# User interface preferences specification

## Purpose

Personalize Cashly appearance and keyboard interaction without changing business access or established component layout.

## Requirements

### Requirement: Personal settings entry

The system MUST expose Configuración only in the authenticated user menu, separately from Perfil and Empresa, and MUST retain standard shared cards, shadows, spacing, and responsive behavior.

#### Scenario: Open settings
- GIVEN an authenticated user on desktop or mobile
- WHEN they choose Configuración from their user menu
- THEN personal appearance and shortcut settings open separately from profile editing.

### Requirement: Theme preference

Users MUST choose Claro, Oscuro, or Del sistema. System mode MUST follow operating-system changes while open. Existing explicit light/dark choices MUST remain usable.

#### Scenario: Explicit theme
- GIVEN a user chooses Oscuro
- WHEN the OS changes to light or the page reloads
- THEN Cashly remains dark until the user changes their preference.

#### Scenario: System theme
- GIVEN Del sistema is selected
- WHEN the OS changes its preferred scheme
- THEN Cashly updates without a reload and retains Del sistema as its preference.

### Requirement: Color modes

Minimalista MUST preserve the present appearance. Informativo MUST apply consistent module-group colors to navigation and relevant section accents and varied chart palettes. Both modes MUST preserve layout, contrast, focus visibility, and semantic success/error/warning meanings.

#### Scenario: Informative mode
- GIVEN a user selects Informativo in either theme
- WHEN they navigate among modules and charts
- THEN each group uses consistent accents and charts use distinguishable colors without geometry or shadow changes.

#### Scenario: Restore minimal mode
- GIVEN Informativo is active
- WHEN the user selects Minimalista
- THEN the current monochrome base appearance returns immediately.

### Requirement: Preference persistence

Preferences MUST be validated, synchronize across devices through the account, persist across reloads, remain isolated between authenticated users, and fall back safely when saved data is unavailable or invalid. Only the authenticated user MUST be able to read or update their preferences. Failed saves MUST visibly report failure and restore the last saved state.

#### Scenario: Account isolation
- GIVEN user A changes preferences and signs out
- WHEN user B signs in on the same browser
- THEN A's personal settings do not become B's saved preferences.

#### Scenario: Device synchronization
- GIVEN user A saves preferences on one device
- WHEN A signs in on another device
- THEN the account preferences are loaded and applied.

#### Scenario: Invalid saved preference
- GIVEN saved preference data contains unsupported values
- WHEN settings initialize
- THEN valid defaults are applied without preventing application use.

### Requirement: Optional and discoverable shortcuts

Shortcuts MUST default off and offer a discoverable list. Enabled shortcuts MUST navigate only authorized modules and invoke explicitly available Nuevo, Filtrar, Guardar/Aceptar, and Cancelar actions. They MUST NOT broaden permissions.

#### Scenario: Enable and use
- GIVEN shortcuts are enabled and an authorized module action is available
- WHEN the user presses its documented shortcut outside an editable control
- THEN that action executes once.

#### Scenario: Disabled or unauthorized action
- GIVEN shortcuts are off or an action is hidden, disabled, or unauthorized
- WHEN its key combination is pressed
- THEN no application action executes.

### Requirement: Keyboard interaction safety

Shortcuts MUST respect editable controls, composition, repeated key events, active dialogs, and native browser combinations. Dialog actions MUST take precedence over background actions.

#### Scenario: Typing protection
- GIVEN focus is in an input, textarea, select, or editable content
- WHEN the user types or holds a shortcut key
- THEN no module navigation or unrelated action occurs; modified save/cancel shortcuts MAY execute only explicitly registered actions belonging to the same form or active dialog.

#### Scenario: Modal scope
- GIVEN a dialog is open with its own registered accept/cancel actions
- WHEN the matching documented shortcut is pressed
- THEN only the dialog action executes, preserving native control behavior.
