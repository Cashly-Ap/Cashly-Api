# Role Management Specification

## Purpose

Replace fixed roles and direct grants with reusable, enforceable permissions.

## Requirements

### Requirement: Role lifecycle and permission matrix

Authorized administrators MUST manage named roles and persisted module/action grants in a dedicated Roles module. Names MUST be trimmed, case-insensitively unique, 1–100 characters; optional descriptions MUST be at most 500. Supported actions MUST include READ, CREATE, UPDATE, DELETE, EXPORT, IMPORT and EXECUTE where applicable. CATALOG and INVENTORY MUST be independently configurable; USERS, ROLES and DEPARTMENTS MUST be separate. Missing grants MUST deny; mutations MUST require READ. Grants MUST NOT be inferred from a role name. Unknown combinations MUST fail validation. Referenced roles MUST NOT be deleted or deactivated before reassignment.

Every application module MUST support independent permission configuration: no access, READ only, or any valid combination of supported actions. Inventory denied and catalog read-only MUST be treated solely as an example, not a special policy or the limit of configurability.

#### Scenario: Configure any module independently
- GIVEN any module in the permission registry and an authorized role administrator
- WHEN the administrator saves no grants, READ only, or READ plus selected supported actions
- THEN assigned users can perform exactly the granted actions in that module in the API and interface
- AND grants for other modules remain independently configured

#### Scenario: Illustrative custom restricted role
- GIVEN a custom role grants supported actions on all modules except INVENTORY and grants only CATALOG READ
- WHEN an assigned user signs in
- THEN permitted modules work, inventory routes and APIs are denied
- AND catalog can be viewed but create, edit, delete, import and export fail

#### Scenario: Safe role administration
- GIVEN a referenced role or an unsupported permission
- WHEN deletion, deactivation or invalid matrix saving is attempted
- THEN the operation fails without partial changes

### Requirement: Complete authoritative enforcement

Every protected endpoint and frontend route/action MUST map to explicit permissions, including bulk, status transitions, conversions, uploads, downloads, reports and background operation entry points. API enforcement MUST be authoritative; absent authentication MUST yield 401 and insufficient permission 403. Navigation, direct URLs, dialogs and buttons MUST reflect permissions. Granting or assigning permissions beyond the actor's own delegable grants MUST be forbidden, with last-administrator continuity protected. Existing deployment/resource boundaries MUST remain; departments MUST NOT create tenancy or bypass ownership rules.

#### Scenario: Bypass UI controls
- GIVEN CATALOG READ without UPDATE or EXECUTE
- WHEN the user calls a write or conversion endpoint directly
- THEN the API returns 403 and performs no side effects

#### Scenario: Escalation attempt
- GIVEN an administrator lacks a requested grant
- WHEN they add it to a role or assign a more privileged role
- THEN the request is denied

### Requirement: Current access for existing sessions

Permission changes, role reassignment and account/role deactivation MUST affect the next request after commit, including requests with existing access or refresh tokens. Legacy JWT claims MUST NOT preserve revoked grants. The client MUST refresh effective access on session restoration and authorization failure and remove stale navigation/actions.

#### Scenario: Revoke while signed in
- GIVEN a signed-in user with CATALOG UPDATE
- WHEN that grant is removed and committed
- THEN the existing token cannot update catalog on its next request
- AND client access state refreshes without restoring the grant

### Requirement: Coherent disposable beta bootstrap

The beta change MUST provide a fresh/reset schema and consistent seeds without preserving old data. Reset MUST be limited to explicitly selected disposable beta/dev/test targets. Every seed loader, SQL seed, bootstrap migration, backend fixture and frontend mock MUST use valid new role/department/user references. Seeds MUST be deterministic, documented in dependency order and repeatable through a documented clean reset. Business records MUST remain referentially and financially consistent; bootstrap MUST create an active protected administrator and a catalog-read-only, inventory-denied demonstration user. This specification MUST NOT itself reset a database.

#### Scenario: Fresh beta startup
- GIVEN an explicitly selected disposable empty database
- WHEN schema and all documented seeds are loaded
- THEN integrity checks pass and administrator/demo login and navigation work
- AND referenced sales, stock, balances, payments and returns remain consistent
