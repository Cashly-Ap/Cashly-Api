# API de Cashly: especificación de microservicios y arquitectura hexagonal

Estado: especificación de implementación propuesta. Auditoría realizada el 2026-10-06. Este documento no modifica la arquitectura ejecutable ni certifica su cumplimiento actual. Mantener Java 24, Spring Boot 3.5.5, MySQL y Flyway; verificados en `pom.xml:8`, `pom.xml:30`, `pom.xml:63` y `pom.xml:84`.

## Decisión y secuencia de implementación

Primero convertir la API existente en un monolito modular con límites verificables. Extraer servicios desplegables de forma independiente solo cuando sus contratos, propiedad de los datos y comportamiento ante fallos superen las pruebas de aceptación. Un paquete Java no constituye automáticamente un contexto delimitado ni un microservicio. No distribuir la confirmación de facturas entre procesos suponiendo que la transacción local actual sigue garantizando atomicidad.

El alcance solicitado es esta especificación. Los futuros cambios de código requieren su propio cambio SDD Explore → Propose → Spec → Design → Tasks → Apply → Verify → Archive. No modificar trabajos OpenSpec existentes que sean ajenos a este alcance. Los eventos de dominio y la mensajería son decisiones futuras opcionales y requieren autorización explícita del usuario antes de introducirse.

## Situación inicial verificada

Las rutas siguientes son relativas a Cashly-Api. Los ejemplos Java usan el prefijo `src/main/java/com/cashly/cashly_api/`. Las referencias de línea describen la versión auditada y deben actualizarse después de los cambios.

| Evidencia | Comportamiento actual | Corrección requerida |
| --- | --- | --- |
| `invoices/application/usecases/ConfirmInvoiceUseCase.java:6`, `:13`, `:23`, `:24`, `:34`, `:40`, `:67`, bajo el prefijo Java | Servicios concretos de movimientos de caja y notificaciones, repositorios de almacenes ajenos y el modelo de usuario participan en un flujo transaccional de facturación. | Capacidades propiedad del consumidor, adaptadores de infraestructura y una política explícita de transacciones y fallos. |
| `auth/token/application/usecases/RefreshTokenUseCase.java:7`, `:8`, `:82`, `:87` | La aplicación construye adaptadores concretos de infraestructura de autenticación. | Un puerto de resolución del sujeto devuelve una proyección del núcleo de autenticación; la construcción pertenece a infraestructura. |
| `cashregister/domain/repositories/CashRegisterRepository.java:3`–`:6`, `:11`, `:27` | Un puerto de dominio expone DTO de informes de aplicación y Spring Page. | Mover los puertos y proyecciones de consultas de informes a aplicación; mantener el repositorio de agregados limitado al dominio y usar paginación independiente del framework. |
| `receivables/domain/repositories/ReceivablesRepository.java:3`–`:8` | El dominio depende de DTO de respuesta de aplicación y Spring Page. | Puerto de consulta de aplicación con proyecciones de lectura específicas y un adaptador de consultas en infraestructura. |
| `payments/domain/services/CreditValidationService.java:12`, `:13`, `:31` | El dominio importa DTO de aplicación de clientes e implementa una interfaz de aplicación de clientes mediante un nombre completamente cualificado. | Política de crédito pura sobre valores locales; la orquestación de aplicación recopila datos propiedad del consumidor mediante puertos. |
| `auth/authentication/domain/services/AuthenticationDomainService.java:4` | El dominio importa Spring Service. | Clase de dominio sin framework, conectada externamente. |
| `portal/application/usecases/CreatePortalOrderUseCase.java:6` | La aplicación del portal importa un caso de uso concreto de pedidos. | Puerto de capacidad de pedidos propiedad del portal, implementado contra un contrato público de entrada de Ventas. |
| `warehouses/infrastructure/config/WarehouseConfig.java:41`–`:47` | La configuración existente ya usa métodos Bean y conexión mediante constructores. | Ampliar este enfoque y sustituir repositorios ajenos por adaptadores sobre capacidades públicas. |
| `src/main/resources/db/migration/V11__create_invoices_tables.sql:29`, `:47` | El esquema de facturas contiene claves foráneas a clientes y productos. | Resolver la propiedad y migrar las referencias antes de separar las bases de datos. |

