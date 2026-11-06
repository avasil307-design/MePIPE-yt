#!/usr/bin/env bash
set -uo pipefail

log_file="${RUNNER_TEMP:-/tmp}/mepipe-gradle-build.log"
set +e
./gradlew --no-daemon assembleDebug --console=plain --stacktrace 2>&1 | tee "$log_file"
gradle_status=${PIPESTATUS[0]}
set -e

if [[ $gradle_status -ne 0 ]]; then
  echo "## Gradle build diagnostics" >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
  echo '```text' >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
  tail -n 100 "$log_file" >> "${GITHUB_STEP_SUMMARY:-/dev/null}"
  echo '```' >> "${GITHUB_STEP_SUMMARY:-/dev/null}"

  root_cause=$(python3 - "$log_file" <<'PY'
import sys
lines = open(sys.argv[1], encoding="utf-8", errors="replace").read().splitlines()
start = next((i for i, line in enumerate(lines) if "What went wrong:" in line), None)
if start is not None:
    print(" | ".join(line.strip() for line in lines[start:start + 18] if line.strip()))
else:
    print(" | ".join(line.strip() for line in lines[-30:] if line.strip()))
PY
  )
  root_cause=${root_cause//'%'/'%25'}
  root_cause=${root_cause//$'\r'/'%0D'}
  root_cause=${root_cause//$'\n'/'%0A'}
  echo "::error title=Gradle root cause::$root_cause"

  while IFS= read -r diagnostic; do
    diagnostic=${diagnostic//'%'/'%25'}
    diagnostic=${diagnostic//$'\r'/'%0D'}
    diagnostic=${diagnostic//$'\n'/'%0A'}
    echo "::error title=Kotlin compiler diagnostic::$diagnostic"
  done < <(grep -E '(^e: |^w: |\.kt:[0-9]+:[0-9]+|Unresolved reference|Type mismatch|Overload resolution ambiguity)' "$log_file" | tail -n 30 || true)
fi

exit "$gradle_status"
