# Budget Terminology Specification

## Purpose

Define the product vocabulary shown to Spanish-speaking users without changing technical quote identifiers.

## Requirements

### Requirement: Visible budget vocabulary

User-visible Spanish copy **MUST** use “presupuesto” or “presupuestos” instead of “cotización” or “cotizaciones”. This applies to backend-produced notification titles and messages and detected frontend UI copy.

#### Scenario: Budget notification is produced

- GIVEN an event that produces a `QUOTE_*` notification
- WHEN its title and message are delivered by REST or WebSocket
- THEN visible Spanish copy MUST use the grammatically correct “presupuesto” form
- AND it MUST NOT contain “cotización” or its unaccented variant

### Requirement: Stable technical contract

Terminology changes **MUST NOT** rename `QUOTE`, `QUOTE_*`, `Quote*`, routes, endpoints, tables, payload fields, or persisted values. REST and WebSocket payload structures **MUST** remain unchanged.

#### Scenario: Notification payload after copy change

- GIVEN a budget-related notification after terminology normalization
- WHEN its payload is serialized
- THEN its technical type and reference values MUST remain unchanged
- AND only user-visible title or message text MAY differ
