# Startup and CI verification implementation

Assigned work unit: VERIFY-05 startup/HTTP/security and U27 isolated CI parity. Local implementation only; no commit or pull request is created by this work unit. The user authorized implementation of the prepared change.

- Added `bootstrap/BackendStartupMySqlIT`: full `@SpringBootTest` on a random HTTP port, real Flyway/MySQL, BCrypt synthetic users and login-generated JWTs. Exercises a safe existing GET for each of the 21 business modules, anonymous denial for each route, missing-permission denial for 20 routes, and a missing-warehouse error. Notifications intentionally require authentication rather than a module permission.
- The initializer rejects every datasource URL except `jdbc:mysql://suite-db:3306/screaming_suite` before context refresh. No development credentials, users or database are used.
- The isolated harness copies sources into the runner using Docker cp, supporting both a local Docker daemon and GitLab DinD without host bind paths or Docker socket mounts. Test databases remain project-scoped tmpfs MySQL services.
- Both GitHub workflows and GitLab test job invoke the same clean verify harness, including every inventoried IT. Existing image/build/package/deploy jobs remain. Reports upload before project-only cleanup.
- Final-mode harness exports sanitized discovery/test/coverage summaries and runs the baseline comparison inside a separate Python analyzer container, preserving an original Maven failure exit status.
- Static checks passed: Docker Compose config, Bash syntax and whitespace checks. Remote GitHub/GitLab execution has not been performed locally and is not claimed as passing.

Runtime startup/HTTP/security equivalence passed: baseline and migrated applications each executed the same 63 cases with zero failures, errors or skips. Baseline used git archive HEAD plus the identical new IT; all 1,248 baseline production source SHA-256 values matched the frozen source inventory. Sanitized evidence is in `evidence/startup-baseline.json`; migrated startup results are included in the full-suite reports. The baseline startup project and its volumes were removed after report export. The full migrated suite remains owned by the root executor and its unrelated failures must be resolved separately.
