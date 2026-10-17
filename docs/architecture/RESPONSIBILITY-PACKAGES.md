# Responsibility package structure

This document is generated from the actual Java source inventory. Module and submodule ownership stays intact; leaf folders identify a class responsibility.

| Layer | Responsibility folders |
| --- | --- |
| application | Operations contain use_case, request, response, services, validation or model as needed; contracts stay in ports/in and ports/out. |
| domain | entity, repository, valueobjects, services and exceptions. |
| infrastructure | web, config, integration, pdf, excel and persistence subfolders identify the implementation technology/responsibility. |
| tests | Class tests mirror the production package; architecture, integration_tests and fixtures identify cross-cutting test responsibilities. |

No empty role folders are required. A folder with a responsibility name is a leaf and may contain files. Structural containers (module, submodule, layer and operation) contain folders. CashlyApplication is the bootstrap entry point and the sole Java exception at the global package root.

The full historical comparison, including every source and test file, is recorded in [structure-before-after.md](../../openspec/changes/responsibility-subpackages/structure-before-after.md).

## Current source and tests

Business-area grouping is now implemented. [ERP-BUSINESS-AREAS.md](ERP-BUSINESS-AREAS.md) contains every current Java source and test path.
