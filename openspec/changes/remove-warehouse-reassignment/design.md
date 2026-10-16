# Technical design

## Reassignment removal

Quotes and orders lose their AssignWarehousesDialog components, list/detail actions and assignWarehouses API/service methods. Initial line allocations used by creation and editing remain. Backend PUT /api/quotes/{id}/allocations and PUT /api/orders/{id}/allocations, their exclusive request/use-case classes and dead wiring are removed. No equivalent standalone invoice assignment flow was found. Conversion remains a direct operation using the document's existing allocations.

## Conversion diagnosis

Read-only development logs repeatedly show DuplicateKeyException: a different object with the same identifier is already associated with the session for QuoteLineCategorySnapshotJpaEntity. Existing repository saves reconstruct entity graphs, including @MapsId category snapshots. A conversion loads the original graph, creates a destination document, then saves the converted source in the same transaction. Snapshot identity must be maintained when that source graph is merged. Orders use the analogous OrderLineCategorySnapshotEntity mapping.

The fix must reproduce the failure on MySQL first and preserve historical category values and transaction rollback. It must not change the exception handler to hide genuine conflicts, bypass inventory checks or erase category snapshots.

## Verification

Run frontend quote/order suites, production build and full frontend tests in Docker. Run focused route-removal/domain/config checks, real isolated MySQL conversion persistence regressions, then clean full backend verification with all IT. Keep any legitimate stock, status, duplicate conversion and financial failures explicit. Read-only log evidence is summarized without customer data or arbitrary environment properties.

## Additional real cash-conversion failure

The user supplied another 409 response timestamped 2026-10-16T22:18:38. Exact read-only logs identify MySQL 1452 on fk_payment_allocations_invoice, a different failure from the snapshot conflict. QuoteInvoiceCreationService and the order-linked CreateInvoiceUseCase invoke authorization/settlement before saving the invoice. Cash settlement inserts payment_allocations referencing that unsaved invoice. The correction must persist and flush a draft invoice before financial references are inserted, then authorize/confirm/save within the same transaction. Flush must never become an independent commit. Tests must exercise the actual payment allocation foreign key and financial rollback, rather than a stub invoice-creation gateway.
