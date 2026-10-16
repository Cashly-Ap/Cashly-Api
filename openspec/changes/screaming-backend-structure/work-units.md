# Reviewable migration work units

Each unit includes production moves, test moves, every affected external consumer/import/FQN, wiring and current documentation. The source manifests identify move versus retain; updating imports in a retained file is still required. A unit is complete only when its source/test compilation and applicable regression checks pass. None is implemented during preparation.

## Delivery guard

Delivery strategy: ask-on-risk. Chain strategy: pending. Do not infer authorization for branches, commits or PRs. Every module unit may be split further by operation, with its own tests/consumers, when measured additions plus deletions exceed the 400-line review budget. Rename detection is useful but does not erase review complexity.

| Unit | Scope and finish condition | Required evidence |
|---|---|---|
| U00 | Freeze all manifests, reconcile active naming convention, assign baseline debt owners/deadlines and build safe relocation checks. | Manifest completeness, noncollision, negative guard tests and no production source changes yet. |
| U01 | Create isolated two-MySQL harness and full baseline/CI plan; correct test discovery without weakening assertions. | Current baseline full suite and all seven IT; exported discovered/executed suite lists and JaCoCo. |
| U02 | `auth`: reconcile all 72 production and 45 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U03 | `categories`: reconcile all 26 production and 12 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U04 | `price_types`: reconcile all 25 production and 10 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U05 | `payment_methods`: reconcile all 22 production and 14 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U06 | `suppliers`: reconcile all 50 production and 13 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U07 | `products`: reconcile all 68 production and 32 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U08 | `warehouses`: reconcile all 86 production and 39 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U09 | `customers`: reconcile all 130 production and 62 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U10 | `quotes`: reconcile all 71 production and 31 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U11 | `orders`: reconcile all 72 production and 26 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U12 | `invoices`: reconcile all 106 production and 43 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U13 | `purchases`: reconcile all 57 production and 19 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U14 | `payments`: reconcile all 82 production and 36 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U15 | `returns`: reconcile all 45 production and 23 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U16 | `credit_notes`: reconcile all 29 production and 15 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U17 | `debit_notes`: reconcile all 27 production and 13 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U18 | `cash_register`: reconcile all 67 production and 32 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U19 | `receivables`: reconcile all 22 production and 11 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U20 | `reports`: reconcile all 148 production and 79 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U21 | `dashboard`: reconcile all 8 production and 3 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U22 | `notifications`: reconcile all 19 production and 2 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U23 | `shared`: reconcile all 9 production and 4 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U24 | `infrastructure`: reconcile all 6 production and 1 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U25 | `bootstrap`: reconcile all 1 production and 4 owned Java test files; update every consumer atomically. | Clean compilation, owner/consumer unit/controller/boundary checks; domain-specific IT where applicable. |
| U26 | Reconcile all resources, noncompiled backups, root tests and source-scanning docs/guards after all moves. | Every manifest entry accounted for; migrated path/package checks; unchanged SQL/resource hashes. |
| U27 | Complete migrated all-module verification and equivalent CI; compare against the same full baseline. | Full ordinary suite, seven IT, persisted rollback, startup/API/security, coverage and warning/debt report. |

The sequence is a review order, not a claim of acyclic module dependencies. Each move updates consumers still in their old package immediately; later units move those consumers themselves. No half-migrated import state is a valid review boundary. For a chosen feature-branch chain, the first slice targets the tracker branch and later slices target their immediate parent; for stacked-to-main, each slice merges in dependency order. Select the strategy before apply.

For every unit, preserve a pre-unit source snapshot, exact command and reports. Roll back source paths and all consumer/test/config edits together; do not drop fixtures, SQL or data. Never tear down the development Compose project to clean a test unit.

Acceptance by owner is detailed in verification-plan.md. Where a module has no meaningful runtime test today, add a focused contract/wiring scenario before declaring that owner verified; the empty application test cannot serve as startup evidence.