`invoices/application/dto/request/ConfirmInvoiceRequest.java:6` es un record pasivo. La auditoría no demuestra que este DTO inyecte servicios. Distinguir un problema real de inyección en DTO de las interfaces de dominio que exponen DTO de aplicación: ambos están prohibidos en el objetivo, pero solo el segundo queda demostrado arriba.

### Inventario de importaciones y limitaciones

Un análisis mediante PowerShell en el equipo leyó los 904 archivos Java de producción bajo el prefijo Java e identificó sentencias de importación. Clasificó las capas de origen mediante `/domain/`, `/application/`, `/infrastructure/` en las rutas; los paquetes de destino mediante `com.cashly.cashly_api`; y los prefijos de framework mediante `org.springframework`, `jakarta`, `javax`, `com.fasterxml`, `org.hibernate`. Los resultados cuentan sentencias de importación, no archivos:

| Categoría | Cantidad |
| --- | ---: |
| El dominio importa aplicación o infraestructura | 11 |
| El dominio importa los espacios de nombres de framework indicados | 5 |
| La aplicación importa infraestructura propia o ajena | 2 |
| La aplicación importa los espacios de nombres de framework indicados | 407 |
| Dominio o aplicación importa otro módulo de primer nivel, excluyendo shared | 618 |

La última categoría es un inventario de acoplamiento, no 618 infracciones demostradas. Incluye candidatos legítimos a reagruparse dentro de un contexto. `auth` y `reports` contienen módulos anidados que este clasificador de primer nivel no separa. No cubre referencias completamente cualificadas, reflexión, referencias dentro de métodos ni dependencias transitivas; la cláusula implements de CreditValidationService es un ejemplo que el recuento de importaciones omite. Los recuentos de framework incluyen anotaciones de validación y transacciones, cuyo tratamiento objetivo debe evaluarse explícitamente. Sustituir esta aproximación inicial por reglas sobre dependencias compiladas; nunca presentarla como prueba exhaustiva de arquitectura.

## Contextos delimitados candidatos y propiedad

Validar estas propuestas mediante los flujos de negocio, el inventario de transacciones, los responsables de mantenimiento y las mediciones de carga de producción antes de fijar los límites de los servicios.

| Contexto propuesto | Módulos existentes | Propiedad y capacidades públicas |
| --- | --- | --- |
| Identidad y acceso | auth | Credenciales, tokens de actualización, usuarios y políticas de autorización; resolver la identidad verificada del actor y las decisiones de acceso. El perfil del cliente permanece fuera de identidad. |
| Gestión de clientes | customers | Agregado de cliente, datos de contacto y perfil, permisos de acceso a almacenes; exponer instantáneas del perfil y de la elegibilidad del cliente. Mover los saldos de crédito a Finanzas solo mediante una migración separada y verificada. |
| Catálogo | products, categories, price_types | Definiciones de productos, categorías y precios; instantáneas de disponibilidad de productos y precios, nunca escrituras en inventario. |
| Inventario | warehouses | Existencias de almacén, reservas, movimientos y consistencia de existencias; capacidades de reserva, liberación y confirmación. |
| Ventas y facturación | quotes, orders, invoices, returns | Ciclo comercial e instantáneas de facturas; mantener inicialmente juntos los flujos estrechamente transaccionales. Los ajustes de inventario por devoluciones usan contratos de Inventario. |
| Finanzas | payments, payment_methods, receivables, credit_notes, debit_notes, cashregister | Asignación de pagos, deuda, ajustes de crédito y débito, libro de caja; capacidades de contabilización idempotente y autorización de crédito. |
| Compras | purchases, suppliers | Relaciones con proveedores y ciclo de compras; Inventario recibe existencias mediante un contrato de capacidad. |
| Entrega y experiencia | notifications, portal | Las notificaciones y la orquestación orientada al cliente son responsabilidades distintas. El portal es candidato a fachada/BFF, no propietario de los datos de Ventas o Finanzas. |
| Informes | reports, dashboard | Modelos de lectura y generación de exportaciones; sin escrituras en agregados operativos. Empezar con contratos de lectura y medir la latencia antes de elegir proyecciones replicadas. |

