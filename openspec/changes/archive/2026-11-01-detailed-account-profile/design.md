# Design

GET /api/auth/profile derives its subject from Spring Security Authentication and reuses the existing UserResponse projection/query. Register only this handler as authenticated self service in the fail-closed coverage policy. Do not reuse the administrative /api/users/{id} route from the account page.

A dedicated frontend self-profile service isolates the richer profile from the compact login/session contract. ConfigurationPage loads this profile and current access, using OnPush/signals and shared PageHeader/DetailsCard/DetailsInfoGrid/DetailsInfoField components. Display empty optional data explicitly and keep credential material out of the UI.

Implementation units: self-read/API tests; complete profile UI/service tests; read-only security audit, Docker build/tests, live browser desktop/mobile verification. No commit/push is included in this request.

## Personal-profile editing extension

PUT /api/auth/profile derives the subject from Authentication and accepts a strict personal-only request: userName, firstName, lastName, address, phone. Unknown identity, assignment, account state and credential properties are rejected. The transaction must serialize with administration before loading/saving, or use a field-specific update, so a concurrent role change cannot be overwritten.

Reuse the existing change-username URL for the shared tabbed Edit my profile form. General and Contact/address tabs use the same validation and components as user management, without access controls. Explicitly project the allowed payload keys in the self-profile HTTP service. Synchronize only cached display identity after a successful save; never derive authority from profile data. Remove the UUID from the own-profile view.
