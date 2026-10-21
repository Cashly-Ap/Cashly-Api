# Cashly API

Cashly API is the private backend for the Cashly business management platform. It exposes the business rules and HTTP contracts used by the back-office application. The repository centralizes transactional data, enforces domain invariants, manages persistence, and provides authenticated integrations for sales, inventory, billing, payments, purchasing, and reporting.

## Scope and modules

| Area | Responsibility |
| --- | --- |
| Authentication and users | JWT authentication, refresh tokens, roles, permissions, and user administration. |
| Customers and merchants | Customer records, contacts, merchants, and related commercial information. |
| Catalog | Categories, products, price types, and differential pricing. |
| Inventory | Warehouses, stock, transfers, and inventory availability. |
| Sales | Quotes, orders, invoices, dispatchers, returns, credit notes, and debit notes. |
| Payments and cash | Payment methods, payment registration, receivables, payables, collections, and cash-register sessions. |
| Purchasing | Suppliers and purchase workflows. |
| Reporting and dashboard | Operational indicators and report data for the applications. |
| Notifications and audit | User notifications and traceability of relevant operations. |

The complete module index is available in [docs/modules/README.md](docs/modules/README.md).

## Architecture

The code follows Clean/Hexagonal Architecture with domain-driven boundaries:

- `domain` contains aggregates, value objects, domain services, and repository ports.
- `application` coordinates use cases, input/output DTOs, validation, and application ports.
- `infrastructure` contains REST controllers, persistence adapters, security, configuration, storage, and external integrations.
- `shared` contains cross-cutting types used by more than one bounded context.

Each business module is organized under `src/main/java/com/cashly/cashly_api/` and should keep framework-specific code outside the domain layer.

## Runtime stack

| Concern | Technology |
| --- | --- |
| Language and framework | Java 24, Spring Boot 3.5.5 |
| Build | Maven Wrapper (`mvnw`) |
| Database | MySQL 8.0 with Flyway migrations |
| Security | Spring Security and JWT |
| File storage | S3-compatible storage through MinIO in local environments |
| Testing | JUnit 5, Spring tests, and Testcontainers |
| Runtime | Docker Compose |

## Local runtime

Prerequisites: Docker with Compose and Git. The default Compose service runs the development profile with MySQL and MinIO:

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`. Verify the service with:

```bash
curl http://localhost:8080/actuator/health
```

Production-image and test profiles are defined in `docker-compose.yaml`:

```bash
docker compose --profile prod up --build
docker compose --profile test up --build
```

Environment variables, database initialization, storage, and seed procedures are documented in [docs/SETUP.md](docs/SETUP.md) and [docs/STORAGE.md](docs/STORAGE.md).

## Database and migrations

MySQL schema is created by Flyway migrations under `src/main/resources/db/migration`; each migration creates final tables and indexes for a fresh development database. Development seed data lives under `db/seeds` and is loaded through `db/seed.sql` after API startup. This migration set is for fresh, non-production databases and is not compatible with databases that retain the previous Flyway history.

## Verification

Use the repository Docker workflow and the commands described in [docs/TESTING.md](docs/TESTING.md). The main technical references are:

- [Architecture](docs/ARCHITECTURE.md)
- [Setup and configuration](docs/SETUP.md)
- [API contracts](docs/API.md)
- [Business rules](docs/BUSINESS_RULES.md)
- [Workflows](docs/WORKFLOWS.md)
- [Testing](docs/TESTING.md)

Authenticator MFA is available from **Perfil → Seguridad**. Set the dedicated
`MFA_ENCRYPTION_KEY` before enrolling users; see [Authenticator MFA setup](docs/SETUP.md#authenticator-mfa)
for key generation, recovery codes, verification limits, and test instructions.
