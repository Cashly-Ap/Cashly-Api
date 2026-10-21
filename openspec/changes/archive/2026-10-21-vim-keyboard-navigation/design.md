# Design

Global J/K scroll the page or active dialog content by a small fixed step, including held-key repeats, only when shortcuts are enabled and focus is outside editable controls. Existing browser modifier and popup protections apply.

Capture Escape before native modal handling only when it should leave an editable field; blur and suppress propagation so the same event cannot also cancel or close. Open selection popups retain native Escape. The shared helper offers an explicit focus-release button. The module picker also offers Navigate results; an explicit scope/focus-exit marker sends focus to its results rather than the page.

The picker handles Up/Down while searching, J/K outside its input and Enter on the active result. Signal-based selection wraps, resets on filtering and clamps when permissions/results change. A listbox, options, aria-selected and aria-activedescendant expose selection; afterRenderEffect scrolls it into view. Permission-filtered destinations remain the navigation authority. No schema changes or dependencies.
