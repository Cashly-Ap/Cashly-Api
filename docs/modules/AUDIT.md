# Auditoría (Audit Trail)

Módulo transversal `platform/audit` que registra eventos inmutables de negocio para
saber **quién** hizo **qué**, **cuándo** y **sobre qué** registro.

## Arquitectura

```
platform/audit/
├── domain/            entidades, value objects, repositorio y excepciones
├── application/       puertos in/out, use cases, DTOs, mappers y diff
└── infrastructure/    JPA, consultas, actor gateway, controller y configuración
```

Los demás módulos dependen únicamente de
`platform.audit.application.ports.in.AuditRecorder` (igual que con
`ActivityNotificationRecorder`).

## Escritura

### Instrumentación con `@Audited`

Los use cases mutantes se anotan con `@Audited`:

```java
@Transactional
@Audited(module = "sales", entityType = "ORDER", action = "CREATE")
public OrderDetailResponse execute(CreateOrderRequest request,
        UUID authenticatedUserId) {
    ...
}
```

`AuditAspect` intercepta la ejecución, extrae el `entityId` y la etiqueta del
resultado (o de los argumentos) y registra el evento solo si la operación
termina correctamente. Si el use case lanza una excepción, no se audita.

Esto cubre todos los lotes:

- **A**: facturas, notas de crédito/débito, devoluciones.
- **B**: presupuestos, pedidos, compras.
- **C**: pagos, cobranzas, caja.
- **D**: catálogo e inventario.
- **E**: clientes, proveedores, usuarios/roles/departamentos, empresa.

Los casos con acción dinámica (`ManageRoles`, `ManageDepartments`,
`CompanyService`) usan `AuditRecorder.record(module, entityType, id, label,
action)` explícitamente.

### Registro manual

```java
auditRecorder.record(AuditEntry.of(
        "finance",
        AuditEntityType.INVOICE,
        invoiceId,
        invoiceNumber,
        AuditAction.CONFIRM));
```

Reglas:

- Un evento por intención de usuario, en el use case de borde.
- Registrar después de persistir el agregado y dentro de la misma transacción.
- Mantener la llamada a `ActivityNotificationRecorder`: son responsabilidades
  distintas.
- `actorId` nulo resuelve `CurrentActor`; si no hay actor queda `Sistema`.
- El nombre y la foto del actor se guardan como snapshot.
- No se persisten diffs de campos ni líneas; el evento es un registro simple de
  acción, entidad, actor y metadata.

## Lectura

| Método | Ruta | Permiso |
|---|---|---|
| GET | `/api/audit-events` | `AUDIT_TRAIL_VIEW_LIST` |
| GET | `/api/audit-events/filter-options` | `AUDIT_TRAIL_VIEW_LIST` |
| GET | `/api/audit-events/{id}` | `AUDIT_TRAIL_VIEW_LIST` |

Parámetros de `GET /api/audit-events`:

| Parámetro | Obligatorio | Notas |
|---|---|---|
| `entityType` | Sí, salvo modo global | `INVOICE`, `ORDER`, ... |
| `entityId` | Sí con `entityType` | Registro concreto |
| `module` | No | Modo menubar global |
| `action` | No | Multiselección |
| `actorId` | No | Filtro por usuario |
| `from` / `to` | No | Fechas ISO (`YYYY-MM-DD`) |
| `page` / `size` | No | `0` / `20`, máximo configurable |

## Configuración

```properties
cashly.audit.fail-closed=true
cashly.audit.max-page-size=100
```

- `fail-closed=true`: si no se puede auditar, la operación falla.
- `fail-closed=false`: se registra un warning y la operación continúa.

## Persistencia

- `V25__create_audit_trail.sql` crea `audit_events`.
- `entity_id` no tiene FK: el historial sobrevive al borrado del registro.
- `actor_id` usa `ON DELETE SET NULL`.
- `V30AuditSeed` carga eventos de ejemplo de forma idempotente.

## Permiso

- Módulo `AUDIT_TRAIL`.
- Acción `VIEW_LIST`.
- Código `AUDIT_TRAIL_VIEW_LIST`.
- El rol `SYSTEM_ADMIN` lo recibe implícitamente por `Role.permissions()`.

## Tests

- `AuditFilterTest`, `AuditEntryTest`, `RecordAuditUseCaseTest`.
- `AuditControllerTest` (contrato HTTP y permisos).
- `AuditPersistenceMySqlIT` (persistencia, orden, paginación, filtros,
  historial sin entidad).
