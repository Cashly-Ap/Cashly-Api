# Propuesta: aislamiento modular y arquitectura hexagonal

Estado: completado. Modo SDD: completo. Persistencia: OpenSpec. Fecha: 2026-10-06.

## Intención

Corregir los 32 puntos de `docs/AUDITORIA-ARQUITECTURA-MODULAR.md` mediante entregas verificables. Mantener endpoints, respuestas, permisos y reglas financieras salvo correcciones explícitamente especificadas. No sustituir el monolito por servicios remotos durante este refactor.

## Capacidades

- Propiedad de inventario, reservas y movimientos dentro de Warehouses.
- Contratos públicos de cada proveedor; gateways propios del consumidor en `application/contracts`.
- Domain independiente de framework y Application independiente de Infrastructure.
- Consultas, transporte, seguridad, storage y persistencia aislados.
- Protección de regresión mediante TDD, contratos y pruebas de arquitectura.
- Capacidad OpenSpec: `modular-architecture`.

## Alcance

Todos los módulos enumerados en la auditoría, con alcance de monolito modular; la extracción a servicios remotos queda para una iniciativa posterior. La implementación y verificación de las tareas están registradas en `tasks.md` y `verification.md`.

Engram no está disponible en este entorno.

## Riesgos y rollback

Cambiar wiring puede romper contexto Spring; mover stock puede alterar metadata/atomicidad; separar consultas puede cambiar paginación; diferir delivery puede perder notificaciones si falla el transporte. Se conservaron assertions de regresión, pruebas específicas y transacciones locales. Por instrucción expresa del usuario, se consolidó la historia Flyway para instalación limpia porque Cashly no está en producción; bases que conserven la historia anterior deben recrearse. No se introdujeron Domain Events. Revertir cada unidad de cambio requiere revisar su diff sin borrar trabajo ajeno.

No hay autorización nueva de commit/push. La implementación queda revisable en el checkout.
