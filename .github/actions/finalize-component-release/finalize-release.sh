#!/usr/bin/env bash
#
# Tags a per-component patch release.
#
# Contract:
#   Inputs (env)
#     SLUG     Component slug, e.g. "sbs-2".
#     VERSION  Patch version being released, "X.Y.Z".
#   Working dir  The repository root; HEAD is the (already-pushed) patch-branch commit to tag.
#   Effect       Creates and pushes the tag "<VERSION>-<SLUG>", refusing to overwrite an existing one.
#                The GitHub Release (with its notes) is created manually by the developer afterwards.
#   Exit         Non-zero if the tag already exists or the git push fails.
#
set -euo pipefail

tag="${VERSION}-${SLUG}"

if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  echo "::error::Tag ${tag} already exists — refusing to overwrite a released component."
  exit 1
fi

git tag "${tag}"
git push origin "${tag}"
echo "Tagged ${tag}"
