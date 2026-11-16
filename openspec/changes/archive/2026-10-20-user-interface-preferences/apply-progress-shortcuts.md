# Apply progress: keyboard shortcuts

Mode: Standard. Work unit 3, local changes only; publication strategy remains outside scope.

Completed tasks: 3.1–3.5. Added opt-in dispatcher, explicit action directive, shortcut reference, authorized module navigator, MainLayout integration, shared form actions/search and annotations in actual list/dialog controls. Native button clicks retain form validation and permission-directive capture guards. Business cancellation/confirmation controls receive no generic cancel shortcut.

Bindings: Alt+Shift+N new; F filters; B search; Enter save/accept; Backspace cancel/back; M module navigator. Physical KeyboardEvent.code supports macOS Option layouts. Navigation/new/filter/search ignore editable targets. Save/cancel while editing is allowed only for registrations inside the same form or active dialog. Composing/repeating/default-prevented/native modifier events and open selection overlays are ignored. Cached opt-in waits for ready account preferences. Disabled/hidden/inert/detached actions and background actions behind active modal dialogs are excluded. Alert dialogs cannot be accepted through shortcuts.

Verification:
- Docker Vitest focused run: keyboard-shortcut.service.spec.ts, module-navigator.component.spec.ts, form-actions.component.spec.ts, search-field.component.spec.ts — 37 tests passed (16 dispatcher, 2 navigator, 12 form actions, 7 search).
- Docker Angular production build — passed; existing CommonJS warnings for qrcode and STOMP remain.

Additional existing change-email/change-password/note/invoice-picker dialog suites: 23 tests passed.

Browser verification: `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium npx playwright test --config e2e/preferences.config.ts --grep "enabled shortcuts"` passed (1 test, 2.9s). Exercises opt-in/off, permission-filtered module navigation, Users filters, New User, input navigation protection and active form cancellation. Fixed actual browser regression: PrimeNG renders an empty `p-dialog role="alertdialog"` host when closed; dispatcher now excludes this unrendered placeholder while preserving active alert-dialog protection. E2E form field uses accessible Nombre textbox rather than assuming explicit type=text attribute.

Final integration: 7 preference browser tests passed, including real active-input form submission; company and MFA regression suites each passed 10 tests. Related-form and accept/modal behavior is covered by dispatcher/form/dialog runtime tests. See verify-report.md.
