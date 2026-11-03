# Cashly backend architecture

The backend is a modular Spring Boot application. Business areas and capabilities precede technical layers in package names. It remains a single deployable service and a single MySQL database; these packages do not imply separate services or transactions.

## Business areas and capabilities

| Area | Capability packages |
| --- | --- |
| `platform/auth` | `authentication`, `token`, `mfa`, `user`, `role`, `department`, `authorization`, `preferences` |
| `platform/company` | Company settings, logos and PDF branding |
| `platform/notifications` | Notifications and WebSocket delivery |
| `partners/customers` | `profile`, `balance`, `credit`, `warehouse_access` |
| `partners/suppliers` | Supplier master data |
| `catalog/products` | Products and `reporting` |
| `catalog/categories` | Categories |
| `catalog/price_types` | Price types |
| `inventory/warehouses` | `management`, `stock`, `reporting` |
| `sales/quotes` | Quotes and `reporting` |
| `sales/orders` | Orders and `reporting` |
| `procurement/purchases` | Purchases and `reporting` |
| `finance/billing/invoices` | Invoices and `reporting` |
| `finance/billing/returns` | Returns |
| `finance/billing/credit_notes` | Credit notes |
| `finance/billing/debit_notes` | Debit notes |
| `finance/treasury/cash_register` | Cash register sessions and `reporting` |
| `finance/treasury/payments` | Payment collection and `reporting` |
| `finance/treasury/receivables` | Receivables and `reporting` |
| `finance/treasury/payment_methods` | Payment methods |
| `analytics/dashboard` | Dashboard queries |

Warehouse lifecycle belongs to `management`; quantities, reservations and movements belong to `stock`. Customer identity and commercial terms belong to `profile`; balances belong to `balance`; credit status belongs to `credit`; warehouse entitlements belong to `warehouse_access`.

## Package structure

```text
com/cashly/cashly_api/
├── analytics/dashboard/{application,infrastructure}/
├── catalog/
│   ├── products/{domain,application,infrastructure,reporting}/
│   ├── categories/{domain,application,infrastructure}/
│   └── price_types/{domain,application,infrastructure}/
├── finance/
│   ├── billing/{invoices,returns,credit_notes,debit_notes}/
│   └── treasury/{cash_register,payments,receivables,payment_methods}/
├── inventory/warehouses/{management,stock,reporting}/
├── partners/
│   ├── customers/{profile,balance,credit,warehouse_access}/
│   └── suppliers/{application,domain,infrastructure}/
├── platform/
│   ├── auth/{authentication,token,mfa,user,role,department,authorization,preferences}/
│   ├── company/{application,domain,infrastructure}/
│   └── notifications/{application,domain,infrastructure}/
├── procurement/purchases/{application,domain,infrastructure,reporting}/
├── sales/
│   ├── quotes/{application,domain,infrastructure,reporting}/
│   └── orders/{application,domain,infrastructure,reporting}/
├── shared/
├── infrastructure/{config,seed,web}/
└── CashlyApplication.java
```

Braced paths describe populated layers, not a requirement to create empty folders. Tests follow the package of the production responsibility they exercise.

### Domain

`domain/entities`, `domain/valueobjects`, `domain/services`, `domain/events` and `domain/repositories` contain business types and persistence contracts. Domain code does not depend on application or infrastructure. Repository implementations and JPA entities belong to infrastructure.

### Application

Operations use named packages such as `application/create`, `register`, `update`, `delete`, `get`, `list` or a specific business action. Operation-specific requests, results and failures live with that action. Shared DTOs, mappers, exceptions and query implementations retain explicit shared roles when multiple operations use them.

| Package | Responsibility |
| --- | --- |
| `application/ports/in` | Public capabilities offered by the owner |
| `application/ports/out` | Consumer-owned external capabilities |
| `application/queries` | Read-capability implementations |
| `application/dto/query` | Immutable query parameters |
| `application/<operation>` | A business operation and its types |
| `application/services`, `mappers`, `exceptions`, `dto` | Shared application responsibilities |

### Infrastructure

Adapters use role-based paths: `infrastructure/web` for HTTP controllers, `integration` for other-module adapters, `security` for authentication adapters, `config` for wiring, and `persistence/{entities,repositories,adapters,queries,mappers,projections}` for storage. PDF and Excel generators live under `pdf` and `excel` where they belong.

## Controllers and composition

A controller belongs to the capability responsible for its HTTP operation. Routing is under `/api/...`; the URL prefix does not dictate Java package ownership. Report controllers live in the owner's `reporting` submodule.

## Authorization

Every HTTP operation declares an explicit `@PreAuthorize` permission code:

```java
@PreAuthorize("hasAuthority('CATALOG_PRODUCTS_DELETE')")
```

- `PermissionRegistry` declares 131 codes as `MODULE_ACTION`.
- `PermissionCatalog` groups the same codes by area, submodule and module.
- `JwtAuthenticationFilter` loads current grants from `auth_role_permissions` on every request.
- The JWT carries identity claims only; it does not carry role or module grants.
- An interceptor rejects controller operations without an explicit mapping.

See [ACCESS_CONTROL.md](modules/ACCESS_CONTROL.md).

## Dependency rules

Dependencies point inward: infrastructure may use application and domain; application may use domain and its own ports; domain cannot use either outer layer. Cross-module capabilities should be accessed through provider input ports and consumer output ports, connected by infrastructure adapters.

The source still contains pre-existing cross-module internal dependencies. Their historical baseline is archived under `openspec/changes/archive`; removing them requires separate behavior-preserving work.

## Verification

These guards enforce package roles, direction and capability placement:

- `ApplicationPackageConventionTest`
- `LayerDependencyTest`
- `CapabilityStructureTest`
- `ReportingOwnershipTest`
- `CustomerWarehouseBoundaryTest`

Run the isolated Docker verification used by CI with:

```bash
bash scripts/run-structure-verification.sh final
```

The current baseline and archived evidence live under `openspec/changes/archive`. Historical change folders are not current implementation guidance.
