# Verification: Simple shortcuts and screen hints

## Verdict
PASS. No known critical issues. Build retains the existing qrcode/STOMP CommonJS notices.

## Runtime evidence
- Docker Maven preference and endpoint permission suites: 590 tests, zero failures/errors. `/tmp/cashly-simple-backend.log` and Surefire XML.
- Docker Vitest preferences, shortcuts/help, settings and MainLayout: 72 tests passed. `/tmp/cashly-simple-unit-verified.log`.
- Docker Playwright preferences: 9 scenarios passed. `/tmp/cashly-simple-e2e-final.log`.
- Angular production build passed. `/tmp/cashly-simple-build-final.log`.
- MySQL V33 applied successfully; show_shortcut_hints is NOT NULL, default 1. Disposable-account live smoke verified GET true, PUT false, a second independent login returning false, missing-field 400 and SQL value 0. Account and related rows were removed.
- Desktop help screenshot visually inspected: cashly-app/output/playwright/preferences/simple-shortcut-help.png.

## Compliance
| Behavior | Covering evidence | Result |
|---|---|---|
| N/F/B/G/Esc/M | Dispatcher units and real navigation/filter/new/save/cancel browser flow | PASS |
| Typing/native protection | Modifier/composition/repeat/editable units; typing G does not save; modal Escape does not cancel background | PASS |
| Saved hints/default true | Controller units, real MySQL/two-login smoke, fresh browser session | PASS |
| Hide help independently | Browser disables hints, creates/cancels through N/Esc and verifies fresh-session preference | PASS |
| Screen actions only | Dynamic registration/disabled/hidden units; Users help excludes form save | PASS |
| Dialog scope | Scope units, renderer move/restore unit, real navigator help has Esc and excludes background Nuevo | PASS |
| Existing appearance/account behavior | Browser OS tracking, six appearance combinations, failed-save retry and fresh-session settings | PASS |

## Limits
Focused verification, not a whole-project coverage claim. No commits or PRs requested or created.
