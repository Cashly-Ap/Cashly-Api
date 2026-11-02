# Design

Use the existing authoritative access endpoint, persisted grant resolution and Spring method authorization. Do not derive access from legacy user role labels. READ remains a prerequisite for all actions. Frontend controls improve usability; API enforcement is the trust boundary.

Route checks must include ancestor module requirements and the selected action. Menus require explicit owner grants for reports, prune empty groups and retain account settings. Shared actions recheck current grants before execution, including popovers opened before a grant change. Permission changes refresh existing access state on denied API requests via the existing interceptor.

Testing covers no access, read-only access, write grants, cross-module exports, direct URLs and current permission changes. Verification runs inside existing Docker development containers. Browser checks use the existing read-only demo account, without altering grants or account details.
