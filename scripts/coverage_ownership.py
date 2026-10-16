"""Reconcile JaCoCo line counters across exact moves and explicit split families."""
import collections
import xml.etree.ElementTree as ET


def class_counters(report):
    result = {}
    for item in ET.parse(report).getroot().iter('class'):
        counter = next((c for c in item.findall('counter') if c.get('type') == 'LINE'), None)
        if counter is not None:
            result[item.attrib['name']] = {key: int(counter.get(key)) for key in ('covered', 'missed')}
    return result


def class_name(path):
    return path.removeprefix('src/main/java/').removesuffix('.java')


def regroup(classes, moves, splits, baseline):
    relocation = {class_name(r['old']): class_name(r['new']) for r in moves if r['old'].startswith('src/main/java/')}
    families = {}
    for row in splits:
        original = class_name(row['old'])
        for name in ([row['old']] if baseline else row['new']):
            families[class_name(name)] = original
    missing = sorted(set(families) - {name.split('$')[0] for name in classes})
    if missing:
        raise ValueError('Missing split classes: ' + ', '.join(missing))
    groups = collections.defaultdict(lambda: {'covered': 0, 'missed': 0})
    total = {'covered': 0, 'missed': 0}
    assigned = {'covered': 0, 'missed': 0}
    for name, counts in classes.items():
        outer, separator, nested = name.partition('$')
        target = relocation.get(outer, outer) + (separator + nested if separator else '') if baseline else name
        family = families.get(outer)
        if '/reports/' in target and not family:
            raise ValueError('Unmapped global report: ' + name)
        parts = target.split('/')
        owner = parts[3] if len(parts) > 3 else 'unknown'
        layer = next((p for p in parts[4:] if p in ('application', 'domain', 'infrastructure')), None)
        layer = layer or ('infrastructure' if owner == 'infrastructure' else 'unclassified')
        # Split classes have no unique owner. Keep all their debt in one explicit
        # family on BOTH sides, never duplicate or arbitrarily assign counters.
        keys = ['layer/' + layer, 'family/' + family] if family else ['layer/' + layer, f'module/{owner}/{layer}']
        if baseline and outer in relocation and not family:
            keys.append('relocated/' + target)
        elif not baseline and outer in set(relocation.values()) and not family:
            keys.append('relocated/' + target)
        for key in keys:
            for metric in total:
                groups[key][metric] += counts[metric]
        for metric in total:
            total[metric] += counts[metric]
            assigned[metric] += counts[metric]
    for counts in groups.values():
        denominator = counts['covered'] + counts['missed']
        counts['line_coverage_percent'] = round(counts['covered'] * 100 / denominator, 4) if denominator else None
    return dict(sorted(groups.items())), {'input': total, 'assigned': assigned, 'split_families': sorted(set(families.values())), 'policy': 'All classes remain in layer gates. Split counters are compared strictly as explicit families and excluded symmetrically from module groups; all other moved classes also have individual nonregression gates.'}
