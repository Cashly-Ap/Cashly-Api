# Keyboard navigation specification

## Requirements

### Requirement: Vim scrolling
Enabled shortcuts MUST support J to scroll down and K up outside editable controls. Repeated presses MAY continue scrolling. Native modifiers and text editing MUST remain intact.

#### Scenario: Scroll and type
- GIVEN shortcuts are enabled
- WHEN J/K are pressed outside a field
- THEN the page or active dialog scrolls; inside a field they remain text.

### Requirement: Focus exit
The helper MUST offer a focus-release button. Escape in an editable field MUST leave focus first, without also cancelling the form/dialog. Another Escape MAY cancel through the existing workflow. Open selection popups MUST retain native handling.

#### Scenario: Escape twice
- GIVEN a focused form field
- WHEN Escape is pressed once
- THEN focus leaves without cancellation; a second Escape may cancel.

### Requirement: Module result navigation
The module picker MUST support Up/Down from its search input, J/K outside the input, Enter to open the active authorized result, and an explicit Navigate results button. Filtering MUST reset selection and an empty list MUST NOT navigate.

#### Scenario: Search, select and open
- GIVEN authorized module results
- WHEN arrows select a result or the user leaves the field and presses J/K
- THEN a visible accessible selection follows; Enter opens only the current result.

#### Scenario: Search letters
- GIVEN the search input is focused
- WHEN j or k is typed
- THEN it updates the query rather than changing selection.
