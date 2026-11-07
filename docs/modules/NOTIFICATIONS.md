# Notifications

In-app notifications for authenticated users. The module has two responsibilities:

1. Business notifications (existing `NotificationType` values).
2. **Activity notifications**: one notification per recipient for every write action executed in the application.

## API

Base path: `/api/notifications`. All endpoints are self-service (`isAuthenticated()`).

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | List the authenticated user's notifications |
| GET | `/unread-count` | Return unread count |
| PUT | `/{id}/read` | Mark one notification as read |
| PUT | `/read-all` | Mark all visible notifications as read |
| GET | `/preferences` | Read the notification preferences |
| PUT | `/preferences` | Update master switch and module preferences |

### Preferences payload

```json
{
  "enabled": true,
  "modules": [
    { "module": "FINANCE_BILLING_INVOICES", "labelKey": "platform.auth.domains.finance_billing_invoices", "enabled": true }
  ]
}
```

Only modules where the user holds at least one mutating permission are eligible. System administrators see every module with a mutating action. Missing rows mean **enabled**.

## Activity engine

Flow:

1. A use case performs its write operation inside its transaction.
2. It calls `ActivityNotificationRecorder.record(ActivityEvent)`.
3. `ActivityNotificationService` resolves recipients through `NotificationAudienceGateway`.
4. One `ACTIVITY` notification is persisted per recipient and delivered over the personal WebSocket topic after commit.

Audience rules:

- active users with the exact `MODULE_ACTION` permission;
- `SYSTEM_ADMIN` roles are always included;
- the actor is excluded;
- master switch and module preference must be enabled;
- inactive users and inactive roles are excluded.

Read operations (`VIEW_*`, reports, downloads, history) never generate activity. `ActivityActionPolicy` classifies every `PermissionAction`.

Message keys live in `modules/platform/notifications/i18n/messages_{es,en}.properties`. Arguments prefixed with `i18n:` are resolved as message keys before rendering, so the same stored notification renders in each recipient's language.

Reference labels use `ActivityActionPolicy` keys and `ActivityEntityCatalog` nouns (quote, invoice, payment, etc.).

## Rules

- No notification is sent to the actor.
- No notification is sent for read-only actions.
- One business action produces one activity per recipient.
- Internal services called by other modules do not emit activity; only the entry use case does.
- The customer portal was removed: there is no `customer_id`, customer topic, or customer-specific notification API.

## Code map

- `platform/notifications/domain` — notification entity, type, repository, preferences.
- `platform/notifications/application/ports/in` — `ActivityNotificationRecorder`, `ActivityEvent`.
- `platform/notifications/application/ports/out` — audience, actor, preferences, delivery, permission access.
- `platform/notifications/application/services` — activity and preferences services.
- `platform/notifications/infrastructure` — JPA/JDBC adapters, WebSocket delivery, REST controller.
