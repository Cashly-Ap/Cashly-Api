# Verification: installation company and user photos

## Delivered scope
One company per installation, as explicitly selected by the user. Settings include legal/trade names, fiscal identifier, fiscal address and contact details. Company branding appears only after authentication and preserves existing Cashly assets as fallback. User administration and own-profile editing support image upload/removal; navbar, profile and user list/details use authenticated image loading with initials fallback.

## Automated checks
- Docker Maven: 580 tests passed across company/avatar/image handling and endpoint permission coverage/enforcement. Includes verification that the generic catalog file endpoint cannot serve identity media.
- Docker Vitest: 60 tests passed across layout, company image state, avatars, shared file input and profile/user forms. After adding two creation/photo-failure cases, all eight affected user-form tests passed (62 distinct frontend tests across scoped runs).
- Production Angular build passed. Existing qrcode/STOMP CommonJS notices remain.
- Five Chromium browser scenarios passed: company field/logo persistence and bounded media reads; mobile shared panel/fallback/no horizontal overflow; denied company route without permission; logo removal restores Cashly icon; own-photo upload updates navbar and persists on reload.
- Desktop/mobile screenshots reviewed under the neighboring frontend checkout's `output/playwright/company/`.
- Prettier checks and both repositories' `git diff --check` passed.

## Real integration evidence
The live API/MinIO/MySQL smoke passed company read/update persistence, logo upload/replacement/removal, PNG output and nosniff, corrupt/SVG rejection without losing prior media, self-photo and administrative photo operations, permission denial, login photo metadata and missing-image 404 behavior. A minimal disposable role without CATALOG or USERS access successfully read the company logo and managed only its own photo. The catalog media alias was verified to reject avatar access even for an administrator.

MySQL verification found exactly one company, 66 required company foreign keys, zero nullable company links and zero application tables missing ownership. V30 adds user photo references and V31 adds installation ownership. The company migration was numbered V31 because V30 had already been applied during the earlier work; no applied migration was changed and no out-of-order migration setting was introduced.

Company details/logo were restored after smoke tests. Disposable users, roles, photo files and their MFA/refresh records were removed.

## Integration fixes
- Prevented reactive logo state from triggering repeated navbar reloads.
- Preserved the existing report EXPORT checks while adding the separate COMPANY:UPDATE menu action.
- Returned 404 for absent user photos at the controller boundary so global exception handling cannot turn normal fallback into HTTP 500.
- Restricted catalog file aliases from exposing the new user/company image namespaces.
- Kept a successfully created user when a subsequent photo upload fails, with a warning and an edit retry rather than duplicate registration.

## Limits
This is installation ownership, not a multi-company tenant model. New database tables must include the installation company link in their migrations. Image uploads accept PNG/JPEG up to 5 MiB and 4096 pixels per dimension. Company/profile text and image saves are separate requests; an image failure may leave successfully saved text in place, with a retry message. The full repository-wide test suite was not run. Existing PDF templates were outside the navbar/profile scope and were not changed.
