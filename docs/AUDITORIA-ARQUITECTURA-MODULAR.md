# Auditoría de arquitectura modular de Cashly API

Fecha: 2026-10-06. Estado: diagnóstico y propuesta; refactor no implementado.

## Alcance y criterio

Revisión estática de los paquetes de producción, imports, constructores, contratos, mappers, configuración, entidades JPA y consultas. Se incluyen Auth y sus submódulos, los módulos de negocio vigentes, Dashboard, Reports y Shared. No se han ejecutado nuevas pruebas funcionales, de concurrencia ni de cobertura para este documento.

La inyección por constructor predomina. Los tres usos de `@Autowired` encontrados están en constructores, no en campos. No se detectaron servicios inyectados dentro de DTOs. Un DTO de Application que transforma una entidad de su propio Domain respeta la dirección de dependencias; un DTO que expone el modelo interno de otro módulo filtra su frontera.

Hay que distinguir: inversión de capas, acoplamiento entre módulos, dependencia de persistencia y riesgo funcional. Una interfaz de repositorio ajena evita depender de JPA directamente, pero permite saltarse la propiedad de los agregados del proveedor. Un contrato público entre módulos puede ser válido; sus casos concretos, repositorios privados y entidades internas no deben convertirse en esa API.

## Estructura actual

```text
Cashly-Api/
├── pom.xml
├── compose.cashly.yaml
├── db/
├── docs/
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── db/migration/
    │   └── java/com/cashly/cashly_api/
    │       ├── CashlyApplication.java
    │       ├── auth/{authentication,token,user}/
    │       ├── cashregister/
    │       ├── categories/
    │       ├── credit_notes/
    │       ├── customers/
    │       ├── dashboard/{application,infrastructure}/
    │       ├── debit_notes/
    │       ├── invoices/
    │       ├── notifications/
    │       ├── orders/
    │       ├── payment_methods/
    │       ├── payments/
    │       ├── price_types/
    │       ├── products/
    │       ├── purchases/
    │       ├── quotes/
    │       ├── receivables/
    │       ├── reports/
    │       │   ├── finance/
    │       │   ├── inventory/
    │       │   ├── invoices/
    │       │   ├── movements/
    │       │   ├── orders/
    │       │   ├── purchases/
    │       │   ├── quotes/
    │       │   ├── stock/
    │       │   └── infrastructure/
    │       ├── returns/
    │       ├── suppliers/
    │       ├── warehouses/
    │       ├── shared/{application,dto,services,storage}/
    │       └── infrastructure/{config,web}/
    └── test/
```

Los módulos de negocio normalmente contienen `domain`, `application` e `infrastructure`. La organización por carpetas existe, pero no garantiza aislamiento. Dashboard puede ser un módulo de consulta/composición sin un Domain artificial.

## Listado consolidado de hallazgos

Rutas de evidencia relativas a `src/main/java/com/cashly/cashly_api/`. P1 indica prioridad alta por propiedad de invariantes o riesgo funcional; P2 separación estructural; P3 endurecimiento del objetivo estricto. Los riesgos funcionales requieren reproducción antes de considerarse errores demostrados.

