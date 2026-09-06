#!/usr/bin/env bash
#
# Creates a git tag and pushes it to origin, refusing to overwrite an existing tag.
#
# Contract:
#   Inputs (env)
#     TAG  The tag to create and push. Required (the action requires it).
#   Exit   Non-zero if TAG is unset or the tag already exists.
#
set -euo pipefail

if [ -z "${TAG:-}" ]; then
  echo "::error::No tag provided."
  exit 1
fi

if git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
  echo "::error::Tag ${TAG} already exists — refusing to overwrite a released component."
  exit 1
fi

git tag "${TAG}"
git push origin "${TAG}"
echo "Tagged ${TAG}"
