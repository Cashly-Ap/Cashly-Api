import importlib.util
from pathlib import Path
import unittest


spec = importlib.util.spec_from_file_location('coverage_ownership', Path(__file__).parents[1] / 'coverage_ownership.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ErpCoverageOwnershipTest(unittest.TestCase):
    def test_sibling_sales_modules_keep_separate_counters(self):
        classes = {
            'com/cashly/cashly_api/sales/quotes/application/create/UseCase': {'covered': 3, 'missed': 2},
            'com/cashly/cashly_api/sales/orders/application/create/UseCase': {'covered': 9, 'missed': 0},
        }
        groups, _ = module.regroup(classes, [], [], baseline=False)
        self.assertEqual(groups['module/quotes/application']['missed'], 2)
        self.assertEqual(groups['module/orders/application']['missed'], 0)

    def test_billing_and_treasury_keep_the_actual_owner(self):
        self.assertEqual(module.module_owner('com/cashly/cashly_api/finance/billing/returns/domain/entity'), 'returns')
        self.assertEqual(module.module_owner('com/cashly/cashly_api/finance/treasury/payments/collection/application'), 'payments')

    def test_old_layout_ownership_stays_compatible(self):
        self.assertEqual(module.module_owner('com/cashly/cashly_api/customers/credit/application'), 'customers')
        self.assertEqual(module.module_owner('com/cashly/cashly_api/infrastructure/config'), 'infrastructure')

    def test_area_grouping_does_not_reclassify_capability_as_owner(self):
        self.assertEqual(module.module_owner('com/cashly/cashly_api/partners/customers/balance/domain/entity'), 'customers')
        self.assertEqual(module.module_owner('com/cashly/cashly_api/platform/auth/token/application'), 'auth')

    def test_relocation_preserves_nested_counter_identity(self):
        old = 'com/cashly/cashly_api/invoices/application/issue/UseCase'
        new = 'com/cashly/cashly_api/finance/billing/invoices/application/issue/UseCase'
        moves = [{'old': 'src/main/java/' + old + '.java', 'new': 'src/main/java/' + new + '.java'}]
        before, audit = module.regroup({old + '$Result': {'covered': 2, 'missed': 1}}, moves, [], baseline=True)
        after, _ = module.regroup({new + '$Result': {'covered': 2, 'missed': 1}}, moves, [], baseline=False)
        self.assertEqual(before, after)
        self.assertEqual(audit['input'], audit['assigned'])


if __name__ == '__main__':
    unittest.main()