`shared` queda limitado a primitivas técnicas estables sin entidades de negocio, modelos de persistencia, DTO específicos de contexto ni localizadores de servicios. `infrastructure` contiene composición y soporte de ejecución, no una capa de negocio compartida. No crear un servicio por cada paquete existente. Excluir un frontend separado de Portal de la migración no implica eliminar los endpoints de portal existentes en la API.

## Reglas obligatorias de dependencias e inyección

1. El dominio depende únicamente de Java y de sus propios tipos de dominio. Sin Spring, Jakarta, Jackson, JPA, HTTP, DTO de aplicación ni agregados o repositorios de otros contextos. Los servicios de dominio son objetos sin framework y reciben valores o interfaces de dominio locales.
2. La aplicación depende de su propio dominio, contratos de aplicación y primitivas independientes del framework permitidas explícitamente. Los casos de uso son clases sin framework con inyección de interfaces por constructor. Sin casos de uso concretos ajenos, tipos de adaptadores, repositorios de otro contexto, clientes HTTP, Spring Page/Pageable ni localizadores de servicios.
3. La infraestructura contiene controladores, validación del transporte, entidades de persistencia, implementaciones de repositorios, clientes, mapeadores, adaptadores de autenticación y raíces de composición. Sus dependencias apuntan hacia dentro e implementa puertos de aplicación o dominio. La composición solo conecta contextos mediante el contrato público de entrada del proveedor.
4. El consumidor es propietario de su puerto de salida y de sus proyecciones de petición y resultado. El proveedor es propietario de su contrato de operación de entrada. El adaptador de integración traduce entre ambos. Se prohíbe compartir la entidad de dominio o el DTO web del proveedor aunque ambos se ejecuten en la misma JVM.
5. Los DTO, comandos, respuestas y objetos de valor de dominio contienen datos e invariantes, no servicios inyectados. Los DTO HTTP permanecen en el adaptador web; los comandos y resultados de aplicación describen operaciones; los mapeos de persistencia permanecen fuera del núcleo. Usar records independientes aunque inicialmente coincidan sus campos.
6. La creación de Bean y la selección de adaptadores ocurren en `infrastructure/config`. Todas las dependencias inyectadas usan constructores; sin Autowired en campos. Recibir una interfaz de puerto en un constructor de aplicación es inversión de dependencias válida: trasladar la orquestación a infraestructura eliminaría el límite del caso de uso.
7. Para el objetivo estricto, eliminar las anotaciones de transacción Spring/Jakarta de aplicación e implementar un decorador transaccional en infraestructura. Esto elige deliberadamente un aislamiento más estricto que la convención existente que permite Transactional. Conectar cada operación una sola vez, evitando el registro duplicado con Service y Bean.
8. Los puertos de repositorio describen persistencia de agregados; los puertos de consultas de informes pertenecen a aplicación. Sustituir la paginación de framework por records propios `PageQuery`/`PageResult<T>` y adaptar externamente la paginación Spring.

### Ejemplo concreto antes y después: confirmación de facturas

Antes: ConfirmInvoiceUseCase recibe directamente `WarehouseStockRepository`, `StockMovementRepository`, `UserRepository`, `CashMovementService` y `NotificationService`. Su `execute` confirma la factura, descuenta existencias, guarda la factura, contabiliza caja y notifica al cliente dentro de una operación anotada, desde `:67`.

Forma propuesta del núcleo, ilustrativa y todavía sin implementar:

```java
public interface InvoiceInventoryPort {
    InventoryCommitResult commit(InvoiceStockCommit command);
}

public interface InvoiceCashPostingPort {
    CashPostingResult post(InvoiceCashPosting command);
}

public interface InvoiceActorPort {
    InvoiceActor resolve(UUID actorId);
}

public final class ConfirmInvoiceUseCase implements ConfirmInvoice {
    private final InvoiceRepository invoices;
    private final InvoiceInventoryPort inventory;
    private final InvoiceCashPostingPort cashPosting;
    private final InvoiceActorPort actors;

    public ConfirmInvoiceUseCase(InvoiceRepository invoices,
                                InvoiceInventoryPort inventory,
                                InvoiceCashPostingPort cashPosting,
                                InvoiceActorPort actors) {
        this.invoices = invoices;
        this.inventory = inventory;
        this.cashPosting = cashPosting;
        this.actors = actors;
    }
}
```

