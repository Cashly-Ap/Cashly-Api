# Requirements

## Company
- Given an existing installation, when the migration runs, then one company exists and existing business records reference it without changing their identities.
- Given a new business record, when inserted through existing application flows, then ownership defaults to the installation company and cannot reference a nonexistent company.
- Given an authenticated user, when requesting company identity/logo, then access does not require catalog permissions.
- Given a user without company-update permission, when updating company details or images, then the API denies the mutation.
- Given an authorized company editor, when saving valid fiscal/contact details, then the values persist across reloads.
- Given a company logo, when viewing the authenticated navbar or mobile navigation, then it is displayed; absent/unloadable logo uses the current Cashly fallback.

## Photos
- Given user creation or editing with a selected photo, when saved successfully, then that user's photo persists; upload failure must not cause duplicate user creation on retry.
- Given a user editing their own profile, when changing/deleting the photo, then their profile/navbar update immediately and continue to match after reload.
- Given an ordinary user, when attempting to alter another user's photo, then the API denies access unless the appropriate user-management permission is present.
- Given no photo or failed media loading, then the UI shows the existing initials fallback.
- Given an invalid, oversized or unsupported image, when submitted, then the API rejects it without changing stored profile/company data or deleting the previous image.
- Given authenticated image reads, then the browser requests use authenticated blob loading; no persistent credentials appear in URLs.

## Presentation
Use existing form containers, headers, cards, field controls and file input components. Keep the same canvas, panel, border, radius and shadow rules used by current lists/profile/Security pages. Verify desktop/mobile layouts and both branding fallbacks.
