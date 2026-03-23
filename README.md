# 📦 Parsun API

> Enterprise invoicing system with customer management, suppliers, products, orders, and warehouse management. Built with Clean Architecture and Spring Boot 3.x 
## 🚀 Quick Start

### Prerequisites
- Java 24+
- Docker & Docker Compose
- Git

### Get Started in 30 seconds

1. **Clone and run:**
   ```bash
   git clone https://github.com/GabrielEVP/parsun-api.git
   cd parsun-api
   docker compose up --build
   ```

2. **Verify it's working:**
   ```bash
   curl http://localhost:8080/actuator/health
   # Response: {"status":"UP"}
   ```

3. **Start developing:**
   ```bash
   DOCKERFILE=Dockerfile.dev SPRING_PROFILES_ACTIVE=dev docker compose up --build
   ```

## 📚 Documentation

### Core Documentation
- **[🏗️ Architecture & Design](docs/ARCHITECTURE.md)** - Clean Architecture, DDD, SOLID principles
- **[⚙️ Setup & Configuration](docs/SETUP.md)** - Installation, Docker environments, configuration
- **[🔌 API Documentation](docs/API.md)** - Endpoints, Postman collection, OpenAPI
- **[🧪 Testing Guide](docs/TESTING.md)** - Testing strategy, TDD, test execution
- **[📋 Business Rules](docs/BUSINESS_RULES.md)** - Complete business logic and validation rules
- **[🔄 Workflows](docs/WORKFLOWS.md)** - System workflows, diagrams, and process flows

### Module Documentation
- **[🔐 AUTH Module](docs/modules/AUTH.md)** - Authentication and authorization (✅ Implemented)
- **[👥 CUSTOMERS Module](docs/modules/CUSTOMERS.md)** - Customer management with DDD aggregate pattern (✅ Implemented)
- **[📝 Module Template](docs/modules/TEMPLATE.md)** - Template for creating new module documentation

---

### Core
- **Java 24** - Latest LTS with modern features
- **Spring Boot 3.5.5** - Enterprise framework
- **MySQL** - Relational database
- **JWT** - Authentication & authorization

### Quality & Testing
- **JUnit 5** - Unit testing
- **Testcontainers** - Integration testing
- **Docker** - Containerization

## 🏃‍♂️ Development Environments

| Environment | Command | Use Case |
|-------------|---------|----------|
| **🚀 Production** | `docker compose up --build` | Deployment, staging |
| **🔧 Development** | `DOCKERFILE=Dockerfile.dev docker compose up --build` | Local coding, debug |
| **🧪 Testing** | `docker compose --profile test up --build` | Test automation |

> 💡 **Tip**: Create `.env` file with `DOCKERFILE=Dockerfile.dev` for persistent dev mode

## 🌟 Main Features

- **🏛️ Clean Architecture** - Separation of concerns, testable, maintainable
- **🎯 Domain-Driven Design** - Aggregate pattern, value objects, domain events
- **🔐 JWT Authentication** - Token-based security with refresh tokens
- **👥 Customer Management** - Complete customer data control with DDD aggregate pattern
- **📦 Product Catalog** - Inventory and pricing management (planned)
- **📋 Orders & Sales** - Order system for sales representatives (planned)
- **🧾 Invoicing** - Invoice generation and management (planned)
- **🏭 Warehouse Management** - Multi-warehouse stock control and transfers (planned)
- **👤 User Management** - Role-based permissions (ADMIN, SALES_REP, WAREHOUSE_WORKER)
- **☁️ Cloud Database** - Remote access for sales representatives

---

## 📁 Project Structure

```
src/main/java/com/parsun/parsun_api/
├── auth/           # Authentication & authorization ✅
│   ├── application/ # DTOs, use cases, ports
│   ├── domain/      # Entities, services, value objects
│   └── infrastructure/ # Web, persistence, configuration
├── customers/      # Customer management ✅
│   ├── application/ # DTOs, use cases, ports
│   ├── domain/      # Entities, services, value objects
│   └── infrastructure/ # Web, persistence, configuration
├── products/       # Product catalog (planned)
├── orders/         # Order management (planned)
├── invoices/       # Invoicing (planned)
├── warehouses/     # Warehouse and inventory management (planned)
└── shared/         # Shared utilities and domain events
```

---

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Clean Architecture principles and modern Java technologies.**

For detailed documentation, visit our [docs folder](docs/) or check individual guides above.