# Design: Capability-first backend packages

## Technical approach

Reorganize all backend production and test sources using the exhaustive move manifests. Preserve Java type names, method signatures, SQL, HTTP contracts and business behavior. A capability is an organizational package inside the modular monolith, not a separate deployable service.

See [module-map.md](module-map.md) for ownership, [source-moves.json](source-moves.json), [test-moves.json](test-moves.json), and [resource-inventory.json](resource-inventory.json) for every existing file; [verification-plan.md](verification-plan.md) defines acceptance execution.

## Architecture decisions

| Decision | Choice and rationale |
|---|---|
| Business packages first | Warehouses: management/stock. Customers: profile/balance/credit/warehouse_access. Payments: collection/credit. Cash register: sessions/reporting. Preserve Auth and Reports' existing capability packages. |
| Avoid artificial domains | Coherent single-aggregate modules keep domain/application/infrastructure directly; organize application by operation. Lines, allocations, contacts and configured commercial terms stay with their aggregate. Read capabilities need no empty domain. |
| Short infrastructure paths | Use infrastructure/web, security, integration, persistence and concrete output roles. Retain persistence entities/repositories/adapters/queries/mappers/projections and directional application ports/in and ports/out. |
| Explicit supersession | This change supersedes only the infrastructure wrapper-path convention in active application-package-naming. Its applied behavior and directional port/persistence rules remain. Do not edit/archive that change or historical specs during preparation. |
| Composition stays explicit | Preserve mixed root configs and the mixed cash-register query/adapter as documented exceptions. Internal sibling orchestration may use sibling repositories initially. No interface is invented solely for a directory boundary. |
| Existing integrations remain accountable | Preserve named baseline foreign-internal accesses; compare canonical type dependency edges and prohibit new ones. The modular-architecture ownership requirement is not waived or declared fully satisfied; existing debt remains separately tracked. |
| Combined views | Reserve module/overview/application and infrastructure/web for future combined reads; do not create six unused classes or new endpoints in a package-only migration. Owned writes retain their existing controller/use case. |

## Data flow

HTTP controller -> application operation -> aggregate and required ports -> persistence/integration adapter. Existing synchronous cross-module calls, required transaction participation, stock lock ordering and rollback behavior stay intact. Customer registration still includes access grants; cash-linked balance mutations and document/inventory effects remain atomic where currently specified. No asynchronous events or new transaction propagation is introduced.

## File changes

| Area | Planned change |
|---|---|
| All src/main/java classes | Move or explicitly retain each manifest entry; update packages, imports and fully qualified references throughout consumers. |
| All src/test files | Relocate peer tests/helpers/backups; preserve test resources and cases. Keep same-package helper access and distinct boundary suites. |
| Architecture tests | Enforce the new paths with positive, negative and nonempty-scan checks; preserve layer and top-level ownership checks. |
| Spring/tests | Update scans, imports, context configurations, explicit type strings, qualifiers and nested-type references. |
| Build and CI | Add an isolated complete-suite harness, explicit IT discovery, coverage comparison and equivalent CI reporting. Existing warnings are compared, not treated as new failures. |
| Resources/docs | Preserve migrations/resources and checksums. Update current guides; leave archived artifacts untouched. |

Wildcard imports and formerly same-package references require symbol-aware rewriting or explicit expansion. For example, DeleteReturnUseCase references GetReturnByIdUseCase.ReturnNotFoundException without an import today. Duplicate InvoiceEditingBoundaryTest and Auth request-test names must keep distinct destinations. Production visibility and assertions remain unchanged. CustomerTestHelper moves to customers.testing; its class and shared factory methods may become public in test scope only, since its currently package-private callers move across capability packages. Preserve fixture values and callers; do not expose a production helper.

## Testing strategy

Run a complete clean baseline before migration and the identical isolated suite after it. Include ordinary tests and all seven current IT classes, use separate MySQL databases for Flyway and create-drop inventory fixtures, and verify actual startup plus existing HTTP/security contracts. Enforce discovered-suite reconciliation, no hidden skips, required layer coverage and no per-module/layer regression. Preserve raw/sanitized evidence per run. Eleven architecture tests passed during preparation; full-suite correctness remains pending.

## Migration and rollback

Perform compileable work units from [work-units.md](work-units.md). Each includes moved types, every consumer reference, tests and its verification. Start with foundation/guards, then coherent module slices; dependency cycles require atomic consumer updates rather than assuming a topological order. Export evidence before clean or isolated teardown. Revert a failed slice and its consumer changes together; no data migration is required. Do not run the destructive development test cleanup script.

## Open questions

User subsequently authorized local implementation. No branches, commits, PRs or deployments were requested. Review-chain selection is deferred until PR creation; local verification follows the work-unit boundaries. Existing foreign-internal accesses are tracked in dependency-debt.json, owned by consuming/provider modules and gated before the next behavior change in each affected integration.
