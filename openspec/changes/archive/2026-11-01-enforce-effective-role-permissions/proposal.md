# Enforce effective role permissions

## Intent
Make assigned role grants authoritative for every business read/write, route, action and navigation item. An account without READ must neither see the module nor open its URLs; a read-only account must not mutate data.

## Scope
Audit persisted role resolution, secured API operations, Angular guards, shared actions and menu pruning. Fix confirmed gaps without changing assigned roles, credentials or customer data. Preserve the existing self-service profile access.

## Rollback
Revert this change's enforcement refinements and regression tests. No migration or role grant changes are planned.
