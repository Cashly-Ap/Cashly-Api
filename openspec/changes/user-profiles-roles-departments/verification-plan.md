# Verification plan

Status: API compile and unit suite verified locally; database integration and frontend builds remain unverified. This change is not ready to archive.

## Verification evidence

- API production sources compile with Java 24: `./mvnw -o -DskipTests compile` — passed.
- API unit suite: `./mvnw -o '-DargLine=-javaagent:<cached-mockito-core-5.17.0.jar>' '-Dtest=*Test,!CashRegisterSessionReportGeneratorTest' test` — 3,209 tests passed, 0 failures, 0 errors. The cached Mockito agent is needed because this macOS/JDK 24 environment cannot dynamically attach it. The existing `CashRegisterSessionReportGeneratorTest` aborts the JVM when run here, so it was excluded.
- Authorization coverage inventory: 181 HTTP operations mapped; 0 unmapped protected operations and 0 legacy fixed-role/module policies.
- `git diff --check` passes in the API and app repositories.
- Seed reset and integrity scripts received static checks and an offline SQLite simulation; they have not run against MySQL.
- Angular build and tests could not run: project `node_modules` is empty and offline installation lacks cached `@angular/cdk`.

## Automated acceptance

| Area | Required evidence |
|---|---|
| Profile | Required names, Unicode trimming, address limits, normalized single phone, round-trip persistence |
| Assignments | Required active references, atomic rejection of stale selections, role delegation ceiling |
| Role management | Permission validation, unique names, optimistic conflicts, protected system role |
| Department management | Unique names, active selections, referenced deletion/deactivation rejected |
| Authorization | Every protected controller mapping has an explicit policy or documented self-service exception |
| Every module | Parameterized no-access, READ-only and selected-action cases; direct API denial without side effects |
| Session updates | Previously issued access and refresh tokens cannot preserve revoked grants |
| Continuity | Concurrent changes cannot remove the last administrator capable of authenticating |
| UI | Create/edit routes and action controls follow effective permissions; custom names and departments appear correctly |
| Seeds | Fresh load and clean repeat reset, no orphans, consistent stock/documents/payments/balances |

## Manual integrated flow

1. Start an explicitly disposable beta database with schema and the documented seed loader.
2. Log in with the seeded system administrator and open Users, Roles and Departments.
3. Create a department and a custom role; configure different permission combinations on several modules.
4. Create a user with complete profile data and select the new role and department.
5. Log in as that user in a separate session. Check visible navigation, direct URLs, read access and allowed/denied writes.
6. Change the role permissions as the administrator. Retry an operation with the user's existing token; the next request must use the new permissions.
7. Verify custom role/dept names and profile fields in user list/detail/edit. Reject invalid phone numbers and unavailable references.
8. Verify deletion/deactivation conflicts for referenced roles/departments and last-administrator protection.
9. Inspect seeded business documents, stock, balances, payments and returns; execute the seed integrity checks.

## Environment limitation

The Docker socket rejects access in this session, so MySQL/Testcontainers integration, application startup, browser flows and an actual seed reset have not run. The frontend dependencies are unavailable offline. Do not treat API unit tests or the SQLite seed simulation as a substitute for those integration checks.