| ID | Prioridad | Hallazgo y evidencia | Cambio propuesto |
|---|---|---|---|
| A01 | P1 | Invoices reserva, descuenta y revierte stock con repositorios de Warehouses. `invoices/application/usecases/ConfirmInvoiceUseCase.java:94`; `CreateInvoiceUseCase.java:244`; también Update, Duplicate, Delete y Cancel. | `InventoryGateway`; Warehouse controla stock, reservas y movimientos mediante operaciones públicas. |
| A02 | P1 | Confirmación/cancelación de factura contienen operaciones de stock dentro de `Optional.ifPresent`. `ConfirmInvoiceUseCase.java:99`; `CancelInvoiceUseCase.java:64`. | Definir stock inexistente como resultado explícito, con rollback o transición comercial especificada. |
| A03 | P1 | Purchases escribe stock y movimientos ajenos. `purchases/application/usecases/ConfirmPurchaseUseCase.java:54`; `CancelPurchaseUseCase.java:60`. | `PurchaseInventoryPort` para recepción y reversión; invariantes dentro de Warehouses. |
| A04 | P1 | Protección desigual de concurrencia: Invoices y operaciones Warehouse leen stock sin bloqueo, aunque existe consulta pesimista usada por Returns. `CreateInvoiceUseCase.java:244`; `warehouses/application/usecases/TransferStockUseCase.java:57`; entidad sin `@Version`. | Estrategia única de bloqueo/actualización atómica, orden de locks y pruebas simultáneas. |
| A05 | P1 | Autorización de facturas consume saldo, crea pagos/allocations y registra caja. `invoices/application/services/InvoiceAuthorizationService.java:31,88,101`. | Separar autorización y liquidación; puertos de identidad, crédito, saldo y pago. |
| A06 | P1 | Payments consulta modelos/repositorios ajenos y llama a Customers/Cashregister/Notifications concretos. `payments/application/usecases/PayInvoiceUseCase.java:33`. | `InvoiceSettlementPort`, `CustomerBalanceGateway`, `CashPostingPort`, `NotificationGateway`. |
| A07 | P1 | Crédito en Payments Domain depende de Customers Application y sus DTOs. `payments/domain/services/CreditValidationService.java:12,31`. | Consulta/agregación en Application; política pura en Domain; implementación del puerto consumidor mediante adaptador. |
| A08 | P1 | Orders llama `CreateInvoiceUseCase` concreto y usa DTOs internos. `orders/application/usecases/ConvertToInvoiceUseCase.java:20,50`. | `InvoiceIssuancePort` y adaptación al contrato público de Invoices. |
| A09 | P1 | Quotes construye/persiste Order e Invoice ajenos y modifica stock. `quotes/application/usecases/ConvertQuoteToOrderUseCase.java:75`; `ConvertQuoteToInvoiceUseCase.java:139`. | Operaciones públicas de creación/conversión; el proveedor ejecuta sus reglas. |
| A10 | P1 | Returns modifica Invoice, stock, pagos y balance directamente. `returns/application/usecases/CreateReturnUseCase.java:119,124,151,210`; `DeleteReturnUseCase.java:125`. | Puertos de devolución de factura, inventario, pago y saldo; coordinación en Returns Application. |
| A11 | P2 | Invoice recibe el agregado Return y Return conoce InvoiceId: ciclo entre dominios. `invoices/domain/entities/Invoice.java:272,304`; `returns/domain/entities/Return.java:3`. | Comando contractual y modelo local de ajuste; referencias neutrales entre módulos. |
| A12 | P2 | Credit/Debit Notes acceden a repositorios y reglas financieras ajenas. `credit_notes/application/usecases/CreateCreditNoteUseCase.java:32`; `debit_notes/application/usecases/CreateDebitNoteUseCase.java:27`. | Consultas de posición financiera/elegibilidad y operaciones públicas de ajuste. |
| A13 | P2 | Products y Warehouses se consultan con repositorios/modelos internos: ciclo. `warehouses/application/usecases/StockEntryUseCase.java:34`; `products/application/usecases/GetProductsForTransactionUseCase.java:30`. | Puertos de catálogo, stock y contexto comercial con snapshots propios y consultas por lote. |
| A14 | P2 | Customers consulta almacenes/usuarios y utiliza servicio concreto de caja. `customers/application/usecases/CreateCustomerAdvanceUseCase.java:17`; `GrantWarehouseAccessUseCase.java:21`. | Puertos de caja, actor y directorio de almacenes; Customers conserva saldo y permisos. |
| A15 | P2 | Shared mezcla reglas de negocio de stock/crédito. `shared/application/StockEnrichmentService.java:15`; `shared/services/CreditGuard.java:14`. | Asignar propietarios de negocio y contratos explícitos; Shared solo elementos neutrales. |
| A16 | P2 | Suppliers consulta PurchaseRepository y Purchases consulta SupplierRepository. `suppliers/application/usecases/DeleteSupplierUseCase.java:15`; `purchases/application/usecases/CreatePurchaseUseCase.java:16`. | Puertos de uso de proveedor y consulta de proveedor; snapshots históricos propios. |
| A17 | P2 | Categories y Price Types consultan Products ocultándolo en sus repositorios. `categories/infrastructure/persistence/repositories/SpringDataCategoryRepository.java:32`; `price_types/infrastructure/persistence/repositories/SpringDataPriceTypeRepository.java:31`. | Separar puertos de uso externo de repositorios de agregados. |
| A18 | P2 | Payment Methods consulta JPA de Payments/Cashregister. `payment_methods/infrastructure/persistence/repositories/PaymentMethodRepositoryAdapter.java:76`. | `PaymentMethodUsagePort` implementado contra entradas públicas de proveedores. |
| A19 | P2 | Products tiene relación JPA con entidad de Price Types y consultas cruzadas de inventario/referencias. `products/infrastructure/persistence/entities/ProductPriceEntity.java:29`; `SpringDataProductRepository.java:52,77`. | Referencia por ID y consultas públicas/modelo de lectura; conservar semántica de filtrado y paginación. |
| A20 | P2 | Orders filtra por categoría consultando ProductEntity ajena. `orders/infrastructure/persistence/repositories/OrderJpaRepository.java:41`. | Consulta pública o proyección explícita; decidir categoría actual frente a histórica. |
| A21 | P2 | Cashregister/Receivables Domain usa DTOs de Application; paginación Spring en Domain y transitivamente en PageResponse. `cashregister/domain/repositories/CashRegisterRepository.java:3`; `receivables/domain/repositories/ReceivablesRepository.java:3`; `customers/domain/repositories/CustomerBalanceRepository.java:5`; `shared/dto/PageResponse.java:17`. | Separar repositorios de agregados y query ports Application; paginación neutral y conversión en Infrastructure. |
| A22 | P2 | Auth Application crea adaptadores Infrastructure. `auth/token/application/usecases/RefreshTokenUseCase.java:82,87`. | Puerto de resolución del sujeto; adaptador ensamblado en configuración. |
| A23 | P2 | Dashboard ejecuta JDBC y SQL en Application. `dashboard/application/usecases/GetDashboardUseCase.java:18`. | `DashboardReadPort` y adaptador JDBC con proyecciones tipadas. |
| A24 | P2 | Notifications publica WebSocket dentro de Application y antes de cerrar la transacción. `notifications/application/services/NotificationService.java:21,37,51`. | Puerto de entrega y destinatarios; definir publicación tras commit y recuperación ante fallo. |
| A26 | P2 | Reports tiene casos concretos/repositorios ajenos, contratos PDF con DTOs internos de otros módulos y JPA/SQL compartidos. `reports/invoices/application/port/InvoicePdfPort.java:3`; `reports/stock/infrastructure/persistence/StockReportJpaRepository.java:11`. | Datos de informe propios, puertos de lectura y generación; modelo de lectura explícito para futura distribución. |
| A27 | P2 | Mappers realizan consultas y DTOs filtran modelos extranjeros. `returns/application/mappers/ReturnMapper.java:28,91`; `purchases/application/mappers/PurchaseMapper.java:28`; `products/application/dto/response/ProductStockByWarehouseResponse.java:3`. | Resolver datos mediante puertos antes del mapping; mappers puros sobre proyecciones locales. |
| A28 | P2 | Application interpreta excepción Spring de persistencia. `products/application/usecases/DeleteProductUseCase.java:29`. | Traducir errores técnicos en el adaptador, con resultado/error neutral. Revisar el momento de flush/commit. |
| A29 | P1 | Orders oculta cualquier fallo de consulta de crédito con catch Exception y valores cero. `orders/application/usecases/GetOrderByIdUseCase.java:43,52`. | Resultado explícito para crédito disponible, cliente ausente o información no disponible. |
| A31 | P3 | Authentication Domain tiene @Service; Customers Application usa PasswordEncoder. `auth/authentication/domain/services/AuthenticationDomainService.java:6`; `customers/application/usecases/RegisterCustomerUseCase.java:15`. | Domain sin Spring; puerto neutral de hashing y adaptación BCrypt. Para pureza total, ensamblado y decoradores transaccionales externos. |
| A32 | P2 | No se encontraron reglas ArchUnit por búsqueda en pom.xml/src/test. Las carpetas permiten reintroducir dependencias. | Incorporar pruebas de arquitectura para capas, contratos públicos y ausencia de ciclos del core. Confirmar también otros mecanismos de validación antes de implementar. |

