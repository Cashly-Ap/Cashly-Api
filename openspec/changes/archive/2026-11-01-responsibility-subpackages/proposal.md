# Responsibility subpackages

## Why
Capability-oriented modules still mix different responsibilities as loose classes in operation and domain containers. For example orders/application/create contains a request, a use case and a from_quote subfolder; domain roots mix entities and repository contracts.

## What changes
Move Java production and test files into explicit responsibility packages across all backend modules. Keep capability/submodule ownership, with operation folders grouping use_case/request/response or source-specific branches. Separate domain entity and repository packages. Retain meaningful existing infrastructure leaf packages and organize any remaining loose infrastructure classes by responsibility.

## Scope
Pure package/file relocation, imports and references, architectural path checks, documentation and verification tooling. Preserve names, behavior, public HTTP contracts, SQL/Flyway history, transactional issuance and all existing uncommitted fixes. The Angular application does not use these Java packages and is outside this structural change.

## Verification and rollback
Capture a complete before/after relocation manifest and source audit. Run a structural guard and a clean isolated full Java/MySQL suite; compare coverage using exact class relocation identities against the verified 3253-test baseline. Reversing the manifest restores prior package locations without undoing unrelated working-tree changes.
