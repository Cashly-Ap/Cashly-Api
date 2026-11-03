# Contrato — Dominios de permiso por caso de uso

Fuente única de verdad para el refactor. Todo código (backend y frontend) debe seguir esta tabla.

## Acciones

`PermissionAction` = `READ, CREATE, UPDATE, DELETE, EXPORT, IMPORT, EXECUTE, CONFIRM, CANCEL`

- `READ` = ver listados y detalles.
- `CREATE` = crear.
- `UPDATE` = editar.
- `DELETE` = eliminar.
- `EXPORT` = descargar PDF / reportes.
- `EXECUTE` = operaciones de ejecución (abrir/cerrar caja, movimientos de stock).
- `CONFIRM` = confirmar o convertir (confirmar pedido/compra/factura, convertir presupuesto).
- `CANCEL` = anular / devolver / reversar.

## Regla de independencia

Se elimina el prerrequisito "READ obligatorio para cualquier acción". Cada `dominio:acción` es independiente: un rol puede tener `SALES_QUOTES:CONFIRM` sin `SALES_QUOTES:READ`. Las pantallas que necesiten listados exigen `READ` en el guard de su propia ruta.

## Dominios

| Dominio | Acciones | Cubre |
|---|---|---|
| `COMPANY` | READ, UPDATE | Datos de la empresa y branding |
| `USERS` | READ, CREATE, UPDATE, DELETE | Usuarios |
| `ROLES` | READ, CREATE, UPDATE, DELETE | Roles y sus permisos |
| `DEPARTMENTS` | READ, CREATE, UPDATE, DELETE | Departamentos |
| `CATALOG_PRODUCTS` | READ, CREATE, UPDATE, DELETE | Productos |
| `CATALOG_CATEGORIES` | READ, CREATE, UPDATE, DELETE | Categorías |
| `CATALOG_PRICES` | READ, CREATE, UPDATE, DELETE | Tipos de precio y precios |
| `INVENTORY_WAREHOUSES` | READ, CREATE, UPDATE, DELETE | Almacenes |
| `SALES_QUOTES` | READ, CREATE, UPDATE, DELETE, CONFIRM, EXPORT | Presupuestos |
| `SALES_ORDERS` | READ, CREATE, UPDATE, DELETE, CONFIRM, CANCEL, EXPORT | Pedidos |
| `CONTACTS_CUSTOMERS` | READ, CREATE, UPDATE, DELETE | Clientes y su saldo |
| `FINANCE_BILLING` | READ, CREATE, UPDATE, DELETE, CONFIRM, CANCEL, EXPORT | Facturas, devoluciones, notas de crédito/débito |
| `FINANCE_TREASURY` | READ, EXECUTE | Cuentas por cobrar y caja |
| `FINANCE_PAYMENT_METHODS` | READ, CREATE, UPDATE, DELETE | Métodos de pago |
| `PURCHASES` | READ, CREATE, UPDATE, DELETE, CONFIRM, CANCEL, EXPORT | Compras |
| `CONTACTS_SUPPLIERS` | READ, CREATE, UPDATE, DELETE | Proveedores |
| `DOCUMENTS` | READ, EXPORT | Documentos globales y reportes globales |
| `DASHBOARD` | READ | Dashboard |

## Migración de grants existentes (`auth_role_permissions`)

| Grant viejo | Grants nuevos |
|---|---|
| `COMPANY:*` | `COMPANY:*` |
| `USERS:READ` | `USERS:READ`, `ROLES:READ`, `DEPARTMENTS:READ` |
| `USERS:CREATE/UPDATE/DELETE` | `USERS:x`, `ROLES:x`, `DEPARTMENTS:x` |
| `CATALOG:READ/CREATE/UPDATE/DELETE` | `CATALOG_PRODUCTS:x`, `CATALOG_CATEGORIES:x`, `CATALOG_PRICES:x` |
| `INVENTORY:CREATE/UPDATE/DELETE` | `INVENTORY_WAREHOUSES:x` |
| `SALES:READ` | `SALES_QUOTES:READ`, `SALES_ORDERS:READ`, `CONTACTS_CUSTOMERS:READ` |
| `SALES:CREATE/UPDATE/DELETE` | `SALES_QUOTES:x`, `SALES_ORDERS:x`, `CONTACTS_CUSTOMERS:x` |
| `SALES:EXECUTE` | `SALES_QUOTES:CONFIRM`, `SALES_ORDERS:CONFIRM`, `SALES_ORDERS:CANCEL` |
| `SALES:EXPORT` | `SALES_QUOTES:EXPORT`, `SALES_ORDERS:EXPORT` |
| `FINANCES:READ` | `FINANCE_BILLING:READ`, `FINANCE_TREASURY:READ`, `FINANCE_PAYMENT_METHODS:READ` |
| `FINANCES:CREATE/UPDATE/DELETE` | `FINANCE_BILLING:x`, `FINANCE_PAYMENT_METHODS:x` |
| `FINANCES:EXECUTE` | `FINANCE_BILLING:CONFIRM`, `FINANCE_BILLING:CANCEL`, `FINANCE_TREASURY:EXECUTE` |
| `PURCHASES:READ` | `PURCHASES:READ`, `CONTACTS_SUPPLIERS:READ` |
| `PURCHASES:CREATE/UPDATE/DELETE` | `PURCHASES:x`, `CONTACTS_SUPPLIERS:x` |
| `PURCHASES:EXECUTE` | `PURCHASES:CONFIRM`, `PURCHASES:CANCEL` |
| `PURCHASES:EXPORT` | `PURCHASES:EXPORT` |
| `DOCUMENT_ADMIN:READ/EXPORT` | `DOCUMENTS:READ/EXPORT` |
| `DASHBOARD:READ` | `DASHBOARD:READ` |

