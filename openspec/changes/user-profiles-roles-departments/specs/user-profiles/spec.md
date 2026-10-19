# User Profiles Specification

## Purpose

Maintain detailed user records while assigning access through reusable roles.

## Requirements

### Requirement: Detailed personal profile

The system MUST retain username, email and account status and support first names, surnames, structured address and a single optional phone number. Users MUST provide first names and surnames. Names MUST be trimmed Unicode text of 1–100 characters; address fields MUST be optional (street 200, city/region/country 100, postal code 20 characters). The optional phone MUST accept 7–15 digits with an optional leading plus and normalize presentation separators. The API MUST expose one nullable `phone` string and reject the former `phones` collection. Credentials MUST retain existing validation and secrecy.

#### Scenario: Save full profile
- GIVEN a permitted user administrator and valid profile fields
- WHEN they create or update a user
- THEN all fields round-trip through form, API, details and persistence
- AND username and email retain their existing purpose

#### Scenario: Invalid profile
- GIVEN a blank required name, excessive field length, invalid phone
- WHEN a profile is submitted
- THEN field validation fails without partial changes

### Requirement: Assign references instead of individual grants

Each new or edited user MUST select exactly one active role and one active department by identifier. The user form MUST display searchable role/department selects and MUST NOT contain a permission editor or fixed role enum choices. User APIs MUST reject direct permission grants; profile and reference changes MUST be atomic. Ordinary profile updates MUST NOT let an actor change their own or another user's access without authorized user administration and role-assignment eligibility.

#### Scenario: Assign existing role and department
- GIVEN active selectable references and an authorized administrator
- WHEN a user is saved
- THEN the user references those records and inherits only the selected role's permissions
- AND departments confer no access

#### Scenario: Stale or forged selection
- GIVEN a deleted, inactive, unknown or ineligible reference
- WHEN a user is saved
- THEN the operation fails without changing the profile or references

### Requirement: Protect account continuity

The system MUST retain existing associated-record deletion safeguards and MUST reject deleting, deactivating or reassigning the last active access administrator. A self-service credential or profile operation MUST NOT grant assignment or permission-management powers.

#### Scenario: Last administrator change
- GIVEN one remaining active administrator able to manage users and roles
- WHEN an operation would remove that ability
- THEN it is rejected even during concurrent administration

#### Scenario: Referenced account deletion
- GIVEN a user with associated business records
- WHEN deletion is requested
- THEN existing reference protections remain enforced

#### Scenario: Concurrent administrator removal
- GIVEN two remaining active access administrators
- WHEN concurrent transactions each attempt to remove one administrator's access
- THEN at most one transaction succeeds
- AND at least one active access administrator remains

### Form presentation

User forms MUST reuse the established shared form container, header, card, inputs and actions. Fields MUST be grouped into General, Contact and address, and Access tabs using the same PrimeNG tabs as supplier/customer forms. Role forms MUST separate General and Permissions. Management lists MUST reuse the application page header, list container and PrimeNG table styling.
