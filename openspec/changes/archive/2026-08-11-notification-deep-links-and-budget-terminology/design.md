# Design: Notification Deep Links and Budget Terminology

## Technical Approach

Preserve the current notification payload and persistence. Add a typed destination resolver to the back-office Angular client. Each notification renders an explicit contextual action button while its card body remains informational. The button completes read-state UI updates, hides its `Popover`, then delegates navigation to existing detail routes. Backend work is limited to visible copy.

## Current Contract and Destination Matrix

REST controllers and WebSocket currently deliver `id`, `type`, `title`, `message`, `referenceType`, `referenceId`, `isRead`, `createdAt`, and `readAt`; references remain nullable.

| Notification type | Actual reference | Backoffice |
|---|---|---|---|
| `PAYMENT_PENDING_APPROVAL` | `PAYMENT`, payment ID | `/payments/:id` |
| `PAYMENT_APPROVED`, `PAYMENT_REJECTED` | `PAYMENT`, payment ID | `/payments/:id` |
| `QUOTE_CREATED`, `QUOTE_APPROVED`, `QUOTE_REJECTED` | `QUOTE`, quote ID | `/quotes/:id` |
| `QUOTE_CONVERTED_TO_ORDER`, `QUOTE_CONVERTED_TO_INVOICE` | `QUOTE`, quote ID | `/quotes/:id` |
| `ORDER_CREATED`, `ORDER_APPROVED`, `ORDER_REJECTED` | `ORDER`, order ID | `/orders/:id` |
| `ORDER_CONVERTED_TO_INVOICE` | `ORDER`, order ID | `/orders/:id` |

Conversion notifications deliberately open the origin. The payload contains no generated order/invoice ID; inferring or parsing one from message text is rejected.

## Architecture Decisions

| Decision | Alternatives | Rationale |
|---|---|---|
| Client-specific typed maps from `PAYMENT \| QUOTE \| ORDER` to collection routes | Backend URL or free-form switches | Avoids backend/UI coupling and centralizes exhaustive routing. |
| Keep `AppNotification` payload unchanged | Add target URL or destination DTO | No migration/versioning; current reference is sufficient. |
| Optimistic read UI; navigation independent of read API result | Wait for read response | Matches current UI and ensures read failures cannot block navigation. |
| “Presupuesto” only in visible copy | Rename `QUOTE` domain identifiers | Preserves API, database, routes, and integrations. |

## Data Flow and Fallback

```text
activate contextual notification button
  ├─ remove unread item; clamp count; PUT read; hide popover
  └─ typed resolver(referenceType, referenceId)
       ├─ valid backoffice → /collection/:id
       ├─ known type + null/inaccessible/missing/failure → /collection
       └─ null/unknown type → remain on current route
```

Back-office navigation resolving `false` or throwing triggers collection fallback. No branch throws to the action handler.

## Interfaces / Contracts

Each resolver exposes a closed reference union and destination result; raw API strings are validated at its boundary.

```typescript
type NotificationReferenceType = 'PAYMENT' | 'QUOTE' | 'ORDER';
type NotificationDestination = { collection: string; resourceId: string | null };
```

## File Changes

| Repository / files | Action | Description |
|---|---|---|
| `cashly-api/.../notifications/application/services/NotificationService.java` | Modify | Replace quote-related visible copy with “Presupuesto”. |
| `cashly-api/.../notifications/application/services/NotificationServiceTest.java` | Create | Lock producer copy and unchanged technical references. |
| `cashly-app/src/app/core/notifications/services/navigation/notification-destination.resolver.ts` and `.spec.ts` | Create | Typed mapping, detail navigation, fallbacks. |
| `cashly-app/src/app/core/notifications/components/notification-bell/notification-bell.component.{ts,html,spec.ts}` | Modify/Create | Contextual action, inert card body, popover close, resolver delegation. |

Invoices need no focus consumer because no existing notification references `INVOICE`.

## Testing Strategy

Use TDD per repository: first failing focused tests, minimal implementation, refactor, then relevant suites/builds. Backend unit tests verify all `QUOTE_*` texts and stable `QUOTE` references. Resolver tests cover every type family, conversions, null/unknown IDs/types, rejected navigation, and collection fallback. Bell tests prove the card body is inert and cover contextual labels, read call, optimistic count/list, `Popover.hide()`, and navigation despite read failure. Manual verification covers one deep link per family in the back-office client.

## Migration / Rollout

No migration, feature flag, payload change, or coordinated data rollout is required. Repositories can deploy independently; old clients ignore the existing reference fields as today.

## Open Questions

None.
