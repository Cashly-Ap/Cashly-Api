# Verificación del refactor modular y hexagonal

Fecha: 2026-10-08. Estado: Apply/Verify completado; 5.5 resuelta y Judgment Day Round 2 aprobado por dos revisores independientes. Sin commit ni push.

## Evidencia acumulada

- Baseline histórico: suite completa Docker con resultados guardados fuera del repositorio en `../evidence/`. Las cifras de esa ejecución preceden a las tandas actuales y no se usan como resultado final.
- Inventario: Red funcional siete fallos; reservas/frontera cuatro fallos. Green de 92 pruebas del foco. Se sustituyeron repositorios mock por gateways/adaptadores/servicios reales conservando assertions de stock y movimientos.
- Auth: Green 408 pruebas del módulo; gateways de sujeto y separación Spring del Domain.
- Notifications: pruebas Red/Green para entrega después de commit, rollback y errores WebSocket. La entrega no dispone de reintentos durables.
- Dashboard: consulta JDBC trasladada a gateway; se preservan mapeo, límite y errores SQL.
- PaymentMethods: 75 pruebas de módulo, providers y contexto Spring sin colisión de beans.
- Warehouses y Purchases: contratos para reservas y movimientos entre módulos; recepción y reversión mediante gateway Warehouse.
- Receivables/Reports: proyecciones tipadas, paginación neutral y contratos de consulta; contenido PDF preservado en pruebas.
- Products: gateways para stock/contexto/precios y consultas de referencias públicas por cada módulo propietario; 163 pruebas del foco.
- Invoices Authorization: gateways propios de identidad, saldo, liquidación, caja y notificación; 1.570 pruebas de regresión del foco.
- Orders/Quotes: conversiones por APIs neutrales; se preserva producto asignado, fecha/plazo y ausencia de efectos posteriores ante fallo de autorización.
- Warehouses 2.4b: inserción idempotente de stock ausente y locks de transferencias ordenados. IT MySQL 8.0.46, 27 pruebas, incluye dos regresiones de concurrencia.
- Payments 4.3: contratos propios para facturas, clientes/saldo, identidad, caja y notificaciones; 214 pruebas focalizadas en Docker.
- Suppliers/Purchases 5.3b: Suppliers expone APIs de directorio/uso; `ListSuppliersUseCase` consulta en lote los IDs con compras y evita la consulta en página vacía; el borrado conserva consulta individual. RED: 7 pruebas, 1 fallo esperado al observar flag falso con la estrategia individual. GREEN: `docker compose --profile test run --rm -T app-test ./mvnw '-Dtest=com.cashly.cashly_api.suppliers.**,com.cashly.cashly_api.purchases.**' test`, 271 pruebas, 0 fallos/errores/skips, BUILD SUCCESS (exit 0). `git diff --check` exit 0; contenedor terminado y target libre.
- Customers 5.2c: contratos de actor/caja, gateway Warehouse y permisos en HTTP; 7 pruebas focalizadas.

## Baseline Flyway y seed

El 2026-10-08 se consolidaron las migraciones Flyway como esquema de creación desde cero, incluida V25 para snapshots de categoría. La historia anterior V24–V28 y algunas migraciones iniciales se sustituyeron conforme a la instrucción explícita de reiniciar las bases porque Cashly no está en producción; los entornos con checksums/versiones anteriores requieren reset y no se consideran una ruta de actualización compatible. La base de test Docker se eliminó y recreó desde almacenamiento vacío. Flyway aplicó V1–V25 y Hibernate/JPA validó el esquema.

Se cargó el seed canónico `db/seed.sql` después de Flyway: 6 usuarios, 11 clientes, 25 productos y 7 facturas. Login de seed HTTP 200; `GET /api/customers?page=0&size=1` autenticado HTTP 200 y anónimo HTTP 401.

## Returns 4.5e: rollback financiero con MySQL

`ReturnsFinancialRollbackMySqlIT` corre en MySQL 8.0.46 aislado. Flyway validó V1–V25 y Hibernate validó entidades. Los cuatro escenarios inyectan fallo tras persistir caja, stock, ajuste de factura y saldo; en todos se revierte la devolución, sus secuencias y escrituras financieras. Las asignaciones/pagos permanecen intactos. Resultado: 4 pruebas, 0 fallos, 0 errores, 0 omitidas.

