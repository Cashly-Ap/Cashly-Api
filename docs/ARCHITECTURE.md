# 🏗️ Architecture & Design - Lexand

## System Overview

**Lexand** is an enterprise invoicing system designed to comprehensively manage business operations. It allows sales representatives to connect remotely to a cloud database to:

- Register and manage customers
- Browse product catalogs
- Create orders and purchase orders
- Generate invoices
- Control inventory across multiple warehouses
- Manage suppliers and purchases

### Main Use Cases

1. **Field Sales Representatives**: Mobile access to create orders in real-time
2. **Inventory Management**: Real-time stock control
3. **Automated Invoicing**: Invoice generation from orders
4. **Supplier Management**: Purchase control and supplier relationships
5. **Reports & Analytics**: Sales analysis, best-selling products, etc.

## Clean Architecture Overview

This project implements **Clean Architecture** (also known as Hexagonal Architecture) to ensure:

- **Framework Independence**: The architecture doesn't depend on specific libraries
- **Testability**: Business rules can be tested without UI, database, or external services
- **UI Independence**: The interface can change without affecting the rest of the system
- **Database Independence**: Business rules are not coupled to the database
- **External Agency Independence**: Business rules don't know about the outside world

### Architecture Layers

```
┌─────────────────────────────────────────┐
│              Controllers                │ ← Interface Adapters
├─────────────────────────────────────────┤
│               Use Cases                 │ ← Application Business Rules
├─────────────────────────────────────────┤
│               Entities                  │ ← Enterprise Business Rules
└─────────────────────────────────────────┘
│              External                   │ ← Frameworks & Drivers
│         (Database, Web, etc.)           │
└─────────────────────────────────────────┘
```

## 🎯 Design Principles

### SOLID Principles

- **S** - Single Responsibility Principle: Each class has one reason to change
- **O** - Open/Closed Principle: Open for extension, closed for modification
- **L** - Liskov Substitution Principle: Objects should be replaceable with instances of their subtypes
- **I** - Interface Segregation Principle: Many client-specific interfaces are better than one general-purpose interface
- **D** - Dependency Inversion Principle: Depend on abstractions, not concretions

### Domain-Driven Design (DDD)

- **Ubiquitous Language**: Common language between developers and domain experts
- **Bounded Contexts**: Clear boundaries between different parts of the domain
- **Aggregates**: Consistency boundaries for business rules
- **Value Objects**: Immutable objects that describe aspects of the domain
- **Domain Events**: Capture business events that domain experts care about

### Test-Driven Development (TDD)

- **Red-Green-Refactor**: Write failing test → Make it pass → Refactor
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **End-to-End Tests**: Test complete user workflows

### DRY (Don't Repeat Yourself)

- Shared utilities and common functionality
- Reusable components and services
- Configuration externalization
- Template patterns for common operations

## 📁 Project Structure

