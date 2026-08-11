# Design: Notification Deep Links and Budget Terminology

## Technical Approach

Preserve the current notification payload and persistence. Add a typed destination resolver to each Angular client. Each notification renders an explicit contextual action button while its card body remains informational. The button completes read-state UI updates, hides its `Popover`, then delegates navigation. Backoffice uses existing detail routes; portal uses collection routes with `focus=<referenceId>`, consumed by the corresponding page to call its existing `openDetail(id)`. Backend work is limited to visible copy.

## Current Contract and Destination Matrix

REST controllers and WebSocket currently deliver `id`, `type`, `title`, `message`, `referenceType`, `referenceId`, `isRead`, `createdAt`, and `readAt`; references remain nullable.

| Notification type | Actual reference | Backoffice | Portal |
|---|---|---|---|
| `PAYMENT_PENDING_APPROVAL` | `PAYMENT`, payment ID | `/payments/:id` | `/payments?focus=:id` |
| `PAYMENT_APPROVED`, `PAYMENT_REJECTED` | `PAYMENT`, payment ID | `/payments/:id` | `/payments?focus=:id` |
| `QUOTE_CREATED`, `QUOTE_APPROVED`, `QUOTE_REJECTED` | `QUOTE`, quote ID | `/quotes/:id` | `/quotes?focus=:id` |
| `QUOTE_CONVERTED_TO_ORDER`, `QUOTE_CONVERTED_TO_INVOICE` | `QUOTE`, quote ID | `/quotes/:id` | `/quotes?focus=:id` |
| `ORDER_CREATED`, `ORDER_APPROVED`, `ORDER_REJECTED` | `ORDER`, order ID | `/orders/:id` | `/orders?focus=:id` |
| `ORDER_CONVERTED_TO_INVOICE` | `ORDER`, order ID | `/orders/:id` | `/orders?focus=:id` |

Conversion notifications deliberately open the origin. The payload contains no generated order/invoice ID; inferring or parsing one from message text is rejected.

## Architecture Decisions

| Decision | Alternatives | Rationale |
|---|---|---|
| Client-specific typed maps from `PAYMENT \| QUOTE \| ORDER` to collection routes | Backend URL or free-form switches | Avoids backend/UI coupling and centralizes exhaustive routing. |
| Keep `AppNotification` payload unchanged | Add target URL or destination DTO | No migration/versioning; current reference is sufficient. |
| Portal query parameter `focus` | New `/:id` portal routes or global state | Supports reloadable links while reusing page dialogs and `openDetail`. |
| Optimistic read UI; navigation independent of read API result | Wait for read response | Matches current UI and ensures read failures cannot block navigation. |
| “Presupuesto” only in visible copy | Rename `QUOTE` domain identifiers | Preserves API, database, routes, and integrations. |

## Data Flow and Fallback

```text
activate contextual notification button
  ├─ remove unread item; clamp count; PUT read; hide popover
  └─ typed resolver(referenceType, referenceId)
       ├─ valid backoffice → /collection/:id
       ├─ valid portal → /collection?focus=:id → page.openDetail(id)
       ├─ known type + null/inaccessible/missing/failure → /collection
       └─ null/unknown type → remain on current route
```

Portal pages subscribe to `queryParamMap`, ignore absent/duplicate focus, invoke `openDetail` once, then remove `focus` with `replaceUrl` and preserved unrelated parameters. If cleanup is canceled or rejected, it retries through Angular Router after the active navigation settles so `currentUrlTree` and the address bar converge; a direct address replacement is only a last visual fallback. Closing a dialog also clears any outstanding focus. API/detail errors leave the collection visible and use existing error toasts. Backoffice navigation resolving `false` or throwing triggers collection fallback. No branch throws to the action handler.

## Interfaces / Contracts

Each resolver exposes a closed reference union and destination result; raw API strings are validated at its boundary.

```typescript
type NotificationReferenceType = 'PAYMENT' | 'QUOTE' | 'ORDER';
type NotificationDestination = { collection: string; resourceId: string | null };
```

## File Changes

| Repository / files | Action | Description |
|---|---|---|
| `parsun-api/.../notifications/application/services/NotificationService.java` | Modify | Replace quote-related visible copy with “Presupuesto”. |
| `parsun-api/.../notifications/application/services/NotificationServiceTest.java` | Create | Lock producer copy and unchanged technical references. |
| `parsun-app/src/app/core/notifications/services/navigation/notification-destination.resolver.ts` and `.spec.ts` | Create | Typed mapping, detail navigation, fallbacks. |
| `parsun-app/src/app/core/notifications/components/notification-bell/notification-bell.component.{ts,html,spec.ts}` | Modify/Create | Contextual action, inert card body, popover close, resolver delegation. |
| `parsun-portal-web/src/app/core/notifications/services/navigation/notification-destination.resolver.ts` and `.spec.ts` | Create | Typed collection/focus mapping and fallbacks. |
| `parsun-portal-web/src/app/core/notifications/components/notification-bell/notification-bell.component.{ts,html,spec.ts}` | Modify/Create | Contextual action, inert card body and delegation. |
| `parsun-portal-web/src/app/features/{quotes,orders,payments}/pages/*.component.{ts,spec.ts}` | Modify | Consume and clear focus; open existing detail dialog. |
| `parsun-portal-web/src/app/features/auth/pages/login/login-page.component.html` | Modify | Use “presupuestos”. |

Invoices need no focus consumer because no existing notification references `INVOICE`.

## Testing Strategy

Use TDD per repository: first failing focused tests, minimal implementation, refactor, then relevant suites/builds. Backend unit tests verify all `QUOTE_*` texts and stable `QUOTE` references. Resolver tests cover every type family, conversions, null/unknown IDs/types, rejected navigation, and collection fallback. Bell tests prove the card body is inert and cover contextual labels, read call, optimistic count/list, `Popover.hide()`, and navigation despite read failure. Portal page tests cover focus consumption, deduplication, cleanup, same-resource reopening after cleanup failure, detail failure, and dialog close. Manual verification covers one deep link per family in each applicable client.

## Migration / Rollout

No migration, feature flag, payload change, or coordinated data rollout is required. Repositories can deploy independently; old clients ignore the existing reference fields as today.

## Open Questions

None.
