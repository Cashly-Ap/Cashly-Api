# Notification Resource Navigation Specification

## Purpose

Define how explicit notification actions open referenced payments, budgets, orders, and invoices in the back-office client.

## Requirements

### Requirement: Notification action lifecycle

The notification card **MUST** remain informational and **MUST NOT** navigate or change read state when its body is clicked. Each actionable notification **MUST** expose a contextual button. When that button is activated, the client **MUST** request that it be marked read, remove it from the unread presentation, decrement the unread count without going below zero, close the notification popover, and resolve its destination. A read-request failure **MUST NOT** prevent destination resolution.

#### Scenario: Click notification body

- GIVEN an unread actionable notification
- WHEN the user clicks its card body outside the action button
- THEN the client MUST NOT navigate
- AND it MUST NOT mark or remove the notification

#### Scenario: Click an unread actionable notification

- GIVEN an unread notification with a supported reference
- WHEN the user activates its contextual action button
- THEN the client MUST mark it read and close the popover
- AND it MUST resolve the referenced destination

#### Scenario: Read request fails

- GIVEN an actionable notification whose read request fails
- WHEN the user activates its contextual action button
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
- WHEN a backoffice user activates its contextual action button
- THEN the client MUST navigate to that resource's concrete detail route

### Requirement: Safe destination fallback

For a known resource family, null IDs, inaccessible or missing resources, and detail-load or guarded-navigation failures **MUST** fall back to its collection. Unknown or null reference types **MUST NOT** navigate. All fallback cases **MUST NOT** throw uncaught errors.

#### Scenario: Known family cannot open detail

- GIVEN a notification for a known family whose ID is null, missing, or inaccessible
- WHEN the destination is resolved
- THEN the client MUST show the matching collection without focused detail

#### Scenario: Unknown reference type

- GIVEN a notification with an unknown or null reference type
- WHEN the user activates its contextual action button
- THEN the client MUST remain on the current screen
- AND it MUST complete the click lifecycle without an uncaught error
