# Centralize invoice issuance

## Why
Quote conversion, order conversion and manual confirmation repeated invoice authorization, payment and confirmation. The duplicated ordering already allowed payment allocation foreign keys to reference an invoice before persistence.

## What changes
Use one internal invoice issuance application service. Separate validation from balance/payment writes. Keep source-specific construction and neutral module contracts. Reject orders outside CONFIRMED before requesting invoice creation.

## Scope
Preserve HTTP contracts, manual draft creation, source notifications, existing manual cash-register accounting, inventory policies, and transaction atomicity. Preserve the independently verified warehouse reassignment removal and conversion persistence fixes already in the working tree. No database migration, distributed services, asynchronous messaging or frontend changes.

## Risks
The three origins intentionally have different inventory and accounting behavior. A shared service must preserve these differences instead of applying all side effects to every origin.
