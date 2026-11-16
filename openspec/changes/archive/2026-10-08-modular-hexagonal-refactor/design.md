# Diseño: monolito modular por contratos

## Decisiones

| Decisión | Motivo |
|---|---|
| Mantener Spring Boot 3.5.5 y Java 24 | Evitar mezclar upgrades con cambio de arquitectura. |
| `application/api` para contratos públicos del proveedor | Ocultar casos concretos y modelos internos. |
| `application/contracts/*Gateway` para necesidades del consumidor | Nombres acordados con el usuario, interfaces neutrales. |
| Adaptadores `infrastructure/integration` contra API del proveedor | Sustituir integración local posteriormente sin acoplar core. |
| Transacciones locales durante refactor | Mantener atomicidad; HTTP no conserva `@Transactional`. |
| Tandas con baseline y pruebas de contrato | Reducir superficie de regresión y evitar interfaces decorativas. |
| Application integra otros módulos solo por `application/api`; Domain mantiene tipos propios y el PageResponse neutral existente | Impedir que modelos internos formen contratos entre módulos. |
| Snapshots de categoría de ventas se guardan en tablas auxiliares de V25 | Preservar la categoría histórica con backfill al instalar o cargar seeds, sin ALTER TABLE; el baseline V1–V25 se consolidó para bases nuevas por solicitud expresa de reinicio. |

## Primera tanda: archivos

- `warehouses/application/api/` y casos de uso propios: confirmar/revertir stock de factura bajo bloqueo; modelos UUID/cantidades/referencias.
- `invoices/application/contracts/InventoryGateway.java`, `infrastructure/integration/`: adaptación al proveedor; `ConfirmInvoiceUseCase.java`, `CancelInvoiceUseCase.java` delegan inventario.
- `auth/token/application/contracts/AuthenticableSubjectGateway.java`, adaptador Infrastructure y `RefreshTokenUseCase.java`: resolver sujeto sin crear adaptadores en core.
- `auth/authentication/domain/services/AuthenticationDomainService.java`, configuración: retirar Spring del Domain.
- `auth/user/application/api/NotificationRecipientsQuery.java`: consulta pública de IDs conservando destinatarios existentes.
- `notifications/application/contracts/`, adaptadores delivery/recipients y `NotificationService.java`: transporte fuera de Application; sincronización Spring tras commit en Infrastructure, sin Domain Events.
- `dashboard/application/contracts/DashboardQueryGateway.java`, `GetDashboardUseCase.java`, `infrastructure/persistence/JdbcDashboardQueryAdapter.java`: SQL y mapping JDBC en adaptador; resultados HTTP existentes conservados.
- Tests en paquetes correspondientes: contratos, errores, metadata, commit/rollback y consultas existentes. Reubicar prueba JDBC existente conservando su assertion de límite.

## Tandas restantes

Inventario completo → posición financiera/crédito → conversiones/devoluciones → catálogos/permisos → consultas y persistencia → transporte/archivos → reglas estrictas de arquitectura. Véase `tasks.md` y la auditoría para rutas exactas.

Los repositorios de Domain son propios; las consultas compuestas se expresan mediante contratos Application. Mappers puros reciben datos resueltos. IDs externos neutrales sustituyen entidades ajenas. DRY se aplica a comportamiento común del propietario, sin crear un Shared con reglas financieras o inventario.

## Verificación y entrega

Baseline aislada del commit actual y ejecución Maven en contenedores, sin tocar la base de datos de desarrollo. Se informa la exclusión heredada de `StockReservationIntegrationTest`; se ejecutará aparte con Docker disponible. Cobertura objetivo: Domain >90%, Application >80%, Infrastructure >60%; déficits heredados no se ocultan.

Se recomienda revisión por unidades porque el alcance supera 400 líneas. No se crean PRs ni commits sin autorización. Cada tanda conserva evidencia Red/Green, suite completa y estado pendiente explícito.

## Extensiones verificadas por tandas

La paginación compartida usa PageResponse sin dependencia Spring; PageResponseMapper vive en shared/infrastructure/pagination. Los repositorios de saldo de Customers y las consultas de Receivables usan el tipo neutral. Cashregister separa el repositorio de agregados y CashRegisterQueryGateway; su controlador conserva la estructura de PageImpl para mantener la respuesta HTTP.

Customers posee PasswordHashingGateway y WarehouseDirectoryGateway. Los adaptadores convierten BCrypt y la consulta pública de Warehouses; registro y cambios de contraseña mantienen validaciones anteriores. Suppliers posee PurchaseUsageGateway y Categories/PriceTypes poseen sus gateways de uso; solo Infrastructure conoce las APIs públicas de los proveedores.

CustomerCreditEligibilityService sustituye la composición de crédito en Shared. Sus consumidores Invoices y Orders mantienen contratos separados con los datos que necesitan; la consulta financiera interna conserva inicialmente sus cálculos y después se migra por gateways de ledger.


