# Tareas por módulo

Estado: Apply, tandas incrementales. `[x]` significa verificado; `[ ]` pendiente. Cada tarea ejecutable requiere Red → Green → Refactor y regresión del módulo. Rutas bajo `src/main/java/com/cashly/cashly_api`.

## 1. Preparación
- [x] 1.1 Explore: auditoría A01–A32 y comprobación de stack/estado heredado.
- [x] 1.2 Propose/Spec/Design: persistir contratos, escenarios y plan.
- [x] 1.3 Baseline: suite completa Docker y registrar exclusiones/cobertura.
- [x] 1.4 Flyway consolidado en baseline V1–V25; base MySQL de pruebas recreada desde vacío, esquema/JPA y seed canónico verificados.

## 2. Inventario e Invoices
- [x] 2.1 Warehouses `application/api`: operaciones de stock de factura y tests R2.
- [x] 2.2 Invoices `ConfirmInvoiceUseCase`/`CancelInvoiceUseCase`: `InventoryGateway`, adaptador, regresión R2.
- [x] 2.3 Invoices Create/Update/Duplicate/Delete: reservas mediante gateway, stock ausente/concurrente.
- [x] 2.4a Warehouses Entry/Exit/Adjustment/Transfer: locks consistentes y dos regresiones MySQL.
- [x] 2.4b Warehouses: catálogo neutral ya existente; creación de stock ausente atómica y bloqueos de transferencias con orden determinista; IT MySQL real.
- [x] 2.5 Purchases Confirm/Cancel: recepción/reversión mediante contrato Warehouse.

## 3. Identidad, entrega y Dashboard
- [x] 3.1 Auth RefreshToken: gateway de sujeto, adaptador, tests R3.
- [x] 3.2 Auth Domain: retirar Spring; API de destinatarios, wiring y regresión.
- [x] 3.3 Notifications: delivery/recipients gateways, afterCommit y tests R4.
- [x] 3.4 Dashboard: query gateway y adaptador JDBC; consultas/errores R5.

## 4. Finanzas y ventas
- [x] 4.1a Payments: política de crédito pura separada de consulta Application; cuatro tests nuevos.
- [x] 4.1b Customers/Payments: crédito por gateways y queries neutrales; 753 pruebas del foco.
- [x] 4.1c Receivables: aislar ownership y consulta financiera al extraer servicios; verificado en `clean verify` Docker.
- [x] 4.2 Invoices Authorization: identidad/saldo/pago/caja mediante gateways; 1.570 pruebas de regresión del foco.
- [x] 4.3 Payments: liquidación, aprobación/rechazo, saldo/caja/notificaciones contractuales; 214 pruebas focalizadas pasan en Docker.
- [x] 4.4a Orders/Quotes: conversiones Order→Invoice y Quote→Order/Invoice por APIs neutrales; pruebas originales y escenarios adicionales preservados.
- [x] 4.4b Orders/Quotes: creación, edición, consultas y resto del ciclo por contratos propios; verificado en `clean verify` Docker.
- [x] 4.5a Returns: ajuste de Invoice mediante API/gateway; Invoice Domain sin dependencia Returns.
- [x] 4.5b Returns: stock mediante gateway y API Warehouses, 128 tests del foco.
- [x] 4.5c Returns: referencia de factura UUID neutral, 122 pruebas Returns pasan.
- [x] 4.5d Returns: gateways de pago/saldo/caja y lecturas propias; 1.253 pruebas de regresión del foco.
- [x] 4.5e Returns: rollback atómico comprobado con MySQL real al fallar tras caja, stock, ajuste de factura y saldo; IT 4/4 verde. El flujo automático con cancelación de pagos queda fuera por el error actual de Payments `Cannot cancel an applied payment`.
- [x] 4.6 Credit/Debit Notes: posición financiera/elegibilidad y ajustes por contratos; verificado en `clean verify` Docker.
- [x] 4.7 Cashregister: separar agregado de reporting; neutralizar DTOs/Page Domain.

## 5. Catálogos y consultas
- [x] 5.1a Products: gateways stock/contexto/precios, DTOs locales y referencias JPA por ID; 152 pruebas del módulo.
- [x] 5.1b Products: filtros de disponibilidad y referencias por consultas públicas de cada propietario; 163 pruebas del módulo.
- [x] 5.1c Products: consultas JPQL/nativas y wiring verificados en MySQL 8.0.46; IT 2/2.
- [x] 5.2a Customers: hashing por contrato y paginación neutral de saldo; 361 tests del módulo pasan.
- [x] 5.2b Customers: directorio de almacenes por gateway/API pública, registro y accesos verificados.
- [x] 5.2c Customers: contratos propios para actor y caja; gateway de almacenes existente; permisos definidos en infraestructura HTTP; Customers Application sin tipos de otros módulos ni flujo de autenticación de usuarios; 7 pruebas enfocadas pasan en Docker.
- [x] 5.3a Suppliers: uso de compras por gateway/API pública; 256 tests Supplier/Purchase pasan.
- [x] 5.3b Suppliers/Purchases: directorio y uso proveedor mediante contratos propios y lectura por lote; 271 pruebas del foco pasan en Docker.
- [x] 5.4a PaymentMethods: uso externo mediante gateways/API pública y wiring probado.
- [x] 5.4b Categories/PriceTypes: consultas de uso externo fuera de repositorios propios.
- [x] 5.5 Orders/Product queries: preservar filtros/paginación y categoría histórica/actual; backfill en V25, seeds y regresión MySQL para pedidos/ofertas.
- [x] 5.6a Shared: PageResponse neutral y mapper Spring en Infrastructure; tests de metadatos preservados.
- [x] 5.6b Shared: CreditGuard retirado; comprobación propia Customers y gateways consumidores Invoice/Order.
- [x] 5.6c Shared: composición stock retirada, provider Warehouses y gateways consumidores propios.
- [x] 5.7 Mappers: resolver lecturas antes de transformación; traducir errores técnicos; verificado en `clean verify` Docker.
- [x] 5.8a Receivables: query gateway Application, paginación neutral y proyección tipada de cuentas; 81 tests del foco pasan.
- [x] 5.8b.i Reports Invoices: DTOs propios, query gateway y API neutral, contenido PDF conservado.
- [x] 5.8b.ii Reports/Receivables: consultas y proyecciones propias para Orders, Quotes, Payments, Purchases y cuentas por cobrar; catálogo con gateway de archivos.

## 6. Cierre
- [x] 6.1a Reglas Domain/Application contracts: Red en baseline, Green integrado.
- [x] 6.1b Reglas intermodulares estrictas; tests de contratos y concurrencia; `LayerDependencyTest` 4/4.
- [x] 6.2 `clean verify` Docker: 3.136 pruebas verdes; JAR generado; V1–V25 aplicadas desde MySQL vacía. 9 IT MySQL verdes (3 de categorías históricas, 2 de consultas de producto y 4 de rollback financiero). Seed completo cargado: snapshots 33/33 pedidos y 20/20 ofertas. Cobertura backend cumple umbrales; cobertura frontend y límite de cancelación de pagos documentados.
- [x] 6.3 Judgment Day ciego doble: dos revisores sin hallazgos, `JUDGMENT: APPROVED`; especificación sincronizada y cambio listo para archivar.

## Previsión de revisión
Alcance >400 líneas: riesgo alto. Recomendada revisión por tandas, sin PR/commit automático. Estado y evidencia en `verification.md`; documentación general en `docs/AUDITORIA-ARQUITECTURA-MODULAR.md`.
