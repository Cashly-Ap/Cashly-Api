# ⚙️ Setup & Configuration

## Prerequisites

- Java 24+
- Maven 3.8+
- Docker & Docker Compose
- Git

## Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/GabrielEVP/lexand-api.git
   cd lexand-api
   ```

2. **Choose your environment and start:**

   **🚀 Production (Default):**
   ```bash
   docker compose up --build
   ```

   **🔧 Development (Hot-reload + Debug):**
   ```bash
   DOCKERFILE=Dockerfile.dev SPRING_PROFILES_ACTIVE=dev docker compose up --build
   ```

   **🧪 Testing:**
   ```bash
   docker compose --profile test up --build
   ```

3. **Verify the application**
   ```bash
   curl http://localhost:8080/actuator/health
   # Response: {"status":"UP"}
   ```

> 💡 **Pro Tip**: For development, set environment variables in `.env` file:
> ```bash
> echo "DOCKERFILE=Dockerfile.dev" > .env
> echo "SPRING_PROFILES_ACTIVE=dev" >> .env
> docker compose up --build
> ```

## 🐳 Docker Environments

The project provides **three optimized Docker environments** for different use cases:

### 📋 Available Environments

| Environment | Dockerfile | Profile | Description | Use Case |
|-------------|------------|---------|-------------|----------|
| **🚀 Production** | `Dockerfile` | `prod` | Optimized production build | **Deployment, Production** |
| **🔧 Development** | `Dockerfile.dev` | `dev` | Hot-reload + Debug support | **Local development** |
| **🧪 Testing** | `Dockerfile.test` | `test` | Isolated test environment | **Automated testing** |

### 🚀 Production Environment (Default)

**Best for**: Production deployment, staging, performance testing

```bash
# Start production environment (DEFAULT)
docker compose up --build

# Run in background
docker compose up -d --build

# Check health
curl http://localhost:8080/actuator/health
```

**✨ Features:**
- ✅ Multi-stage optimized build
- ✅ Minimal runtime image (Alpine)
- ✅ Non-root user security
- ✅ Production JVM optimizations
- ✅ Health checks enabled
- ✅ No development tools

### 🔧 Development Environment

**Best for**: Local development, debugging, hot-reload

```bash
# Start development environment
DOCKERFILE=Dockerfile.dev SPRING_PROFILES_ACTIVE=dev docker compose up --build

# Or create .env file with:
echo "DOCKERFILE=Dockerfile.dev" > .env
echo "SPRING_PROFILES_ACTIVE=dev" >> .env
docker compose up --build
```

**✨ Features:**
- ✅ **Hot Reload** - Changes reflect automatically
- ✅ **Remote Debug** - Port 5005 for IDE debugging
- ✅ **LiveReload** - Port 35729 for frontend
- ✅ **Development Tools** - vim, git, wget included
- ✅ **Source Mounting** - Live code synchronization
- ✅ **Dev Profile** - Development database settings

**🔌 Debug Setup (IntelliJ/VS Code):**
```
Host: localhost
Port: 5005
Transport: Socket
Debugger mode: Attach
```

### 🧪 Testing Environment

**Best for**: Running tests, CI/CD pipelines, test automation

```bash
# Run complete test suite
docker compose --profile test up --build

# Run tests in background and check results
docker compose --profile test up -d --build
docker compose logs app-test
```

**✨ Features:**
- ✅ **Isolated Test DB** - Separate test database (port 3307)
- ✅ **Test Profile** - Test-specific configurations
- ✅ **Maven Test Runner** - Executes complete test suite
- ✅ **CI/CD Ready** - Perfect for automated pipelines
- ✅ **No Side Effects** - Doesn't affect dev/prod data

## 🛠️ Container Management

### Basic Operations

```bash
# Stop all containers
docker compose down

# Stop and remove volumes (⚠️ deletes data)
docker compose down -v

# View running containers
docker compose ps

# Check logs
docker compose logs app
docker compose logs -f app  # Follow logs

# Execute commands in running container
docker compose exec app bash

# Rebuild specific service
docker compose up --build app
```

### Troubleshooting

```bash
# Check container health
docker compose ps
curl http://localhost:8080/actuator/health

# View application logs
docker compose logs app

# Check database connectivity
docker compose exec db mysql -u lexand_user -p lexand_db

# Remove everything and start fresh
docker compose down -v
docker system prune -f
docker compose up --build
```

## 🔧 Configuration

### Database Configuration
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/lexand_db
spring.datasource.username=${DB_USER:lexand_user}
spring.datasource.password=${DB_PASSWORD:lexand_pass}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### Security Configuration
```properties
# OAuth2
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}

# JWT
jwt.secret=${JWT_SECRET:your-secret-key}
jwt.expiration=${JWT_EXPIRATION:86400}
```

### Environment Variables
```bash
# Required for production
export DB_USER=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
```

## 🔄 Database Migrations

### Flyway Migrations

Migrations are located in `src/main/resources/db/migration/` and follow the naming convention:
```
V{version}__{description}.sql
```

### Running Migrations

```bash
# Migrations run automatically on application startup
docker compose up --build

# Or run manually with Maven
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info

# Repair migrations (if needed)
./mvnw flyway:repair
```

### Current Development Baseline

The current Flyway set is a fresh-database baseline for the non-production Cashly environment. It creates the final schema through V24 and intentionally does not upgrade databases with the previous Flyway history. Recreate only the local development database before using this baseline. Once a migration set is deployed to a shared or production database, keep its applied files immutable and add a forward migration for later schema changes.

### Database Access

```bash
# Connect to database
docker compose exec db mysql -u lexand_user -p lexand_db

# Backup database
docker compose exec db mysqldump -u lexand_user -p lexand_db > backup.sql

# Restore database
docker compose exec -T db mysql -u lexand_user -p lexand_db < backup.sql
```

---

[← Back to Main README](../README.md)
