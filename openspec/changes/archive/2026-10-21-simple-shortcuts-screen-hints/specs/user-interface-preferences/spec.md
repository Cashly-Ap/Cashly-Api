# Delta: User interface preferences

## MODIFIED Requirements

### Requirement: Optional and discoverable shortcuts

Shortcuts MUST default off and offer a discoverable list. Enabled shortcuts MUST navigate only authorized modules and invoke explicitly available Nuevo, Filtrar, Guardar/Aceptar, and Cancelar actions. They MUST NOT broaden permissions. Bindings MUST be N (new), F (filter), B (search), G (save/accept), Escape (cancel/back), and M (module navigator), without requiring modifier keys.
(Previously: discoverable shortcuts used modifier combinations.)

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
(Previously: modified save/cancel shortcuts could run inside the active editable form.)

#### Scenario: Typing protection
- GIVEN focus is in an input, textarea, select, or editable content
- WHEN the user types or holds a shortcut key
- THEN no module navigation or unrelated action occurs; single-letter shortcuts MUST NOT execute; Escape MAY cancel only explicitly registered actions belonging to the same form or active dialog when native controls do not consume it.

#### Scenario: Modal scope
- GIVEN a dialog is open with its own registered accept/cancel actions
- WHEN the matching documented shortcut is pressed
- THEN only the dialog action executes, preserving native control behavior.
