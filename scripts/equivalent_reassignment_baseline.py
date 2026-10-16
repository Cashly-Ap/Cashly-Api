#!/usr/bin/env python3
"""Subtract only reviewed removed production lines from the prior coverage evidence."""
import argparse
import difflib
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET

PREFIX = 'src/main/java/com/cashly/cashly_api/'
REMOVED = [
    'quotes/application/assign_warehouses/AssignQuoteWarehousesRequest.java',
    'quotes/application/assign_warehouses/AssignQuoteWarehousesUseCase.java',
    'orders/application/assign_warehouses/AssignOrderWarehousesRequest.java',
    'orders/application/assign_warehouses/AssignOrderWarehousesUseCase.java',
    'orders/application/ports/out/OrderWarehouseGateway.java',
    'orders/infrastructure/integration/LocalOrderWarehouseAdapter.java',
]
MODIFIED = [
    'quotes/domain/Quote.java', 'quotes/infrastructure/config/QuoteConfig.java',
    'quotes/infrastructure/web/QuoteController.java',
    'orders/infrastructure/config/OrderConfig.java',
    'orders/infrastructure/web/OrderController.java',
]

def counts(node):
    counter = next((c for c in node.findall('counter') if c.get('type') == 'LINE'), None)
    return {k: int(counter.get(k)) for k in ('covered', 'missed')} if counter is not None else {'covered': 0, 'missed': 0}

def line_counts(lines):
    # JaCoCo LINE treats partially covered lines as covered; each source line
    # counts once even when a generated lambda method shares it.
    return {'covered': sum(int(l.get('ci')) > 0 for l in lines),
            'missed': sum(int(l.get('ci')) == 0 for l in lines)}

def subtract(value, removed):
    result = {k: value[k] - removed[k] for k in ('covered', 'missed')}
    if min(result.values()) < 0:
        raise ValueError('Removal exceeds original counters')
    if 'line_coverage_percent' in value:
        total = sum(result.values())
        result['line_coverage_percent'] = round(100 * result['covered'] / total, 4) if total else None
    return result

def removed_lines(before, after, executable):
    deleted = set()
    retained = set()
    for tag, a, b, c, d in difflib.SequenceMatcher(None, before, after, autojunk=False).get_opcodes():
        if tag == 'equal':
            retained.update(range(a + 1, b + 1))
        elif tag == 'delete':
            deleted.update(range(a + 1, b + 1))
        elif tag == 'replace':
            # Only constructor signature punctuation changes are expected;
            # executable replacement would require a fresh, reviewed policy.
            if set(range(a + 1, b + 1)) & executable:
                expected_before = ['DeleteOrderUseCase deleteOrderUseCase,', 'AssignOrderWarehousesUseCase assignOrderWarehousesUseCase) {']
                expected_after = ['DeleteOrderUseCase deleteOrderUseCase) {']
                if [s.strip() for s in before[a:b]] != expected_before or [s.strip() for s in after[c:d]] != expected_after:
                    raise ValueError('Executable replacement cannot be excluded as deletion')
                # The old final parameter line carries the constructor's implicit
                # super instruction. That constructor remains; retain its counter.
                retained.update(range(a + 1, b + 1))
    assert not deleted & retained
    return deleted, retained

