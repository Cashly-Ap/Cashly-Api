# Coverage equivalence for deleted reassignment code

The comparison uses the existing module-owned-reporting baseline, adjusted only for the production code deliberately deleted by this change. It does not reset measured coverage or exclude modified classes.

`removed-production-coverage-audit.json` records input hashes, all 166 quote/order class counter matches against the original 3,239-test JaCoCo report, the six removed source files (including nested records), exact old method LINE counters, unique removed executable source lines and retained counters. Package-based layer/module counters are adjusted with source counters; class counters remain available for the existing ownership/split reconciliation.

For `Quote`, the removed public methods have 8 + 12 + 8 covered LINE counters. Its two generated lambdas share already-counted source lines and therefore are not subtracted twice. The retained class has 112 covered / 2 missed lines. The constructor's implicit super instruction stays in the baseline even though its original source line was the removed final parameter declaration. Deleted controller assignments and endpoint/config methods are subtracted; all other controller/config code remains subject to the original gates.

No snapshot entity or persistence-adapter line is excluded: those classes contain the actual conversion fix and must retain their baseline coverage while covering the new logic. No coverage threshold or report ownership/split rule changes.

Rebuild using `scripts/equivalent_reassignment_baseline.py` with an original source export from commit `a056d57`, the original JaCoCo report and the current source tree. Execution and six meaningful tooling tests passed inside Docker. The complete tooling suite passes 20 tests.