Estos records propiedad del consumidor contienen identificadores, importes, cantidades, claves de operación y códigos de resultado explícitos; no contienen agregados de almacén, entidades de usuario, tipos Spring ni DTO del proveedor. Completar los puertos de autorización de facturas y notificación según el comportamiento existente; el ejemplo abreviado no los elimina silenciosamente. Los adaptadores de infraestructura dentro del proceso llaman a interfaces públicas de entrada de Inventario y Finanzas. Tras la extracción, los adaptadores HTTP los sustituyen sin modificar los contratos del dominio o casos de uso de facturación. La configuración de infraestructura construye el caso de uso y lo decora con transacciones locales. El diseño del flujo distribuido que sigue es obligatorio antes de sustituirlos por adaptadores remotos.

## Datos, transacciones y comportamiento ante fallos distribuidos

Durante la limpieza del monolito, mantener la base de datos existente y el comportamiento atómico hasta que las pruebas demuestren equivalencia. Registrar qué tablas lee o escribe cada operación y qué bloqueos o garantías de concurrencia protegen existencias, saldos y números de factura. Tras la extracción, las transacciones locales no deben abarcar llamadas remotas lentas.

Cada servicio extraído es propietario de su base de datos o esquema, credenciales, historial Flyway y autoridad de escritura. Otros servicios no pueden consultar sus tablas, compartir repositorios o entidades JPA ni usar claves foráneas entre servicios. Las referencias pasan a ser identificadores con validación mediante contratos e instantáneas históricas cuando sean necesarias. Las claves foráneas dentro de un mismo propietario siguen siendo válidas. Preservar las migraciones aplicadas byte a byte; añadir migraciones nuevas para tablas nuevas, datos copiados, restricciones y limpieza. Usar carga de datos históricos, conciliación, ensayos de cambio de servicio y restauración; las instantáneas de datos de cliente y producto en las facturas deben conservar su significado histórico.

Antes de extraer Inventario o Finanzas, definir la confirmación de facturas como un flujo durable de aplicación: reservar existencias; autorizar y contabilizar fondos con una clave estable de operación; confirmar inventario y estado de factura; conciliar resultados desconocidos. Persistir el progreso y la intención de reintento en el servicio propietario. Cada operación remota permite consultar por clave de operación y ejecutarse sin duplicados. Los pasos fallidos requieren reglas de compensación aprobadas, como liberar la reserva o revertir un asiento; nunca eliminar evidencia financiera. Confirmar con el negocio las transiciones de estado y las reglas legales de reversión. Agotar el tiempo de espera representa un resultado desconocido, no prueba que no haya ocurrido nada. No devolver éxito confirmado hasta cumplir la condición de finalización de negocio elegida; exponer un resultado pendiente explícito solo mediante un contrato versionado.

HTTP síncrono es la base inicial de integración. Esta especificación no introduce intermediarios de mensajes, eventos de dominio, outbox ni bibliotecas de sagas. Si posteriormente se autoriza entrega asíncrona, especificar por separado la propiedad y versiones de eventos, outbox/inbox durables, reintentos, mensajes fallidos, orden, reproducción y conciliación. La recuperación durable de flujos síncronos sigue siendo necesaria sin mensajería.

## Contratos y requisitos de ejecución

- Capturar endpoints y errores públicos existentes en OpenAPI y pruebas de caracterización antes de modificar límites. Mantener el contrato actual del frontend mediante una fachada; romper campos, estados HTTP o semánticas de pendiente requiere versionado y un plan explícito de migración.
- Definir pruebas de contrato consumidor/proveedor para éxito, validación, recurso inexistente, autorización denegada, conflicto, fallo de dependencia y resultado desconocido por tiempo de espera agotado. Los códigos de error son estables y procesables por máquina; los errores de transporte del proveedor se traducen a resultados del consumidor.
- Especificar límites finitos de conexión y petición por adaptador, presupuestos de tiempo propagados, concurrencia limitada e interrupción de llamadas a dependencias fallidas según los SLO medidos de endpoints. Permitir reintentos limitados con espera creciente solo para lecturas o escrituras idempotentes. Una clave de operación queda delimitada por tenant, comando y actor u operación de negocio; persistir el hash de petición y el resultado original, y rechazar claves reutilizadas con datos diferentes.
- Validar la identidad autenticada en cada límite de servicio; aplicar permisos de tenant, cliente y almacén en las operaciones propietarias. Usar credenciales de servicio con privilegios mínimos, transporte cifrado e inyección de secretos. No confiar en identificadores de usuario o tenant solo porque los aporta el llamante. Evitar contraseñas y tokens en registros.
- Propagar identificadores de traza, correlación y operación. Emitir registros estructurados, métricas de latencia y errores, disponibilidad de dependencias, discrepancias de conciliación de existencias y caja, y recuentos de flujos bloqueados. Las comprobaciones de disponibilidad reflejan dependencias locales esenciales; las de actividad no provocan bucles de reinicio ante caídas de servicios remotos.
- Cada servicio tendrá finalmente su propia compilación Docker, comprobaciones de salud, configuración de despliegue y reversión. Compilar y probar mediante Docker; no cambiar las versiones de la pila incidentalmente durante la migración arquitectónica.

