# Users, roles, departments and access control

## Purpose and verification status

The implementation separates user identity, reusable role permissions and organizational departments. Source ownership is `platform/auth/{user,role,department,authorization,authentication,token}`. This reference was checked against the current implementation, including the 131-code permission contract and the Docker test suite.

## Domain model

| Record | Fields and constraints |
|---|---|
| User profile | Required first/last names, trimmed 1–100 characters; optional address; one phone number |
| Address | Optional street (200), city/region/country (100 each), postalCode (20) |
| Phone | One nullable `phone` string; 7–15 digits with optional leading `+`; presentation separators normalized |
| User assignment | Exactly one active assignable role and active department, saved atomically with profile |
| Role | UUID, unique trimmed name (1–100), description (500), active, optimistic version, permission codes |
| Department | UUID, unique trimmed name (1–100), description (500), active, optimistic version; grants no permissions |

Role names carry no authority. The immutable `SYSTEM_ADMIN` system key identifies the protected recovery role; its effective grants are computed from the server registry. Custom roles configure any supported permission code. Empty grants deny access. Role writes use a flat set of permission codes; there is no per-user grant table.

## Permission model

Each permission is a single code composed as `MODULE_ACTION`, for example:

```text
CATALOG_PRODUCTS_DELETE
FINANCE_TREASURY_RECEIVABLES_PAY_DIRECT
COMPANY_EDIT
```

`PermissionModule` defines 21 modules. `PermissionAction` defines 35 actions. `PermissionRegistry` declares exactly 131 supported `module/action` pairs and `PermissionCatalog` classifies each pair exactly once.

| Area | Submodule | Modules |
|---|---|---|
| SALES | — | `SALES_QUOTES`, `SALES_ORDERS` |
| PURCHASES | — | `PURCHASES` |
| CATALOG | — | `CATALOG_PRODUCTS`, `CATALOG_CATEGORIES`, `CATALOG_PRICES` |
| INVENTORY | — | `INVENTORY_WAREHOUSES` |
| FINANCE | `FINANCE_BILLING` | `FINANCE_BILLING_INVOICES`, `FINANCE_BILLING_RETURNS`, `FINANCE_BILLING_CREDIT_NOTES`, `FINANCE_BILLING_DEBIT_NOTES` |
| FINANCE | `FINANCE_TREASURY` | `FINANCE_TREASURY_CASH_REGISTER`, `FINANCE_TREASURY_RECEIVABLES`, `FINANCE_TREASURY_PAYMENT_METHODS` |
| CONTACTS | — | `CONTACTS_CUSTOMERS`, `CONTACTS_SUPPLIERS` |
| ANALYTICS | — | `DASHBOARD` |
| PLATFORM | — | `USERS`, `ROLES`, `DEPARTMENTS`, `COMPANY` |

The catalog and registry must stay aligned. Boot fails when a registered pair has no catalog entry or a catalog entry is not registered.

## API contracts

All endpoints require authentication unless explicitly noted. Management reads require the matching `VIEW_LIST` or `VIEW_DETAIL` code; writes require `CREATE`, `EDIT` or `DELETE`. Options expose only minimal active records and require user administration eligibility; role options additionally enforce the actor's delegation ceiling.

| Method and path | Contract |
|---|---|
| GET `/api/users` | Paged list; `roleId` UUID filter, search, active and pagination |
| GET `/api/users/{id}` | Detailed profile, role/department identifiers and display names |
| POST `/api/users` | Create identity, profile and required assignments |
| PUT `/api/users/{id}` | Atomic profile/assignment/status update |
| DELETE `/api/users/{id}` | Delete subject to associated-record, target privilege and administrator-continuity safeguards |
| PUT `/api/users/{id}/email` | Authorized administrative email change with target privilege check |
| PUT `/api/users/{id}/password` | Authorized administrative password change with target privilege check |
| GET/POST `/api/roles` | List/create roles |
| GET/PUT/DELETE `/api/roles/{id}` | Read/update/delete role; updates include version |
| GET `/api/roles/options` | Assignable active role IDs/names |
| GET `/api/permissions/catalog` | Area → submodule → module → permission catalog with i18n keys and ordering; requires `ROLES_VIEW_LIST` |
| GET/POST `/api/departments` | List/create departments |
| GET/PUT/DELETE `/api/departments/{id}` | Read/update/delete department; updates include version |
| GET `/api/departments/options` | Active department IDs/names |
| GET `/api/auth/access` | Current role identity, department and effective `permissions: string[]` |
| POST `/api/auth/register` | Protected compatibility creation endpoint; requires `USERS_CREATE` |
| PATCH `/api/auth/change-password` | Own-account change with current password, without requiring user administration |

User create accepts `email`, `userName`, `password`, `firstName`, `lastName`, `address`, `phone`, `roleId`, `departmentId`. User update accepts `userName`, profile/reference fields, `active`, `emailVerified`. Email and credentials are edited separately. Unknown user request properties, including direct grants and the old writable role enum, are rejected. `/api/users/{id}/module-permissions` is retired.

Role writes use `{name, description, active, version, permissions}` where `permissions` is an array of permission codes. Department writes omit permissions. Creation ignores an incoming version and starts at zero; updates must supply the fetched version. Protected system roles and referenced role/department deletion or deactivation return conflicts. Validation returns 400, unauthenticated requests 401, insufficient access 403, missing references 404 and lifecycle/version conflicts 409.

## Enforcement and concurrency

JWT identity is verified and current account/role grants are loaded from `auth_role_permissions` for every authenticated request. The JWT contains identity claims only; it does not contain role/module grants and stored claims never authorize an operation. Method annotations declare explicit permission codes; an interceptor rejects unmapped API handlers except the named authentication/notification self-service operations.

Every endpoint declares its exact code. Stock-aware transaction lookups additionally require `INVENTORY_WAREHOUSES_VIEW_STOCK`. Stock-changing operations declare `INVENTORY_WAREHOUSES_RECORD_ENTRY` or `INVENTORY_WAREHOUSES_RECORD_EXIT`. Reflection coverage tests fail when a controller declares a code that is absent from the registry.

Administrative mutations take a shared database administration-row lock under READ_COMMITTED before authorization/state reads. Role delegation cannot exceed the actor's grants; system role assignment requires an existing system administrator. Target-account checks apply to profile, deletion and credential administration, including inactive targets. At least one active system administrator must remain.

## Persistence, seeds and tests

Flyway V2 adds `auth_roles`, `auth_role_permissions`, `auth_departments` and the administration lock. `auth_role_permissions` stores `(role_id, module, action)` pairs; the effective permission code is `module + "_" + action`. V3 adds `users`; the retired `user_module_permissions` table is not part of the schema. The two permissions without an enforcing endpoint were removed from the registry before the contract freeze.

The system administrator seed derives all 131 codes from the registry. The operator seed grants a read/payment subset. Permission labels and descriptions are available in English and Spanish under `platform/auth/authorization/i18n/permissions_{en,es}.properties`.

Domain, management, authorization, request and controller-coverage tests accompany this change. The suite covers the 131-code contract, catalog translation parity, endpoint authority registration, role delegation and administrator-continuity checks.

## Permission catalog

`PermissionCatalog` describes every permission registered by `PermissionRegistry` as `area → submodule → module → permission`. Each `module/action` pair is classified exactly once; boot fails when the catalog and the registry diverge.

The role editor renders that catalog as one section per area, one submodule block for Finance, one module block per module and one row per permission with its business label. The protected system administrator role renders every permission as allowed and stays read-only. There is no standalone permission screen: permissions are inspected and edited where roles are edited.
