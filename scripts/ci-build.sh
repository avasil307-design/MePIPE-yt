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

  while IFS= read -r diagnostic; do
    echo "::error title=Gradle diagnostic::$diagnostic"
  done < <(grep -Ei '(^e: |^FAILURE:|^\* What went wrong:|^Execution failed for task|^Could not resolve |^Could not find |requires|minCompileSdk|compileSdkVersion|AAR metadata|Android Gradle plugin)' "$log_file" | tail -n 40 || true)
fi

exit "$gradle_status"
