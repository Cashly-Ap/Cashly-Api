#!/usr/bin/env bash
# Runs only in a uniquely named verification project. Sources are copied into an
# ephemeral runner before Maven executes; live source edits cannot alter that run.
set -euo pipefail
umask 077

if [[ $# -lt 1 || $# -gt 2 || ! "$1" =~ ^(baseline|final)$ ]]; then
  echo 'Usage: run-structure-verification.sh baseline|final [source-snapshot-directory]' >&2
  exit 2
fi
verification_mode="$1"
backend_root="$(cd "$(dirname "$0")/.." && pwd)"
export SCREAMING_SOURCE_DIR="$(cd "${2:-$backend_root}" && pwd)"
verification_project="${SCREAMING_PROJECT:-cashly-screaming-${verification_mode}-$(date -u +%Y%m%d%H%M%S)}"
if [[ ! "$verification_project" =~ ^cashly-screaming-[a-z0-9-]+$ ]]; then
  echo 'SCREAMING_PROJECT must begin with cashly-screaming- and contain only lowercase letters, digits and hyphens.' >&2
  exit 2
fi
verification_output="${SCREAMING_OUTPUT_DIR:-$backend_root/target/structure-verification/$verification_project}"
coverage_baseline="${SCREAMING_COVERAGE_BASELINE:-$backend_root/openspec/changes/remove-warehouse-reassignment/evidence/coverage-baseline.json}"
coverage_ownership="${SCREAMING_COVERAGE_OWNERSHIP:-$backend_root/openspec/changes/module-owned-reporting/relocation-manifest.json}"
coverage_splits="${SCREAMING_COVERAGE_SPLITS:-$backend_root/openspec/changes/module-owned-reporting/split-manifest.json}"
if [[ -e "$verification_output" ]]; then
  echo "Evidence directory already exists; use a fresh project/output: $verification_output" >&2
  exit 2
fi
mkdir -p "$verification_output"
compose=(docker compose -p "$verification_project" -f "$backend_root/compose.screaming-test.yaml")
# Explicit file + project avoids .env/default development services and resources.
"${compose[@]}" config --quiet
if [[ -n "$("${compose[@]}" ps -aq)" ]]; then
  echo 'Verification project already has containers; choose a fresh project.' >&2
  exit 2
fi
{
  printf 'mode=%s\nproject=%s\nsource=%s\nstarted_utc=%s\n' "$verification_mode" "$verification_project" "$SCREAMING_SOURCE_DIR" "$(date -u +%FT%TZ)"
  printf 'image=%s\n' "${SCREAMING_TEST_IMAGE:-maven:3.9.9-amazoncorretto-24}"
} > "$verification_output/run.txt"
# Never tear down automatically: reports and failed database state survive errors.
"${compose[@]}" up -d --wait suite-db inventory-db runner
# Docker cp also works with a remote DinD daemon; a client-side bind path does not.
for source_item in src pom.xml .mvn mvnw; do
  "${compose[@]}" cp "$SCREAMING_SOURCE_DIR/$source_item" "runner:/workspace/$source_item"
done
"${compose[@]}" exec -T runner sh -eu -c '
  test -e /workspace/pom.xml
  chmod +x /workspace/mvnw
  find src -type f -print0 | sort -z | xargs -0 sha256sum > /workspace/source-sha256.txt
  sha256sum pom.xml mvnw >> /workspace/source-sha256.txt
  java -version
  ./mvnw -version
' > "$verification_output/runtime.txt" 2>&1
maven_command=(./mvnw -B clean verify '-Dtest=Test*,*Test,*Tests,*TestCase,*IT' -DfailIfNoTests=true -Dinventory.test.url=jdbc:mysql://inventory-db:3306/screaming_inventory -Dinventory.test.username=screaming_test -Dinventory.test.password=screaming_test)
printf '%q ' "${maven_command[@]}" > "$verification_output/command.txt"
printf '\n' >> "$verification_output/command.txt"
set +e
"${compose[@]}" exec -T runner "${maven_command[@]}" > "$verification_output/maven.log" 2>&1
verification_result=$?
set -e
printf 'finished_utc=%s\nexit_code=%s\n' "$(date -u +%FT%TZ)" "$verification_result" >> "$verification_output/run.txt"
# Missing reports are recorded without discarding an earlier Maven failure.
for report in source-sha256.txt target/surefire-reports target/site/jacoco target/jacoco.exec; do
  if "${compose[@]}" exec -T runner test -e "/workspace/$report"; then
    "${compose[@]}" cp "runner:/workspace/$report" "$verification_output/$(basename "$report")" >> "$verification_output/export.log" 2>&1
  else
    printf 'Missing: %s\n' "$report" >> "$verification_output/export.log"
  fi
done
if [[ "$verification_mode" == final ]]; then
  "${compose[@]}" up -d analyzer
  "${compose[@]}" cp "$verification_output" analyzer:/analysis/evidence
  "${compose[@]}" cp "$SCREAMING_SOURCE_DIR/src" analyzer:/analysis/src
  for analysis_script in summarize-structure-verification.py compare-structure-coverage.py coverage_ownership.py; do
    "${compose[@]}" cp "$backend_root/scripts/$analysis_script" "analyzer:/analysis/$analysis_script"
  done
  "${compose[@]}" cp "$coverage_baseline" analyzer:/analysis/baseline.json
  "${compose[@]}" cp "$coverage_ownership" analyzer:/analysis/ownership.json
  "${compose[@]}" cp "$coverage_splits" analyzer:/analysis/splits.json
  "${compose[@]}" exec -T analyzer python summarize-structure-verification.py /analysis/evidence /analysis /analysis/summary
  set +e
  "${compose[@]}" exec -T analyzer python compare-structure-coverage.py /analysis/baseline.json /analysis/summary/summary.json /analysis/summary/coverage-comparison.json --ownership /analysis/ownership.json --splits /analysis/splits.json
  coverage_result=$?
  set -e
  if [[ "$verification_result" -eq 0 ]]; then verification_result=$coverage_result; fi
  "${compose[@]}" cp analyzer:/analysis/summary "$verification_output/summary"
  printf 'coverage_gate_exit_code=%s\n' "$coverage_result" >> "$verification_output/run.txt"
fi
printf 'Evidence: %s\nMaven exit: %s\n' "$verification_output" "$verification_result"
printf 'After reviewing/exporting evidence, cleanup ONLY this project:\n'
printf '%q ' "${compose[@]}" down --volumes --remove-orphans
printf '\n'
exit "$verification_result"
