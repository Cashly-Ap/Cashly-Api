# Cashly API

Cashly API is the backend for the Cashly business management platform. It exposes the HTTP contracts used by the back-office application and owns transactional data, domain rules, persistence, authentication and reporting.

## Business modules

| Area | Source root | Responsibility |
| --- | --- | --- |
| Platform auth | `platform/auth` | Authentication, JWT, MFA, users, roles, departments and authorization |
| Platform company | `platform/company` | Installation company settings, logos and PDF branding |
| Platform notifications | `platform/notifications` | User notifications and WebSocket delivery |
| Partners | `partners/customers`, `partners/suppliers` | Customers, balances, credit, warehouse access and suppliers |
| Catalog | `catalog/products`, `catalog/categories`, `catalog/price_types` | Products, categories, price types and product reporting |
| Inventory | `inventory/warehouses` | Warehouses, stock, reservations, movements and inventory reporting |
| Sales | `sales/quotes`, `sales/orders` | Quotes and sales orders |
| Procurement | `procurement/purchases` | Purchases and purchase reporting |
| Finance billing | `finance/billing` | Invoices, returns, credit notes and debit notes |
| Finance treasury | `finance/treasury` | Cash register, payments, receivables and payment methods |
| Analytics | `analytics/dashboard` | Dashboard indicators |

The module index is in [docs/modules/README.md](docs/modules/README.md).

## Architecture

The backend follows Hexagonal Architecture with DDD boundaries:

- `domain`: entities, value objects, domain services and repository ports.
- `application`: use cases, DTOs, mappers, validation and application ports.
- `infrastructure`: REST controllers, JPA adapters, security, storage and integrations.
- `shared`: cross-cutting types used by more than one bounded context.

The application is a single Spring Boot deployment and a single MySQL database. Package boundaries do not imply separate services.

## Runtime stack

| Concern | Technology |
| --- | --- |
| Language and framework | Java 24, Spring Boot 3.5.5 |
| Build | Maven Wrapper (`mvnw`) |
| Database | MySQL 8.0 with Flyway |
| Security | Spring Security and JWT |
| File storage | S3-compatible storage through MinIO locally |
| Testing | JUnit 5, Spring tests and Testcontainers |
| Runtime | Docker Compose |

## Local runtime

Prerequisites: Docker with Compose and Git.

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`:

```bash
curl http://localhost:8080/actuator/health
```

Other profiles:

```bash
docker compose --profile prod up --build
docker compose --profile test up --build
```

Configuration, storage and seeding are documented in [docs/SETUP.md](docs/SETUP.md) and [docs/STORAGE.md](docs/STORAGE.md).

## Database and migrations

- Migrations live in `src/main/resources/db/migration`. The current baseline ends at **V37**.
- Migrations create the final schema for a fresh database.
- Seed data is implemented as 29 ordered Java steps in `infrastructure/seed/steps`, orchestrated by `DatabaseSeedRunner`.
- Set `CASHLY_SEED_ENABLED=true` to run the seed after startup. The default is `false`.

To recreate a disposable database from scratch:

```bash
docker compose stop app
docker exec cashly-api-mysql mysql -uroot -pverysecret \
  -e "DROP DATABASE mydatabase; CREATE DATABASE mydatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
docker compose start app
```

## Permissions

Authorization uses a single permission code per operation:

```text
MODULE_ACTION
```

Examples:

```text
CATALOG_PRODUCTS_DELETE
FINANCE_TREASURY_RECEIVABLES_PAY_DIRECT
COMPANY_EDIT
```

- `PermissionRegistry` declares **131** supported codes.
- `PermissionCatalog` exposes them grouped by area, submodule and module.
- `GET /api/auth/access` returns the current user's `permissions: string[]`.
- Roles store permission codes in `auth_role_permissions(role_id, module, action)`.
- The JWT contains identity claims only; grants are resolved from the database on every request.
- Per-user module grants and the `modulePermissions` API field are retired.

See [docs/modules/ACCESS_CONTROL.md](docs/modules/ACCESS_CONTROL.md) for the full contract.

## Verification

Backend tests run inside the API container:

```bash
docker compose exec app ./mvnw test
```

Specific tests:

```bash
docker compose exec app ./mvnw -Dtest=PermissionRegistryTest test
```

The isolated Docker verification script used by CI is:

```bash
bash scripts/run-structure-verification.sh final
```

Main technical references:

- [Architecture](docs/ARCHITECTURE.md)
- [Setup and configuration](docs/SETUP.md)
- [API contracts](docs/API.md)
- [Business rules](docs/BUSINESS_RULES.md)
- [Workflows](docs/WORKFLOWS.md)
- [Testing](docs/TESTING.md)

Authenticator MFA is available from **Perfil → Seguridad**. Set `MFA_ENCRYPTION_KEY` before enrolling users; see [Authenticator MFA setup](docs/SETUP.md#authenticator-mfa).
