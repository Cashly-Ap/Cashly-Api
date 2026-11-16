# Department Management Specification

## Purpose

Manage organizational departments independently from access roles.

## Requirements

### Requirement: Department administration

The system MUST offer a dedicated Departments module with list, detail, create, edit and deactivate operations, governed by its own role permissions. Names MUST be trimmed, 1–100 characters and case-insensitively unique within the existing deployment scope; description MUST be optional and limited to 500 characters. Departments MUST NOT confer permissions.

#### Scenario: Create and select a department
- GIVEN DEPARTMENTS create permission
- WHEN an administrator creates a uniquely named active department
- THEN it appears in the department list and authorized user-assignment options

#### Scenario: Invalid or unauthorized update
- GIVEN a duplicate name or an actor without DEPARTMENTS update permission
- WHEN they submit a department update
- THEN the operation fails and the record remains unchanged

### Requirement: Reference-safe department lifecycle

The system MUST block deletion or deactivation of a department referenced by any user until those users are reassigned. Unreferenced departments MAY be deleted with delete permission. Assignment lookups MUST expose only active departments and only minimal selectable fields to authorized user administrators, without granting department management access.

#### Scenario: Department still in use
- GIVEN a department assigned to an active or inactive user
- WHEN deletion or deactivation is requested
- THEN a conflict explains reassignment is required
- AND users retain their department

#### Scenario: User administrator needs options
- GIVEN user-management access without department-management read access
- WHEN the user form requests department options
- THEN only active department identifiers and names are available
- AND department details and mutations remain forbidden