`IMPORT` no se usa. Si un grant viejo combina acciones, se expande por acción.

## Mapeo de endpoints (guía)

El dominio se elige por **la entidad que se toca**, no por la URL:

| Área | Dominio y acción |
|---|---|
| `/api/company/**`, branding | `COMPANY:READ/UPDATE` |
| `/api/users/**` | `USERS:…` |
| `/api/roles/**` | `ROLES:…` |
| `/api/departments/**` | `DEPARTMENTS:…` |
| `/api/products/**`, `/api/categories/**`, `/api/price-types/**` | `CATALOG_PRODUCTS/CATEGORIES/PRICES:…` |
| `/api/warehouses/**` | `INVENTORY_WAREHOUSES:…` |
| `/api/quotes/**` (crear/editar/eliminar) | `SALES_QUOTES:CREATE/UPDATE/DELETE`, `READ` para listado/detalle |
| `POST /api/quotes/{id}/convert-to-*` | `SALES_QUOTES:CONFIRM` + `FINANCE_BILLING:CREATE` o `SALES_ORDERS:CREATE` (según destino) |
| `/api/orders/**` (confirmar/cancelar) | `SALES_ORDERS:CONFIRM/CANCEL` |
| `/api/customers/**` | `CONTACTS_CUSTOMERS:…`; saldo `FINANCE_TREASURY:READ` |
| `/api/invoices/**`, notes, returns | `FINANCE_BILLING:…` (confirmar/anular = CONFIRM/CANCEL) |
| receivables, cash-register | `FINANCE_TREASURY:READ/EXECUTE` |
| payment-methods | `FINANCE_PAYMENT_METHODS:…` |
| `/api/purchases/**` | `PURCHASES:…` |
| `/api/suppliers/**` | `CONTACTS_SUPPLIERS:…` |
| `/api/documents/**`, reportes globales | `DOCUMENTS:READ/EXPORT` |
| `/api/dashboard/**`, analytics | `DASHBOARD:READ` |


## Catálogo para la UI

El catálogo expone **áreas → dominios → permisos**, para que el editor de roles muestre:

```
SALES                                   ← área
├── Presupuestos (SALES_QUOTES)         ← dominio
│   ├── Ver ............. READ
│   ├── Crear ........... CREATE
│   ├── Editar .......... UPDATE
│   ├── Eliminar ........ DELETE
│   ├── Confirmar ....... CONFIRM
│   └── Exportar ........ EXPORT
├── Pedidos (SALES_ORDERS) …
└── Clientes (CONTACTS_CUSTOMERS) …
```

Contrato de `GET /api/permissions/catalog`:

```json
[
  {
    "area": "SALES",
    "labelKey": "platform.auth.areas.sales",
    "sortOrder": 60,
    "domains": [
      {
        "module": "SALES_QUOTES",
        "labelKey": "platform.auth.domains.sales_quotes",
        "sortOrder": 10,
        "permissions": [
          { "action": "READ", "labelKey": "platform.auth.permissions.sales_quotes.read", "descriptionKey": "platform.auth.permissions.sales_quotes.read.description", "sortOrder": 10 }
        ]
      }
    ]
  }
]
```

Reglas: cada `(dominio, acción)` aparece exactamente una vez; `domains` y `permissions` ordenados por `sortOrder`; el catálogo se valida contra `PermissionRegistry` al arrancar.
