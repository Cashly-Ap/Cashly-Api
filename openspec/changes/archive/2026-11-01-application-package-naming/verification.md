# Hexagonal Package Naming Verification

## Scope

Application input ports use `application/ports/in`; output ports use `application/ports/out`. Port-specific models remain below the corresponding direction. Infrastructure adapters use `infrastructure/adapters/in` and `infrastructure/adapters/out`. HTTP input adapters use `controllers`. Persistence sources use responsibility packages: `entities`, `repositories` (Spring Data interfaces), `adapters` (repository-port implementations), `queries`, `mappers`, and `projections`. The cash register module package is `cash_register`; class names and HTTP routes remain unchanged. Domain repository interfaces remain in the domain. Query implementations and input DTOs retain their established packages.

## Red and Green Evidence

- The original direction guards were executed before the production moves: two tests failed against undirected packages. Before the subsequent controller/persistence moves, the five-test naming suite failed three checks: legacy `web`, unclassified persistence sources, and concrete repository implementations under `repositories`. Red reports are preserved at `target/hexagonal-verification/red-persistence`.
- Before moving the cash register module, its naming guard failed: six naming tests, one failure, zero errors. Red reports are preserved at `target/hexagonal-verification/red-cash-register`.
- The final source snapshot compiled 1,248 production sources and 573 test sources inside Docker.
- `mvn -B clean verify` completed successfully on 2026-10-13 in 68 seconds: 3,143 tests, zero failures, zero errors, and zero skipped tests.
- All six `ApplicationPackageConventionTest` checks and all five `LayerDependencyTest` checks passed.
- Maven built and repackaged the application JAR and generated the JaCoCo report.

## Coverage

JaCoCo line coverage aggregated across package paths containing each layer:

| Layer | Covered lines | Total lines | Coverage | Required threshold |
| --- | ---: | ---: | ---: | ---: |
| Domain | 2,939 | 3,123 | 94.11% | >90% |
| Application | 4,479 | 4,981 | 89.92% | >80% |
| Infrastructure | 5,853 | 8,115 | 72.13% | >60% |

The authoritative final reports are in ignored build output at `target/hexagonal-verification/cash-register/surefire-reports` and `target/hexagonal-verification/cash-register/jacoco`. A fresh destination avoids stale reports from package names that changed during the refactor.

## Runtime Checks

The primary agent rebuilt the API from the final production sources and verified:

- Health reports `UP` on port 8080.
- Login succeeds with HTTP 200.
- Cash register active and history reads return HTTP 200; anonymous history access returns HTTP 401.
- Authenticated customer, product, invoice, order, and warehouse reads return HTTP 200.
- An unknown customer returns HTTP 404.
- An unauthenticated customer read returns HTTP 401.
- Login through the frontend proxy on port 4000 returns HTTP 200 after restarting the frontend to refresh the backend container address.

## Execution and Limitations

Tests ran with Maven 3.9.9 and Java 24 inside Docker, against an isolated MySQL test service. Production seeded data was not used. The final source and test snapshot was copied into a container-local workspace to avoid slow Windows bind mounts.

The successful verification covers the default Surefire suite. The configured `StockReservationIntegrationTest` exclusion remains in effect; explicitly named `*IT` integration suites are not selected by default `verify`. No frontend code changed, so no frontend coverage run was required.

Existing deprecation, unchecked-operation, Commons Logging, native-access, Unsafe, and JDK agent warnings remain. They did not fail the build.
Temporary verification containers and the isolated network were removed after exporting reports. The running Cashly API, frontend, MySQL and MinIO remained healthy.
