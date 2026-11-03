# Authorization requirements

## No list access
Given an authenticated user lacks `VIEW_LIST` for a business module, when navigating normally or directly to a list URL, then that module is absent from navigation and the route and API list are denied.

## No detail access
Given an authenticated user lacks `VIEW_DETAIL` for a business module, when opening a detail URL or requesting a record by id, then the route and API detail request are denied even if `VIEW_LIST` is granted.

## Exact grants
Given the required owner permission code and all cross-module codes, when invoking a permitted operation, then it is available. Neither an administrator label nor a stale legacy grant bypasses effective permissions. Each endpoint declares its exact `MODULE_ACTION` code.

## Mutations
Given only view codes, when attempting `CREATE`, `EDIT`, `DELETE`, `CONFIRM`, `CANCEL` or another mutation, then the operation is denied.

## Reports and exports
Given a report request, then the owning module report code is required. Invoice, quote, order and purchase PDFs use `DOWNLOAD_PDF` for a single document and `REPORT_PDF` for the listing report. The commercial catalog uses `CATALOG_PRODUCTS_COMMERCIAL_CATALOG_PDF`.

## Changed access
Given an action menu was opened before a grant changed, when invoking an obsolete action, then current permissions prevent execution. Revoked permissions disappear when authoritative access refreshes.

## Account access
Given no business permission codes, then own profile/account settings remain available without exposing user management or modifying permissions.
