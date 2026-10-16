#!/usr/bin/env python3
"""Relocate reviewed Java types while preserving symbol references and resources."""
from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from pathlib import Path

CHANGE = Path('openspec/changes/screaming-backend-structure')
PACKAGE = re.compile(r'^package\s+([\w.]+);', re.M)
IMPORT = re.compile(r'^import\s+(static\s+)?([\w.]+)(\.\*)?;', re.M)


def fqn(path: str) -> str:
    return path.split('/java/', 1)[1].removesuffix('.java').replace('/', '.')


def code_tokens(text: str) -> set[str]:
    masked = re.sub(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*[\s\S]*?\*/|//[^\n]*', ' ', text)
    masked = re.sub(r'^(?:import|package)\s+[^;]+;', '', masked, flags=re.M)
    return set(re.findall(r'\b[A-Za-z_$][\w$]*\b', masked))


def rewrite_text(text: str, own_fqn: str | None, translated: dict[str, str], package_symbols: dict[str, list[str]], implicit: list[str] = ()) -> str:
    tokens = code_tokens(text)
    explicit_names = {m.group(2).rsplit('.', 1)[-1] for m in IMPORT.finditer(text) if not m.group(1) and not m.group(3)}
    extra = set()
    def expand(match):
        static, name, wildcard = match.groups()
        if not static and wildcard and name in package_symbols:
            symbols = [typ for typ in package_symbols[name] if typ.rsplit('.', 1)[1] in tokens and typ.rsplit('.', 1)[1] not in explicit_names]
            return '\n'.join('import ' + translated.get(typ, typ) + ';' for typ in symbols)
        return match.group(0)
    text = IMPORT.sub(expand, text)
    if translated:
        pattern = re.compile(r'(?<![\w$])(?:' + '|'.join(re.escape(x) for x in sorted(translated, key=len, reverse=True)) + r')(?![\w])')
        text = pattern.sub(lambda match: translated[match.group(0)], text)
    current_package = PACKAGE.search(text).group(1)
    if own_fqn:
        new_package = translated.get(own_fqn, own_fqn).rsplit('.', 1)[0]
        text = PACKAGE.sub('package ' + new_package + ';', text, count=1)
        current_package = new_package
    for typ in implicit:
        new_type = translated.get(typ, typ)
        if new_type.rsplit('.', 1)[0] != current_package:
            extra.add(new_type)
    existing = {m.group(2) for m in IMPORT.finditer(text) if not m.group(1) and not m.group(3)}
    extra -= existing
    if extra:
        point = PACKAGE.search(text).end()
        text = text[:point] + '\n\n' + '\n'.join('import ' + typ + ';' for typ in sorted(extra)) + text[point:]
    # Wildcard expansion can create an import already provided explicitly.
    seen = set()
    def dedupe(match):
        key = match.group(0)
        if key in seen:
            return ''
        seen.add(key)
        return key
    return IMPORT.sub(dedupe, text)


def load_rows(root: Path) -> list[dict]:
    return [row for name in ('source-moves.json', 'test-moves.json') for row in json.loads((root / CHANGE / name).read_text())]


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path.cwd())
    parser.add_argument('--owners', nargs='+', required=True)
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    root = args.root.resolve()
    rows = load_rows(root)
    selected = {row['owner'] for row in rows} if args.owners == ['all'] else set(args.owners)
    active = []
    for row in rows:
        if row['old'] != row['new'] and (row['owner'] in selected or ((root / row['new']).exists() and not (root / row['old']).exists())):
            active.append(row)
    translated = {fqn(row['old']): fqn(row['new']) for row in active}
    symbols = defaultdict(list)
    for row in rows:
        typ = fqn(row['old'])
        symbols[typ.rsplit('.', 1)[0]].append(typ)
    implicit_path = root / CHANGE / 'relocation-symbols.json'
    implicit = json.loads(implicit_path.read_text())['required_imports_by_old_fqn']
    by_path = {row[side]: row for row in rows for side in ('old', 'new')}
    writes = {}
    moves = []
    for path in sorted((root / 'src').rglob('*.java')):
        relative = str(path.relative_to(root))
        row = by_path.get(relative)
        own = fqn(row['old']) if row else None
        destination = root / row['new'] if row and row in active else path
        original = path.read_text()
        text = rewrite_text(original, own, translated, symbols, implicit.get(own, []))
        if own == 'com.cashly.cashly_api.customers.application.usecases.CustomerTestHelper' and row in active:
            text = re.sub(r'(?m)^final class CustomerTestHelper', 'public final class CustomerTestHelper', text)
            text = re.sub(r'(?m)^(    )static (Customer|Warehouse|CustomerWarehouseAccess) (build\w+)\(', r'\1public static \2 \3(', text)
        if destination != path:
            assert not destination.exists(), 'Destination already exists: ' + str(destination)
            moves.append((path, destination))
        if destination != path or text != original:
            writes[destination] = text
    result = {'selected_owners': sorted(selected), 'types_relocated': len(moves), 'java_files_rewritten': len(writes), 'apply': args.apply}
    if args.apply:
        for path, text in writes.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text)
        for old, new in moves:
            old.unlink()
        for row in json.loads((root / CHANGE / 'resource-inventory.json').read_text()):
            if row['old'] != row['new'] and (root / row['old']).exists():
                destination = root / row['new']
                assert not destination.exists()
                destination.parent.mkdir(parents=True, exist_ok=True)
                (root / row['old']).rename(destination)
        for path in sorted((root / 'src').rglob('*'), key=lambda p: len(p.parts), reverse=True):
            if path.is_dir() and not any(path.iterdir()):
                path.rmdir()
    print(json.dumps(result, indent=2))


if __name__ == '__main__':
    main()
