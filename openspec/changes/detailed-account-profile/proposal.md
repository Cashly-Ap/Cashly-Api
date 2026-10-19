# Detailed own-account profile

Replace the sparse account configuration summary with a complete view of the signed-in user's stored identity, contact/address, organizational assignment, account state, dates and current effective permissions. Preserve the existing account action routes and shared customer detail presentation.

Scope: authenticated self-profile GET/PUT, frontend self-profile service, account screen and own-profile form, focused tests and browser verification. No schema changes, credential exposure, arbitrary-user access, permission writes, invented session/last-login data or credential-edit API.

The user additionally requested personal-profile editing through the standard tabbed form and removal of the account UUID from the own-profile screen. Editable fields are limited to username, names, phone and address; organization, access, account status and credentials remain immutable through this operation.
