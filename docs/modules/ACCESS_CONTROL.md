# Users, roles, departments and access control

## Purpose and verification status

The implementation separates user identity, reusable role permissions and organizational departments. Source ownership is `platform/auth/{user,role,department,authorization,authentication,token}`. This reference was checked against the implementation; container compilation, integration tests and fresh/reset database smoke checks remain pending because the session cannot access Docker.

## Domain model

| Record | Fields and constraints |
|---|---|
| User profile | Required first/last names, trimmed 1–100 characters; optional address; one phone number |
| Address | Optional street (200), city/region/country (100 each), postalCode (20) |
| Phone | One nullable `phone` string; 7–15 digits with optional leading `+`; presentation separators normalized |
| User assignment | Exactly one active assignable role and active department, saved atomically with profile |
| Role | UUID, unique trimmed name (1–100), description (500), active, optimistic version, permission matrix |
| Department | UUID, unique trimmed name (1–100), description (500), active, optimistic version; grants no permissions |

Role names carry no authority. The immutable `SYSTEM_ADMIN` system key identifies the protected recovery role; its effective grants are computed from the server registry. Custom roles may independently configure any module's supported actions. Empty grants deny access; every nonempty module grant must include READ. Unsupported combinations are rejected.

| Modules | Supported actions |
|---|---|
| USERS, ROLES, DEPARTMENTS | READ, CREATE, UPDATE, DELETE |
| CATALOG | READ, CREATE, UPDATE, DELETE, EXPORT |
| INVENTORY, SALES, FINANCES, PURCHASES | READ, CREATE, UPDATE, DELETE, EXPORT, EXECUTE |
| DOCUMENT_ADMIN | READ, EXPORT |
| DASHBOARD | READ |

IMPORT is reserved in the action type and is not currently exposed as a supported cell because no import operation exists. Inventory-denied/catalog-read-only is a seed example; it is not a special authorization policy.

## API contracts

All endpoints require authentication unless explicitly noted. Management reads require the matching module READ; writes require its CREATE, UPDATE or DELETE. Options expose only minimal active records and require user creation/update eligibility; role options additionally enforce the actor's delegation ceiling.

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
| GET `/api/roles/registry` | Supported module/action cells |
| GET `/api/permissions/catalog` | Module → group → permission catalog with i18n keys and ordering; requires ROLES READ |
| GET/POST `/api/departments` | List/create departments |
| GET/PUT/DELETE `/api/departments/{id}` | Read/update/delete department; updates include version |
| GET `/api/departments/options` | Active department IDs/names |
| GET `/api/auth/access` | Current role identity, department and effective permission matrix |
| POST `/api/auth/register` | Protected compatibility creation endpoint; requires USERS CREATE |
| PATCH `/api/auth/change-password` | Own-account change with current password, without requiring user administration |

User create accepts `email`, `userName`, `password`, `firstName`, `lastName`, `address`, `phone`, `roleId`, `departmentId`. User update accepts `userName`, profile/reference fields, `active`, `emailVerified`. Email and credentials are edited separately. Unknown user request properties, including direct grants and the old writable role enum, are rejected. `/api/users/{id}/module-permissions` is retired.

Role writes use `{name, description, active, version, permissions}` where permissions maps module codes to action arrays. Department writes omit permissions. Creation ignores an incoming version and starts at zero; updates must supply the fetched version. Protected system roles and referenced role/department deletion or deactivation return conflicts. Validation returns 400, unauthenticated requests 401, insufficient access 403, missing references 404 and lifecycle/version conflicts 409.

## Enforcement and concurrency

JWT identity is verified and current account/role grants are loaded for every authenticated request. Stored JWT role/module claims never authorize an operation. Method annotations declare explicit module/action grants; an interceptor rejects unmapped API handlers except the named authentication/notification self-service operations. Product file reads require CATALOG READ and use private, noncacheable responses.

Reports require the owning module EXPORT and DOCUMENT_ADMIN EXPORT. Stock-aware product transaction lookup additionally requires INVENTORY READ. Cross-owner mutations declare additional grants, including inventory execution for stock-changing invoice/return operations. The exact controller inventory is maintained in the change's `endpoint-permission-inventory.json`; runtime reflection coverage tests guard missing mappings.

Administrative mutations take a shared database administration-row lock under READ_COMMITTED before authorization/state reads. Role delegation cannot exceed the actor's grants; system role assignment requires an existing system administrator. Target-account checks apply to profile, deletion and credential administration, including inactive targets. At least one active system administrator must remain.

## Persistence, seeds and tests

Flyway V2 adds `auth_roles`, `auth_role_permissions`, `auth_departments` and the administration lock; V3 adds `users` and the legacy direct-grant table. Existing legacy enum/direct-grant storage remains compatibility data and no longer authorizes access.

See [the disposable beta dataset guide](../../db/README.md) for the explicit reset script, deterministic users/roles/departments and integrity checks. A planning or implementation session does not automatically reset the database.

Domain, management, authorization, request and controller-coverage tests accompany this change. The isolated real-HTTP startup suite includes custom-role access and administrator credential-escalation denial checks. Tests are written; successful execution is not claimed until evidence is recorded.

## Permission catalog and matrix

`PermissionCatalog` describes every permission registered by `PermissionRegistry` as `module → group → permission`. Each `module:action` pair is classified exactly once; boot fails when the catalog and the registry diverge, so the description can never drift from enforcement. Groups classify grants for display only: they carry no grants of their own, and a module-level grant (for example `FINANCES:EXECUTE`) still covers every group of that module.

The permissions tab of the role editor renders that catalog: one section per module with the existing access presets, one block per group and one row per permission showing its business label and its allowed or denied state. Denied rows stay visible so the editor never hides a gap, and rows outside the actor's grants are not editable. The protected system administrator role renders every permission as allowed and stays read-only. There is no standalone permission screen: permissions are inspected and edited where roles are edited.
