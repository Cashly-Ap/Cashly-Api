# Proposal: Notification Deep Links and Budget Terminology

## Intent

Notifications become read without opening their resource. Visible copy mixes “cotización” with “presupuesto”. This change makes existing types actionable and standardizes terminology across clients.

## Scope

### In Scope

- Resolve every existing `PAYMENT_*`, `QUOTE_*`, and `ORDER_*` notification through its current `referenceType` and `referenceId`.
- Open back-office detail routes for referenced resources.
- Fall back safely for null, unknown, inaccessible, missing, or failed references.
- Replace detected visible “cotización(es)” with “presupuesto(s)”.
- Test copy, routing, focus, read behavior, and fallbacks.

### Out of Scope

- Renaming `QUOTE`, `Quote*`, enums, endpoints, tables, or routes.
- Migrations, payload changes, or stored frontend URLs.
- Generated resource IDs for conversion notifications.
- ClickUp changes or unrelated notification redesign.

## Capabilities

### New Capabilities

- `notification-resource-navigation`: Clicking any existing notification opens or focuses its referenced resource with deterministic fallbacks.
- `budget-terminology`: User-visible product copy consistently uses “presupuesto(s)” instead of “cotización(es)”.

### Modified Capabilities

None; no existing main specs are present.

## Approach

The back-office client maps current `referenceType` values to existing `/{resource}/:id` routes. A click marks the notification read and closes the popover. Unsupported or failed targets use the known collection, or remain in place without a mapping.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `notifications/application/services/NotificationService.java` | Modified | Standardize visible budget copy. |
| `cashly-app/src/app/core/notifications/` | Modified | Route clicks and fallback. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Deleted or unauthorized reference | Medium | Catch loading/guard failures and use collection fallback. |
| Focus reopens after close/reload | Medium | Consume and clear the parameter deterministically. |
| Read state diverges on navigation failure | Low | Specify and test click ordering and API failure behavior. |

## Rollback Plan

Revert click/focus and copy changes independently; API payload and persistence remain compatible.

## Dependencies

- Existing back-office detail endpoints, routes, and guards.

## Success Criteria

- [ ] Every existing notification type opens or focuses the intended resource in both applicable clients.
- [ ] Null, unknown, inaccessible, missing, and failed references follow the specified fallback without uncaught errors.
- [ ] No visible “cotización(es)” remains in the detected notification and login copy.
- [ ] Payload, persistence schema, endpoints, and technical `QUOTE` identifiers remain unchanged.
