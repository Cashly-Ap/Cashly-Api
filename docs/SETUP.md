# ⚙️ Setup and configuration

## Prerequisites

- Docker with Compose
- Git

Java 24 and Maven are only needed if you run the build outside Docker.

## Clone

```bash
git clone git@gitlab.com:personal-proyect-gabriel/cashly/cashly-api.git
cd cashly-api
```

## Start the API

The default Compose service runs the development image with hot reload, MySQL and MinIO:

```bash
docker compose up --build
```

Verify it:

```bash
curl http://localhost:8080/actuator/health
```

Other profiles:

```bash
# Optimized production image
docker compose --profile prod up --build

# Isolated test database and test runner
docker compose --profile test up --build
```

## Services

| Service | Container | Purpose |
| --- | --- | --- |
| `app` | `cashly-api-app` | API with the `dev` profile and hot reload |
| `app-prod` | `cashly-api-app-prod` | Production image, enabled with `--profile prod` |
| `mysql` | `cashly-api-mysql` | MySQL 8.0 on port 3306 |
| `mysql-test` | `cashly-api-mysql-test` | Isolated test MySQL on port 3307 |
| `minio` | `cashly-api-minio` | S3-compatible local storage |
| `minio-init` | — | Creates the local bucket and exits |

## Configuration

Values are read from `.env` by Docker Compose. The main variables are:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_NAME` | `mydatabase` | MySQL database |
| `DB_USER` | `myuser` | MySQL application user |
| `DB_PASSWORD` | `secret` | MySQL application password |
| `DB_ROOT_PASSWORD` | `verysecret` | MySQL root password |
| `DB_PORT` | `3306` | MySQL host port |
| `SERVER_PORT` | `8080` | API host port |
| `JWT_SECRET` | development default | JWT signing key |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token lifetime in ms |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token lifetime in ms |
| `MFA_ENCRYPTION_KEY` | empty | AES key for authenticator secrets |
| `CASHLY_SEED_ENABLED` | `false` | Runs the Java seed on startup |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Allowed frontend origin |
| `STORAGE_S3_ENDPOINT` | `http://minio:9000` | Local S3 endpoint |
| `STORAGE_S3_ACCESS_KEY` | `minioadmin` | Local S3 access key |
| `STORAGE_S3_SECRET_KEY` | `minioadmin` | Local S3 secret key |
| `STORAGE_S3_BUCKET` | `cashly-files` | Local S3 bucket |

The application profiles are in `src/main/resources/application-dev.properties` and `application-production.properties`.

## Database

The schema is created by the Flyway migrations under `src/main/resources/db/migration`. The current baseline ends at **V37**.

Migrations run automatically at API startup. Do not edit an applied migration; add a forward migration for later changes.

### Reset a disposable database

This drops the local database and lets Flyway rebuild it:

```bash
docker compose stop app
docker exec cashly-api-mysql mysql -uroot -pverysecret \
  -e "DROP DATABASE mydatabase; CREATE DATABASE mydatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
docker compose start app
```

If your `.env` changes the root password or database name, replace the values in the command.

### Seed data

Seeding is implemented as 29 ordered Java steps in `infrastructure/seed/steps` and orchestrated by `DatabaseSeedRunner`.

Enable it in `.env`:

```dotenv
CASHLY_SEED_ENABLED=true
```

The seed is idempotent and runs after startup. Leave it disabled for production-like data.

### Connect to MySQL

```bash
docker compose exec mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME"
```

Backup and restore:

```bash
docker compose exec mysql mysqldump -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" > backup.sql
docker compose exec -T mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < backup.sql
```

## Permissions

The API uses 131 permission codes in the form `MODULE_ACTION`. The catalog is exposed at:

```http
GET /api/permissions/catalog
```

Current access for the signed-in user is exposed at:

```http
GET /api/auth/access
```

and returns `permissions: string[]`. See [ACCESS_CONTROL.md](modules/ACCESS_CONTROL.md).

## Container operations

```bash
docker compose ps
docker compose logs -f app
docker compose exec app bash
docker compose exec app ./mvnw test
docker compose down
docker compose down -v   # removes volumes and database data
```

---

[← Back to Main README](../README.md)

## Authenticator MFA

Cashly supports six-digit TOTP codes from Microsoft Authenticator, Google Authenticator,
1Password, and other RFC 6238 compatible authenticators. Google sign-in is separate and
is not enabled by this feature.

Before enrolling an account, configure `MFA_ENCRYPTION_KEY` in the ignored API `.env`
and in your production secret manager. Generate a dedicated 32-byte key, encoded in
Base64, for example with `openssl rand -base64 32`. Both API Compose configurations pass
this variable to the application. Recreate the API container after changing its environment.
Existing accounts can still sign in without this setting; enrollment fails until a valid key
is configured.

Keep this key stable and back it up separately from the database. Authenticator secrets
are encrypted with AES-256-GCM. Losing or replacing the key prevents validation of existing
authenticator codes. Key rotation requires decrypting and re-encrypting enrolled secrets;
simply replacing the environment variable does not rotate stored data.

In **Perfil → Seguridad**, confirm the current password, scan the QR code, and enter a
current authenticator code to enable MFA. The setup expires after ten minutes. Save the
ten recovery codes when they are first shown: only their SHA-256 hashes are stored, and
each recovery code works once. Disabling MFA requires both the current password and an
authenticator or recovery code. Enable/disable invalidates refresh sessions and the web app
asks the user to sign in again. Previously issued access tokens retain their existing short
expiry (up to fifteen minutes).

With MFA enabled, `POST /api/auth/login` returns `mfaRequired: true` and an opaque
`challengeToken`, with no access or refresh token. Submit `{challengeToken, code}` to
`POST /api/auth/mfa/verify` to finish signing in. Challenges expire after five minutes,
are single-use, and a new password login replaces the previous challenge. Codes use a
30-second period with one step of clock tolerance; an accepted step cannot be reused.
Five failed MFA/password reauthentication attempts lock MFA verification for fifteen minutes.
Starting another login does not reset that limit. Keep the server clock synchronized.

Management endpoints require an authenticated user:

| Endpoint | Request | Response |
| --- | --- | --- |
| `GET /api/auth/mfa` | — | `enabled`, `recoveryCodesRemaining` |
| `POST /api/auth/mfa/setup` | `{password}` | `secret`, `otpauthUri`, `expiresIn` |
| `POST /api/auth/mfa/enable` | `{code}` | `recoveryCodes` |
| `POST /api/auth/mfa/disable` | `{password, code}` | HTTP 204 |

Run `MfaTransactionsTest` with `MFA_TEST_JDBC_URL` pointing to the dedicated MySQL
`mfa_verification` schema and `SPRING_DATASOURCE_USERNAME`/`SPRING_DATASOURCE_PASSWORD` set for that schema.
The test recreates only the `auth_mfa` table in that isolated schema; never point it at a
development or production schema.
