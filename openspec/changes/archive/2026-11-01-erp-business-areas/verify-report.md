# Verification report

## Final result

The backend and Angular application now use shared ERP business areas. Returns belongs to finance/billing in both projects. Existing public browser/API URLs, permission identifiers, persistence schema and transactional business behavior are preserved. Report implementations remain with their business owners; analytics/reporting/routing composes the legacy report URLs.

## Backend

The isolated Java 24/MySQL project cashly-screaming-erp-20261017-final1 passed Maven clean verify: 3265 tests, zero failures/errors/skips, 569 ordinary executable source classes and 10 real MySQL IT classes, with no missing executable classes. All 3258 prior cases remain covered; six new area tests and one boundary test with three sibling-access assertions add seven cases.

Coverage reconciliation passed without exclusions or weakened thresholds, including 1128 individually compared moved instrumented classes. Counters are unchanged: domain 94.3244%, application 91.8106%, infrastructure 77.6797%. Concrete owner identity is preserved below area/group prefixes, so sibling modules do not disappear into one area-level coverage bucket. Tooling passed all 25 Python tests, including five new owner-reconciliation regressions.

The source audit maps 1231 production and 567 test relocations. The 1249 production Java files retain their business behavior; the exact substitution equivalence audit is in backend-source-equivalence.json. The before capture includes the newly added QA area guard; the baseline remains the preceding 3258-test run. Final test source count is 587, including the new guard and technical path helper.

Final code/resource hashes match the tested source snapshot. The snapshot also contained one Finder .DS_Store file subsequently removed; this non-code metadata difference is explicitly recorded rather than counted as a source change. Raw logs/XML remain ignored under target/structure-verification; tracked evidence is sanitized. Isolated database containers and temporary verification volumes were removed after export.

Development output was regenerated and the backend restarted. All 1231 old package class identities are absent and the new identities exist. Health returns HTTP 200/UP. Read-only GET checks confirm existing protected quote/order/invoice/return URLs still return 401 without authentication. Regression tests do not use development business data.

## Angular

The final complete Vitest run passed 2529 tests across 260 files, with no failures or skips. All 258 original spec files are retained or mapped through the explicit finance-service split. Its 16 existing HTTP contract cases are preserved in two owner-local suites; the new area/lazy-navigation guard adds 27 cases. One stale @app/core/notifications import in a relocated test was corrected; all three affected tests and the repeated complete suite pass.

The production build passed. It retains the pre-existing @stomp/stompjs CommonJS optimization warning. Production behavior was not changed after the successful build; subsequent fixes concerned test imports and E2E selectors. Final source hashes are recorded in the frontend verification evidence.

All 89 browser E2E scenarios passed with real headless Chromium, two workers and zero skipped/flaky/failing cases. Existing fixtures intercept API calls, so browser navigation does not write development business data. Coverage includes login success/error/pending states, protected routes, module shells, desktop/mobile navigation, permissions, report dialogs, forms, logos, charts and light/dark layouts. Two ambiguous navigation selectors were tightened to actual accessible roles and scoped submenus; no product behavior was changed to satisfy them.

## Documentation and limits

The backend and frontend architecture documents contain their complete current source/test trees, and both before/after structures are recorded. OpenSpec strict validation passes. Source paths change internally; this is a modular monolith regrouping, not a microservice extraction or a new installable application system. Verification preceded the subsequent request to commit and push by feature. Area commits keep their source, tests and required collaborating references together; intermediate Java snapshots compile and pass architecture checks, and Angular snapshots pass TypeScript checks.
