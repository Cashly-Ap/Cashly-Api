# Notification Resource Navigation Specification

## Purpose

Define how notification clicks open referenced payments, budgets, orders, and invoices across backoffice and portal clients.

## Requirements

### Requirement: Notification click lifecycle

When a user clicks an unread notification, the client **MUST** request that it be marked read, remove it from the unread presentation, decrement the unread count without going below zero, close the notification popover, and resolve its destination. A read-request failure **MUST NOT** prevent destination resolution.

#### Scenario: Click an unread actionable notification

- GIVEN an unread notification with a supported reference
- WHEN the user clicks it
- THEN the client MUST mark it read and close the popover
- AND it MUST resolve the referenced destination

#### Scenario: Read request fails

- GIVEN an actionable notification whose read request fails
- WHEN the user clicks it
- THEN the client MUST still attempt destination resolution
- AND it MUST NOT produce an uncaught error

### Requirement: Supported notification families

The clients **MUST** support all existing `PAYMENT_*`, `QUOTE_*`, and `ORDER_*` types. Technical `QUOTE` references **MUST** resolve to the budgets UI. Conversion notifications **MUST** target the origin represented by their unchanged `referenceType` and `referenceId`.

#### Scenario: Existing type has a valid reference

- GIVEN any existing notification type with its supported reference type and ID
- WHEN its destination is resolved
- THEN the client MUST select the payment, budget, or order destination matching that reference

#### Scenario: Converted resource notification

- GIVEN a quote-to-order, quote-to-invoice, or order-to-invoice notification
- WHEN the user opens it
- THEN the client MUST open the referenced quote or order origin

### Requirement: Backoffice resource navigation

Backoffice **MUST** navigate valid references to the existing concrete detail route for the resource ID.

#### Scenario: Open backoffice detail

- GIVEN a supported notification with an accessible reference ID
- WHEN a backoffice user clicks it
- THEN the client MUST navigate to that resource's concrete detail route

### Requirement: Portal resource focus

Portal **MUST** navigate to the referenced resource collection with a stable focus value and open its existing detail view. Focus **MUST** be cleared after consumption or close so stale state does not reopen the detail.

#### Scenario: Focus portal resource

- GIVEN a supported notification with an accessible reference ID
- WHEN a portal user clicks it
- THEN the portal MUST navigate to the matching collection and focus that ID
- AND the matching detail view MUST open

#### Scenario: Close focused detail

- GIVEN a detail opened from notification focus
- WHEN the user closes it
- THEN the portal MUST clear focus without reopening the detail

### Requirement: Safe destination fallback

For a known resource family, null IDs, inaccessible or missing resources, and detail-load or guarded-navigation failures **MUST** fall back to its collection. Unknown or null reference types **MUST NOT** navigate. All fallback cases **MUST NOT** throw uncaught errors.

#### Scenario: Known family cannot open detail

- GIVEN a notification for a known family whose ID is null, missing, or inaccessible
- WHEN the destination is resolved
- THEN the client MUST show the matching collection without focused detail

#### Scenario: Unknown reference type

- GIVEN a notification with an unknown or null reference type
- WHEN the user clicks it
- THEN the client MUST remain on the current screen
- AND it MUST complete the click lifecycle without an uncaught error
