# Implemented relocation integrity

Result: **PASS**. This source audit complements, and does not replace, the runtime test report.

Baseline: independent extraction of `git archive HEAD src` at commit `3d9c2dad796502e28d8f8ff3d18ca2aee93d12de`, copied into the Docker application container at `/tmp/cashly-integrity-head`. Every baseline file SHA-256 was checked against the reviewed manifests before comparing the relocated result.

Command executed inside Docker:

```sh
docker compose exec -T app python3 openspec/changes/screaming-backend-structure/tools/validate_change.py --implemented --baseline-root /tmp/cashly-integrity-head --self-test
```

- All 1,248 production Java files and 573 baseline Java tests/helpers exist at their mapped destinations, with matching package declarations and no old moved paths remaining.
- All 41 resource/backup files retain their exact baseline bytes, including historical database migrations and nine `.java.bak` files.
- Production code and baseline tests match deterministic package/import/FQN relocation. Import order and formatting are ignored for semantic comparison; Java string/character/text-block literals remain exact.
- The existing `CustomerTestHelper` is public with public test factories because its callers now span packages; this exception affects tests only.
- Four existing structural tests intentionally changed: `ApplicationPackageConventionTest`, `LayerDependencyTest`, `CustomerWarehouseBoundaryTest`, and `CustomerCreditQueryBoundaryTest`. The latter updates its concatenated expected contract package from `payments.application.ports.out` to `payments.credit.application.ports.out`; its assertions remain intact. Their structural assertions must also pass at runtime.
- Two new verification classes are present: `CapabilityStructureTest` and `BackendStartupMySqlIT`. They do not replace or remove baseline tests.
- No unexpected semantic changes or missing baseline artifacts were found.
- Four negative manifest fixtures reject duplicate sources, destination collisions, unsafe paths, and invalid hashes.

The machine-readable result is [implemented-integrity.json](implemented-integrity.json). The complete target tree retains the baseline inventory; extra verification tests are recorded by the validator above.
