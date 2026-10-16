# Design

## Package convention
Capability/module ownership is retained. Structural folders group capabilities and operations; Java files live at responsibility leaves. Existing responsibility leaves are not recursively subdivided: no empty folder ceremony is required.

- Application operations group use_case, request, response, services, validation or model. Source-specific operations retain branches such as create/from_quote and then their own role leaves.
- Existing ports/in, ports/out, dto/request, dto/response, mappers, exceptions and services are explicit responsibility leaves.
- Domain entities move to entity, repository contracts to repository; exceptions, valueobjects and services retain their distinct roles.
- Infrastructure already separates web, config, integration, document generators and persistence adapters/entities/repositories/mappers/queries. These leaves remain stable.
- Tests of relocated classes mirror their package. Cross-cutting suites use architecture and integration_tests; fixtures have their own technical responsibility.
- CashlyApplication is the global bootstrap exception.

The relocation manifest records 411 production and 251 test moves. The complete before Java inventory contains 1249 main and 584 test files. Nine tracked .java.bak files are non-executable historical copies in obsolete packages and are removed, with hashes recorded in evidence; no executable tests are removed.

## Dependency and runtime preservation
Update package declarations, imports (including formerly implicit same-package references), fully-qualified literals and source-path architecture assertions. Preserve all class names, methods, HTTP contracts, persistence schemas, transactions and behavior. Framework scanning remains under com.cashly.cashly_api.

A clean isolated Maven build avoids duplicate old/new compiled components. Refreshing development output must also remove stale compiled package identities before startup, without deleting stored verification evidence or business database contents.

## Verification identity
The verified 3253-test issuance run is the immediate baseline. Coverage comparison maps its class identities through the exact relocation manifest, comparing each moved class and module/layer counters; there are no class splits or exclusions. The test harness accepts explicit baseline/ownership/split artifact paths while preserving previous defaults for prior changes.

Complete main/test trees are published in structure-before-after.md and docs/architecture/RESPONSIBILITY-PACKAGES.md. A structural regression guard checks actual source paths and test mirrors.