La cancelación automática de pago aplicado se excluyó porque `PaymentAllocationService.cancelPayment` lanza `IllegalStateException: Cannot cancel an applied payment`. No se modificó Payments dentro de esta tanda.

## Verificación integrada final (2026-10-08)

Se eliminó y recreó `mysql-test` antes de la prueba integrada; el servicio usa `tmpfs`, por lo que la base comenzó vacía. `docker compose --env-file ../.env --profile test run --rm -T app-test ./mvnw -B verify`: BUILD SUCCESS; 3.136 pruebas, 0 fallos, 0 errores, 0 omitidas. Se compilaron en Docker 1.248 fuentes principales y 572 de test, y se generó el JAR ejecutable. `LayerDependencyTest`: 4/4.

IT MySQL explícitos desde esquema V1–V25 recién creado: `OrderHistoricalCategoryMySqlIT` 3/3, `ProductPersistenceQueriesMySqlIT` 2/2 y `ReturnsFinancialRollbackMySqlIT` 4/4; total 9/9. La prueba de Returns requiere su Compose dedicado o `returns.mysql.*`; al pasar esas propiedades al servicio `app-test`, validó la misma base MySQL 8.0.46 limpia.

Cobertura JaCoCo backend: Domain 94,12% (2.945/3.129 líneas), Application 89,92% (4.479/4.981) e Infrastructure 70,90% (5.852/8.254); los tres umbrales se cumplen. La cobertura frontend previa es 59,69%, inferior al 75%; no hubo cambios frontend en esta tanda. Git `diff --check` no detectó whitespace, aunque informó conversiones LF/CRLF en Windows.

## Products 5.1c: consultas JPQL y wiring JPA en MySQL

Se añadió `ProductPersistenceQueriesMySqlIT` con `@DataJpaTest`, MySQL 8.0.46, Flyway V1–V24 y `ddl-auto=validate`. El test carga categorías, tipos de precio, productos, componentes y precios, y ejecuta filtros paginados, consultas de pedidos con/sin stock, carga de componentes, SQL nativo `BIN_TO_UUID`, borrado por componente y consultas de uso de categoría/tipo de precio. Resultado Docker: 2 pruebas, 0 fallos, 0 errores, 0 omitidas, BUILD SUCCESS. La primera aserción de componente esperaba el ID padre cuando el contrato devuelve el ID del componente; se corrigió el test y pasó sin cambios de producción.

## Suppliers/Purchases 5.3b: consulta de uso en lote

`ListSuppliersUseCase` obtiene los IDs de proveedores de la página y hace una consulta por lote para marcar los que tienen compras; el borrado conserva la comprobación individual. La regresión Red ejecutó 7 pruebas con un fallo esperado (el listado no usaba batch). Green/regresión focalizada Docker: 271 pruebas, 0 fallos, 0 errores, 0 omitidas, BUILD SUCCESS. Se añadió cobertura para página vacía y ausencia de llamadas individuales desde el listado.

## Judgment Day y cierre SDD

Dos jueces ciegos coincidieron en Round 1: (1) la historia anterior no es compatible con bases que conservaran migraciones/checksums previos; se acepta ese corte porque el usuario pidió explícitamente reiniciar y consolidar las migraciones al no estar en producción; (2) V25 debía rellenar snapshots históricos. Se corrigió el segundo hallazgo con backfill en V25 y en seeds, y se añadió una regresión MySQL que cambia la categoría del producto y comprueba pedidos y ofertas.

Dos revisores independientes realizaron Round 2 sobre el cambio completo y la corrección de snapshots; ambos informaron `NONE` y no encontraron hallazgos accionables. Veredicto: `JUDGMENT: APPROVED`.

Tareas 1.1–6.2 completadas. 5.5 queda verificada con migración, seed y 3 IT MySQL de categorías; 6.3 queda aprobada tras revisión doble. El esquema limpio se aplicó en MySQL Docker y el seed quedó comprobado con snapshots 33/33 para pedidos y 20/20 para ofertas. La historia Flyway anterior requiere reinicio conforme a la instrucción del usuario.
