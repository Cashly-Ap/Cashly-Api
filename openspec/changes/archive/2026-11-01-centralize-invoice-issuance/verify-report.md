# Verification

The final isolated Java 24/MySQL run passed 3253 tests across 567 ordinary executable classes and 10 MySQL IT classes, with zero failures, errors, skipped tests or missing classes. Maven clean verify built the application successfully. Current source hashes match the tested snapshot.

The existing coverage comparison passed without baseline adjustment or new waivers: domain 94.3244%, application 91.8106%, infrastructure 77.6797%. The complete report is in evidence/full-final. Raw logs/XML remain in the ignored target directory; tracked evidence is sanitized.

Focused verification passed 57 tests, including 14 new sequence/policy/architecture cases. The suite retains real MySQL tests for payment allocation foreign keys, category snapshot identity, source conversion and rollback of invoice/payment/allocation/balance/cash/stock/source state. Python verification tooling passed 20 tests. OpenSpec strict validation passed.

Frontend code and HTTP contracts did not change in this refactor. Previously verified frontend reassignment removal remains covered by its own evidence (2502 tests and production build).

Manual accounting semantics are intentionally preserved; see evidence/qa-scope.md for the known accounting interpretation and RED evidence limits. No microservice extraction was implemented.

The development runtime was compiled, the obsolete authorization component class was removed, and the app restarted successfully. Health returned HTTP 200 with status UP. The isolated verification project and its temporary volumes were removed after evidence export; development business data was not used by the suite.