## Estructura propuesta por módulo

Ejemplo de Invoices; se conserva inicialmente el monolito y los nombres de módulos para limitar el alcance.

```text
invoices/
├── domain/
│   ├── entities/
│   ├── valueobjects/
│   ├── services/                 # Políticas puras
│   ├── repositories/             # Repositorios de agregados propios
│   └── exceptions/
├── application/
│   ├── api/                      # Única entrada pública entre módulos
│   │   ├── IssueInvoice.java      # Interfaces de entrada
│   │   ├── CommitInvoice.java
│   │   ├── commands/
│   │   └── results/              # Contratos neutrales, sin entidades internas
│   ├── contracts/
│   │   ├── InventoryGateway.java
│   │   ├── CustomerBalanceGateway.java
│   │   ├── PaymentSettlementGateway.java
│   │   ├── ActorIdentityGateway.java
│   │   └── NotificationGateway.java
│   ├── usecases/                 # Implementaciones internas y orquestación
│   ├── queries/
│   └── mappers/                  # Transformación pura
└── infrastructure/
    ├── config/                   # Wiring de constructores y decoradores
    ├── web/
    │   ├── controllers/
    │   ├── dto/                  # Transporte HTTP y validación web
    │   └── mappers/
    ├── persistence/              # JPA y SQL de Invoices
    └── integration/
        ├── LocalWarehouseInventoryAdapter.java
        ├── LocalCustomerBalanceAdapter.java
        ├── LocalPaymentSettlementAdapter.java
        ├── LocalIdentityAdapter.java
        └── LocalNotificationAdapter.java
```

