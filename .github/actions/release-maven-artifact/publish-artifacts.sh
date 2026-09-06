#!/usr/bin/env bash
#
# Publishes an explicit list of Axelix Maven artifacts (starters and/or plugins) to Maven Central.
#
# Contract:
#   Input (env)  ARTIFACTS  Whitespace/newline-separated Gradle project paths, e.g.
#                           ":sbs:axelix-spring-boot-2-starter :plugins:axelix-gradle-plugin".
#                           Required; must be non-empty.
#   Working dir            The repository root (the Gradle build).
#   Effect                 Runs "<path>:publish" for every listed artifact in a single Gradle
#                          invocation. Signing (GPG) and the OSSRH promote step run inside Gradle.
#   Exit                   Non-zero if ARTIFACTS is empty or the Gradle publish fails.
#
set -euo pipefail

chmod +x ./gradlew

tasks=()
for artifact in ${ARTIFACTS}; do
  tasks+=("${artifact}:publish")
done

if [ ${#tasks[@]} -eq 0 ]; then
  echo "::error::release-maven-artifact requires a non-empty 'artifacts' list."
  exit 1
fi

echo "Publishing: ${tasks[*]}"
./gradlew "${tasks[@]}"
