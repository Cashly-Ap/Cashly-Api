# Accounts payable and supplier credit terms

## Intent
Give the business the mirror of accounts receivable: a place to see what is owed to suppliers, when each commitment is due, and how to pay it. Today purchases are recorded but the debt they create has no owner in the system, so supplier balances, due dates and aging cannot be tracked.

## Scope
A new `finance/treasury/payables` module with supplier balances, due dates and aging derived from confirmed purchases, plus supplier credit terms (credit limit and payment term days) and payments to suppliers that settle those commitments.

Included:
- Supplier credit terms: credit limit, default payment term in days, and the resulting due date per purchase.
- Payables list with totals, aging buckets (current, 1-30, 31-60, 61-90, +90) and per-supplier debt detail.
- Supplier payments applied to specific purchases or to the oldest debt first, with partial payments.
- Cash register integration so an outgoing payment reduces the open session.
- Statements/PDF per supplier, following the existing reporting pattern.

Excluded:
- Changes to purchase confirmation rules beyond computing the due date.
- Multi-currency.

## Rollback
Revert the module, its migrations and its UI. Supplier credit terms live in new columns/tables, so existing suppliers keep working with null terms; no purchase, payment or cash register data is destroyed.
