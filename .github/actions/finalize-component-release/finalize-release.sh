#!/usr/bin/env bash
#
# Tags a per-component patch release and publishes its GitHub Release.
#
# Contract:
#   Inputs (env)
#     SLUG            Component slug, e.g. "sbs-2".
#     COMPONENT_NAME  Human-readable name for the release title/notes, e.g. "Axelix Spring Boot 2 Starter".
#     VERSION         Patch version being released, "X.Y.Z".
#     BRANCH          Patch branch the release is cut from, "X.Y.0-<slug>" (used in the notes only).
#     GH_TOKEN        Token for "gh release create" (needs "contents: write").
#   Working dir       The repository root; HEAD is the (already-pushed) patch-branch commit to tag.
#   Effect            1. Refuses to re-tag an already-released "<VERSION>-<SLUG>".
#                     2. Renders release notes from .github/release-notes/patch-release-notes.md, filling
#                        the changelog from commits since this component's previous tag (or the fleet tag).
#                     3. Creates and pushes the tag "<VERSION>-<SLUG>".
#                     4. Publishes a GitHub Release for that tag (never marked "Latest").
#   Exit              Non-zero if the tag already exists, the template is missing, or any git/gh call fails.
#
set -euo pipefail

tag="${VERSION}-${SLUG}"

if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  echo "::error::Tag ${tag} already exists — refusing to overwrite a released component."
  exit 1
fi

# Build the changelog from the commits added on this patch branch since the previous release of THIS
# component — its last "X.Y.*-<slug>" tag if it has been patched before, otherwise the fleet tag it was
# forked from. The version-bump commit itself is filtered out.
minor="${VERSION%.*}"
prev="$(git tag -l "${minor}.*-${SLUG}" | sort -V | tail -n1)"
base="${prev:-v${minor}.0}"
CHANGES="$(git log --no-merges --pretty=format:'- %s (%h)' "${base}..HEAD" | grep -vE '^- release: ' || true)"
[ -z "${CHANGES}" ] && CHANGES="_No changelog entries for this patch._"
export CHANGES TAG="${tag}" MINOR="${minor}"

# Render the release notes from the template (only our placeholders are substituted).
template="${GITHUB_WORKSPACE}/.github/release-notes/patch-release-notes.md"
if [ ! -f "${template}" ]; then
  echo "::error::Release notes template not found at ${template}."
  exit 1
fi
notes="$(mktemp)"
envsubst '${COMPONENT_NAME} ${VERSION} ${TAG} ${BRANCH} ${MINOR} ${CHANGES}' < "${template}" > "${notes}"

# Tag the current commit (the developer already pushed the branch) only after everything above succeeded.
git tag "${tag}"
git push origin "${tag}"
echo "Tagged ${tag} on ${BRANCH}"

# A per-component patch must NOT claim the "Latest" badge over the fleet release, hence --latest=false.
gh release create "${tag}" \
  --title "${COMPONENT_NAME} ${VERSION}" \
  --notes-file "${notes}" \
  --latest=false
echo "Published GitHub Release ${tag}"
