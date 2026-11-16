# Auditoría — Guía de integración Frontend

Módulo backend: `platform/audit`  
Base URL: `/api/audit-events`

---

## 1. Permiso

Todas las consultas requieren:

```text
AUDIT_TRAIL_VIEW_LIST
```

Si el usuario no lo tiene, la API responde `403 Forbidden`.

---

## 2. Endpoints

### 2.1 Historial paginado

```http
GET /api/audit-events
```

#### Query params

| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `entityType` | string | Sí, salvo modo global | Tipo de entidad. Ej: `INVOICE`, `ORDER` |
| `entityId` | UUID | Sí cuando hay `entityType` | Registro concreto |
| `module` | string | No | Modo global/menubar. Ej: `finance`, `sales` |
| `action` | string / repetible | No | Multiselección. Ej: `action=CONFIRM&action=PAY` o `action=CONFIRM,PAY` |
| `actorId` | UUID | No | Filtro por usuario |
| `from` | `YYYY-MM-DD` | No | Inicio de rango, inclusivo |
| `to` | `YYYY-MM-DD` | No | Fin de rango, inclusivo |
| `page` | int | No | Default `0` |
| `size` | int | No | Default `20`, máximo `100` por defecto |

> El orden es siempre `occurredAt desc`. No hay parámetro `sort` en esta versión.

#### Modos de consulta

**Historial de un registro**

```http
GET /api/audit-events?entityType=INVOICE&entityId=550e8400-e29b-41d4-a716-446655440000&page=0&size=20
```

**Historial global de un módulo**

```http
GET /api/audit-events?module=finance&page=0&size=20
```

**Con filtros combinados**

```http
GET /api/audit-events?entityType=INVOICE&entityId=...&action=CONFIRM&action=PAY&actorId=...&from=2026-11-01&to=2026-11-08
```

---

### 2.2 Opciones de filtro

```http
GET /api/audit-events/filter-options
```

Mismos parámetros de alcance:

| Parámetro | Tipo | Obligatorio |
|---|---|---|
| `entityType` | string | Sí, salvo modo global |
| `entityId` | UUID | Sí cuando hay `entityType` |
| `module` | string | No |

Devuelve **solo los valores presentes** para ese registro/módulo, con conteos y rango de fechas. Evita selects vacíos.

---

### 2.3 Detalle de evento

```http
GET /api/audit-events/{id}
```

`id` es el UUID del evento, no el UUID de la entidad auditada.

---

## 3. Contratos TypeScript

### 3.1 Paginación

```ts
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

### 3.2 Evento de auditoría

```ts
export interface AuditEventResponse {
  id: string;
  module: string;
  entityType: AuditEntityType;
  entityId: string;
  entityLabel: string;
  secondaryLabel: string | null;
  action: AuditAction;
  actor: AuditActorResponse;
  occurredAt: string; // ISO-8601 UTC
  descriptionKey: string;
  descriptionArgs: string[];
  metadata: Record<string, unknown>;
}

export interface AuditActorResponse {
  id: string | null;
  name: string;
  photoUrl: string | null;
}
```

### 3.3 Opciones de filtro

```ts
export interface AuditFilterOptionsResponse {
  actions: AuditActionOption[];
  actors: AuditActorOption[];
  dateRange: AuditDateRange;
}

export interface AuditActionOption {
  value: AuditAction;
  labelKey: string; // ej: audit.actions.CONFIRM
  count: number;
}

export interface AuditActorOption {
  id: string;
  name: string;
  photoUrl: string | null;
  count: number;
}

export interface AuditDateRange {
  min: string | null; // ISO-8601 UTC
  max: string | null; // ISO-8601 UTC
}
```

---

## 4. Enums

### 4.1 `AuditAction`

```ts
export type AuditAction =
  | 'CREATE'
  | 'EDIT'
  | 'DELETE'
  | 'CONFIRM'
  | 'CANCEL'
  | 'DUPLICATE'
  | 'ISSUE'
  | 'CONVERT_TO_ORDER'
  | 'CONVERT_TO_INVOICE'
  | 'PAY'
  | 'PAY_ALL'
  | 'PARTIAL_PAYMENT'
  | 'PAY_DIRECT'
  | 'PAY_WITH_VALIDATION'
  | 'APPROVE'
  | 'REJECT'
  | 'OPEN'
  | 'CLOSE'
  | 'RECORD_ENTRY'
  | 'RECORD_EXIT'
  | 'RECORD_MOVEMENT'
  | 'ADJUST'
  | 'TRANSFER'
  | 'REFUND'
  | 'ADVANCE'
  | 'TOGGLE'
  | 'UPLOAD_PHOTO'
  | 'REMOVE_PHOTO'
  | 'BALANCE';
