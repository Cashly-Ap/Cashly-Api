# Installation company and identity images

- The installation has exactly one immutable company identity. Existing persisted records and new records inserted into current tables reference it through mandatory foreign keys and database defaults.
- Authenticated users can read company identity and logo independently of catalog permissions. Company settings/logo mutation requires COMPANY:UPDATE.
- Legal/trade names, fiscal identifier, fiscal address and contact details are editable from the shared company form. The authenticated navbar/mobile navigation display the company logo and current brand name; absent/unloadable images retain the existing Cashly assets.
- Users upload/remove their own photo using authenticated-principal endpoints. Administrative photo reads require USERS:READ and modifications require USERS:UPDATE. An ordinary user cannot alter another user's photo without those grants, and a failed photo upload during user creation does not produce a duplicate user on retry.
- Photo metadata is included in user, profile and login responses. Navbar/profile/list/details use authenticated blobs, update after successful self edits, survive reload and fall back to initials when appropriate.
- Supported images are bounded PNG/JPEG rasters normalized to PNG. Active content, corrupt and oversized images are rejected. Old media survives failed replacement; newly stored media is cleaned on rollback. Catalog routes cannot bypass identity-media permissions.
- All screens reuse the existing form, file-input, card, border, radius and shadow components.

Implementation and evidence: `openspec/changes/archive/2026-11-01-company-settings-user-photos/`.
