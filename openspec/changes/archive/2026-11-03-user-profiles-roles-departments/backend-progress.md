# Backend implementation progress

## Scope and delivery

Implemented the assigned foundation and policy work units locally. No commits, branches, pull requests, resets or deployments were performed. Delivery follows the resolved auto-chain work-unit split: (1) schema/profile contracts and (2) authoritative policy/management. Test execution and integration verification remain open; this document does not claim verification completion or change task checkboxes.

Implementation mode: standard. No strict-TDD configuration exists in this change. New tests were added, but a RED/GREEN cycle could not be executed in this environment.

## Implemented

- `V26__user_profiles_roles_departments.sql`: roles, permission triples, departments, singleton administration lock and mandatory user profile/reference columns. Existing migration checksums were preserved. Legacy non-administrator data receives a deny-by-default role until the disposable beta seed set is loaded.
- Domain profile validation: trimmed Unicode first/last names, bounded structured address, one optional normalized phone, invalid phone rejection.
- Atomic profile and UUID role/department assignment in user create/update. Assignments validate active references and the actor's delegable grants. User JSON rejects unknown fields, including legacy enum/direct-grant writes. The old direct-grant request/use case/endpoint was removed.
- Role/department CRUD and minimal active options; UUID role list filtering; version-checked updates, case-insensitive names and reference-safe deactivation/deletion. Role permissions persist atomically. System-administrator grants derive exclusively from the immutable database system key, never the display name.
- Registry of every existing module and its supported actions. Unsupported combinations and mutation grants without READ are rejected. IMPORT is reserved in the action enum but has no supported endpoint yet. DOCUMENT_ADMIN supports READ/EXPORT; DASHBOARD supports READ.
- Current access is loaded from the database on every authenticated request; token role/module claims confer no authority. Login and refresh infrastructure resolve active account/current role. `/api/auth/access` exposes current effective permissions.
- Every business controller has explicit module/action annotations. No runtime HTTP-method inference is used. Unknown application handlers deny through a coverage interceptor; authentication, own credentials/access and own notifications are narrowly listed exceptions.
- Reports require owner EXPORT and DOCUMENT_ADMIN EXPORT. Product photos require CATALOG READ and private/no-store caching. Public account registration is closed to unauthorized actors.
- Stock mutation dependencies require INVENTORY EXECUTE on sales order and invoice operations, quote conversions, purchase confirmation/cancellation and return stock restoration/reversal. Endpoints returning live stock additionally require INVENTORY READ (transaction/purchase pickers and stock-rich sales/purchase/invoice detail responses). Normal owner lists remain separately readable.
- Credential administration applies the actor ceiling to existing target roles, including inactive targets. Own password changes remain available without USERS administration and require the current password. All administration mutations take the singleton lock before authorization reads under READ_COMMITTED isolation, protecting the last active protected administrator against concurrent removal and stale assignment writes.
- Notification STOMP connections require current active access; incoming subscriptions revalidate access and may address only the authenticated user's topic. Client SEND operations are denied. Existing outbound subscription delivery is not forcibly disconnected on revocation; the next HTTP/inbound request is denied.
- Current actor lookup is an application port backed by an infrastructure SecurityContext adapter. Application conflicts/not-found exceptions are mapped to HTTP in infrastructure.

## New regression coverage authored

- `PermissionRegistryTest`: independent deny/read/supported actions for every registered module; unsupported actions and custom administrator-name non-bypass.
- `UserProfileTest`: Unicode normalization, bounds, phone presentation and normalized duplicate rejection.
- `AccessServiceTest`: every-module revocation, delegation ceiling, role deactivation, last-administrator lock ordering, protected inactive-target/credential safeguards and own-password access.
- `ManageRolesUseCaseTest`: protected role lifecycle, referenced-role conflicts, stale-version propagation and restricted options.
- `ManageDepartmentsUseCaseTest`: case-insensitive duplicates and referenced lifecycle conflicts.
- `NotificationChannelInterceptorTest`: inactive connection denial, other-user topic denial and subscription denial after revocation.

The root agent owns updates to legacy API fixtures, startup HTTP integration tests and exhaustive endpoint coverage inventory/tests. Those files must be included in the final verification run.

## Checks actually performed

- `git -C cashly-api diff --check`: passed.
- Lexical balanced-delimiter scan of the 87 auth Java sources at the time of the check: passed. This is not a compiler or test result.
- `docker compose -f compose.screaming-test.yaml ps`: blocked with `permission denied while trying to connect to the docker API at unix:///Users/gabrielvargas/.docker/run/docker.sock`.
- No sandbox escalation was attempted. Maven/Java were not run on the host because the backend workflow requires Docker.

## Pending verification and known boundaries

Run Docker compilation/unit tests, complete HTTP policy tests, MySQL version/lifecycle/concurrency checks, fresh startup, seed/reset integrity and repeated-reset/login smoke. No database was reset here. New tests have not been compiled or executed. Keep the OpenSpec verification tasks open until those checks produce evidence.

Legacy internal `UserRole`/direct-module persistence and read-only response fields remain temporarily for existing internal fixtures/session contract compatibility; they are not writable through user APIs and never authorize operations. Role and department management are grouped application operations rather than one class per CRUD action. No new tenancy was introduced; existing business ownership checks remain.