```plaintext
src/
└── main/
    └── java/
        └── com/
            └── lexand/
                └── lexand_api/
                    ├── auth/               # Authentication & Authorization ✅ IMPLEMENTED
                    │   ├── application/
                    │   │   ├── dto/        # Request & Response DTOs
                    │   │   │   ├── LoginRequest, RegisterUserRequest
                    │   │   │   ├── AuthenticationResponse, UserResponse
                    │   │   │   └── RefreshTokenRequest, LogoutRequest
                    │   │   ├── exceptions/ # Custom exceptions
                    │   │   ├── mappers/    # DTO ↔ Domain mappers
                    │   │   └── usecases/   # Use cases (Login, Register, Refresh, Logout)
                    │   ├── domain/
                    │   │   ├── entities/   # User, RefreshToken
                    │   │   ├── repositories/ # Repository ports (interfaces)
                    │   │   ├── services/   # AuthenticationDomainService, PasswordEncoder
                    │   │   └── valueobjects/ # Email, Password, UserName, UserId
                    │   └── infrastructure/
                    │       ├── config/     # JWT, Security, BCrypt
                    │       ├── persistence/ # JPA entities & repositories
                    │       ├── security/   # JWT filters, authentication
                    │       └── web/        # AuthController, ExceptionHandler
                    │
                    ├── customers/          # Customer Management ✅ IMPLEMENTED
                    │   ├── application/
                    │   │   ├── dto/        # Request & Response DTOs
                    │   │   │   ├── request/    # CreateCustomerRequest, UpdateCustomerRequest
                    │   │   │   └── response/   # CustomerListResponse, CustomerDetailResponse
                    │   │   │                   # EmailRequest/Response, PhoneRequest/Response
                    │   │   ├── exceptions/ # CustomerNotFoundException
                    │   │   ├── mappers/    # CustomerMapper
                    │   │   └── usecases/   # Register, Get, List, Update, Delete
                    │   ├── domain/
                    │   │   ├── entities/   # Customer (aggregate root)
                    │   │   │               # CustomerEmail, CustomerPhone, CustomerAddress (internal entities)
                    │   │   ├── events/     # CustomerCreatedEvent
                    │   │   ├── repositories/ # CustomerRepository (port)
                    │   │   ├── services/   # CustomerCodeGenerator
                    │   │   └── valueobjects/ # CustomerId, CustomerCode, LegalName, Rif, etc.
                    │   └── infrastructure/
                    │       ├── persistence/ # JPA entities & repositories
                    │       ├── services/   # SequentialCustomerCodeGenerator
                    │       └── web/        # CustomerController
                    │
                    ├── products/           # Product Catalog ✅ IMPLEMENTED
                    │   ├── application/    # CRUD products, compositions, stock validation
                    │   │   ├── dto/        # Request & Response DTOs
                    │   │   │   ├── request/    # CreateProductRequest, UpdateProductRequest
                    │   │   │   │               # ProductCompositionRequest, UpdateCompositionRequest
                    │   │   │   └── response/   # ProductListResponse, ProductDetailResponse
                    │   │   │                   # ProductCompositionResponse, StockEntryResponse
                    │   │   ├── exceptions/ # ProductNotFoundException, InsufficientComponentStockException
                    │   │   ├── mappers/    # ProductMapper, CompositionMapper
                    │   │   └── usecases/   # Create, Update, Delete, AddStock, ValidateComposition
                    │   ├── domain/         # Product (aggregate), ProductComposition (internal entity)
                    │   │   ├── entities/   # Product, ProductComposition
                    │   │   ├── repositories/ # ProductRepository, ProductCompositionRepository (ports)
                    │   │   ├── services/   # ProductCodeGenerator, CompositionValidator
                    │   │   └── valueobjects/ # ProductId, ProductCode, ProductName, ProductType
                    │   │                   # UnitOfMeasure, BulkQuantity, QuantityRequired
                    │   └── infrastructure/ # REST, persistence, services
                    │       ├── persistence/ # JPA entities & repositories
                    │       ├── services/   # SequentialProductCodeGenerator
                    │       └── web/        # ProductController
                    │
                    ├── orders/             # Order Management 🚧 PLANNED
                    │   ├── application/    # Create, modify, approve orders
                    │   ├── domain/         # Order (aggregate), OrderLine, Status
                    │   └── infrastructure/ # REST, persistence
                    │
                    ├── invoices/           # Invoicing 🚧 PLANNED
                    │   ├── application/    # Generate, send invoices
                    │   ├── domain/         # Invoice (aggregate), InvoiceLine, Payment
                    │   └── infrastructure/ # PDF generation, REST
                    │
                    ├── warehouses/         # Warehouse Management 🚧 PLANNED
                    │   ├── application/    # Stock control, movements, transfers
                    │   ├── domain/         # Warehouse, Stock, StockMovement, Transfer
                    │   └── infrastructure/ # REST, persistence
                    │
                    ├── suppliers/          # Supplier Management 🚧 PLANNED
                    │   ├── application/    # DTOs, use cases
                    │   ├── domain/         # Supplier entities and logic
                    │   └── infrastructure/ # Web, config, persistence
                    │
                    ├── purchases/          # Purchase Management 🚧 PLANNED
                    │   ├── application/    # Supplier purchases
                    │   ├── domain/         # Purchase (aggregate), PurchaseLine
                    │   └── infrastructure/ # REST, persistence
                    │
                    ├── infrastructure/     # Global Infrastructure
                    │   └── config/         # CorsConfig, OpenApiConfig
                    │
                    └── shared/             # Shared Module
                        ├── domain/
                        │   ├── AggregateRoot.java
                        │   └── DomainEvent.java
                        ├── exceptions/     # GlobalExceptionHandler, custom exceptions
                        └── utils/          # ControllerUtils
```

### Module Structure Pattern

Each module follows this structure:

```
[module]/
├── application/                 # Application Layer (Use Cases)
│   ├── dto/
│   │   ├── request/            # Input DTOs (Create, Update)
│   │   └── response/           # Output DTOs (List, Detail, Create)
│   ├── exceptions/             # Module-specific exceptions
│   ├── mappers/                # DTO ↔ Domain mappers
│   └── usecases/               # Business use cases
├── domain/                      # Domain Layer (Business Logic)
│   ├── entities/               # Domain entities (Aggregates + Internal Entities)
│   ├── events/                 # Domain events
│   ├── repositories/           # Repository ports (interfaces)
│   ├── services/               # Domain services
│   └── valueobjects/           # Value objects
└── infrastructure/              # Infrastructure Layer (Technical Details)
    ├── config/                 # Module configuration
    ├── persistence/            # JPA entities & repositories
    ├── services/               # Infrastructure service implementations
    └── web/                    # REST controllers & exception handlers
```

### Layer Responsibilities

#### 🏛️ Domain Layer (Enterprise Business Rules)

**Purpose**: Contains pure business logic independent of frameworks, databases, or external systems.

