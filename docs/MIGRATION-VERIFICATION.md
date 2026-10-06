# Verificación de la migración Cashly

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
| Domain | 2.898 / 3.211 | 90,25 % | >90 % | CUMPLE |
| Application | 4.373 / 5.752 | 76,03 % | >80 % | INFERIOR AL OBJETIVO |
| Infrastructure | 3.713 / 7.541 | 49,24 % | >60 % | INFERIOR AL OBJETIVO |
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

En la verificación inicial no se ejecutó un flujo de negocio autenticado satisfactorio porque la base aislada nueva todavía no contenía usuarios. La carga SQL posterior se verifica en la sección siguiente. La evidencia de login no cubre todos los estados de carga/vacío/error del ERP. Los endpoints conservan su comportamiento existente; los cambios de este alcance son marca y configuración.

## Seed SQL de desarrollo

Se ha cargado el seed existente `db/seed.sql` directamente en el contenedor MySQL, con `db/seeds` montado en `/seeds`, después de las migraciones Flyway. Se eliminó el script PowerShell y las variables de administrador añadidas anteriormente.

La carga terminó sin errores SQL. Resultado: 6 usuarios, 11 clientes y 7 facturas de demostración. Las credenciales de acceso son las definidas en `db/seeds/01_users.sql`. No se modificaron migraciones Flyway.
## Verificación final

La imagen final `cashly-app:local` se construyó correctamente con instalación congelada desde `bun.lock`. La compilación Angular de producción final pasó, al igual que 37 pruebas enfocadas: 21 de login y 16 de main-layout (`cashly-final-app-check.log`). La API rechazó `{}` en `/api/auth/login` con HTTP 400. La captura final contiene el login vacío sin validaciones activadas. No hay diferencias preparadas en migraciones Flyway aplicadas.

La ejecución Docker V8 terminó con código 0: 257 archivos y 2.541 pruebas correctas. Su tabla global muestra 59,68 % de líneas/statements, 84,25 % de branches y 70,30 % de functions; el frontend no alcanza >75 % de líneas. La suma exacta de contadores LCOV es 16.414/27.499 líneas (59,69 % redondeado); la tabla de consola V8 publica 59,68 %. Ambos resultados quedan por debajo del objetivo. Los informes HTML, JSON y LCOV se copiaron a `coverage-output/` en el directorio de trabajo. Los porcentajes reflejan las exclusiones y el descubrimiento configurados en `vitest.config.mts`; no son cobertura exhaustiva de todos los flujos de negocio.
