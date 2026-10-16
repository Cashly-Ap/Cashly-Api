import importlib.util
from pathlib import Path
import unittest
import json
import subprocess
import sys
import tempfile

spec = importlib.util.spec_from_file_location('coverage_ownership', Path(__file__).parents[1] / 'coverage_ownership.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ReportingCoverageTest(unittest.TestCase):
    def test_relocated_nested_class_keeps_original_counter(self):
        before = {'com/cashly/cashly_api/reports/stock/application/UseCase$Row': {'covered': 2, 'missed': 1}}
        moves = [{'old': 'src/main/java/com/cashly/cashly_api/reports/stock/application/UseCase.java', 'new': 'src/main/java/com/cashly/cashly_api/warehouses/reporting/application/UseCase.java'}]
        groups, audit = module.regroup(before, moves, [], baseline=True)
        self.assertEqual(groups['module/warehouses/application']['missed'], 1)

    def test_split_family_keeps_all_debt_without_arbitrary_owner(self):
        old = 'com/cashly/cashly_api/reports/finance/infrastructure/Config'
        splits = [{'old': old, 'new': ['com/cashly/cashly_api/payments/reporting/infrastructure/Config', 'com/cashly/cashly_api/receivables/reporting/infrastructure/Config']}]
        groups, audit = module.regroup({old: {'covered': 0, 'missed': 7}}, [], splits, baseline=True)
        self.assertEqual(groups['family/' + old], {'covered': 0, 'missed': 7, 'line_coverage_percent': 0.0})

    def test_split_final_combines_both_classes(self):
        old = 'com/cashly/cashly_api/reports/finance/infrastructure/Config'
        names = ['com/cashly/cashly_api/payments/reporting/infrastructure/Config', 'com/cashly/cashly_api/receivables/reporting/infrastructure/Config']
        groups, audit = module.regroup({names[0]: {'covered': 3, 'missed': 1}, names[1]: {'covered': 2, 'missed': 4}}, [], [{'old': old, 'new': names}], baseline=False)
        self.assertEqual(groups['family/' + old]['missed'], 5)

    def test_unmapped_global_report_fails_closed(self):
        with self.assertRaisesRegex(ValueError, 'Unmapped'):
            module.regroup({'com/cashly/cashly_api/reports/Unknown': {'covered': 1, 'missed': 0}}, [], [], baseline=True)

    def test_missing_split_destination_fails_closed(self):
        with self.assertRaisesRegex(ValueError, 'Missing split'):
            module.regroup({}, [], [{'old': 'old', 'new': ['new']}], baseline=False)

    def test_individual_report_regression_cannot_hide_in_module_improvement(self):
        old = 'com/cashly/cashly_api/reports/stock/application/UseCase'
        new = 'com/cashly/cashly_api/warehouses/reporting/application/UseCase'
        peer = 'com/cashly/cashly_api/warehouses/stock/application/Peer'
        summary = {'counts': {'tests': 1, 'errors': 0, 'failures': 0, 'skipped': 0},
                   'missing_executable_classes': [],
                   'coverage': {'layer/' + layer: {'covered': 100, 'missed': 0, 'line_coverage_percent': 100} for layer in ('domain', 'application', 'infrastructure')}}
        before = dict(summary, coverage_by_class={old: {'covered': 9, 'missed': 1}, peer: {'covered': 0, 'missed': 90}})
        after = dict(summary, coverage_by_class={new: {'covered': 8, 'missed': 2}, peer: {'covered': 90, 'missed': 0}})
        with tempfile.TemporaryDirectory() as directory:
            folder = Path(directory)
            inputs = {'before.json': before, 'after.json': after, 'moves.json': [{'old': 'src/main/java/' + old + '.java', 'new': 'src/main/java/' + new + '.java'}], 'splits.json': []}
            for filename, payload in inputs.items():
                (folder / filename).write_text(json.dumps(payload))
            subprocess.run([sys.executable, str(Path(__file__).parents[1] / 'compare-structure-coverage.py'), str(folder/'before.json'), str(folder/'after.json'), str(folder/'result.json'), '--ownership', str(folder/'moves.json'), '--splits', str(folder/'splits.json')], capture_output=True)
            self.assertIn('Coverage regression: relocated/' + new, json.loads((folder/'result.json').read_text())['errors'])

    def test_total_line_counts_are_conserved_with_split(self):
        old = 'com/cashly/cashly_api/reports/finance/infrastructure/Config'
        groups, audit = module.regroup({old: {'covered': 4, 'missed': 7}}, [], [{'old': old, 'new': ['com/cashly/cashly_api/payments/reporting/infrastructure/Config']}], baseline=True)
        self.assertEqual(audit['assigned'], audit['input'])


if __name__ == '__main__':
    unittest.main()
