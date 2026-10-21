# Delta: Keyboard interaction safety

## MODIFIED Requirements

### Requirement: Keyboard interaction safety

Shortcuts MUST respect editable controls, composition, repeated key events, active dialogs, and native browser combinations. Dialog actions MUST take precedence over background actions.

#### Scenario: Typing protection
- GIVEN focus is in an input, textarea, select, or editable content
- WHEN the user types or holds a shortcut key
- THEN no module navigation or unrelated action occurs; single-letter shortcuts MUST NOT execute; Escape MUST first leave the editable field without cancellation; a subsequent Escape outside the field MAY cancel an available active form or dialog. Native selection popup handling MUST take precedence.

#### Scenario: Modal scope
- GIVEN a dialog is open with its own registered accept/cancel actions
- WHEN the matching documented shortcut is pressed
- THEN only the dialog action executes, preserving native control behavior.
