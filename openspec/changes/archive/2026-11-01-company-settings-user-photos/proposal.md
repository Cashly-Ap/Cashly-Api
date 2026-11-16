# Installation company and user photos

## Intent and confirmed scope
The user explicitly selected one company per installation. All existing and future installation records belong to that company. Add company legal/contact details and branding, plus user avatars in administration and self-profile editing. Preserve the existing Cashly design system and its shared card shadows.

## Scope
- One immutable company identity; editable legal name, trade name, fiscal identifier, fiscal address, email, telephone and website.
- Migration associates existing records with the company and enforces ownership for new records through mandatory foreign keys/defaults. No company selector or cross-company account model.
- Authenticated company branding; permission-controlled settings and logo mutation.
- Upload/replace/remove company logo and user photos using existing storage and shared upload controls.
- Navbar branding and avatar immediately reflect saved changes with the exact current assets/initials as fallbacks.
- User administration create/edit and self-profile editing support photos; profile/list/detail surfaces show available photos.

## Risks and rollback
Preserve current MFA/authentication changes and existing data. Apply new migrations only. File storage and database changes must not destroy the old image on failed replacement. Validate actual raster content, dimensions and size; do not open catalog file permissions to support branding. Database association is installation ownership, not tenant isolation. Back up the database before applying changes outside development.
