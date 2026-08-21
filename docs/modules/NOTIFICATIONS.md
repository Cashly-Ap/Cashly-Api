# Notifications

Notifications deliver role-scoped in-app events and unread counts.

## API

Base path: `/api/notifications`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | List the authenticated user's notifications |
| GET | `/unread-count` | Return unread count |
| PUT | `/{id}/read` | Mark one notification as read |
| PUT | `/read-all` | Mark all visible notifications as read |

## Rules

- A user can only read notifications addressed to that user or role.
- Read operations are idempotent.
- Notification creation is performed by application events/use cases.

## Code map

See `notifications/domain`, `notifications/application`, and `notifications/infrastructure`.
