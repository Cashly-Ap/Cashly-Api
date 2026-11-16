#!/usr/bin/env python3
"""Enforce complete discovery, layer thresholds and module/layer nonregression."""
import argparse
import json
from pathlib import Path
from coverage_ownership import regroup

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('baseline', type=Path)
parser.add_argument('final', type=Path)
parser.add_argument('output', type=Path)
parser.add_argument('--ownership', type=Path)
parser.add_argument('--splits', type=Path)
args = parser.parse_args()
baseline = json.loads(args.baseline.read_text())
final = json.loads(args.final.read_text())
errors = []
ownership_audit = None
if args.ownership:
    if not args.splits:
        parser.error('--ownership requires --splits')
    moves = json.loads(args.ownership.read_text())
    splits = json.loads(args.splits.read_text())
    try:
        baseline_groups, baseline_audit = regroup(baseline['coverage_by_class'], moves, splits, baseline=True)
        final_groups, final_audit = regroup(final['coverage_by_class'], moves, splits, baseline=False)
        # JaCoCo package counters de-duplicate source lines shared by nested
        # classes. Preserve the existing global threshold definition exactly.
        for summary, groups in ((baseline, baseline_groups), (final, final_groups)):
            groups.update({key: value for key, value in summary['coverage'].items() if key.startswith('layer/')})
            summary['coverage'] = groups
        ownership_audit = {'baseline': baseline_audit, 'final': final_audit}
    except (ValueError, KeyError) as error:
        errors.append('Coverage ownership reconciliation failed: ' + str(error))

if not final['counts']['tests']:
    errors.append('No tests executed')
for metric in ('failures', 'errors', 'skipped'):
    if final['counts'][metric]:
        errors.append(f'Nonzero {metric}: {final["counts"][metric]}')
if final['missing_executable_classes']:
    errors.append('Executable classes missing from reports')
for layer, minimum in {'domain': 90, 'application': 80, 'infrastructure': 60}.items():
    value = final['coverage'].get('layer/' + layer, {}).get('line_coverage_percent')
    if value is None or value <= minimum:
        errors.append(f'{layer} coverage must exceed {minimum}%: {value}')
comparison = {}
for key, previous in baseline['coverage'].items():
    if key.endswith('/unclassified'):
        continue
    current = final['coverage'].get(key)
    if current is None:
        errors.append('Missing coverage group: ' + key)
        continue
    comparison[key] = {'baseline': previous, 'final': current}
    # Compare exact fractions rather than rounded display percentages.
    before_total = previous['covered'] + previous['missed']
    after_total = current['covered'] + current['missed']
    if current['covered'] * before_total < previous['covered'] * after_total:
        errors.append('Coverage regression: ' + key)
result = {'status': 'FAIL' if errors else 'PASS', 'errors': errors,
          'counts': final['counts'], 'comparison': comparison, 'ownership_audit': ownership_audit}
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps({'status': result['status'], 'errors': errors}, indent=2))
raise SystemExit(bool(errors))