- **Entities**: Main business objects with identity and lifecycle
  - **Aggregate Roots**: Customer, Order, Invoice, Product, Warehouse
  - **Internal Entities**: CustomerEmail, CustomerPhone, OrderLine, InvoiceLine
  - Encapsulate business rules and invariants
  - Protect their own consistency
  
- **Value Objects**: Immutable objects representing domain concepts
  - Email, Address, Money, Quantity, CustomerCode, Rif
  - Self-validating
  - No identity (compared by value)
  - Examples: `Email("user@example.com")`, `Money(100.50, "USD")`
  
- **Domain Services**: Business logic that doesn't belong to a single entity
  - Tax calculation, stock validation, price calculation
  - Coordinate multiple entities
  - Stateless operations
  
- **Domain Events**: Capture important business events
  - CustomerCreatedEvent, OrderApprovedEvent, InvoiceGeneratedEvent
  - Enable decoupling between modules
  - Support event-driven architecture
  
- **Repository Ports**: Interfaces defining persistence contracts
  - Define WHAT data operations are needed
  - Implementation in infrastructure layer
  - Example: `CustomerRepository`, `OrderRepository`

**Rules**:
- ✅ NO dependencies on frameworks or external libraries
- ✅ NO dependencies on other layers
- ✅ Pure Java with business logic only
- ✅ All validation in value objects and entities
- ❌ NO annotations (except Java built-in)
- ❌ NO database, web, or infrastructure concerns

---

#### 🎯 Application Layer (Application Business Rules)

**Purpose**: Orchestrates domain objects to fulfill use cases. Defines application flow.

- **Use Cases**: Coordinate domain objects to fulfill business requirements
  - One use case = one business operation
  - Examples: `CreateOrderUseCase`, `GenerateInvoiceUseCase`, `UpdateStockUseCase`
  - Input: Request DTO
  - Output: Response DTO
  - Orchestrates: Domain entities, domain services, repositories
  
- **DTOs (Data Transfer Objects)**: Transfer data between layers
  - **Request DTOs**: Input from API
    - Validation annotations (`@NotBlank`, `@Email`, `@Valid`)
    - No business logic
    - Immutable (Java records preferred)
  - **Response DTOs**: Output to API
    - Simple data structures
    - Tailored for specific views (List, Detail, Edit, Create)
    - Immutable (Java records preferred)
    
- **Mappers**: Convert between DTOs and Domain objects
  - DTO → Domain (for input)
  - Domain → DTO (for output)
  - Stateless utility classes
  
- **Repository Ports**: Interfaces for persistence
  - Defined in domain layer
  - Used in application layer
  - Implemented in infrastructure layer

**Rules**:
- ✅ Can depend on domain layer
- ✅ Orchestrates domain objects
- ✅ Transaction boundaries
- ✅ Validation of input DTOs
- ❌ NO business logic (delegate to domain)
- ❌ NO direct database access
- ❌ NO framework-specific code

---

#### 🔌 Infrastructure Layer (Frameworks & Drivers)

**Purpose**: Implements technical details and integrations with external systems.

- **Persistence**: Database implementations
  - **JPA Entities**: Database table mappings
    - Separate from domain entities
    - Framework-specific annotations
  - **JPA Repositories**: Spring Data implementations
    - Implement domain repository ports
    - Handle database transactions
  - **Entity Mappers**: JPA Entity ↔ Domain Entity conversion
  
- **Web (REST API)**: HTTP controllers
  - **Controllers**: REST endpoints
    - Receive HTTP requests
    - Call use cases
    - Return HTTP responses
  - **Exception Handlers**: Convert exceptions to HTTP errors
  - **Request/Response validation**: Spring validation
  
- **Security**: Authentication and authorization
  - JWT token generation and validation
  - Spring Security configuration
  - Password encryption (BCrypt)
  
- **Configuration**: Application setup
  - CORS configuration
  - OpenAPI/Swagger documentation
  - Database connection
  - External service integration

**Rules**:
- ✅ Can depend on application and domain layers
- ✅ Framework-specific code allowed
- ✅ External library integrations
- ✅ Database and web concerns
- ❌ NO business logic
- ❌ Should be easily replaceable

---

### DTO Organization Pattern

Each module organizes DTOs into **request** and **response** subpackages:

```
application/dto/
├── request/                    # Input DTOs for API requests
│   ├── Create[Entity]Request.java
│   ├── Update[Entity]Request.java
│   └── [Nested]Request.java   # e.g., EmailRequest, PhoneRequest
└── response/                   # Output DTOs for API responses
    ├── [Entity]ListResponse.java      # Minimal data for list views
    ├── [Entity]DetailResponse.java    # Complete data for detail views
    ├── [Entity]CreateResponse.java    # Data returned after creation
    └── [Nested]Response.java          # e.g., EmailResponse, PhoneResponse
```

**Example: Customers Module**

