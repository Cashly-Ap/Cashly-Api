# Tasks: Simple shortcuts and screen hints

## Review workload forecast
Estimated changed lines: 300–600 including tests. Local reviewable units; no PR publication requested.
Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

Units: backend contract/migration/tests; frontend simple bindings/context help/tests.

## Backend foundation
- [x] 1.1 Write preference tests for true defaults, false persistence and missing/null hints rejection.
- [x] 1.2 Add required showShortcutHints to domain/request/entity/upsert and V33 default-true column.
- [x] 1.3 Run focused Maven/permission tests and real API/MySQL migration/session smoke; clean disposable rows.

## Frontend
- [x] 2.1 Normalize legacy cache and add showShortcutHints signal/setter, preserving account queue/isolation.
- [x] 2.2 Replace bindings with N/F/B/G/Escape/M and update editable/native safety tests.
- [x] 2.3 Add settings toggle and contextual help derived from active available actions; test visibility/permissions/modal scope.
- [x] 2.4 Run browser simple-action/help tests and production build.

## Closure
- [x] 3.1 Record runtime compliance, sync delta/new capability and archive only after verification passes.
