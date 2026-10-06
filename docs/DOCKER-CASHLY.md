# Entorno Docker local de Cashly

## Alcance

`compose.cashly.yaml` levanta App, API, MySQL y MinIO con datos separados de otros proyectos. `minio-init` crea el bucket y termina correctamente; no es un servicio permanente. Este entorno es de desarrollo. No representa un despliegue de producción ni una extracción a microservicios.

Clona ambos repositorios en carpetas hermanas `Cashly-Api` y `Cashly-App`. El directorio de esta entrega incluye `compose.yaml`, que carga la configuración de la API; también puedes ejecutar directamente el archivo del repositorio:

```powershell
docker volume create cashly-maven-cache
docker compose --env-file ..\.env -f compose.cashly.yaml up -d --build
```

Este comando se ejecuta desde `Cashly-Api`. Las rutas de construcción se resuelven respecto a `compose.cashly.yaml`.

## Variables locales

El `.env` de la carpeta Cashly se ha generado con valores aleatorios y permanece fuera de Git. En una instalación nueva, crea ese archivo a partir de `.env.cashly.example` y rellena `CASHLY_DB_PASSWORD`, `CASHLY_DB_ROOT_PASSWORD`, `CASHLY_STORAGE_PASSWORD` y `CASHLY_JWT_SECRET`. Usa secretos distintos; JWT debe tener al menos 32 bytes de entropía. No reutilices secretos de otro proyecto ni pongas credenciales en URLs.

Compose exige estas variables. El volumen externo `cashly-maven-cache` solo guarda dependencias Maven; se crea con el comando anterior.

| Servicio | Contenedor | Acceso local | Datos |
| --- | --- | --- | --- |
| App | `cashly-app` | `http://localhost:4201` | Imagen y dependencias propias |
| API | `cashly-api` | `http://localhost:8081` | Java 24 / Spring Boot 3.5.5 |
| MySQL | `cashly-mysql` | `127.0.0.1:3308` | Base `cashly`, volumen `cashly_mysql_data` |
| MinIO API | `cashly-minio` | `http://localhost:9002` | Bucket `cashly-files`, volumen `cashly_minio_data` |
| MinIO consola | `cashly-minio` | `http://localhost:9003` | Usuario `cashly`, contraseña `CASHLY_STORAGE_PASSWORD` |

Los puertos se publican únicamente en la interfaz de loopback. App consume API mediante su proxy `/api` y `/ws`, con el alias interno `cashly-api-app`. Las comprobaciones de salud ordenan el arranque: MySQL/MinIO → bucket → API → App.

## Datos y arranque

Flyway crea el esquema al iniciar API. Los SQL heredados de `db/init` no se montan durante la inicialización de MySQL: contienen INSERT sobre tablas que todavía no existen antes de Flyway. No se importan usuarios, contraseñas, facturas ni archivos del proyecto original.

### Seed SQL de desarrollo

El seed existente `db/seed.sql` carga los archivos de `db/seeds` en orden. Docker monta el archivo como `/seed.sql` y la carpeta como `/seeds`. Con API iniciada y las migraciones Flyway aplicadas, carga los datos directamente en MySQL desde Cashly:

```powershell
docker compose --env-file .env exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" mysql --default-character-set=utf8mb4 --user="$MYSQL_USER" "$MYSQL_DATABASE" < /seed.sql'
```

Acceso local: `superadmin@cashly.com`, contraseña `49510561Gabriel.`, rol `SUPER_ADMIN`. El seed también crea usuarios y datos de demostración. Reinicializa las tablas indicadas por los SQL: úsalo sobre el entorno de desarrollo, no sobre datos reales. No se cargará antes de Flyway ni se ejecutará automáticamente en cada arranque.
MinIO se construye desde las versiones fijadas del servidor y cliente en `Dockerfile.minio`, porque la descarga del repositorio de imágenes original no estaba disponible durante esta entrega. App usa Bun 1.3.3 y `bun.lock` para instalaciones reproducibles. Los cambios de código requieren reconstruir la imagen; este Compose no monta el código del host ni ofrece recarga automática.

## Operación

Desde la carpeta Cashly:

```powershell
docker compose --env-file .env config --quiet
docker compose --env-file .env up -d --build
docker compose --env-file .env ps
docker compose --env-file .env logs --tail 100 api app
docker compose --env-file .env build app
docker compose --env-file .env up -d app
docker compose --env-file .env stop
```

`stop` conserva contenedores y volúmenes. `down` retira contenedores y red, manteniendo volúmenes. No añadas `-v` si necesitas conservar los datos.

## Comprobación

```powershell
docker compose --env-file .env exec api curl --fail --silent http://localhost:8080/actuator/health
docker compose --env-file .env exec app node node_modules/@angular/cli/bin/ng.js build --configuration production
docker compose --env-file .env exec app node node_modules/vitest/vitest.mjs run --coverage
```

La salida de salud debe indicar `UP`. Para pruebas API, usa un contenedor separado del proceso de aplicación y ejecuta `mvn verify`; no ejecutes Flyway de pruebas contra la base local activa. Los resultados observados de esta entrega se documentan en [MIGRATION-VERIFICATION.md](MIGRATION-VERIFICATION.md).