```java
// Request DTOs (no IDs for nested entities)
public record CreateCustomerRequest(
    String legalName,
    String type,
    List<EmailRequest> emails      // EmailRequest has only 'email' field
)

public record UpdateCustomerRequest(
    String legalName,
    String type,
    List<EmailRequest> emails      // Same structure, backend replaces all
)

// Response DTOs (with IDs for nested entities)
public record CustomerListResponse(
    String id,
    Long code,
    String alias,
    String legalName
    // Minimal fields for list view, NO nested entities
)

public record CustomerDetailResponse(
    String id,
    Long code,
    String legalName,
    List<EmailResponse> emails     // EmailResponse has id, customerId, email
    // Complete data with all nested entities
)

public record CustomerCreateResponse(
    String id,                     // Generated by backend
    Long code,                     // Generated by backend
    String legalName,
    List<EmailResponse> emails     // With generated IDs
)
```

**Key Points**:
- Request DTOs: NO IDs in nested entities (emails, phones, addresses)
- Response DTOs: WITH IDs in all nested entities
- Different response DTOs for different views (List vs Detail)
- Update replaces ALL nested entities (no merge)

---

### Aggregate Pattern (DDD)

**Aggregates** are clusters of domain objects treated as a single unit for data changes.

#### Aggregate Root Rules

1. **One Entry Point**: All access to aggregate goes through the root
2. **Consistency Boundary**: Aggregate ensures all internal rules are satisfied
3. **Transaction Boundary**: Save/delete entire aggregate in one transaction
4. **No Direct Access**: Cannot access internal entities directly from outside

#### Example: Customer Aggregate

```
Customer (Aggregate Root)
├── CustomerEmail (Internal Entity) - Cannot be accessed independently
├── CustomerPhone (Internal Entity) - Cannot be accessed independently
└── CustomerAddress (Internal Entity) - Cannot be accessed independently
```

