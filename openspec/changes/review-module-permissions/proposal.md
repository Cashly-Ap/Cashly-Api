# Review and fix module permissions

## Intent
Make the permission model correct and predictable: every module must expose the actions it actually needs, every gate must evaluate the module that owns the action, and every role must grant exactly what it is meant to grant.

## Scope
Audit the permission catalog (`auth_role_permissions`), the Angular `PermissionDirective` resolution rules, the API authorization checks and the seeded role grants. Fix gaps such as actions that exist in the UI but not in the catalog (for example `DASHBOARD:EXECUTE` did not exist while a dialog evaluated it), and actions that are gated with the current page module instead of the owning module.

## Rollback
Revert the catalog migration and the guard refinements. Role grants can be restored from the previous seed snapshot; no customer or document data is touched.
