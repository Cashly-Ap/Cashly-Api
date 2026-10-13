# Especificación y tareas del refactor modular

Este documento es el punto de entrada del cambio SDD. El refactor está en curso; los 32 hallazgos no están todos resueltos.

- [Propuesta y alcance](../openspec/changes/modular-hexagonal-refactor/proposal.md).
- [Requisitos y escenarios Given/When/Then](../openspec/changes/modular-hexagonal-refactor/spec.md).
- [Diseño, archivos y contratos](../openspec/changes/modular-hexagonal-refactor/design.md).
- [Tareas por módulo y estado](../openspec/changes/modular-hexagonal-refactor/tasks.md).
- [Pruebas, cobertura y limitaciones](../openspec/changes/modular-hexagonal-refactor/verification.md).
- [Auditoría previa y estructura objetivo](AUDITORIA-ARQUITECTURA-MODULAR.md).

## Estado por módulo

| Módulo | Aplicado y verificado | Pendiente |
|---|---|---|
| Warehouses | APIs de inventario de facturas/compras, operaciones propias bajo bloqueo, directorio público y tests MySQL | Catálogo neutral, creación concurrente de stock y orden de bloqueos |
| Invoices | Gateway inventario en Create/Update/Duplicate/Delete/Confirm/Cancel; dominio sin Returns; API de ajuste | Autorización, saldo/pago/caja/identidad, consultas y mappers |
| Auth | Refresh por gateway; Domain sin Spring; API pública de destinatarios | Otros contratos de identidad entre módulos |
| Notifications | Gateways propios, transporte Infrastructure, entrega tras commit, error posterior controlado | Reintentos durables y gateways de consumidores restantes |
| Dashboard | Gateway consulta y JDBC Infrastructure; HTTP conservado | Modelo de lectura independiente al extraer servicios |
| PaymentMethods | Uso externo vía APIs Payments/Cashregister; repositorio propio aislado | Paginación neutral global |
| Payments | API de uso de método y política de crédito pura | Posición financiera, gateways liquidación/saldo/caja/notificaciones |
| Cashregister | API de uso de método; agregado separado de reporting y paginación neutral | Integración financiera por contratos |
| Returns | Ajuste de Invoice, inventario por contratos y referencia UUID neutral | Pagos/saldo/consultas |
| Customers | Hashing neutral, paginación de saldo y gateway de directorio almacenes | Crédito, caja, identidad y permisos restantes |
| Orders / Quotes | Sin refactor aplicado en estas tandas | Conversiones públicas, inventario, catálogo y consultas |
| Purchases / Suppliers | Inventario compras por gateway; Suppliers consulta uso por API Purchases | Directorio proveedor para Purchases, mappers y consultas por lote |
| Credit Notes / Debit Notes | Sin refactor aplicado en estas tandas | Elegibilidad, posición financiera, ajustes y caja |
| Products / Categories / Price Types | Categories/PriceTypes usan gateways; Products expone consultas de uso | Catálogo/stock/precios y referencias JPA |
| Receivables / Reports | Receivables query gateway, paginación neutral y proyección de cuentas tipada | Otros reportes, DTOs propios y persistencia explícita |
| Shared / Infrastructure global | PageResponse neutral; mapper Spring en Infrastructure; crédito fuera de Shared | Retirar composición de stock y completar reglas intermodulares |

Los contratos consumidores se llaman `*Gateway` y viven en `application/ports/out`; sus adaptadores viven en Infrastructure. No se crean eventos de dominio, no se reescriben migraciones aplicadas y no se hacen commits automáticamente.


## Persistence Package Responsibilities

Persistence contains only populated responsibility folders: entities (58 JPA entities), repositories (42 Spring Data interfaces), adapters (29 concrete repository implementations), queries (10 database read implementations), mappers (12 transformations), and projections (4 database result interfaces). Concrete classes implementing repository ports belong in adapters, regardless of a Repository suffix. No loose production Java types remain directly in persistence. Input HTTP adapters use controllers.


## Cash Register Module Naming

The backend cash register module uses the cash_register directory and Java package segment. Production and test paths, imports and fully qualified references follow this name. Class names and the /api/cash-register HTTP route remain unchanged.
