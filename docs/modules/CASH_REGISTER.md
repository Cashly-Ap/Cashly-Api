# CASH REGISTER Module

> Estructura actual del código: `cash_register/sessions` y `cash_register/reporting`. Dentro de cada responsabilidad se ubican sus capas existentes; los controllers están en `infrastructure/web`, la persistencia en `infrastructure/persistence` y los casos de uso en `application/<operación>`. Consulta [ARCHITECTURE.md](../ARCHITECTURE.md) y [el árbol completo](../../openspec/changes/screaming-backend-structure/trees/after.md) para las rutas vigentes. Los ejemplos de planificación o paquetes Lexand de esta guía requieren contrastarse con el código actual.

## Overview

The cash register is a session-based ledger. A session starts only when an authorized user opens it and remains active until an authorized user closes it, including across calendar days. The system allows multiple sessions on the same day but enforces at most one open session globally.

## Accounting model

Each session stores an opening balance and immutable movements. Its balances are calculated as:

`expected balance = opening balance + income - expense`

Closing requires the physically declared balance. The recorded difference is:

`difference = declared balance - expected balance`

Movements carry their direction, concept, amount, payment method snapshot, source operation, operator, and timestamp. The ledger separates monetary concepts from commercial concepts:

- Monetary: payments, advances, refunds, manual income, expenses, withdrawals, and adjustments.
- Commercial: confirmed invoices, credit notes, debit notes, and returns.

Commercial concepts calculate net billing without changing the expected physical balance:

`net billing = confirmed invoices + debit notes - credit notes - returns`

## Business rules

1. Reading the active register never opens one automatically.
2. Only an explicit `POST /api/cash-register/open` opens a session.
3. A session can span multiple dates and several sessions can be opened and closed on the same date.
4. At most one session may have status `OPEN`; the database also enforces this invariant.
5. Any monetary or commercial operation recorded by cash requires an open session before changing its source aggregate.
6. Using an existing customer credit balance does not move new money and therefore does not require a session.
7. Pending payments do not affect cash until approval.
8. Source operation and cash movement are persisted in the same transaction.
9. Closing a session never opens another one.
10. Income concepts only accept incoming movements and expense concepts only accept outgoing movements; adjustments may use either direction.
11. A confirmed credit invoice affects billing immediately but affects cash only when an applied payment is created.
12. A confirmed cash invoice creates its applied payment through the selected payment method and therefore affects billing and cash in the same transaction.
13. Credit notes and returns reduce net billing; debit notes increase it. They affect physical cash only if a separate refund or payment is recorded.

## Monetary integrations

- Applied payments and approved pending payments: income.
- Invoice, partial-debt, and full-debt payments made through a payment method: income.
- Customer advances: income.
- Payment and customer-balance refunds: expense.
- Manual income, expenses, withdrawals, and adjustments: explicit ledger movements.
- Customer credit consumed on an invoice: non-cash operation.

## Commercial integrations

- Confirmed cash and credit invoices: gross billing.
- Debit notes: positive billing adjustment.
- Credit notes: negative billing adjustment.
- Returns: negative billing adjustment.
- Payments subsequently applied to credit invoices: monetary income, without duplicating billing.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/cash-register/active` | Returns the open session or `404` without creating one |
| `POST` | `/api/cash-register/open` | Opens a session with `openedByName` and `openingBalance` |
| `POST` | `/api/cash-register/{id}/close` | Closes with `closedByName`, `declaredBalance`, and notes |
| `POST` | `/api/cash-register/{id}/movements` | Records a manual monetary movement |
| `GET` | `/api/cash-register/history` | Paged history; `detailed=true` includes ledger movements |
| `GET` | `/api/cash-register/{id}/report` | Session report with `format=PDF|EXCEL` and `type=SIMPLE|DETAILED` |
| `GET` | `/api/cash-register/reports` | Date-range report with PDF/Excel, simple/detailed/grouped modes and commercial filters |

All endpoints require an administrative or operator role and `MODULE_FINANCES` authority.

## Persistence

Migration `V30__create_cash_register_ledger.sql` removes the date uniqueness restriction, adds session balance columns, enforces a single open session, creates `cash_register_movements`, normalizes legacy open sessions, and backfills monetary operations belonging to the surviving active session.

Closed legacy sessions keep their stored aggregate totals when no itemized ledger is available, so detailed history and reports remain arithmetically correct while showing an empty movement list.

The unique `(source_type, source_id)` index prevents the same source operation from being posted twice.

## Reports

Simple PDF and Excel reports contain session identity, opening/closing timestamps, net billing, opening balance, income, expense, expected balance, declared balance, and difference. Detailed reports additionally list every monetary and commercial movement with payment method, description, source, operator, and timestamp.

The finance menu date-range report supports:

- `format=PDF|EXCEL`.
- `type=SIMPLE|DETAILED|GROUPED`; grouped output summarizes by customer.
- Required `dateFrom` and `dateTo` parameters.
- Optional `customerId`, `productId`, and `categoryId` filters.

Detailed range PDFs list operations in ascending chronological order, format every date as `dd/MM/yyyy`, and omit internal module classification from the visible table.

Product and category filters include invoices, returns, notes, and applied payments associated with matching invoice lines. Customer advances and standalone refunds are excluded when a product or category filter is active because they do not identify a product.

## Testing

Domain and application tests cover session duration, repeated sessions per day, opening/closing arithmetic, closed-register rejection, cash and credit invoice accounting, notes, returns, non-cash credit consumption, source linkage, report generation, and controller contracts. Flyway and the Spring application context are validated against MySQL 8.
