# Architecture guard RED result

A first attempt on the shared checkout overlapped compilation and was discarded as invalid evidence.

The valid RED run used the complete original source snapshot from `target/reporting-baseline-source`, copied inside the application container to `/tmp/reporting-guards-red`. Only the new `ReportingOwnershipTest` and updated `CapabilityStructureTest` were overlaid. Maven ran in that temporary directory without touching the working checkout target or any database.

Command: `docker compose exec -T app sh -c 'cd /tmp/reporting-guards-red; ./mvnw -B -Dtest=ReportingOwnershipTest,CapabilityStructureTest test'`

Result: **9 tests; 4 expected failures; 0 errors; 0 skipped**.

Expected failures:
- `CapabilityStructureTest.should_place_existing_business_capabilities_before_layers`: original warehouses/payments lack reporting capabilities.
- `ReportingOwnershipTest.should_remove_global_reports_packages_and_references_in_production_and_tests`: original global reports packages remain.
- `ReportingOwnershipTest.should_keep_document_generation_and_visual_helpers_in_their_approved_owner`: original generators are outside their owner reporting submodule.
- `ReportingOwnershipTest.should_have_application_infrastructure_and_tests_for_every_document_report_owner`: new owner trees do not exist in original snapshot.

The remaining five tests passed, including positive/negative path fixtures and package/path consistency. Raw local log: `/tmp/cashly-reporting-isolated-red.log`; XML remains in the container temporary directory. Full XML properties are intentionally not committed.
