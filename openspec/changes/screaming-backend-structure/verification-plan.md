# Verification plan

## Status and scope

This document plans verification of a structural refactor across **all backend modules and tests**. The production refactor and the isolated test harness have not been implemented. Only the architecture baseline described in `verify-report.md` has run. No full-suite, integration, startup, migration or coverage result is asserted.

The inventory at preparation time contains:

| Scope | Files |
|---|---:|
| Production Java (`src/main/java`) | 1,248 |
| All files under `src/test` | 584 |
| Test Java | 573 |
| Ordinary classes containing test annotations | 559 |
| Explicit `*IT.java` classes | 7 |
| Java helpers/configuration | 6 |
| Empty test class | 1 |
| Immutable `.java.bak` source backups | 9 |
| Test resources | 2 |

There are 3,106 declared test annotations in Java sources. This is **not** the number of JUnit executions: parameterized and nested tests affect that number. Compare actual JUnit reports rather than claiming an annotation count as passed tests. Historical reports, including the earlier 3,143-test figure, are historical evidence only and must be reconciled with a fresh baseline.

Inventory and map every source, helper, resource and backup individually. Every entry must have exactly one destination or a documented keep-in-place decision. Preserve immutable backups byte-for-byte; never silently delete, compile, rewrite or omit them. Reconcile any concurrent source changes before applying the migration.

## Current discovery and runtime constraints

- Surefire 3.5.3 does not discover `*IT` by default. There is no Failsafe configuration. Neither existing `test` nor `verify` alone proves that integration tests ran.
- The POM excludes `StockReservationIntegrationTest.java`, which does not currently exist. Record and reconcile that obsolete exclusion; do not report a nonexistent test as passed. Any real exclusion must have an explicit alternative execution path.
- Four warehouse concurrency IT share `InvoiceInventoryConcurrencyIT.PersistenceConfig`. Its `hibernate.hbm2ddl.auto=create-drop` **requires a separate database** from tests using Flyway and the complete application schema.
- Warehouse IT accept `inventory.test.url`, `inventory.test.username` and `inventory.test.password` system properties. Without an external URL they attempt Testcontainers.
- `ReturnsFinancialRollbackMySqlIT` requires `RETURNS_MYSQL_URL`, `RETURNS_MYSQL_USERNAME`, `RETURNS_MYSQL_PASSWORD`, or the corresponding `returns.mysql.*` system properties. The ordinary Spring datasource environment variables are insufficient for that test.
- Orders and Products IT use the `test` profile with real MySQL, Flyway and database replacement disabled. Returns also validates the real schema.
- `application-test.properties` defaults to MySQL at localhost:3307. Override it explicitly with the isolated database. `application-web-test.properties` uses H2/create-drop with Flyway disabled; it cannot substitute for MySQL verification.
- `CashlyApplicationTests` is empty. It is not application-startup evidence. Add a meaningful full-context startup check during implementation.
- `TestcontainersConfiguration` is profile-gated by `testcontainers`; `TestJpaConfig` scans the root package. Preserve the intended test configuration discovery.
- Existing CI invokes Maven on the host without provisioning the complete MySQL verification environment or selecting all `*IT`. Update both workflows to reproduce the complete isolated verification and publish reports even when a gate fails.

### Explicit integration test routing

| Class | Runtime and required verification |
|---|---|
| `ProductPersistenceQueriesMySqlIT` | Suite MySQL; Flyway and real product/catalog/usage queries |
| `OrderHistoricalCategoryMySqlIT` | Suite MySQL; historical/current categories, query results and migration resource |
| `ReturnsFinancialRollbackMySqlIT` | Suite MySQL; explicit RETURNS properties; all injected failure stages and financial rollback |
| `InvoiceInventoryConcurrencyIT` | Separate inventory MySQL; explicit inventory properties; reservation concurrency |
| `ManualStockConcurrencyIT` | Separate inventory MySQL; explicit inventory properties; manual stock concurrency |
| `StockDeductionConcurrencyIT` | Separate inventory MySQL; explicit inventory properties; deduction concurrency |
| `WarehouseStockConcurrencyIT` | Separate inventory MySQL; explicit inventory properties; stock and transfer concurrency |

Run tests sharing mutable fixtures sequentially. Concurrency tests themselves own their worker threads; do not introduce suite-level parallelism over the same tables.

## Package-sensitive checks

