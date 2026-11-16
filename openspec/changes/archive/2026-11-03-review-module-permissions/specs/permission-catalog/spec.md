# Permission Catalog

## ADDED Requirements

### Requirement: Grouped permission catalog
The system SHALL expose a catalog that describes every permission registered by `PermissionRegistry` exactly once, classified by module and, inside each module, by group.

#### Scenario: Every registered permission is classified
- **GIVEN** the permission registry defines the actions of a module
- **WHEN** the application starts
- **THEN** the catalog SHALL contain one entry per registered `(module, action)` pair
- **AND** startup SHALL fail if a registered pair is unclassified or a catalog entry is not registered

#### Scenario: Catalog preserves registry semantics
- **GIVEN** the catalog is served to a client
- **WHEN** a module group lists its permissions
- **THEN** the union of the actions in all groups of a module SHALL equal `PermissionRegistry.modules().get(module)`

### Requirement: Catalog endpoint
The API SHALL serve the catalog through `GET /api/permissions/catalog`, ordered deterministically, and SHALL require `ROLES:READ`.

#### Scenario: Authorised read
- **GIVEN** an authenticated user holding `ROLES:READ`
- **WHEN** the user requests the catalog
- **THEN** the API responds `200` with modules sorted by `sortOrder` and groups and permissions sorted within their parent

#### Scenario: Denied read
- **GIVEN** an authenticated user without `ROLES:READ`
- **WHEN** the user requests the catalog
- **THEN** the API responds `403`

#### Scenario: Entry metadata
- **GIVEN** a permission entry
- **WHEN** it is serialised
- **THEN** it carries `action`, `labelKey`, `descriptionKey` and `sortOrder`
- **AND** each group carries `key`, `labelKey` and `sortOrder`

### Requirement: Detailed permissions inside the role editor
The role editor SHALL render, per module and group, every permission of the catalog with its allowed or denied state, and SHALL keep granting actions through the existing module presets.

#### Scenario: Grouped detailed permissions
- **GIVEN** the user edits a role and opens the Permissions tab
- **WHEN** a module section is expanded
- **THEN** each group of that module is displayed with its label
- **AND** every permission row shows its business label, its description and an allowed or denied state
- **AND** denied rows are visible, not hidden

#### Scenario: Toggling a permission
- **GIVEN** a role without any company permission
- **WHEN** the user checks the `COMPANY:READ` row
- **THEN** the module preset becomes read-only
- **AND** the row shows allowed

#### Scenario: Delegation ceiling
- **GIVEN** a permission the actor does not hold
- **WHEN** the editor renders that row
- **THEN** the row is not editable and is marked as outside the actor's scope

#### Scenario: System administrator role
- **GIVEN** the edited role is the protected system administrator
- **WHEN** the Permissions tab renders
- **THEN** every catalog action of every module is shown as allowed and the tab is read-only

### Requirement: No standalone permission screen
Permissions SHALL be inspected and edited only through the role editor; the application SHALL NOT expose a separate permission page.

#### Scenario: No permission route
- **GIVEN** an authenticated user
- **WHEN** the user navigates to `/platform/permissions`
- **THEN** the route does not exist and the request falls back to the default route
