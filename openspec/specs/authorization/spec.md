# Authorization requirements

## No read access
Given an authenticated user lacks READ for a business module, when navigating normally or directly to a list/details/form URL, then that module is absent from navigation and the route and API read are denied.

## Read-only access
Given READ alone, when viewing permitted data, then viewing succeeds and create/update/delete/execute/export actions remain unavailable and direct API mutations fail.

## Exact grants
Given the required owner action and all cross-module grants, when invoking a permitted operation, then it is available. Neither an administrator label nor a stale legacy grant bypasses effective permissions. Export requires owner EXPORT and document EXPORT.

## Changed access
Given an action menu was opened before a grant changed, when invoking an obsolete action, then current permissions prevent execution. A revoked module disappears when authoritative access refreshes.

## Account access
Given no business grants, then own profile/account settings remain available without exposing user management or modifying permissions.
