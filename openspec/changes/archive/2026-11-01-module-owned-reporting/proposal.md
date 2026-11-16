# Module-owned reporting

## Why

The global reports package separates document generation and report queries from their business owners. The user approved an explicit reporting submodule within each owner, including production code and tests.

## What Changes

- Move invoices, orders, quotes and purchases reports into each owner's reporting submodule.
- Move stock, movements and the warehouse inventory catalog into warehouses/reporting; commercial product catalog into products/reporting.
- Move payment receipts/history into payments/reporting and debt/collections into receivables/reporting.
- Split the mixed finance controller/configuration and inventory configuration by owner.
- Move neutral PDF presentation helpers into shared/infrastructure/pdf.
- Preserve URLs, permissions, response contracts, SQL, business behavior and all existing test cases.
- Update architecture guards, module documentation and equivalent class-aware coverage gates.

## Impact

The 148 production and 79 test files in the former reports tree are fully accounted for by relocation-manifest.json and explicit split mappings. This extends the verified screaming-backend-structure change and supersedes its global reports ownership only. Existing payment-history and collections empty-data placeholders remain unchanged. No frontend move, new endpoints, migrations or domain events are in scope.

The user authorized implementation, complete verification, commit and push. Delivery is on the existing develop branch with a normal push; no PR or force push is requested.