1. Preserve `LayerDependencyTest` behavior while adding the chosen submodule boundaries. It currently identifies a module by the first package segment, so moving packages alone does not prove submodule isolation.
2. Adapt `ApplicationPackageConventionTest` to the selected structure. It currently requires classified persistence packages, directed adapters and interface-only Spring Data repository packages. Do not remove assertions simply to achieve a green run.
3. Update the literal path in `CustomerWarehouseBoundaryTest` from `customers/application` to the actual submodule roots. Assert that scanners discover sources; an empty search must not pass vacuously.
4. Update `DashboardArchitectureTest` package checks without losing its boundary guarantees.
5. Preserve the literal migration-resource reference in `OrderHistoricalCategoryMySqlIT`: `src/main/resources/db/migration/V25__create_sales_line_category_snapshots.sql`. Moving Java does not authorize rewriting migration history.
6. Recheck `@Import`, `@ContextConfiguration`, `@EntityScan`, `@ComponentScan`, `@EnableJpaRepositories`, mock/spy bindings, qualifiers and bean uniqueness. In particular, warehouse concurrency configuration scans entities by `WarehouseStockEntity.class.getPackageName()` and repositories by `basePackageClasses`; a changed package can silently alter the scan set.
7. Preserve production package-private access and reflective class loading. Test-only shared fixture access adjustments, including CustomerTestHelper, are explicit in design.md; retain fixture values and assertions. No production `sealed`/`permits` declarations were found during inspection; clean compilation must still detect any hierarchy/package restriction introduced or discovered later.
8. Check logging/configuration package strings, root application discovery, test resource paths, endpoint serialization, exception handlers and security rules after each slice.

## Isolated Docker harness to implement

**Pending:** create `compose.screaming-test.yaml` in the backend root during implementation. It must be independent of the development Compose configuration and must not extend its services.

Use two MySQL databases: one for Flyway/full-schema tests, one for inventory `create-drop`. Use project-scoped networks/volumes, no fixed `name` or `container_name`, no host database ports, no development volume/network, and a dedicated test runner. Mount source pieces read-only into a writable runner workspace. Keep `target` an ordinary directory inside the container, not a volume mountpoint that Maven `clean` cannot remove. Export reports before removing the runner. Disable Spring's automatic Compose lifecycle. The explicit external database route avoids Docker socket access/Testcontainers nesting.

The following is a **proposed file**, not a file already created or validated:

```yaml
services:
  suite-db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: screaming_suite
      MYSQL_USER: screaming_test
      MYSQL_PASSWORD: screaming_test
      MYSQL_ROOT_PASSWORD: screaming_root
    tmpfs: [/var/lib/mysql]
    healthcheck:
      test: [CMD-SHELL, 'MYSQL_PWD="$$MYSQL_ROOT_PASSWORD" mysqladmin ping --silent -h localhost -u root']
      interval: 5s
      timeout: 5s
      retries: 30
      start_period: 30s
  inventory-db:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: screaming_inventory
      MYSQL_USER: screaming_test
      MYSQL_PASSWORD: screaming_test
      MYSQL_ROOT_PASSWORD: screaming_root
    tmpfs: [/var/lib/mysql]
    healthcheck:
      test: [CMD-SHELL, 'MYSQL_PWD="$$MYSQL_ROOT_PASSWORD" mysqladmin ping --silent -h localhost -u root']
      interval: 5s
      timeout: 5s
      retries: 30
      start_period: 30s
  runner:
    image: maven:3.9.9-amazoncorretto-24
    working_dir: /workspace
    entrypoint: [sleep, infinity]
    volumes:
      - ./src:/workspace/src:ro
      - ./pom.xml:/workspace/pom.xml:ro
      - ./.mvn:/workspace/.mvn:ro
      - ./mvnw:/workspace/mvnw:ro
      - maven_cache:/root/.m2
    environment:
      SPRING_PROFILES_ACTIVE: test
      SPRING_DOCKER_COMPOSE_ENABLED: 'false'
      SPRING_DATASOURCE_URL: jdbc:mysql://suite-db:3306/screaming_suite
      SPRING_DATASOURCE_USERNAME: screaming_test
      SPRING_DATASOURCE_PASSWORD: screaming_test
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver
      RETURNS_MYSQL_URL: jdbc:mysql://suite-db:3306/screaming_suite
      RETURNS_MYSQL_USERNAME: screaming_test
      RETURNS_MYSQL_PASSWORD: screaming_test
    depends_on:
      suite-db: {condition: service_healthy}
      inventory-db: {condition: service_healthy}
volumes:
  maven_cache:
```

These credentials are synthetic test values. Relative source mounts resolve against the file in `cashly-api`. Use a unique Compose project for the run and validate its resolved configuration before executing it.

### Planned commands

Run from `cashly-api` only **after** implementing and reviewing the dedicated harness:

```sh
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml config --quiet
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml up -d --wait suite-db inventory-db runner
```

Capture a complete baseline before migration, then repeat the same clean command after migration. The explicit selector includes ordinary tests and all IT:

