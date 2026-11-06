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

  while IFS= read -r line; do
    line=${line//'%'/'%25'}
    line=${line//$'\r'/'%0D'}
    line=${line//$'\n'/'%0A'}
    echo "::notice title=Gradle output::$line"
  done < <(tail -n 40 "$log_file")
fi

exit "$gradle_status"
