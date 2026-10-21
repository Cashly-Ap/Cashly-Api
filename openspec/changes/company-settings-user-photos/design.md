# Technical approach

The installation contains one immutable company row. A new migration backfills ownership and supplies a mandatory company foreign key/default for persistence tables. Existing ORM inserts inherit the company automatically; no tenant context or tenant filtering is introduced for a single-company installation.

Company identity and company logo reads are available to authenticated users. Mutations require COMPANY:UPDATE. Self-photo handlers derive the user ID from the authenticated principal; administrative photo handlers enforce user-management permissions. All handlers declare explicit authorization to satisfy permission-coverage enforcement.

Reuse the configured FileStorageService through shared validated raster storage. Bound file size and decoded dimensions, normalize images to PNG, generate storage names and retire replaced files only after successful database work. Dedicated logo/avatar read endpoints preserve catalog storage restrictions.

Angular uses shared form/card/upload components and an authenticated company/identity media state. Revoke object URLs when replaced or destroyed. Keep image state synchronized after self edits and clear private media on logout. Registration-photo failures must retain the created user identity for safe retry.

Verification combines schema/backfill checks, permission and file-validation tests, focused frontend tests/build, mocked browser workflows and live HTTP smoke with disposable data. Restore any temporary changes to company settings/logo after testing.
