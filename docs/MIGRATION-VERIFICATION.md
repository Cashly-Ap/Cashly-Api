# Verificación de la migración Cashly

## Estado de reconstrucción de esquema: 2026-10-08

Estado actual verificado el 2026-10-08: Flyway aplica V1–V25 desde una base MySQL vacía. V25 crea las tablas de snapshots históricos y rellena las categorías existentes de pedidos y ofertas. Hibernate validó JPA. El seed canónico cargó correctamente: 6 usuarios, 11 clientes, 25 productos y 7 facturas; además, los 33 renglones de pedidos y 20 de ofertas tienen snapshot de categoría. `clean verify` de API pasó 3.136 pruebas, con el JAR generado. Las cifras y la descripción anteriores de V1–V24 son evidencia histórica y no describen el esquema vigente.

Verificado el 2026-10-06. Alcance: cambio de nombre de App/API, configuración Docker local y sustitución de marca. La especificación de microservicios y arquitectura hexagonal propone una implementación futura; esta migración no ejecuta ni certifica esa transformación.

## Evidencia automática

| Comprobación | Resultado observado | Evidencia |
| --- | --- | --- |
| Maven verify de API en Docker | CORRECTO: 2.892 pruebas, cero fallos/errores/omitidas; BUILD SUCCESS | `cashly-api-check.log` en el directorio de trabajo |
| Compilación de producción de App en Docker | CORRECTO: Angular generó el bundle | `cashly-app-check.log` |
| Regresión Vitest de App en Docker | CORRECTO: 257 archivos, 2.541 pruebas | `cashly-app-check.log` |
| Cobertura V8 de App en Docker | CORRECTO: 257 archivos y 2.541 pruebas; informe generado | `cashly-frontend-coverage.log` |

El runner Maven predeterminado excluye explícitamente `StockReservationIntegrationTest.java` (`pom.xml:249`). El éxito de la suite predeterminada no demuestra la ejecución de ese escenario de integración. Las pruebas existentes aportan evidencia de regresión, pero no aceptación exhaustiva del ERP.

## Cobertura medida y límites de calidad

Los contadores de líneas JaCoCo proceden de `api-coverage/jacoco.xml` y se agregaron por segmento de paquete `/domain/`, `/application/` e `/infrastructure/`. Son totales por capa; no certifican cada paquete individual.

| Capa | Líneas cubiertas / totales | Cobertura de líneas | Requisito | Resultado |
| --- | ---: | ---: | ---: | --- |
| Domain | 2.945 / 3.129 | 94,12 % | >90 % | CUMPLE |
| Application | 4.479 / 4.981 | 89,92 % | >80 % | CUMPLE |
| Infrastructure | 5.852 / 8.254 | 70,90 % | >60 % | CUMPLE |
| Frontend (LCOV) | 16.414 / 27.499 | 59,69 % | >75 % | INFERIOR AL OBJETIVO |

Las carencias de Application, Infrastructure y Frontend siguen pendientes en la suite existente. Esta migración de nombre/configuración no incluye una expansión indiscriminada de pruebas ajenas al cambio. Los umbrales no sustituyen la evidencia de comportamiento, concurrencia, seguridad y contratos.

## Revisión de configuración y especificación

`compose.cashly.yaml` define un entorno local de desarrollo: procesos de desarrollo de API/App, MySQL, MinIO e inicialización del bucket. No es un manifiesto de despliegue de producción. Los puertos del host se limitan a loopback; las credenciales obligatorias usan variables de entorno; MySQL/MinIO usan volúmenes persistentes. La caché Maven externa debe existir antes del arranque. El código de App/API se copia a las imágenes; los cambios de código requieren reconstruirlas.

El proxy Angular activo es `proxy.conf.js`, seleccionado por `angular.json`. Su destino API/WebSocket `cashly-api-app:8080` coincide con el alias de red de Compose. El archivo alternativo `proxy.conf.json` no está seleccionado por esa configuración de serve.

`MICROSERVICES-HEXAGONAL-SPEC.md` diferencia problemas actuales y reglas objetivo, puertos del consumidor y composición de infraestructura, y transacciones locales y recuperación distribuida duradera. El inventario de imports declara que no cubre referencias completamente cualificadas/transitivas; no certifica la arquitectura. Las migraciones aplicadas deben conservarse. La extracción futura necesita su propio SDD, reglas sobre dependencias compiladas, pruebas de contrato/fallos y evidencia de propiedad de datos/cutover. Domain Events siguen sujetos a autorización específica.

