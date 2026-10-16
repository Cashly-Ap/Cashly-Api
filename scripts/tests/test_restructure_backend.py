import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('restructure_backend', Path(__file__).parents[1] / 'restructure_backend.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

class JavaRelocationTests(unittest.TestCase):
    def test_explicit_import_takes_precedence_over_wildcard(self):
        text = 'package client;\nimport invoice.PaymentType;\nimport customer.*;\nclass Client { PaymentType value; }'
        result = module.rewrite_text(text, None, {}, {'customer':['customer.PaymentType']})
        self.assertIn('import invoice.PaymentType;', result)
        self.assertNotIn('import customer.PaymentType;', result)

    def test_split_wildcard_resolves_each_used_type(self):
        text = 'package old.client;\nimport old.operations.*;\nclass Client { Create first; Update second; }'
        result = module.rewrite_text(text, None, {'old.operations.Create': 'new.create.Create', 'old.operations.Update': 'new.update.Update'}, {'old.operations': ['old.operations.Create','old.operations.Update','old.operations.Unused']})
        self.assertIn('import new.create.Create;', result)
        self.assertIn('import new.update.Update;', result)
        self.assertNotIn('Unused', result)
        self.assertNotIn('operations.*', result)

    def test_same_package_nested_type_gets_explicit_import(self):
        text = 'package old.operations;\nclass Delete { Get.Missing failure; }'
        result = module.rewrite_text(text, 'old.operations.Delete', {'old.operations.Delete':'new.delete.Delete','old.operations.Get':'new.get.Get'}, {}, ['old.operations.Get'])
        self.assertIn('package new.delete;', result)
        self.assertIn('import new.get.Get;', result)
        self.assertIn('Get.Missing failure;', result)

    def test_static_nested_and_reflective_fqns_move_without_prefix_corruption(self):
        text = 'package client;\nimport static old.Type.factory;\nclass Client { String name = "old.Type$Nested"; old.Type.Nested value; old.TypeExtra other; }'
        result = module.rewrite_text(text, None, {'old.Type':'new.Type'}, {})
        self.assertIn('import static new.Type.factory;', result)
        self.assertIn('new.Type.Nested', result)
        self.assertIn('old.TypeExtra', result)
        self.assertIn('"new.Type$Nested"', result)

    def test_unchanged_class_changes_consumed_reference_only(self):
        text = 'package consumer;\nimport old.provider.Type;\nclass Consumer { Type value; }'
        result = module.rewrite_text(text, 'consumer.Consumer', {'old.provider.Type':'new.provider.Type'}, {})
        self.assertIn('package consumer;', result)
        self.assertIn('import new.provider.Type;', result)

    def test_comment_does_not_select_wildcard_symbol(self):
        text = 'package client;\nimport old.*;\n// Unused\nclass Client { String note = "Unused"; }'
        result = module.rewrite_text(text, None, {'old.Unused':'new.Unused'}, {'old':['old.Unused']})
        self.assertNotIn('import new.Unused;', result)

    def test_duplicate_import_is_not_introduced(self):
        text = 'package client;\nimport old.Type;\nimport old.*;\nclass Client { Type value; }'
        result = module.rewrite_text(text, None, {'old.Type':'new.Type'}, {'old':['old.Type']}, ['old.Type'])
        self.assertEqual(1, result.count('import new.Type;'))

if __name__ == '__main__': unittest.main()