```

### 4.2 `AuditEntityType`

```ts
export type AuditEntityType =
  | 'QUOTE'
  | 'ORDER'
  | 'PURCHASE'
  | 'INVOICE'
  | 'INVOICE_LINE'
  | 'RETURN'
  | 'CREDIT_NOTE'
  | 'DEBIT_NOTE'
  | 'PAYMENT'
  | 'PAYMENT_ALLOCATION'
  | 'RECEIVABLE'
  | 'CASH_REGISTER'
  | 'CASH_MOVEMENT'
  | 'PRODUCT'
  | 'CATEGORY'
  | 'PRICE_TYPE'
  | 'WAREHOUSE'
  | 'CUSTOMER'
  | 'SUPPLIER'
  | 'USER'
  | 'ROLE'
  | 'DEPARTMENT'
  | 'COMPANY'
  | 'PAYMENT_METHOD';
```

---

## 5. Ejemplos de respuesta

### 5.1 Historial

```json
{
  "content": [
    {
      "id": "52d29ab1-7793-4b59-b1cf-4a1b5579591f",
      "module": "finance",
      "entityType": "INVOICE",
      "entityId": "85078c38-61e7-36b0-abe3-8e42751e242f",
      "entityLabel": "INV-2025-0001",
      "secondaryLabel": null,
      "action": "CONFIRM",
      "actor": {
        "id": "978c90a6-d968-398f-bdeb-35d06669fcae",
        "name": "Cashly Administrator",
        "photoUrl": null
      },
      "occurredAt": "2025-01-15T10:00:00Z",
      "descriptionKey": "audit.events.confirm.description",
      "descriptionArgs": ["INV-2025-0001"],
      "metadata": {
        "statusFrom": "DRAFT",
        "statusTo": "PENDING_PAYMENT",
        "documentNumber": "INV-2025-0001"
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

### 5.2 Opciones de filtro

```json
{
  "actions": [
    { "value": "CONFIRM", "labelKey": "audit.actions.CONFIRM", "count": 1 },
    { "value": "PAY", "labelKey": "audit.actions.PAY", "count": 1 }
  ],
  "actors": [
    {
      "id": "978c90a6-d968-398f-bdeb-35d06669fcae",
      "name": "Cashly Administrator",
      "photoUrl": null,
      "count": 1
    }
  ],
  "dateRange": {
    "min": "2025-01-15T10:00:00Z",
    "max": "2025-01-15T11:30:00Z"
  }
}
```

---

## 6. Errores

### 6.1 Formato

```json
{
  "timestamp": "2026-11-08T10:15:07.921",
  "status": 400,
  "code": "AUDIT_INVALID_FILTER",
  "message": "El rango de fechas no es válido"
}
```

### 6.2 Códigos

| HTTP | Código | Cuándo |
|---|---|---|
| 400 | `AUDIT_INVALID_FILTER` | Filtro inválido: rango, página, tamaño, acción o alcance |
| 404 | `AUDIT_EVENT_NOT_FOUND` | El evento no existe |
| 403 | — | Falta `AUDIT_TRAIL_VIEW_LIST` |

### 6.3 Claves i18n de error

```text
audit.errors.entityRequired
audit.errors.invalidRange
audit.errors.unknownEntityType
audit.errors.invalidPageSize
audit.errors.invalidPage
audit.errors.unknownAction
audit.errors.eventNotFound
```

---

## 7. i18n

### 7.1 Acciones

```text
audit.actions.<ACTION>
```

Ejemplos:

```text
audit.actions.CREATE
audit.actions.EDIT
audit.actions.CONFIRM
audit.actions.PAY
audit.actions.CONVERT_TO_INVOICE
```

### 7.2 Entidades

```text
audit.entities.<ENTITY_TYPE>
```

Ejemplos:

```text
audit.entities.INVOICE
audit.entities.ORDER
audit.entities.PRODUCT
audit.entities.CUSTOMER
```

### 7.3 Descripciones de evento

El backend devuelve `descriptionKey` + `descriptionArgs`. El frontend debe traducir:

```text
audit.events.<action_lower>.description
```

Con argumentos posicionales `{0}`, `{1}`, etc.

Ejemplo:

```json
{
  "descriptionKey": "audit.events.confirm.description",
  "descriptionArgs": ["INV-2025-0001"]
}
```

Render:

```text
Se confirmó INV-2025-0001
```

Si la clave no existe, usar como fallback:

```text
audit.actions.<ACTION>
```

---

## 8. Reglas de UI recomendadas

### 8.1 Historial por registro

Usar una pestaña o drawer “Historial” dentro del detalle de cada entidad:

```ts
loadHistory({
  entityType: 'INVOICE',
  entityId: invoice.id,
  page: 0,
  size: 20
});
```

### 8.2 Historial global

En el menú principal o campana de actividad, consultar por `module`:

```ts
loadGlobalHistory({ module: 'finance', page: 0, size: 20 });
```

### 8.3 Filtros

1. Llamar primero a `filter-options`.
2. Mostrar solo acciones/actores con `count > 0`.
3. Usar `dateRange.min` y `dateRange.max` para limitar el calendario.
4. Si `actions` o `actors` están vacíos, ocultar ese filtro.

### 8.4 Actor

`photoUrl` puede ser `null`. Usar iniciales del nombre como fallback.

### 8.6 Metadata

Es específica por acción. Ejemplos:

```ts
// CONFIRM
{ statusFrom, statusTo, documentNumber }

// CANCEL
{ reason, statusFrom, statusTo }

// CONVERT_TO_INVOICE
{ sourceId, sourceNumber, targetId, targetNumber }

// PAY / PARTIAL_PAYMENT
{ amount, method, appliedAllocations }

// APPROVE / REJECT
{ reason, previousStatus, newStatus }
```

Usarla como detalle secundario, no como texto principal.

### 8.7 Paginación

Usar paginación de servidor con `page` y `size`. No cargar todo el historial en cliente.

### 8.8 Vacío

Cuando `empty=true`, mostrar un estado vacío:

```text
Todavía no hay movimientos para este registro.
```

---

## 9. Ejemplo Angular 21

### 9.1 Modelos

```ts
// audit.models.ts
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export type AuditAction =
  | 'CREATE' | 'EDIT' | 'DELETE' | 'CONFIRM' | 'CANCEL' | 'DUPLICATE'
  | 'ISSUE' | 'CONVERT_TO_ORDER' | 'CONVERT_TO_INVOICE' | 'PAY' | 'PAY_ALL'
  | 'PARTIAL_PAYMENT' | 'PAY_DIRECT' | 'PAY_WITH_VALIDATION' | 'APPROVE'
  | 'REJECT' | 'OPEN' | 'CLOSE' | 'RECORD_ENTRY' | 'RECORD_EXIT'
  | 'RECORD_MOVEMENT' | 'ADJUST' | 'TRANSFER' | 'REFUND' | 'ADVANCE'
  | 'TOGGLE' | 'UPLOAD_PHOTO' | 'REMOVE_PHOTO' | 'BALANCE';

export type AuditEntityType =
  | 'QUOTE' | 'ORDER' | 'PURCHASE' | 'INVOICE' | 'INVOICE_LINE' | 'RETURN'
  | 'CREDIT_NOTE' | 'DEBIT_NOTE' | 'PAYMENT' | 'PAYMENT_ALLOCATION'
  | 'RECEIVABLE' | 'CASH_REGISTER' | 'CASH_MOVEMENT' | 'PRODUCT'
  | 'CATEGORY' | 'PRICE_TYPE' | 'WAREHOUSE' | 'CUSTOMER' | 'SUPPLIER'
  | 'USER' | 'ROLE' | 'DEPARTMENT' | 'COMPANY' | 'PAYMENT_METHOD';

export interface AuditEventResponse {
  id: string;
  module: string;
  entityType: AuditEntityType;
  entityId: string;
  entityLabel: string;
  secondaryLabel: string | null;
  action: AuditAction;
  actor: { id: string | null; name: string; photoUrl: string | null };
  occurredAt: string;
  descriptionKey: string;
  descriptionArgs: string[];
  metadata: Record<string, unknown>;
}

export interface AuditFilterOptionsResponse {
  actions: { value: AuditAction; labelKey: string; count: number }[];
  actors: { id: string; name: string; photoUrl: string | null; count: number }[];
  dateRange: { min: string | null; max: string | null };
}

export interface AuditHistoryQuery {
  entityType?: AuditEntityType;
  entityId?: string;
  module?: string;
  action?: AuditAction[];
  actorId?: string;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}
```

### 9.2 Servicio

```ts
// audit.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AuditEventResponse,
  AuditFilterOptionsResponse,
  AuditHistoryQuery,
  PageResponse,
} from './audit.models';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/audit-events';

  find(query: AuditHistoryQuery): Observable<PageResponse<AuditEventResponse>> {
    return this.http.get<PageResponse<AuditEventResponse>>(this.baseUrl, {
      params: this.toParams(query),
    });
  }

  filterOptions(query: AuditHistoryQuery): Observable<AuditFilterOptionsResponse> {
    return this.http.get<AuditFilterOptionsResponse>(
      `${this.baseUrl}/filter-options`,
      { params: this.toParams(query) },
    );
  }

  findById(id: string): Observable<AuditEventResponse> {
    return this.http.get<AuditEventResponse>(`${this.baseUrl}/${id}`);
  }

  private toParams(query: AuditHistoryQuery): HttpParams {
    let params = new HttpParams();
    if (query.entityType) params = params.set('entityType', query.entityType);
    if (query.entityId) params = params.set('entityId', query.entityId);
    if (query.module) params = params.set('module', query.module);
    if (query.actorId) params = params.set('actorId', query.actorId);
    if (query.from) params = params.set('from', query.from);
    if (query.to) params = params.set('to', query.to);
    if (query.page != null) params = params.set('page', query.page);
    if (query.size != null) params = params.set('size', query.size);
    for (const action of query.action ?? []) {
      params = params.append('action', action);
    }
    return params;
  }
}
```

### 9.3 Render de descripción

```ts
function renderDescription(
  event: AuditEventResponse,
  translate: (key: string, args?: Record<string, unknown>) => string,
): string {
  try {
    const args = event.descriptionArgs.reduce<Record<string, unknown>>(
      (acc, value, index) => ({ ...acc, [index]: value }),
      {},
    );
    return translate(event.descriptionKey, args);
  } catch {
    return translate(`audit.actions.${event.action}`);
  }
}
```

---

## 10. Componentes PrimeNG sugeridos

- `p-table` para el historial paginado.
- `p-multiSelect` para `action`.
- `p-select` para `actorId`.
- `p-datePicker` con `minDate`/`maxDate` desde `dateRange`.
- `p-avatar` para el actor, con iniciales cuando `photoUrl` sea `null`.
- `p-tag` para la acción (`audit.actions.<ACTION>`).
- `p-drawer` o `p-dialog` para el detalle del evento.

---

## 11. Checklist de integración

- [ ] Agregar `AUDIT_TRAIL_VIEW_LIST` al modelo de permisos del frontend.
- [ ] Crear modelos TypeScript.
- [ ] Crear `AuditService`.
- [ ] Agregar pestaña/drawer “Historial” en entidades auditables.
- [ ] Agregar vista global por módulo.
- [ ] Cargar `filter-options` al abrir filtros.
- [ ] Traducir `descriptionKey` con `descriptionArgs`.
- [ ] Fallback de avatar con iniciales.
- [ ] Manejar `AUDIT_INVALID_FILTER` y `AUDIT_EVENT_NOT_FOUND`.
- [ ] Estado vacío cuando `empty=true`.
- [ ] Paginación de servidor.

---

## 12. Notas importantes

1. `occurredAt`, `dateRange.min` y `dateRange.max` son ISO-8601 en UTC.
2. `from` y `to` son fechas `YYYY-MM-DD` y representan días completos en UTC.
3. `photoUrl` puede ser `null` en esta versión.
4. El orden es fijo `occurredAt desc`; no hay `sort` configurable.
5. El historial sobrevive al borrado del registro auditado.
6. `entityId` del evento puede apuntar a un registro que ya no existe.