`application/api` es una convención propuesta para agrupar puertos de entrada y sus contratos; equivale a usar `ports/in` más contratos públicos. No se debe mantener un segundo contrato duplicado sin propósito. Los adaptadores consumidores pueden depender de esa API pública del proveedor, nunca de sus implementaciones, entidades o repositorios internos.

Ejemplo de ejecución:

```text
InvoiceController
  -> invoices.application.api.CommitInvoice
  -> invoices.application.usecases.ConfirmInvoiceUseCase
  -> invoices.application.contracts.InventoryGateway
  -> invoices.infrastructure.integration.LocalWarehouseInventoryAdapter
  -> warehouses.application.api.CommitReservation
  -> warehouses.application.usecases.CommitReservationUseCase
  -> warehouses.domain.repositories.WarehouseStockRepository
  -> warehouses.infrastructure.persistence.JpaWarehouseStockRepository
```

La dirección de implementación en tiempo de compilación es adaptador -> puerto, aunque el flujo de ejecución sea caso de uso -> adaptador. Los constructores permanecen en Application recibiendo puertos; Infrastructure conecta las implementaciones. Mover únicamente el `new` a un Config no elimina dependencias del core.

## Límites y contratos después del cambio

| Propietario | Responsabilidad conservada | Contratos públicos orientativos |
|---|---|---|
| Warehouses | Stock, reservas, movimientos y concurrencia | ReserveStock, CommitReservation, ReleaseReservation, ReceivePurchaseStock, ReverseStockOperation, QueryStock |
| Products | Catálogo, composición y precios | QueryProductCatalog, QueryProductUsage |
| Customers | Cliente, permisos de almacén, saldo y condiciones comerciales | QueryCustomerSalesContext, QueryWarehouseAccess, CreditBalance, DebitBalance |
| Invoices | Ciclo de factura, importes y aplicación de ajustes | IssueInvoice, CommitInvoice, ApplyInvoiceAdjustment, QueryInvoiceFinancialPosition |
| Payments | Pago, aprobación y asignación | SubmitPayment, SettlePayment, ReversePayment, QueryPayment |
| Cashregister | Caja, movimientos y cierre | ValidateOpenRegister, PostCashMovement, ReverseCashMovement |
| Orders / Quotes | Sus estados y conversión mediante contratos del destino | CreateOrderFromSource, QueryOrder, QueryQuote |
| Purchases / Returns / Notes | Orquestación de su operación y agregado propio | ConfirmPurchase, ApplyReturn, IssueCreditNote, IssueDebitNote |
| Auth | Identidad y autenticación | ResolveActor, ResolveAuthenticableSubject |
| Notifications / Storage | Entrega y archivos | DeliverNotification, StoreProvisionalFile, ConfirmFile, RemoveFile |
| Reporting / Dashboard | Composición de lecturas, sin modificar agregados ajenos | QueryReportData, QueryDashboardData |

