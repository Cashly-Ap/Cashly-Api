# Design

Source modules validate their document and invoke existing neutral creation gateways. Invoice creation facades build the invoice using their existing source snapshots. An internal IssueInvoiceService owns issuance; its aggregate parameter never becomes a cross-module public port.

The sequence is DRAFT check, actor lookup, read-only authorization and payment planning, new CASH draft persistence/flush (manual drafts already exist), balance and settlement writes, domain confirmation, origin inventory policy, final persistence and existing notifications/accounting. All steps join the caller transaction. Flush makes the referenced row visible inside that transaction without committing it. Source status changes remain in the same outer transaction.

InvoiceIssueValidator checks credit authorization, payment method and open register without payment writes. InvoicePaymentService consumes balance and settles the remainder. IssueInvoiceCommand carries issuance options; InvoiceOrigin identifies the inventory and compatibility policy.

| Origin | Inventory | Existing compatibility behavior |
| --- | --- | --- |
| QUOTE | Direct allocation deduction | Source use case owns quote notification |
| CONFIRMED_ORDER | No second deduction | Created and confirmed invoice notifications |
| MANUAL_DRAFT | Confirm existing reservations | Existing register entry and confirmed notification |

CreateInvoiceUseCase still creates an unconfirmed draft when no primary order is supplied. Existing QuoteInvoiceCreationApi and OrderInvoiceCreationApi remain the external contracts. No new public API exposes Invoice.

The manual CASH path historically records an invoice register entry in addition to the payment settlement. This refactor preserves that behavior; deciding whether those entries represent duplicate accounting requires a separate accounting change and is not represented as fixed here.

Before-change audit: source-before.json. Previously verified backend: 3239 tests; frontend unchanged, previously 2502 tests and production build passed. New verification must run against the final source in the isolated MySQL harness.
