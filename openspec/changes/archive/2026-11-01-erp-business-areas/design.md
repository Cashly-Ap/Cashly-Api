# Design

Business areas are organizational parents; existing modules retain their own contracts and internals. Backend finance groups billing (invoices, returns, credit/debit notes) and treasury (payments, cash register, receivables, payment methods). Partners retains existing customer profile/balance/credit/warehouse access ownership. Technical shared/bootstrap infrastructure stays separate from business capabilities.

Java tests follow the same package relocations. Module boundary ownership resolves the concrete original owner below an area prefix, so moving both quotes and orders under sales cannot erase their separation. Reporting, layer and responsibility guards keep their original behavioral intent.

Angular mirrors business areas with kebab-case names. Auth users belong beneath platform/auth/user. Existing router folders become routing. Existing coherent models/interfaces remain technical responsibility leaves; no empty ceremonial folders or speculative feature modules are introduced. Owner-local reporting implements all PDFs and report screens; analytics/reporting/routing composes legacy URLs without owning generators. Shared PDF viewing remains a technical shared component.

Every original production/test file is audited. Any report service split records responsibility ownership and retained test scenarios. Public URLs, permission enum values and persistence contracts are stable. There is one deployment per existing project, no distributed transaction change.

The verified 3258-test backend run is the immediate coverage baseline. Relocation maps exact class identities; module ownership stays comparable below the new area hierarchy. Independent checks must catch invalid returns ownership, old roots, hidden sibling module access and broken lazy route targets.