```sh
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml exec -T runner ./mvnw -B clean verify '-Dtest=Test*,*Test,*Tests,*TestCase,*IT' -DfailIfNoTests=true -Dinventory.test.url=jdbc:mysql://inventory-db:3306/screaming_inventory -Dinventory.test.username=screaming_test -Dinventory.test.password=screaming_test
```

If a focused integration rerun is necessary, export the full-run evidence first:

```sh
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml exec -T runner ./mvnw -B test -Dtest=ProductPersistenceQueriesMySqlIT,OrderHistoricalCategoryMySqlIT,ReturnsFinancialRollbackMySqlIT,InvoiceInventoryConcurrencyIT,ManualStockConcurrencyIT,StockDeductionConcurrencyIT,WarehouseStockConcurrencyIT -DfailIfNoTests=true -Dinventory.test.url=jdbc:mysql://inventory-db:3306/screaming_inventory -Dinventory.test.username=screaming_test -Dinventory.test.password=screaming_test
```

Create distinct baseline/final output directories before export, and export before a subsequent `clean` or teardown. Original reports can contain environment properties; retain originals in restricted local/CI artifacts and publish sanitized summaries in the change documentation.

```sh
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml cp runner:/workspace/target/surefire-reports ./output/screaming-verification/final/surefire-reports
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml cp runner:/workspace/target/site/jacoco ./output/screaming-verification/final/jacoco
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml cp runner:/workspace/target/jacoco.exec ./output/screaming-verification/final/jacoco.exec
```

Only after evidence export, remove resources belonging to the unique verification project:

```sh
docker compose -p cashly-screaming-20261016 -f compose.screaming-test.yaml down --volumes --remove-orphans
```

Do not run `run-tests.sh`: its cleanup executes `docker-compose --profile test down -v` without dedicated project isolation. Do not run `clean-test-db.sh` on shared resources. `compose.returns-it.yaml` has globally fixed container/network/volume names, so merely adding `-p` does not fully isolate it. Do not run any full suite in the existing development app/database.

## Acceptance matrix

| Gate | Required evidence |
|---|---|
| Inventory integrity | Complete before/after map of production, tests, resources and immutable backups; unique destinations, no silent loss or collisions; backup hashes unchanged |
| Clean build | Java 24 clean main/test compilation and packaged application, without stale classes from former packages |
| Architecture | Existing invariants retained, selected module/submodule conventions enforced; nonempty scanning and negative fixtures proving violations are detected |
| Domain | Preserved validations, state transitions, quantity/money arithmetic, snapshots and business constraints across every module |
| Application | Use-case/query successes and meaningful errors; preserve customer registration plus initial warehouse grants and document workflows |
| HTTP/security | Endpoint paths, methods, status/validation/error behavior, JSON, pagination/order and permissions unchanged |
| Full wiring | Meaningful context startup; no missing/duplicate beans, repositories or entities; isolated MySQL/Flyway and local test storage |
| MySQL persistence | Products and Orders IT with real schema/query behavior; migrations preserved and schema validation succeeds |
| Stock concurrency | All four warehouse IT execute; reservation/deduction/manual stock/transfer behavior and lock semantics remain correct |
| Financial rollback | Returns IT runs every failure-stage variant and leaves no partial cash, balance, stock or invoice changes |
| Cross-module workflows | Orders/invoices/purchases/returns inventory operations; payments/balance/credit; reports/dashboard composition; relevant transactional failures |
| Files/notifications | Existing PDF/Excel, storage adapters, WebSocket resource messages and authorization behavior preserved; targeted smoke checks where current tests do not prove it |
| Discovery completeness | Reconcile every executable inventory class against actual JUnit XML; 559 ordinary classes + 7 IT at current inventory; no new disabled/skipped tests or silent subset success |
| Coverage | JaCoCo line coverage: domain **>90%**, application **>80%**, infrastructure **>60%**; no regression by layer/module against baseline using equivalent classes/exclusions |
| CI | Both workflows reproduce the isolated complete gates and publish sanitized summaries plus appropriately restricted full reports, coverage and inventories even on failure |
| Diagnostics | Record known baseline warnings; investigate new warnings/errors attributable to the change. Do not impose a zero-existing-warning gate |

JaCoCo currently prepares its agent and produces a report at `verify`; it does not enforce a `check` threshold. Implement auditable aggregation by the new package layers and compare equivalent classes before/after (aggregate XML counters rather than average percentages). If baseline coverage is below a required threshold, report the debt and add meaningful tests to meet the acceptance gate. Never label an unknown or below-threshold metric as passed.

Every run must record source revision/tree identity, exact command, profile, versions, timestamps, actual JUnit totals, exclusions, failures and skips. Classify baseline failures before attributing them to the migration. A historical green report cannot replace the new baseline. Overview composition tests are required only for existing behavior or separately approved functionality; the structural change does not itself authorize new endpoints.