## Tareas de implementación y condiciones de publicación

| Fase | Trabajo concreto | Condición de salida |
| --- | --- | --- |
| 0: caracterizar | Inventariar controladores, tablas persistentes, flujos transaccionales e importaciones; capturar contratos de API y mapa de módulos y contextos. Medir cobertura actual y SLO de ejecución. | Los artefactos iniciales explican fallos conocidos y decisiones de propiedad; sin cobertura inventada. |
| 1: reparar dependencias hacia dentro | Corregir las filtraciones indicadas del dominio, construcción de adaptadores RefreshToken, paginación de framework y orquestación de políticas de crédito. Introducir pruebas arquitectónicas sobre código compilado. | Sin dependencias prohibidas del núcleo; las pruebas puras de dominio y aplicación se ejecutan sin Spring. |
| 2: establecer límites de capacidades | Sustituir dependencias concretas entre contextos en los flujos de factura, portal, compras y pagos por puertos del consumidor y contratos de entrada del proveedor; implementar adaptadores dentro del proceso. | Se conserva evidencia del comportamiento de API, regresiones y reversión; sin acceso a repositorios de otro contexto. |
| 3: preparar propiedad | Asignar tablas y rutas de escritura; añadir migraciones aditivas y tareas de conciliación y carga histórica; diseñar transiciones idempotentes del flujo. | Ensayos de restauración y cambio de servicio e invariantes superados con datos representativos de producción. |
| 4: extraer un contexto de bajo riesgo | Elegir Notificaciones o generación de informes solo después de medir acoplamiento y semánticas de entrega; desplegar de forma independiente detrás de la fachada existente. | Se superan comprobaciones de contratos, fallos, seguridad, observabilidad y reversión. |
| 5: extraer capacidades transaccionales | La extracción de Inventario y Finanzas sigue un diseño explícito de flujo durable de facturación y compensaciones aprobadas por el negocio. | Se superan pruebas de aceptación de duplicados, reintentos, concurrencia y fallos parciales antes de cambiar el tráfico. |
| 6: verificar y archivar | Ejecutar verificación completa en Docker, comprobaciones manuales de endpoints y frontend y dos revisores independientes a ciegas de Judgment Day. Resolver hallazgos confirmados y repetir la revisión dentro de los límites de la skill. | APPROVED de ambos o informar ESCALATED; solo entonces archivar SDD. |

Usar TDD Red → Green → Refactor para nuevo comportamiento ejecutable. Mantener cambios suficientemente pequeños para revisar cada límite. Commit y push siguen sujetos a autorización del usuario. Conservar un monolito modular bien delimitado es un resultado aceptable si el coste de extracción supera el beneficio medido.

## Especificación de aceptación BDD

