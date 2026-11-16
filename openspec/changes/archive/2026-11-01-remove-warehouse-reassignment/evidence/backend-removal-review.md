# Backend standalone reassignment removal review

- Removed `PUT /api/quotes/{id}/allocations` and `PUT /api/orders/{id}/allocations`, their request types, use cases and Spring wiring.
- Removed `OrderWarehouseGateway` and its local integration adapter: their sole production consumer was the removed order reassignment use case.
- Removed the quote aggregate's standalone `assignAllocation`, `confirmWarehouseAssignment`, and `clearAllocations` methods; no remaining production consumer exists. Initial line allocation methods remain.
- Kept quote/order create and update allocations, persisted allocation entities, warehouse availability queries, and conversion payload allocations.
- No standalone reassignment endpoint or modal exists in invoices.
- Removed six exclusive application tests and six exclusive quote-domain tests for the deleted feature. Added two HTTP tests asserting the old routes return 404.

## Conversion diagnosis review

Both quote and order persistence reconstruct detached entity graphs before `JpaRepository.save`. Their historical line-category snapshot child uses a shared primary key (`@MapsId`). Reconstructing an existing child without its ID makes Hibernate treat it as a new child while the original same-ID snapshot remains managed, producing an identity conflict on source save during conversion. Each conversion is transactional; no `REQUIRES_NEW` was found in related sales, stock or financial modules. The regression suite must cover quote-to-order, quote-to-invoice and order-to-invoice with non-null historical categories. Existing legitimate business conflicts must remain errors.

## Focused verification

Focused verification is run from an independent source copy in the app container, avoiding stale incremental build artifacts. Full MySQL/security/coverage verification is performed separately by the main task.

Result: **35 tests, 0 failures, 0 errors, 0 skipped** (`QuoteWarehouseReassignmentRemovalTest`, `OrderWarehouseReassignmentRemovalTest`, `QuoteTest`, `QuoteModuleBoundaryTest`, `OrderConfigTest`). All 1,245 production and 581 test sources compiled from scratch in `/tmp/cashly-reassignment-focused` inside the app container. Local log: `/tmp/cashly-reassignment-backend-isolated.log`.
