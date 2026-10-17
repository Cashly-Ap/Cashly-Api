# Align backend and frontend ERP business areas

## Why
Backend and Angular features are individually modular but flat at the business-module level. The user approved shared ERP areas, explicitly placing invoice returns under finance/billing in both projects.

## What changes
Group all backend modules and their Java tests under sales, procurement, catalog, inventory, finance/billing, finance/treasury, partners, analytics and platform. Apply equivalent Angular feature ownership, retaining role-specific internals and colocated tests. Move Angular report implementations to their owner/reporting capability; keep existing report URLs through thin routing composition.

## Scope
Relocate source packages/files, imports, lazy route references, source-path architecture checks and current documentation. Preserve every existing HTTP/browser URL, permission identifier, data schema, transactional boundary and business behavior. Shared technical utilities and global bootstrap infrastructure retain explicit technical ownership. No microservices, new domain events or database migrations.

## Verification
Retain full relocation/source audits, execute clean backend MySQL regressions against an isolated database, reconcile each relocated instrumented class against the verified 3258-test baseline, execute the full frontend suite and production build, verify lazy routes and read-only browser navigation. No test execution may mutate the development business database.
