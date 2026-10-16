"""Validate preparation or implemented relocation integrity; never replaces runtime tests."""

from __future__ import annotations

import argparse
import copy
import hashlib
import json
import re
import importlib.util
from collections import Counter
from pathlib import Path


CHANGE = Path(__file__).resolve().parents[1]
REPO = CHANGE.parents[2]
MANIFESTS = ("source-moves.json", "test-moves.json", "resource-inventory.json")


def implemented_integrity(rows: list[dict], baseline: Path) -> dict:
    """Compare every tracked baseline artifact with the deterministic relocation.

    The baseline directory must be an independently extracted Git HEAD or the
    frozen pre-migration source, with hashes verified against the manifests.
    """
    module_spec = importlib.util.spec_from_file_location("relocator", REPO / "scripts/restructure_backend.py")
    relocator = importlib.util.module_from_spec(module_spec)
    module_spec.loader.exec_module(relocator)
    java_rows = [row for row in rows if row["old"].endswith(".java")]
    translated = {relocator.fqn(row["old"]): relocator.fqn(row["new"]) for row in java_rows if row["old"] != row["new"]}
    symbols = {}
    for row in java_rows:
        typ = relocator.fqn(row["old"])
        symbols.setdefault(typ.rsplit(".", 1)[0], []).append(typ)
    implicit = json.loads((CHANGE / "relocation-symbols.json").read_text())["required_imports_by_old_fqn"]
    # These tests intentionally change to assert the new source layout.
    reviewed_tests = {
        "ApplicationPackageConventionTest.java", "LayerDependencyTest.java",
        "CustomerWarehouseBoundaryTest.java", "CustomerCreditQueryBoundaryTest.java",
    }
    deviations, reviewed, hashes = [], [], 0
    for row in rows:
        old = baseline / row["old"]
        assert old.is_file(), "Missing baseline artifact: " + row["old"]
        assert hashlib.sha256(old.read_bytes()).hexdigest() == row["source_sha256"], "Baseline hash mismatch: " + row["old"]
        hashes += 1
        new = REPO / row["new"]
        assert new.is_file(), "Missing relocated artifact: " + row["new"]
        if row["old"] != row["new"]:
            assert not (REPO / row["old"]).exists(), "Old path remains: " + row["old"]
        if old.suffix != ".java":
            assert old.read_bytes() == new.read_bytes(), "Resource bytes changed: " + row["new"]
            continue
        own = relocator.fqn(row["old"])
        expected = relocator.rewrite_text(old.read_text(), own, translated, symbols, implicit.get(own, []))
        if old.name == "CustomerTestHelper.java":
            expected = re.sub(r"(?m)^final class CustomerTestHelper", "public final class CustomerTestHelper", expected)
            expected = re.sub(r"(?m)^(    )static (Customer|Warehouse|CustomerWarehouseAccess) (build\w+)\(", r"\1public static \2 \3(", expected)
        actual = new.read_text()
        package = re.search(r"^package\s+([^;]+);", actual, re.M)
        assert package and package.group(1) == relocator.fqn(row["new"]).rsplit(".", 1)[0], "Package/path mismatch: " + row["new"]
        if expected != actual:
            # Different batching order can only affect whitespace/import order.
            def body(text):
                text = re.sub(r"^(?:package|import)\s+[^;]+;", "", text, flags=re.M)
                # Preserve literals exactly: stripping all whitespace would hide
                # behavioral edits such as changing an HTTP/error string.
                tokens = re.findall(r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|/\*[\s\S]*?\*/|//[^\n]*|[^\s]', text)
                return [token for token in tokens if not token.startswith(("/*", "//"))]
            if body(expected) != body(actual):
                if row["new"].startswith("src/test/") and old.name in reviewed_tests:
                    reviewed.append(row["new"])
                else:
                    deviations.append(row["new"])
    expected_paths = {row["new"] for row in rows}
    actual_paths = {str(path.relative_to(REPO)) for root in (REPO / "src/main", REPO / "src/test") for path in root.rglob("*") if path.is_file()}
    additions = sorted(actual_paths - expected_paths)
    assert not expected_paths - actual_paths, "Lost baseline artifacts"
    assert all(path.startswith("src/test/") and path.endswith(".java") for path in additions), "Unreviewed production/resource additions"
    return {"baseline_hashes_verified": hashes, "immutable_resources_verified": len(rows) - len(java_rows), "reviewed_structural_test_changes": reviewed, "additional_test_files": additions, "unexpected_semantic_changes": deviations}


def assert_valid_rows(rows: list[dict]) -> None:
    old = [row["old"] for row in rows]
    new = [row["new"] for row in rows]
    assert len(old) == len(set(old)), "Duplicate source entries"
    assert len(new) == len(set(new)), "Destination collision"
    for row in rows:
        for key in ("old", "new"):
            path = Path(row[key])
            assert not path.is_absolute() and ".." not in path.parts, "Unsafe path"
            assert path.parts[:2] in (("src", "main"), ("src", "test")), "Unexpected source root"
        assert Path(row["old"]).name == Path(row["new"]).name, "Filename changed"
        assert row["disposition"] == ("retain" if row["old"] == row["new"] else "move")
        assert re.fullmatch(r"[0-9a-f]{64}", row["source_sha256"]), "Invalid snapshot hash"
        assert row["reason"].strip(), "Missing ownership reason"


def reject(mutator, original: list[dict]) -> None:
    rows = copy.deepcopy(original)
    mutator(rows)
    try:
        assert_valid_rows(rows)
    except AssertionError:
        return
    raise AssertionError("Negative manifest check did not reject invalid data")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--implemented", action="store_true")
    parser.add_argument("--baseline-root", type=Path)
    args = parser.parse_args()
    manifests = {name: json.loads((CHANGE / name).read_text()) for name in MANIFESTS}
    rows = [row for group in manifests.values() for row in group]
    assert_valid_rows(rows)
    actual = {
        str(path.relative_to(REPO))
        for root in (REPO / "src/main", REPO / "src/test")
        for path in root.rglob("*") if path.is_file()
    }
    integrity = None
    if args.implemented:
        assert args.baseline_root, "--implemented requires --baseline-root from independent Git HEAD/frozen source"
        integrity = implemented_integrity(rows, args.baseline_root)
    else:
        assert actual == {row["old"] for row in rows}, "Incomplete or stale inventory"
    for row in ([] if args.implemented else rows):
        source = REPO / row["old"]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == row["source_sha256"], (
            "Source changed since review: " + row["old"]
        )
        if source.suffix == ".java":
            match = re.search(r"^package\s+([^;]+);", source.read_text(), re.MULTILINE)
            expected = str(source.relative_to(REPO / "src" / source.relative_to(REPO / "src").parts[0] / "java").parent).replace("/", ".")
            assert match and match.group(1) == expected, "Baseline package/path mismatch: " + row["old"]
    counts = {name: len(group) for name, group in manifests.items()}
    assert counts == {"source-moves.json": 1248, "test-moves.json": 573, "resource-inventory.json": 41}
    assert sum(row["old"].startswith("src/test/") for row in rows) == 584
    assert sum(row["old"].endswith(".java.bak") for row in rows) == 9
    discovery = json.loads((CHANGE / "test-discovery-inventory.json").read_text())
    assert discovery["total_files"] == 584
    assert discovery["counts_by_kind"]["integration-it"] == 7
    baseline = json.loads((CHANGE / "evidence/architecture-baseline/summary.json").read_text())
    assert baseline, "Missing scoped baseline evidence"
    for document in CHANGE.rglob("*.md"):
        text = document.read_text()
        assert text.count("```") % 2 == 0, "Unbalanced fenced block: " + str(document)
        for target in re.findall(r"\]\(([^)]+)\)", text):
            if "://" in target or target.startswith("#"):
                continue
            local = target.split("#", 1)[0]
            assert (document.parent / local).exists(), "Broken document link: " + target
    for name in ("proposal.md", "design.md", "tasks.md", "module-map.md", "verification-plan.md", "verify-report.md", "state.yaml"):
        assert (CHANGE / name).is_file(), "Missing change artifact: " + name
    for spec in (CHANGE / "specs").glob("*/spec.md"):
        text = spec.read_text()
        assert "## ADDED Requirements" in text
        blocks = re.split(r"^### Requirement:", text, flags=re.MULTILINE)[1:]
        assert blocks
        for block in blocks:
            assert re.search(r"\b(MUST|SHALL)\b", block), "Missing normative requirement"
            scenarios = re.split(r"^#### Scenario:", block, flags=re.MULTILINE)[1:]
            assert scenarios, "Requirement has no scenarios"
            for scenario in scenarios:
                assert all(word in scenario for word in ("GIVEN", "WHEN", "THEN")), "Untestable scenario"
    tasks = (CHANGE / "tasks.md").read_text()
    if not args.implemented:
        assert "- [x]" not in tasks.lower(), "Implementation prematurely marked complete"
        for line in ("Decision needed before apply: Yes", "Chained PRs recommended: Yes", "Chain strategy: pending", "400-line budget risk: High"):
            assert line in tasks
    for direction in ("old", "new"):
        tree = (CHANGE / "trees" / ("before.md" if direction == "old" else "after.md")).read_text()
        expected = Counter(Path(row[direction]).name for row in rows if row[direction].endswith(".java"))
        found = Counter(re.findall(r"\b([A-Za-z0-9_]+\.java)\b(?!\.bak)", tree))
        assert found == expected, "Tree source names differ from manifest"
    negative_checks = 0
    if args.self_test:
        reject(lambda group: group.append(copy.deepcopy(group[0])), rows)
        reject(lambda group: group[1].update(new=group[0]["new"]), rows)
        reject(lambda group: group[0].update(new="../outside.java"), rows)
        reject(lambda group: group[0].update(source_sha256="missing"), rows)
        negative_checks = 4
    print(json.dumps({"status": "FAIL" if integrity and integrity["unexpected_semantic_changes"] else "PASS", "scope": "implemented relocation integrity" if args.implemented else "preparation snapshot and document integrity only", "manifests": counts, "unique_artifacts": len(rows), "negative_checks": negative_checks, "runtime_migration_verified": False, "implementation": integrity}, indent=2))
    assert not integrity or not integrity["unexpected_semantic_changes"], "Unexpected semantic changes; inspect report above"


if __name__ == "__main__":
    main()
