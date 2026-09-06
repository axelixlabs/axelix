#!/usr/bin/env bash
#
# Reads the axelixVersion property from a gradle.properties file, optionally validating it.
#
# Contract:
#   Inputs (env)
#     FILE     Path to the gradle.properties to read. Required (the action defaults it).
#     PATTERN  Optional POSIX ERE; when non-empty the version must match it or the build fails.
#   Output     Writes "version=<value>" to $GITHUB_OUTPUT.
#   Exit       Non-zero if the file is missing, axelixVersion is unset, or it fails PATTERN.
#
set -euo pipefail

if [ ! -f "${FILE}" ]; then
  echo "::error::gradle.properties not found at ${FILE}."
  exit 1
fi

version="$(sed -n 's/^axelixVersion=//p' "${FILE}" | head -n1 | tr -d '[:space:]')"

if [ -z "${version}" ]; then
  echo "::error::${FILE} does not set axelixVersion."
  exit 1
fi

if [ -n "${PATTERN}" ] && [[ ! "${version}" =~ ${PATTERN} ]]; then
  echo "::error::axelixVersion '${version}' in ${FILE} does not match the required pattern /${PATTERN}/."
  exit 1
fi

echo "version=${version}" >> "${GITHUB_OUTPUT}"
echo "Resolved axelixVersion=${version} from ${FILE}"
