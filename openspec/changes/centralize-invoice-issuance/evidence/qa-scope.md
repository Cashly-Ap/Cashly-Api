# Issuance QA scope

The new focused tests exercise production `InvoiceIssueValidator` and `InvoicePaymentService` behind the shared issuance service, mocking only output ports. They verify the draft persistence boundary before cash references, quote deduction, manual reservation consumption, confirmed-order inventory reuse, credit without cash settlement, invalid state and authorization rejection before effects, and preservation of the existing manual invoice cash entry.

The architectural guard detects confirmation calls outside `application/issue` and financial mutations inside the validator; positive and negative fixtures make the detectors falsifiable.

Existing real-MySQL regression suites already cover quote cash conversion with 0/30/100 balance, confirmed-order settlement allocation, historical category snapshots, and rollback of invoice/payment/balance/cash/stock/source state after an injected failure. These must still execute in the isolated full verification harness.

## Accounting behavior intentionally preserved

Manual draft confirmation already records an invoice cash-register entry, while CASH settlement records a payment entry. This refactor preserves that behavior explicitly. No claim of absence of duplicate cash movements is made. Deciding whether those entries double count a receipt requires a separate accounting behavior change and regression specification; it is outside this structural centralization.

## RED evidence limit

The architecture guard was written before the shared service files appeared, but production changes landed before a coordinated Maven execution. No old-structure Maven RED result is claimed. The new-class behavior tests were written against the published service contract during implementation.

Manual draft creation continues to reserve inventory during line preparation before validating optional linked source documents. Its existing transaction provides rollback. The new validation-before-effects guarantee concerns issuance/cash settlement; this change does not claim that every existing draft-construction read occurs before every reservation write.

## Focused execution

The backend implementation agent executed the coordinated focused Maven run in the existing app container. Surefire reports inspected by QA confirm `InvoiceIssuanceTest`: 9 tests, 0 failures/errors/skips; `InvoiceIssuanceArchitectureTest`: 5 tests, 0 failures/errors/skips. `git diff --check` passed. This is focused unit/architecture evidence, not a substitute for the pending isolated full-MySQL verification.
