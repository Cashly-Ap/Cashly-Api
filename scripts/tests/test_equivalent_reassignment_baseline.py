import sys
import unittest
from pathlib import Path
import xml.etree.ElementTree as ET
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from equivalent_reassignment_baseline import line_counts, removed_lines, subtract

class EquivalentReassignmentBaselineTest(unittest.TestCase):
    def test_only_deleted_executable_lines_are_removed(self):
        before = ['retain();', 'obsolete();', 'retainMore();']
        deleted, retained = removed_lines(before, ['retain();', 'retainMore();'], {1, 2, 3})
        self.assertEqual(deleted & {1, 2, 3}, {2})
        self.assertFalse(deleted & retained)

    def test_constructor_implicit_super_line_is_retained(self):
        before = ['DeleteOrderUseCase deleteOrderUseCase,', 'AssignOrderWarehousesUseCase assignOrderWarehousesUseCase) {']
        deleted, retained = removed_lines(before, ['DeleteOrderUseCase deleteOrderUseCase) {'], {2})
        self.assertFalse(deleted)
        self.assertIn(2, retained)

    def test_executable_changes_cannot_be_disguised_as_removal(self):
        with self.assertRaisesRegex(ValueError, 'Executable replacement'):
            removed_lines(['oldBusiness();'], ['newBusiness();'], {1})

    def test_partial_line_is_covered_and_lambda_overlap_is_not_summed(self):
        lines = [ET.fromstring('<line nr="1" ci="2" mi="1"/>'), ET.fromstring('<line nr="2" ci="0" mi="3"/>')]
        self.assertEqual(line_counts(lines), {'covered': 1, 'missed': 1})

    def test_subtraction_cannot_make_negative_counters(self):
        with self.assertRaises(ValueError):
            subtract({'covered': 1, 'missed': 0}, {'covered': 2, 'missed': 0})

    def test_retained_coverage_is_recomputed_exactly(self):
        self.assertEqual(subtract({'covered': 4, 'missed': 2, 'line_coverage_percent': 66.6667}, {'covered': 1, 'missed': 1}),
                         {'covered': 3, 'missed': 1, 'line_coverage_percent': 75.0})

if __name__ == '__main__':
    unittest.main()
