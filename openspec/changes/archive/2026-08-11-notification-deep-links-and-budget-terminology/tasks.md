# Tasks: Notification Deep Links and Budget Terminology

## Review Workload Forecast

| Field | Value |
|---|---|
| Estimated lines | 600–850 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | API → app → portal foundation → portal focus |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|---|---|---|---|
| 1 | Backend terminology contract | API PR | `cashly-api` change and tests. |
| 2 | Backoffice deep links | App PR | `cashly-app` change and tests. |
| 3 | Portal resolver and bell | Portal PR 1 | Base per selected chain strategy. |
| 4 | Portal focus consumers | Portal PR 2 | Immediate parent Portal PR 1; includes UI tests. |

## Phase 1: Backend

- [x] 1.1 Confirm artifact matrices match `NotificationType`, producers, payload DTOs, and routes; record drift.
- [x] 1.2 RED: create `cashly-api/src/test/java/com/cashly/cashly_api/notifications/application/services/NotificationServiceTest.java` covering all `QUOTE_*` visible copy and unchanged `QUOTE` references.
- [x] 1.3 GREEN: update `cashly-api/src/main/java/com/cashly/cashly_api/notifications/application/services/NotificationService.java` to emit “Presupuesto” copy only.
- [x] 1.4 REFACTOR: consolidate fixtures without payload changes; run `./run-tests.sh NotificationServiceTest`, then the Docker suite. Evidence: 2/2 tests and Docker compile passed.

## Phase 2: Backoffice (`cashly-app`)

- [x] 2.1 RED: test `PAYMENT`, `QUOTE`, `ORDER`, conversions, null/unknown references, rejected navigation, and fallback.
- [x] 2.2 GREEN: create `src/app/core/notifications/services/navigation/notification-destination.resolver.ts` with typed validation and detail-route navigation.
- [x] 2.3 RED: test inert card-body clicks plus contextual-button optimistic read, count clamping, `Popover.hide()`, resolver delegation, and navigation after read failure.
- [x] 2.4 GREEN: update the notification bell with contextual `Ver pago`, `Ver pedido`, or `Ver presupuesto` actions; keep card bodies non-interactive.
- [x] 2.5 REFACTOR: remove duplicated mapping/error branches; run focused specs, Docker `bun test --run`, and `bun run build`. Evidence: focused 4/4 final iteration (previously 9/9) and build passed.

## Phase 3: Portal Foundation

- [x] 3.1 RED: test collection routes, `focus`, null/unknown references, navigation failure, and conversion origins.
- [x] 3.2 GREEN: create the typed resolver; update bell TS/HTML/spec for inert card bodies, contextual actions, read, close, focus, and read failures.
- [x] 3.3 REFACTOR: centralize mapping and ensure unsupported references never throw; run focused Vitest specs.

## Phase 4: Portal Focus and Copy

- [x] 4.1 RED: test quotes, orders, and payments focus consumption, deduplication, cleanup, close, and load failure.
- [x] 4.2 GREEN: call existing `openDetail(id)` once and clear `focus` with replacement navigation preserving other parameters and synchronizing Angular Router state after canceled/rejected cleanup.
- [x] 4.3 GREEN: change login copy from “cotizaciones” to “presupuestos”; verify no visible occurrence remains.
- [x] 4.4 REFACTOR: share only genuinely identical focus behavior; run portal Docker `bun test --run` and `bun run build`. Evidence: 58/58 tests and build passed.

## Phase 5: Documentation, Verification, Archive

- [x] 5.1 Review each repository diff; confirm no migration, contract, route, table, or technical rename. Evidence: all repository diff checks passed.
- [x] 5.2 Manual IAB verification confirmed copy and guards. Authenticated deep-link traversal remained unverified because no session or credentials were available.
- [x] 5.3 No additional module documentation was required; no ClickUp work was created.
- [x] 5.4 Final judgment APPROVED: both judges CLEAN at R4; archive authorized.
