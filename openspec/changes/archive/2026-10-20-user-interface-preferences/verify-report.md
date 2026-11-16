# Verification report: user-interface-preferences

Mode: Standard. No strict_tdd project configuration exists. Backend tests were written before implementation; RED/GREEN evidence is retained in backend-apply-progress.md.

## Completeness

All 19 implementation/verification tasks are complete. Source inspection, executed tests, browser checks, and independent QA/security review support closure. No critical issue remains.

## Executed evidence

| Layer | Command / method | Confirmed result |
|---|---|---|
| Backend | `docker exec cashly-api-app ./mvnw test -Dtest=UserPreferencesServiceTest,UserPreferencesControllerTest,EndpointPermissionCoverageTest,EndpointPermissionEnforcementTest -q` | 585 passed; 0 failures/errors/skips |
| Database/API | Actual HTTP requests using disposable accounts and independent login sessions; MySQL metadata queries | V32 success; defaults, persistence, account isolation, validation, concurrent first upserts and company ownership passed |
| Frontend | Targeted Docker Vitest/V8 run; `/tmp/cashly-preferences-coverage.log` | 102 passed in 12 files; 0 failures |
| Frontend regression | Profile/users/login/security/company; `/tmp/cashly-preferences-regression-unit-final.log` | 47 passed |
| Dialog controls | Targeted Vitest run | 23 passed; change-email/password, note and invoice-picker suites |
| Browser | `npx playwright test --config e2e/preferences.config.ts` in development container | 7 preferences + 10 company + 10 MFA browser tests passed (27 total) |
| Build | Angular production build in development container | Passed; existing qrcode/STOMP CommonJS warnings only |

Scoped frontend V8 coverage for new preference/shortcut/theme/settings TypeScript: 97.63% lines and 91.3% branches. This is not whole-project coverage. Backend JaCoCo reports preference domain/application/controller lines at 100%; storage adapter/config automatic coverage is 0%, with both storage paths exercised through real HTTP/MySQL verification. No whole-project backend coverage claim is made.

## Behavioral compliance matrix

| Requirement / scenario | Covering execution | Status |
|---|---|---|
| Personal settings entry on desktop/mobile | `preferences.spec.ts`: user-menu-only settings; mobile shared cards/overflow | COMPLIANT |
| Explicit theme remains selected | ThemeService unit; browser explicit mode versus OS | COMPLIANT |
| System mode follows OS live | ThemeService unit; browser system changes | COMPLIANT |
| Informative grouped colors and chart colors | module-appearance and dashboard units; browser chart/navigation/shadow test | COMPLIANT |
| Restore minimalist appearance | Dashboard palette restore unit; browser appearance test | COMPLIANT |
| Account isolation | Backend service/controller units; actual two-account HTTP verification | COMPLIANT |
| Device synchronization | Actual independent login sessions; fresh browser-context test | COMPLIANT |
| Invalid saved preference defaults | Preferences service invalid-cache/server-value units | COMPLIANT |
| Enabled shortcuts use authorized actions | Shortcut service/navigator units; browser navigation/filter/new/cancel test | COMPLIANT |
| Off/disabled/unauthorized shortcuts do nothing | Shortcut service permission/disabled/off units | COMPLIANT |
| Editable/repeat/composition protection | Shortcut service editable/composition/repeat/native-key units; browser active-form submit | COMPLIANT |
| Modal actions take precedence | Shortcut service modal/alert/selection-popup units and dialog controls | COMPLIANT |

## Correctness and design coherence

Self-only CurrentActor identity is used on every backend operation; no endpoint accepts an account selector. JPA storage uses one atomic native upsert and a user primary key. V32 contains required user/company foreign keys. Request fields reject missing/unsupported values and responses disable caching.

Frontend preferences validate per-user cache and server data, serialize saves, restore the last saved state after failure, and offer retry. Account changes cancel requests and invalidate epochs. Independent review found a queued-save race in the identity effect gap; it was corrected by checking the live identity as well as epoch before processing responses or dispatching another queued PUT. The dedicated regression covers that sequence.

Settings uses shared detail cards and user-menu entries, with a dedicated authenticated route. ThemeService preserves its existing consumer interface and cleans up the operating-system listener. Module accents are scoped to informative mode; chart colors remain reactive.

Active-form submit/cancel from an editable control is an intentional refinement of the initial design: only the containing form or active dialog may execute; module navigation and unrelated actions remain blocked. The final design records this interaction contract.

## Issues

- CRITICAL: None. The identity-race correction passed its regression and independent re-review.
- WARNING: JPA adapter/config lacks automated coverage; real MySQL/HTTP verification exercised defaults, reads, writes and concurrent first upserts. Existing build CommonJS warnings remain.
- SUGGESTION: None.

## Verdict

PASS WITH WARNINGS — all specified scenarios have passing runtime coverage; no critical or untested scenario remains. Coverage scope and existing build warnings are recorded above.

Evidence logs: `/tmp/cashly-preferences-coverage.log`, `/tmp/cashly-preferences-regression-unit-final.log`, `/tmp/cashly-preferences-e2e-final.log`, `/tmp/cashly-preferences-company-regression.log`, `/tmp/cashly-preferences-mfa-regression.log`, `/tmp/cashly-preferences-build-final.log`, `/tmp/cashly-preferences-backend-coverage.log`. Backend execution and disposable-account cleanup details are retained in backend-apply-progress.md. Routine independent QA/security review was performed; Judgment Day was not requested or invoked.