La posición financiera del cliente necesita una decisión explícita de propiedad entre Customers, Payments y Receivables. No conviene preservar varias fórmulas divergentes de deuda/crédito ni decidir la propiedad únicamente por la ubicación actual de la clase.

Los módulos de consulta no necesitan crear entidades de dominio vacías para copiar el árbol. Shared queda reducido a valores/resultados neutrales y utilidades sin imports de módulos de negocio. Storage puede conservar su puerto neutral y adaptadores propios.

## Reglas verificables del objetivo

1. Domain no importa Application, Infrastructure, Spring, JPA ni DTOs de transporte.
2. Application no importa Infrastructure; utiliza modelos propios, puertos y contratos neutrales.
3. Core de un módulo no importa entidades, repositorios ni casos concretos de otro módulo.
4. Integración entre módulos concentrada en adaptadores Infrastructure contra API pública Application del proveedor.
5. Puertos consumidores no retornan entidades, DTOs internos ni repositorios ajenos.
6. Repositorio de agregado solo persiste su propio módulo; las consultas compuestas son capacidades explícitas.
7. Mappers no realizan I/O. Los contratos de aplicación no reciben MultipartFile ni leen contexto Spring Security.
8. Decisiones de concurrencia, idempotencia, rollback y publicación se prueban como comportamiento, no solo mediante imports.

Las anotaciones `@Transactional` de Application pueden mantenerse durante la transición, como concesión actual del proyecto. Para independencia absoluta del framework, usar decoradores transaccionales en Infrastructure, comprobando proxies y propagación. No cambiar todo el wiring simultáneamente.

## Secuencia de migración y aceptación

1. Añadir caracterización de operaciones actuales y reglas de arquitectura que puedan ampliarse por módulo sin bloquear todo el legado.
2. Encapsular stock/reservas/movimientos en Warehouses. Probar ausencia de stock, concurrencia, transferencias, cancelación y rollback.
3. Separar crédito/saldo/pago/caja. Definir propietario de la posición financiera y del asiento; comprobar que cada operación se registra una vez.
4. Migrar conversiones y devoluciones a comandos públicos. Mantener comportamiento, transacción local y referencias de operación.
5. Separar consultas/mappers/reporting, preservando filtros, totales, paginación y permisos.
6. Eliminar dependencias técnicas del core y activar reglas estrictas de arquitectura.
7. Evaluar extracción a servicios únicamente después: contratos versionados, autenticación entre servicios, idempotencia, timeouts y recuperación deben estar definidos.

No se introduce mensajería ni Domain Events en esta propuesta. El paso a HTTP no conserva la atomicidad de `@Transactional`: cada servicio tendría transacciones locales y necesitaría una estrategia explícita de consistencia y compensación.

Pruebas posteriores necesarias: contratos de integración local; stock concurrente; confirmación/cancelación/repetición; crédito/saldo/pagos/notas; conversiones equivalentes; permisos y errores de identidad; fallo de storage/SQL y limpieza; entrega de notificaciones tras rollback; paginación y categoría histórica/actual. Respetar los umbrales de cobertura del proyecto y reportar déficits reales.
