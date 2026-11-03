# Proposal: User profiles, roles and departments

## Intent

Users need complete personal records and reusable job roles whose saved permissions actually control access and actions. Today users have a fixed role enum and direct module grants; catalog and inventory cannot be independently configured.

## Scope

### In Scope
- Personal names, surnames, address and one optional phone number.
- Dedicated Roles and Departments management modules; single role and department selects on users.
- Named roles with independently configurable permissions for every module and its supported actions, enforced throughout API and Angular. Inventory denied and catalog read-only is only an illustrative combination.
- Coherent beta database reset/seed plan, session permission refresh, administrator continuity and regression coverage.

### Out of Scope
- Implementation in this change preparation, multiple simultaneous roles, department-based grants, new tenancy, payroll and organizational hierarchies.

## Capabilities

### New Capabilities
- `user-profiles`: Detailed profile validation and transactional role/department assignment.
- `role-management`: Role lifecycle, permission matrix, runtime enforcement and beta bootstrap.
- `department-management`: Department lifecycle and user selection.

### Modified Capabilities
None. Existing baseline specs have no user or authorization capability; this explicitly specified contract change preserves their module boundaries.

## Approach

Extend the modular backend and Angular features. Replace enum/direct-grant administration with persisted roles and deny-by-default actions. Split CATALOG from INVENTORY; map every authorization check. Resolve current access on every request. Defaults in the design are proposed decisions.

## Affected Areas

| Area | Impact |
|---|---|
| `src/main/java/com/cashly/cashly_api/platform/auth/` | Profile, role, department and token authorization contracts |
| `src/main/resources/db/migration/`, `db/` | Fresh beta schema and complete consistent seed reset |
| `../cashly-app/src/app/features/platform/auth/` | User forms, new management pages, permission state/guards |
| `../cashly-app/src/app/core/layouts/` | Permission-filtered navigation |
| Business controllers and Angular feature actions | Module/action enforcement |

## Risks

| Risk | Mitigation |
|---|---|
| Privilege expansion or lockout | Explicit bootstrap grants, protected last administrator |
| Stale JWT privileges | Authoritative access lookup per request; refresh client state |
| Unmapped operations | Complete endpoint/route inventory and deny-by-default coverage tests |

## Rollback Plan

For disposable beta/dev/test only, revert application/schema/seed versions together and rebuild the disposable database from matching seeds. No production reset is permitted. Preparing this specification performs no deletion or reset; a future reset must use an explicitly identified disposable target.

## Dependencies

Coordinate existing architecture/reporting changes without changing their paths or behavior accidentally.

## Success Criteria

- [ ] Profile and selects persist atomically; users cannot edit direct grants.
- [ ] Every module supports independent denial, read-only access and selected supported actions in UI and direct API calls; inventory/catalog is only an example.
- [ ] Existing sessions lose revoked access on their next request.
- [ ] Fresh seeded startup, login and navigation work with valid business references and an active access administrator.