| Dado | Cuando | Entonces |
| --- | --- | --- |
| Clases de producción en todos los contextos | Las reglas arquitectónicas inspeccionan dependencias compiladas | El dominio no tiene dependencias hacia fuera ni de framework; aplicación no depende de infraestructura ni framework; los núcleos de contexto no dependen de tipos privados ajenos. |
| Una factura con existencias reservadas y fondos autorizados | La confirmación tiene éxito | Se produce exactamente una transición de factura, confirmación de inventario y contabilización en caja, conservando el comportamiento actual de API antes de la extracción. |
| Una dependencia agota el tiempo de espera después de procesar un comando | Se reintenta la misma clave de operación | Se resuelve o devuelve el resultado previo; no se duplican existencias ni caja. |
| Una clave reutilizada con datos modificados | Se intenta una escritura | El propietario rechaza la discrepancia con un código estable de conflicto. |
| Confirmaciones o descuentos de existencias simultáneos | Ambas peticiones compiten | Se mantienen las invariantes del agregado y las existencias no negativas mediante control de concurrencia del propietario. |
| Un flujo falla después de un efecto remoto confirmado | Se reanuda la recuperación | El estado durable identifica el efecto y realiza un reintento o compensación aprobado; sin falso éxito ni eliminación financiera silenciosa. |
| Un actor sin autorización o un recurso de otro tenant | Se llama a una capacidad directamente o mediante la fachada | El propietario deniega acceso y emite un registro de auditoría seguro. |
| Referencias existentes de clientes y productos en facturas | Los datos migran a almacenamiento con propietarios separados | Se concilian identificadores e instantáneas históricas; no quedan claves foráneas entre servicios ni escrituras compartidas. |
| El contrato existente del frontend | La implementación de un proveedor cambia de local a remota | Se siguen superando contratos de éxito y error entre consumidor/proveedor y de extremo a extremo. |
| Clases de dominio, aplicación y DTO | Pruebas unitarias sin framework las construyen | No se requiere contexto Spring, cliente HTTP, modelo JPA ni DTO con servicios inyectados. |

## Diseño de verificación

Añadir ArchUnit como dependencia de pruebas en una implementación futura autorizada, con una versión verificada compatible con Java 24. Las reglas deben operar sobre clases compiladas de producción para cubrir referencias completamente cualificadas y firmas. Prohibir paquetes de framework en dominio y aplicación; prohibir dominio→aplicación/infraestructura y aplicación→infraestructura; permitir núcleo→dominio/aplicación propios y una lista explícita de primitivas permitidas; prohibir tipos privados de contextos ajenos y todos los repositorios y entidades ajenos. Los adaptadores de integración de infraestructura solo pueden depender de contratos de entrada exportados por el proveedor. Detectar ciclos entre contextos y prohibir Autowired en campos. Complementar reglas estáticas con comprobaciones de dependencias y constructores de DTO y pruebas de integración de composición y decoradores transaccionales. No usar únicamente nombres de paquetes como prueba del comportamiento correcto en ejecución.

Usar pruebas de integración con MySQL real para concurrencia de existencias, asignación de pagos, aislamiento y migraciones. Actualmente `pom.xml:249` excluye `StockReservationIntegrationTest.java` en el ejecutor configurado: asegurar que la futura canalización de verificación ejecuta explícitamente esa cobertura con una configuración Docker/Testcontainers compatible, sin asumir que la suite predeterminada la cubre. Existe configuración JaCoCo en `pom.xml:254`; esta auditoría no midió la cobertura actual.

Cobertura de líneas medida requerida: dominio >90%, aplicación >80%, infraestructura >60%, frontend >75%. Publicar informes por paquete y evidencia significativa de ramas e invariantes; los umbrales no sustituyen las comprobaciones de aceptación. Verificar estados vacíos, de carga y de error del frontend cuando los cambios de contrato afecten a la interfaz. Informar con sinceridad de ejecutores no disponibles, rutas de integración sin cobertura y advertencias. Las comprobaciones documentales de esta entrega son evidencia de rutas y líneas y revisión del contenido; no se afirma haber implementado la arquitectura ni haber superado compilaciones, pruebas o cobertura.

## Riesgos y decisiones pendientes

La propiedad contextual de saldos de crédito, devoluciones y autorización necesita confirmación del negocio. Los informes pueden causar demasiadas llamadas síncronas a múltiples servicios; medir antes de introducir proyecciones replicadas. Separar factura, existencias y finanzas pierde atomicidad local y aumenta la complejidad de recuperación. Un despliegue compartido, contención de datos o dependencias completamente cualificadas ocultas pueden persistir después de renombrar puertos, por lo que superar reglas de importación no basta. Una puerta de enlace/BFF puede conservar contratos, pero se convierte en cuello de botella si contiene reglas de negocio. Preferir menos servicios cohesivos a una fragmentación prematura; documentar el beneficio operativo medido, las invariantes de datos y las limitaciones de reversión de cada extracción antes de aprobarla.
