#!/usr/bin/env python3
"""Publish safe test/coverage summaries; never copy XML environment properties."""
import argparse
import collections
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
from coverage_ownership import class_counters, module_owner

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("evidence", type=Path)
parser.add_argument("source_snapshot", type=Path)
parser.add_argument("output", type=Path)
args = parser.parse_args()
args.output.mkdir(parents=True, exist_ok=True)
expected = {}
for source in sorted((args.source_snapshot / "src/test/java").rglob("*.java")):
    contents = source.read_text()
    package = re.search(r"^package\s+([\w.]+);", contents, re.M)
    if package and re.search(r"@(?:Test|ParameterizedTest|RepeatedTest|TestFactory|TestTemplate)\b", contents):
        expected[f"{package.group(1)}.{source.stem}"] = "IT" if source.stem.endswith("IT") else "ordinary"

suites = []
executed_owners = set()
for report in sorted((args.evidence / "surefire-reports").glob("TEST-*.xml")):
    suite = ET.parse(report).getroot()
    name = suite.attrib["name"]
    if int(suite.attrib.get("tests", 0)):
        executed_owners.add(name.split("$")[0])
    cases = suite.findall("testcase")
    for case in cases:
        executed_owners.add(case.attrib.get("classname", name).split("$")[0])
    suites.append({
        "suite": name,
        **{metric: int(suite.attrib.get(metric, 0)) for metric in ("tests", "failures", "errors", "skipped")},
        "original_xml_sha256": hashlib.sha256(report.read_bytes()).hexdigest(),
        "failed_cases": [{"class": case.attrib.get("classname", name), "name": case.attrib["name"],
                          "error_types": [element.attrib.get("type", element.tag)
                                          for element in list(case) if element.tag in ("failure", "error")]}
                         for case in cases if case.find("failure") is not None or case.find("error") is not None]
    })

coverage = {}
coverage_report = args.evidence / "jacoco/jacoco.xml"
if coverage_report.exists():
    groups = collections.defaultdict(lambda: {"covered": 0, "missed": 0})
    for package in ET.parse(coverage_report).getroot().findall("package"):
        parts = package.attrib["name"].split("/")
        if len(parts) < 4:
            continue
        module = module_owner(package.attrib["name"])
        layer = next((part for part in parts[4:] if part in ("domain", "application", "infrastructure")), None)
        if layer is None and module == "infrastructure":
            layer = "infrastructure"
        if layer is None:
            layer = "unclassified"
        counter = next((item for item in package.findall("counter") if item.attrib["type"] == "LINE"), None)
        if counter is None:
            continue
        for key in (f"layer/{layer}", f"module/{module}/{layer}"):
            groups[key]["covered"] += int(counter.attrib["covered"])
            groups[key]["missed"] += int(counter.attrib["missed"])
    for key, value in sorted(groups.items()):
        total = value["covered"] + value["missed"]
        coverage[key] = {**value, "line_coverage_percent": round(100 * value["covered"] / total, 4) if total else None}

summary = {
    "scope": "Complete explicitly selected suite including all inventoried IT; inspect failures and discovery reconciliation",
    "sanitization": "Only suite/test identities, numeric counters and XML hashes; no JVM/environment properties or error messages",
    "counts": {metric: sum(item[metric] for item in suites) for metric in ("tests", "failures", "errors", "skipped")},
    "expected_ordinary_classes": sum(kind == "ordinary" for kind in expected.values()),
    "expected_it_classes": sum(kind == "IT" for kind in expected.values()),
    "missing_executable_classes": sorted(set(expected) - executed_owners),
    "executed_inventory_classes": sorted(set(expected) & executed_owners),
    "report_files": len(suites),
    "suites": suites,
    "coverage": coverage,
    "coverage_by_class": class_counters(coverage_report) if coverage_report.exists() else {},
}
(args.output / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
counts = summary["counts"]
lines = ["# Complete-suite execution summary", "", f"Tests: {counts['tests']}; failures: {counts['failures']}; errors: {counts['errors']}; skipped: {counts['skipped']}.",
         f"Expected source classes: {summary['expected_ordinary_classes']} ordinary + {summary['expected_it_classes']} IT.",
         f"Missing executable classes: {len(summary['missing_executable_classes'])}.", "", "Raw reports remain in the restricted ignored evidence directory; summary.json retains report SHA-256 hashes."]
if counts['failures'] or counts['errors'] or summary['missing_executable_classes']:
    lines += ["", "Status: FAILED or INCOMPLETE. Do not treat this run as passing."]
else:
    lines += ["", "Status: all reported cases passed; review coverage and remaining acceptance gates separately."]
for key, item in coverage.items():
    if key.startswith("layer/"):
        lines.append(f"{key}: {item['line_coverage_percent']}% ({item['covered']} covered, {item['missed']} missed).")
for item in suites:
    for failure in item['failed_cases']:
        lines.append(f"Failure: {failure['class']} :: {failure['name']} ({', '.join(failure['error_types'])}).")
(args.output / "summary.md").write_text("\n".join(lines) + "\n")
print(json.dumps({"counts": counts, "missing_classes": summary['missing_executable_classes'], "report_files": len(suites), "coverage": {key: value for key, value in coverage.items() if key.startswith('layer/')}}, indent=2))