def build(baseline_path, jacoco_path, original_root, current_root, output):
    baseline = json.loads(baseline_path.read_text())
    report = ET.parse(jacoco_path).getroot()
    packages = {p.get('name'): p for p in report.findall('package')}
    classes = {c.get('name'): c for p in packages.values() for c in p.findall('class')}
    # Prior baseline predates report relocation, but unchanged quote/order
    # business classes must match exactly before any adjustment is allowed.
    matched = [n for n in baseline['coverage_by_class'] if '/quotes/' in n or '/orders/' in n]
    checked = []
    for name in matched:
        if '/reports/' in name:
            continue
        if name not in classes:
            # Report classes formerly lived under the global reports owner.
            raise ValueError('Missing baseline class in raw report: ' + name)
        if counts(classes[name]) != baseline['coverage_by_class'][name]:
            raise ValueError('Raw baseline differs before subtraction: ' + name)
        checked.append(name)
    audit = {'policy': 'Subtract only deleted classes and unique executable source lines; retain every modified class and every retained line. No thresholds changed.',
             'input_baseline_sha256': hashlib.sha256(baseline_path.read_bytes()).hexdigest(),
             'input_jacoco_sha256': hashlib.sha256(jacoco_path.read_bytes()).hexdigest(),
             'raw_quote_order_classes_confirmed_equal': checked, 'removals': []}
    for relative in REMOVED + MODIFIED:
        path = PREFIX + relative
        original = original_root / path
        current = current_root / path
        package, _, filename = path.removeprefix('src/main/java/').rpartition('/')
        source = next((s for s in packages[package].findall('sourcefile') if s.get('name') == filename), None)
        class_name = package + '/' + filename.removesuffix('.java')
        affected = [n for n in classes if (n == class_name or n.startswith(class_name + '$')) and n in baseline['coverage_by_class']]
        entry = {'path': path, 'original_sha256': hashlib.sha256(original.read_bytes()).hexdigest()}
        if relative in REMOVED:
            if current.exists():
                raise ValueError('Expected deleted source still exists: ' + path)
            entry['kind'] = 'deleted_type'
            entry['removed_classes'] = {n: counts(classes[n]) for n in affected}
            for n in affected:
                del baseline['coverage_by_class'][n]
            removed = counts(source) if source is not None else {'covered': 0, 'missed': 0}
        else:
            entry['kind'] = 'deleted_lines_in_retained_class'
            lines = {int(l.get('nr')): l for l in source.findall('line')}
            deleted, retained = removed_lines(original.read_text().splitlines(), current.read_text().splitlines(), set(lines))
            executable = sorted(deleted & set(lines))
            removed = line_counts([lines[n] for n in executable])
            if len(affected) != 1:
                raise ValueError('Retained class unexpectedly has nested class counters')
            baseline['coverage_by_class'][class_name] = subtract(baseline['coverage_by_class'][class_name], removed)
            entry.update({'deleted_executable_source_lines': executable, 'retained_line_overlap': sorted(set(executable) & retained),
                          'retained_class': class_name, 'class_before': counts(classes[class_name]),
                          'class_after': baseline['coverage_by_class'][class_name],
                          'removed_method_LINE_counters': {m.get('name') + m.get('desc'): counts(m) for m in classes[class_name].findall('method') if int(m.get('line', '0')) in deleted}})
        entry['unique_source_LINE_counters_removed'] = removed
        owner = relative.split('/')[0]
        layer = next(p for p in relative.split('/') if p in ('application', 'domain', 'infrastructure'))
        for key in ('layer/' + layer, f'module/{owner}/{layer}'):
            baseline['coverage'][key] = subtract(baseline['coverage'][key], removed)
        audit['removals'].append(entry)
    baseline['coverage_adjustment'] = {k: v for k, v in audit.items() if k != 'removals'}
    baseline['coverage_adjustment']['audit'] = 'removed-production-coverage-audit.json'
    output.mkdir(parents=True, exist_ok=True)
    (output / 'coverage-baseline.json').write_text(json.dumps(baseline, indent=2) + '\n')
    (output / 'removed-production-coverage-audit.json').write_text(json.dumps(audit, indent=2) + '\n')
    print(json.dumps({'checked_unchanged_classes': len(checked), 'removed': [{r['path']: r['unique_source_LINE_counters_removed']} for r in audit['removals']]}, indent=2))

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    for key in ('baseline', 'jacoco', 'original_root', 'current_root', 'output'):
        parser.add_argument(key, type=Path)
    args = parser.parse_args()
    build(args.baseline, args.jacoco, args.original_root, args.current_root, args.output)