## Advertencias y verificación manual

La compilación Angular de producción avisa de una dependencia CommonJS de `@stomp/stompjs`. El orquestador observó también un aviso de postinstall bloqueado por Bun durante la instalación. Compilar y pasar pruebas no significa que esas advertencias estén eliminadas.

El orquestador aportó la evidencia manual de arranque, API y login registrada abajo. No se certifican todos los flujos ERP, cada pantalla vacía/cargando/error, el despliegue de producción, los contratos de microservicios ni el cumplimiento hexagonal estricto. No se afirma un archivo SDD ni aprobación Judgment Day.

La cobertura se ejecuta sobre la imagen anterior al último ajuste de proporciones/logo y lockfile. Esos ajustes posteriores no cambian la lógica TypeScript probada; su comprobación visual y la reconstrucción final se registran por separado.

## Evidencia manual aportada por el orquestador

- Compose: API, App, MySQL y MinIO saludables; `minio-init` terminó con código 0.
- API: `/actuator/health` respondió `{"status":"UP"}`; `/api/users` sin autenticación respondió HTTP 401.
- Chrome en `http://localhost:4201/auth/login`: título Cashly y logo blanco/negro visible con proporción correcta.
- Formulario vacío: botón de acceso deshabilitado. Email inválido: mensaje `Invalid email format` y botón deshabilitado.
- Email inexistente y contraseña incorrecta: respuesta real por proxy/API y aviso `Error de autenticación` / `Credenciales incorrectas. Verifica tu email y contraseña.`
- El registro de navegador no mostró warnings ni errores. Captura: `evidence/cashly-login.png`.

En la verificación inicial no se ejecutó un flujo de negocio autenticado satisfactorio porque la base aislada nueva todavía no contenía usuarios. La evidencia de login no cubre todos los estados de carga/vacío/error del ERP. Los endpoints conservan su comportamiento existente; los cambios de este alcance son marca y configuración.

## Seed Java de desarrollo

El mecanismo SQL histórico fue reemplazado por `DatabaseSeedRunner` y 29 pasos Java ordenados. En una base MySQL recreada desde cero, Flyway validó y aplicó las 29 migraciones y, con `CASHLY_SEED_ENABLED=true`, el runner completó los 29 pasos sin errores. La configuración ausente utiliza `false` y producción fuerza el seed deshabilitado.
## Verificación final

La imagen `cashly-app:local` se construyó correctamente con instalación congelada desde `bun.lock`. Esa captura histórica cubrió 37 pruebas enfocadas y precede a la suite completa ejecutada el 2026-10-08. Las conclusiones de ese momento no representan el esquema Flyway actual.

La ejecución Docker V8 terminó con código 0: 257 archivos y 2.541 pruebas correctas. Su tabla global muestra 59,68 % de líneas/statements, 84,25 % de branches y 70,30 % de functions; el frontend no alcanza >75 % de líneas. La suma exacta de contadores LCOV es 16.414/27.499 líneas (59,69 % redondeado); la tabla de consola V8 publica 59,68 %. Ambos resultados quedan por debajo del objetivo. Los informes HTML, JSON y LCOV se copiaron a `coverage-output/` en el directorio de trabajo. Los porcentajes reflejan las exclusiones y el descubrimiento configurados en `vitest.config.mts`; no son cobertura exhaustiva de todos los flujos de negocio.


## Integración de devoluciones (2026-10-08)

ReturnsFinancialRollbackMySqlIT ejecutó 4 escenarios en MySQL 8.0.46 aislado, verificando rollback tras caja, stock, ajuste de factura y saldo. Queda fuera la cancelación automática de pagos aplicados porque Payments lanza IllegalStateException: Cannot cancel an applied payment; no se modificó ese flujo.

`OrderHistoricalCategoryMySqlIT` ejecutó 3 escenarios contra MySQL, incluida la modificación de categoría de un producto tras crear líneas de pedido y oferta. Los documentos conservaron la categoría original mediante snapshots. `ProductPersistenceQueriesMySqlIT` ejecutó 2 escenarios. En total, los IT dirigidos a MySQL para estos tres casos ejecutaron 9 tests sin fallos.
