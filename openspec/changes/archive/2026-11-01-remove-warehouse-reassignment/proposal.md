# Remove standalone warehouse reassignment and repair sales conversions

## Why

The user requested removal of the post-creation warehouse assignment dialogs and their backend-only endpoints. Quote conversions currently return HTTP 409 because Hibernate encounters two category snapshot objects with the same shared primary key in one persistence context. Order persistence has the same mapping pattern.

## What Changes

- Delete standalone reassignment UI, actions, service methods and request types in quotes/orders; inspect invoices for equivalent code.
- Delete exclusive backend assignment use cases, requests, routes, wiring and unused dependencies, preserving initial create/edit allocations.
- Correct shared-primary-key category snapshot persistence for quotes and orders without disabling historical categories, stock, credit or transaction rules.
- Reproduce failed conversion saves against isolated MySQL and retain regression tests.
- Fix real cash conversion FK ordering: persist the draft invoice before payment allocations, while keeping rollback atomic.
- Preserve conversion dialogs and expose useful server errors to the user.

## Scope

No development document/data mutations, schema changes, stock-control bypasses, frontend redesign, commits or pushes are requested in this turn. Existing resources and historical data remain intact. Removing reassignment is an explicitly approved API removal and supersedes earlier blanket route-preservation requirements only for those endpoints.