**API Design**:
- ✅ POST /api/customers (create customer with emails, phones, addresses)
- ✅ PUT /api/customers/{id} (update customer and ALL nested entities)
- ✅ DELETE /api/customers/{id} (delete customer + cascade delete nested)
- ❌ POST /api/customers/{id}/emails (NO - violates aggregate pattern)
- ❌ PUT /api/emails/{id} (NO - emails don't exist independently)
- ❌ DELETE /api/phones/{id} (NO - phones managed through customer)

**Update Pattern**:
- Backend **replaces ALL** nested entities on update
- Frontend sends complete arrays of desired nested entities
- Backend deletes old + creates new
- No merge logic, no partial updates

**Benefits**:
- Strong consistency within aggregate
- Clear ownership and lifecycle
- Simplified API (fewer endpoints)
- Transactional integrity guaranteed

## 🎭 Implemented Modules

### 🔐 Auth Module (Authentication & Authorization)
**Status**: ✅ **IMPLEMENTED**  
**Purpose**: JWT-based authentication and user management

**Aggregate Root**: User

**Domain Entities**:
- `User`: System user with credentials
  - Value Objects: UserId, Email, UserName, Password
  - Fields: active, emailVerified, createdAt, updatedAt
  - Methods: changePassword, activate, deactivate, verifyEmail
- `RefreshToken`: Token for refreshing access tokens
  - Value Objects: RefreshTokenId, UserId
  - Fields: token, expiresAt, revoked, createdAt
  - Methods: isExpired, isValid, revoke

**Use Cases**:
- RegisterUserUseCase: Create new user account
- LoginUserUseCase: Authenticate and generate tokens
- RefreshTokenUseCase: Refresh access token
- LogoutUserUseCase: Revoke refresh token
- GetUserByIdUseCase: Retrieve user details

**API Endpoints**:
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get tokens
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout (revoke refresh token)

**Security**:
- JWT access tokens (1 hour expiration)
- Refresh tokens (7 days expiration)
- BCrypt password hashing
- Token blacklist via revocation

**Documentation**: [docs/modules/AUTH.md](./modules/AUTH.md)

---

### 👥 Customers Module
**Status**: ✅ **IMPLEMENTED**  
**Purpose**: Complete customer management for invoicing system

**Aggregate Root**: Customer

**Domain Entities**:
- `Customer` (Aggregate Root):
  - Value Objects: CustomerId, CustomerCode, LegalName, Rif, CustomerAlias, CustomerType, CreditDays, CreditLimit
  - Fields: id, code, legalName, type, rif, alias, creditDays, creditLimit, active, createdAt, updatedAt
  - Collections: emails, phones, addresses (managed internally)
  - Methods: updateLegalName, updateRif, updateAlias, updateCreditDays, updateCreditLimit, activate, deactivate
  
- `CustomerEmail` (Internal Entity):
  - Value Objects: EmailId, CustomerId, EmailAddress
  - **NEVER accessed directly** - managed through Customer aggregate
  
- `CustomerPhone` (Internal Entity):
  - Value Objects: PhoneId, CustomerId, PhoneNumber
  - **NEVER accessed directly** - managed through Customer aggregate
  
- `CustomerAddress` (Internal Entity):
  - Value Objects: AddressId, CustomerId
  - Fields: address, billing, active
  - Methods: markAsBilling, unmarkAsBilling, activate, deactivate
  - **NEVER accessed directly** - managed through Customer aggregate

**Customer Types** (Enum):
- INDIVIDUAL: Natural person
- BUSINESS: Company/Business
- GOVERNMENT: Government entity
- NON_PROFIT: Non-profit organization

**Use Cases**:
- RegisterCustomerUseCase: Create new customer
- GetCustomerUseCase: Get customer by ID
- ListCustomersUseCase: Get all customers (minimal data)
- UpdateCustomerUseCase: Update customer (replaces nested entities)
- DeleteCustomerUseCase: Delete customer (cascade delete nested)

**DTOs**:
- **Request**: CreateCustomerRequest, UpdateCustomerRequest
  - Nested: EmailRequest, PhoneRequest, AddressRequest (NO IDs)
- **Response**: CustomerListResponse, CustomerDetailResponse, CustomerCreateResponse
  - Nested: EmailResponse, PhoneResponse, AddressResponse (WITH IDs)

**API Endpoints**:
- `POST /api/customers` - Create customer
- `GET /api/customers` - List all (returns CustomerListResponse)
- `GET /api/customers/{id}` - Get by ID (returns CustomerDetailResponse)
- `PUT /api/customers/{id}` - Update customer (replaces ALL nested entities)
- `DELETE /api/customers/{id}` - Delete customer

**Key Features**:
- Auto-generated sequential customer code
- Credit management (creditDays, creditLimit)
- Multiple emails, phones, and addresses per customer
- Only one billing address allowed
- Aggregate pattern: NO separate endpoints for emails/phones/addresses
- Update replaces all nested entities (no merge)

**Business Rules**:
1. Customer code auto-generated sequentially (1, 2, 3...)
2. At least 1 email, 1 phone, 1 address required
3. Only 1 billing address per customer
4. RIF must follow valid Venezuelan format
5. Emails, phones, addresses ONLY managed through Customer
6. Update operation replaces ALL nested entities

**Domain Events**:
- CustomerCreatedEvent: Triggered on customer creation

**Documentation**: [docs/modules/CUSTOMERS.md](./modules/CUSTOMERS.md)

---

### 📦 Products Module
**Status**: ✅ **IMPLEMENTED**  
**Purpose**: Product catalog with composite products, material management, and multiple units of measure

**Aggregate Root**: Product

**Domain Entities**:
- `Product` (Aggregate Root):
  - Value Objects: ProductId, ProductCode, ProductName, ProductDescription, ProductCategory, Money
  - Fields: id, code, name, description, category, costPrice, salePrice, photo, active
  - Enums: ProductType (SIMPLE, COMPOSITE), UnitOfMeasure (UNIT, KILOGRAM, BULK)
  - Fields for BULK: bulkQuantity (units per bulk package)
  - Methods: updatePrice, updateCategory, activate, deactivate
  
- `ProductComposition` (Internal Entity):
  - Defines component materials for COMPOSITE products
  - Fields: parentProductId, componentProductId, quantityRequired, componentUnitOfMeasure
  - **NEVER accessed directly** - managed through Product aggregate

**Product Types**:
- **SIMPLE**: Standard products with direct stock tracking
- **COMPOSITE**: Products assembled from components (auto-deducts materials when stock added)

**Units of Measure**:
- **UNIT**: Individual items (laptop, phone, bottle)
- **KILOGRAM**: Weight-based (rice, flour, coffee)
- **BULK**: Packages/bundles (case of 12 sodas, box of 50 pens)

**Use Cases**:
- CreateSimpleProduct: Create standard product
- CreateCompositeProduct: Create product with component requirements
- UpdateProduct: Update product details
- UpdateProductComposition: Update component list (replaces all)
- DeleteProduct: Soft delete (mark inactive)
- AddStockToProduct: Add stock (auto-deducts components for COMPOSITE)
- RemoveStockFromProduct: Deduct stock (sales, damage)
- ValidateCompositionStock: Check component availability
- CalculateMaxAssemblableUnits: Calculate max units that can be assembled

**DTOs**:
- **Request**: CreateProductRequest, UpdateProductRequest, UpdateCompositionRequest
  - Nested: ProductCompositionRequest (NO IDs)
- **Response**: ProductListResponse, ProductDetailResponse, StockEntryResponse
  - Nested: ProductCompositionResponse, ComponentDeductionResponse (WITH IDs)

**API Endpoints**:
- `POST /api/products` - Create product (simple or composite)
- `GET /api/products` - List all (returns ProductListResponse, filterable by type, category, UOM)
- `GET /api/products/{id}` - Get by ID (returns ProductDetailResponse with composition)
- `PUT /api/products/{id}` - Update product details
- `PUT /api/products/{id}/composition` - Update product composition (COMPOSITE only)
- `DELETE /api/products/{id}` - Delete product

**Key Features**:
- Auto-generated sequential product codes (PRD-00001, PRD-00002...)
- Composite products with automatic component deduction
- Multi-unit of measure support (units, kg, bulk packages)
- Stock validation before assembly
- Circular dependency detection
- Maximum assemblable units calculation
- Nested compositions supported (composite made from composites)

**Business Rules**:
1. Product code auto-generated sequentially
2. COMPOSITE products require at least 1 component
3. SIMPLE products cannot have components
4. BULK products require bulkQuantity > 0
5. Adding stock to COMPOSITE auto-deducts components atomically
6. Cannot add COMPOSITE stock without sufficient components
7. Cannot delete product with existing stock or used as component
8. No circular dependencies in compositions
9. Component UOM must match component product's primary UOM

**Stock Management Integration**:
- When adding stock to COMPOSITE product:
  - System calculates required component quantities
  - Validates all components have sufficient stock
  - Deducts all components atomically (transaction)
  - Adds composite product stock
  - Creates stock movements for all changes
- If any component insufficient, entire operation fails

**Documentation**: [docs/modules/PRODUCTS.md](./modules/PRODUCTS.md)

---

## 🚧 Planned Modules

### 🏢 Suppliers Module
**Status**: 🚧 **PLANNED**  
**Purpose**: Supplier database connected to purchases and products

**Aggregate Root**: Supplier (similar to Customer aggregate pattern)

Supplier administration with:
- **Core Entity**: Supplier with unique tax ID, purchase history
- **Sub-entities**:
  - **Addresses**: Supplier locations and delivery addresses
  - **Phone Numbers**: Contact numbers with classification
  - **Emails**: Communication channels with validation
  - **Bank Accounts**: Payment account details

**Key Features**:
- Supplier product catalog linkage
- Purchase order history
- Payment terms and conditions
- Supplier performance tracking
- Multi-contact management

---

### 📋 Orders Module
**Status**: 🚧 **PLANNED**  
**Purpose**: Sales order and quotation management for invoice processing

**Aggregate Root**: Order

Order management with:
- **Core Entity**: Order/Budget created by sales reps
- **Sub-entities**:
  - **Order Lines**: Product items or services
    - Product reference (optional for custom items)
    - Quantity, unit price, tax rate, discount
    - Stock availability checking
    - Price validation against catalog

**Status Flow**: DRAFT → PENDING → APPROVED → INVOICED → DELIVERED → CANCELLED

**Key Features**:
- Field sales rep order creation
- Customer assignment and validation
- Real-time stock availability checking
- Customer-specific pricing
- Automatic invoice generation on approval
- Stock reservation system
- Order approval workflow
- Order cancellation with reason tracking

**Reports**:
- **Voided Documents Report**: Complete audit trail of cancelled orders
  - Cancellation date and time
  - User who cancelled
  - Cancellation reason
  - Original order details

---

### 🧾 Invoices Module
**Status**: 🚧 **PLANNED**  
**Purpose**: Invoice generation and financial tracking for customers

**Aggregate Root**: Invoice

Invoice management with:
- **Core Entity**: Invoice with sequential numbering
- **Sub-entities**:
  - **Invoice Lines**: Billed items
    - Reference to order lines (if from order)
    - Manual entry supported
    - Quantity, unit price, tax calculation
  - **Payment Due Dates (Vencimientos)**: Payment schedule
    - Multiple installments support
    - Amount and due date per installment
    - Status tracking (pending, paid, overdue)
  - **Payments**: Payment records
    - Payment date, amount, method
    - Application to specific due dates (FIFO)
    - Reference numbers for reconciliation

**Key Features**:
- Auto-generation from approved orders
- Manual invoice creation
- Tax calculation (line and total)
- Customer balance impact
- Payment application (FIFO method)
- PDF generation and email delivery
- Sequential invoice numbering
- Payment term management
- Invoice cancellation with audit trail

**Reports**:
- **Sales Reports**: 
  - Detailed: Individual invoice breakdown
  - Summary: Aggregated by customer, period, payment method
  - Filterable by customer, date range, payment status
- **Accounts Receivable Report**:
  - Customer debt status
  - Aging analysis (current, 30 days, 60 days, 90+ days)
  - Outstanding balance per customer
- **Due Dates Report** (Vencimientos):
  - Upcoming due dates (next 7, 15, 30 days)
  - Overdue invoices with days overdue
  - Filterable by customer and date range
- **Voided Documents Report**:
  - Cancelled invoices with reason and audit trail
  - User who cancelled and timestamp

---

### 🏭 Warehouses Module
**Status**: 🚧 **PLANNED**  
**Purpose**: Multi-warehouse stock management with transfer capabilities and comprehensive inventory control

**Aggregate Roots**: Warehouse, StockTransfer

Warehouse management with:
- **Core Entity**: Warehouse location
- **Sub-entities**:
  - **Stock**: Product quantities per warehouse
    - Current quantity
    - Reserved quantity
    - Available quantity
    - Location within warehouse
  - **Stock Transfers (Traspasos)**: Inter-warehouse movements
    - Origin and destination warehouses
    - Product and quantity
    - Status: INITIATED → IN_TRANSIT → RECEIVED → COMPLETED
    - Transfer date and responsible user
    - Cancellation capability (before completion)
  - **Stock Movements**: Complete audit trail
    - Movement type: purchase, sale, transfer-out, transfer-in, adjustment, load, unload
    - Product, quantity, warehouse reference
    - Date, time, user responsible
    - Reference document (purchase ID, order ID, transfer ID)
    - Before/after quantities for audit

**Key Features**:
- Multiple physical warehouse locations
- Real-time stock levels per product per warehouse
- Inter-warehouse transfers with tracking
- Stock movement audit trail (full traceability)
- Reservation system for pending orders
- Stock adjustment with admin authorization
- **Load Operations**: Receive inventory from purchases or production
- **Unload Operations**: Release inventory for sales or transfers
- Low stock alerts and reorder points
- Stock reconciliation and counting support

**Reports**:
- **Stock Report (Existencias)**:
  - Current inventory levels by product
  - Breakdown by warehouse
  - Available vs. reserved quantities
  - Reorder point status
  - Stock value (cost × quantity)
- **Movement Report (Movimientos)**:
  - All product entries and exits
  - Filter by product, warehouse, date range, movement type
  - Full audit trail with user and reference document
  - Running balance after each movement

---

### 💼 Purchases Module
**Status**: 🚧 **PLANNED**  
**Purpose**: Purchase order management to increase warehouse stock

**Aggregate Root**: Purchase

Purchase management with:
- **Core Entity**: Purchase order linked to suppliers
- **Sub-entities**:
  - **Purchase Lines**: Individual items (linked to products or free-form)
    - Product reference (optional) - updates product cost when received
    - Quantity, unit price, tax rate, discount
    - Line total calculation
  - **Payment Due Dates (Vencimientos)**: Payment schedule with multiple installments
    - Amount, due date, status (pending, paid, overdue)
  - **Payments**: Payment records with traceability
    - Payment date, amount, method, reference number

**Key Features**:
- Supplier purchase order creation
- Product association (optional for one-time items)
- Stock impact upon receipt
- Payment obligation tracking
- Cost price updates for products
- Purchase approval workflow

---

## 📚 Module Documentation

Each module has detailed documentation in `docs/modules/`:

- ✅ [AUTH.md](./modules/AUTH.md) - Authentication & Authorization
- ✅ [CUSTOMERS.md](./modules/CUSTOMERS.md) - Customer Management
- ✅ [PRODUCTS.md](./modules/PRODUCTS.md) - Product Catalog with Composite Products & UOM
- ✅ [WAREHOUSE.md](./modules/WAREHOUSE.md) - Warehouse & Stock Management
- ✅ [DIFFERENTIAL.md](./modules/DIFFERENTIAL.md) - Differential/Adjustments Module
- 🚧 ORDERS.md - Order Management (planned)
- 🚧 INVOICES.md - Invoicing System (planned)
- 🚧 WAREHOUSES.md - Warehouse Management (planned)
- 🚧 PURCHASES.md - Purchase Management (planned)
- 🚧 SUPPLIERS.md - Supplier Management (planned)

**Documentation Standard**: Each module documentation includes:
1. Overview and purpose
2. Complete domain model (entities, value objects, enums)
3. API endpoints with request/response examples
4. DTOs with validation rules
5. Use cases
6. Repository interfaces
7. Business rules summary
8. Database schema
9. Exception handling
10. Developer notes and testing requirements

---

## 🛠️ Technology Stack

### Backend Framework
- **Java 24**: Latest Java LTS version
- **Spring Boot 3.5.5**: Modern Spring framework
- **Spring Data JPA**: Database abstraction
- **Spring Security**: Authentication & Authorization
- **Spring Validation**: Request validation

### Database
- **MySQL 8**: Relational database
- **Flyway**: Database migrations
- **H2**: In-memory database for testing

### Security
- **JWT (JSON Web Tokens)**: Stateless authentication
- **BCrypt**: Password hashing
- **Spring Security**: Authorization framework

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework
- **Testcontainers**: Integration testing with Docker
- **H2 Database**: In-memory database for tests

### Build & Dependencies
- **Maven**: Build automation tool
- **Maven Wrapper**: Embedded Maven version

### API Documentation
- **SpringDoc OpenAPI**: Swagger/OpenAPI 3.0 documentation
- **Swagger UI**: Interactive API documentation

### DevOps
- **Docker**: Containerization
- **Docker Compose**: Multi-container orchestration
- **GitLab CI/CD**: Continuous Integration/Deployment

### Development Tools
- **Lombok** (optional): Reduce boilerplate (used minimally)
- **Jakarta Validation**: Bean validation

---

## 🏗️ Development Workflow

### 1. Module Development Process

When implementing a new module, follow this order:

1. **Documentation First**
   - Create `docs/modules/[MODULE].md`
   - Define domain model, entities, value objects
   - Design API endpoints with examples
   - Document DTOs and business rules

2. **Domain Layer**
   - Create value objects (self-validating, immutable)
   - Create domain entities (aggregate roots first)
   - Implement domain services if needed
   - Define repository ports (interfaces)
   - Define domain events

3. **Application Layer**
   - Create request DTOs with validation
   - Create response DTOs (List, Detail, Create)
   - Create mappers (DTO ↔ Domain)
   - Implement use cases
   - Define exception classes

4. **Infrastructure Layer**
   - Create JPA entities (separate from domain)
   - Create JPA repositories (implement domain ports)
   - Create REST controllers
   - Create exception handlers
   - Add security configuration

5. **Testing**
   - Unit tests for value objects (validation)
   - Unit tests for entities (business logic)
   - Unit tests for use cases (mocked repositories)
   - Integration tests for controllers (with Testcontainers)
   - Test coverage target: 80%+

6. **Database Migration**
   - Create Flyway migration script
   - Test migration on clean database
   - Document schema in module documentation

### 2. Git Workflow

- **Branch Strategy**: Feature branches
- **Commit Messages**: Conventional Commits format
- **Pull Requests**: Required for main branch
- **Code Review**: At least one approval required

### 3. Testing Strategy

**Testing Pyramid**:
- 70% Unit Tests (domain, use cases)
- 20% Integration Tests (controllers, repositories)
- 10% E2E Tests (complete workflows)

**What to Test**:
- ✅ All value object validations
- ✅ All domain entity business methods
- ✅ All use case logic
- ✅ All API endpoints (happy path + error cases)
- ✅ All database operations
- ✅ All security rules (authentication, authorization)

---

## 📊 Quality Standards

### Code Quality
- **Clean Code**: Self-documenting, readable code
- **SOLID Principles**: Applied consistently
- **DRY**: No code duplication
- **YAGNI**: Don't implement what you don't need yet
- **KISS**: Keep it simple

### Documentation Quality
- **English Only**: All documentation in English
- **No Code Comments**: Documentation in .md files, not code
- **Up-to-Date**: Sync docs with code changes
- **Examples**: Provide JSON examples for all DTOs
- **Diagrams**: Use Mermaid for complex flows

### Testing Quality
- **Code Coverage**: Minimum 80%
- **Test Names**: Descriptive, follow pattern
- **Fast Tests**: Unit tests < 1s, integration tests < 5s
- **Isolated Tests**: No test dependencies
- **Repeatable**: Tests pass consistently

### API Quality
- **RESTful**: Follow REST conventions
- **Consistent**: Same patterns across modules
- **Versioned**: API version in URL (/api/v1)
- **Documented**: OpenAPI/Swagger documentation
- **Error Handling**: Consistent error responses

---

## 🔍 Common Patterns

### Naming Conventions

**Packages**:
- `domain.entities`: PascalCase (Customer, Order)
- `domain.valueobjects`: PascalCase (Email, Money)
- `application.dto.request`: PascalCaseRequest (CreateCustomerRequest)
- `application.dto.response`: PascalCaseResponse (CustomerDetailResponse)
- `application.usecases`: VerbNounUseCase (CreateOrderUseCase)
- `infrastructure.persistence`: JpaPascalCase (JpaCustomerRepository)

**Methods**:
- Use cases: `execute(request)`
- Repositories: `save`, `findById`, `findAll`, `delete`
- Mappers: `toDto`, `toDomain`, `toEntity`

**Tests**:
- Unit: `ClassNameUnitTest`
- Integration: `ClassNameIntegrationTest`
- Method: `methodName_condition_expectedResult`

### Exception Handling

**Exception Hierarchy**:
```
RuntimeException
├── DomainException (base for domain exceptions)
│   ├── CustomerNotFoundException
│   ├── InsufficientStockException
│   └── InvalidEmailException
└── ApplicationException (base for application exceptions)
    ├── InvalidCredentialsException
    └── TokenExpiredException
```

**HTTP Status Mapping**:
- 400 Bad Request: Validation errors
- 401 Unauthorized: Authentication failed
- 403 Forbidden: Authorization failed
- 404 Not Found: Resource not found
- 409 Conflict: Duplicate resource
- 422 Unprocessable Entity: Business rule violation
- 500 Internal Server Error: Unexpected errors

---

## 📈 Future Improvements

### Short Term (Q1 2025)
- [ ] Complete Products module
- [ ] Complete Orders module
- [ ] Complete Invoices module
- [ ] Refactor DTOs into request/response subpackages
- [ ] Add API versioning (/api/v1)

### Medium Term (Q2 2025)
- [ ] Implement Warehouses module
- [ ] Implement Purchases module
- [ ] Add event-driven architecture (domain events)
- [ ] Add caching layer (Redis)
- [ ] Performance optimization

### Long Term (Q3-Q4 2025)
- [ ] Multi-tenancy support
- [ ] Advanced reporting system
- [ ] Real-time notifications (WebSocket)
- [ ] Mobile app integration
- [ ] Analytics dashboard

---

[← Back to Main README](../README.md)
