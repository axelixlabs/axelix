---
name: prepare-minor-release
description: Prepare an Axelix minor lockstep release — the pre-release housekeeping changes, a hand-editable release-notes draft, and the post-release bump to the next -SNAPSHOT. Use this skill whenever the user says they are cutting, preparing, or shipping a release ("we're releasing 1.1.0", "prepare the minor release", "release housekeeping"), asks to draft or write release notes for a version, or asks to move master to the next snapshot / development version after a release. Also use it for the docs part alone ("swap the upcoming notices for released notices") — that swap is a post-release-housekeeping step.
---

# Prepare a minor release

Axelix minors are **lockstep** releases: every component (starters, plugins, Master JAR/Docker/Helm) ships
together under one `X.Y.0` version, marked by a `vX.Y.0` tag on `master`. The authoritative procedure lives in
`docs/docs/more/development/releases.mdx` — read it before starting; if it disagrees with this skill, the docs
win and this skill should be updated.

This skill covers three phases. Figure out from the conversation which one the user is in; if the target
version is not stated, derive it from `axelixVersion` in the root `gradle.properties` (strip `-SNAPSHOT`) and
confirm it with the user before editing anything.

1. **Pre-release housekeeping** — version, playgrounds.
2. **Release notes draft** — diff against the previous minor tag.
3. **Post-release housekeeping** — move `master` to the next minor's `-SNAPSHOT`, swap the docs notices.

## Hard boundaries

The point of these boundaries is that a release is a deliberate, human-triggered act — the skill prepares,
the developer pulls the trigger.

- **Never commit and never push.** Housekeeping must land as a *single commit on `master`* so the release can
  be reset or cherry-picked cleanly — the developer reviews the staged diff and makes that commit themselves.
  Leave every change in the working tree and show a summary.
- **Never create tags.** The release pipeline creates `vX.Y.0` after its pre-release checks pass; a hand-made
  tag bypasses those checks.
- **Never launch the pipeline.** `.github/workflows/release-lockstep.yaml` is a `workflow_dispatch` the
  developer triggers manually. Do not run it via `gh workflow run` or otherwise.

## Phase 1 — Pre-release housekeeping

All of the following becomes one commit (made by the user). Two groups of edits:

### 1. Fleet version

In the **root** `gradle.properties`, set `axelixVersion` to the exact release version — `1.1.0-SNAPSHOT` →
`1.1.0`. This is the value the pipeline reads and what the tag is named after. The root `gradle.properties`
is the **only** place the version lives — every component inherits it from there. Subproject
`gradle.properties` files never contain `axelixVersion`; do not touch them.

### 2. Playgrounds

The example apps under `playgrounds/` are deliberately separate projects: they do **not** inherit
`axelixVersion` and pin the Axelix starter and plugin versions they consume explicitly. Bump **every** Axelix
pin — starters *and* plugins — to the release version.

Find the pins by searching rather than trusting a memorized list (playgrounds get added):

```bash
grep -rn "com.axelixlabs" playgrounds/ --include="build.gradle*" --include="pom.xml"
```

- Gradle apps: the `id("com.axelixlabs.axelix") version "..."` plugin line and the
  `com.axelixlabs:axelix-spring-boot-N-starter:...` dependency string in `build.gradle.kts`.
- Maven apps: the `<version>` of every `com.axelixlabs` artifact (starter dependency and
  `axelix-maven-plugin`) in `pom.xml`.

Afterwards verify no Axelix `-SNAPSHOT` or stale pin survived:
`grep -rn "com.axelixlabs" playgrounds/ --include="build.gradle*" --include="pom.xml" | grep -v "X.Y.0"`
should only return lines that carry no version at all (e.g. Maven `<artifactId>` lines).

### Wrap-up

Show `git status` and a short per-group summary of what changed, plus the verification grep results. Remind
the user this is meant to be a **single housekeeping commit on `master`**, and that after it lands they launch
`release-lockstep.yaml` themselves.

## Phase 2 — Release notes draft

The pipeline creates the tag but not the notes — the developer writes them by hand against the tag. Draft
them so the developer edits instead of starting from scratch.

1. **Find the previous minor tag**: the highest `vX.Y.0` tag. Ignore pre-releases (`v1.0.0-M1`) and
   patch tags (`vX.Y.Z` with a non-zero patch, e.g. `v1.0.2` — fleet tags too, but cut from a patch
   branch) — the comparison base is the previous *minor* cut from `master`.
2. **Collect the changes**: `git log vPREV..master` (merge commits and PR references are usually the best
   unit of meaning; `gh pr view` for detail when a commit message is thin).
3. **Categorize** into exactly these sections:
   - **New features**
   - **Bug fixes**
   - **Noteworthy changes** — not breaking, but users should know (behavior changes, defaults within the
     contract, deprecations, notable dependency upgrades).
   - **Breaking changes** — defined by the compatibility contract in
     `docs/docs/more/compatibility-and-versioning.mdx`, *not* by Java-level API diffs. Breaking means:
     changes to Master's **configuration contract** (property names, default values, types — for both the
     Helm chart and the standalone JAR; the contract promises these change only in majors, so finding one in
     a minor is a red flag to raise with the user), or revisited **interaction protocols** between components
     (Master ↔ starter, plugin ↔ starter — these force the fleet-wide `major.minor` upgrade). Public Java
     API changes of Master or the starters are explicitly **internal** and are *not* breaking.
4. **Write the draft** to `release-notes-vX.Y.0.md` in the repository root. It is a working file for the
   developer: it stays **untracked** and must not slip into the housekeeping commit — say so explicitly, and
   never `git add` it. The developer publishes the final text against the `vX.Y.0` tag manually.

## Phase 3 — Post-release housekeeping

After the pipeline has published the fleet and created `vX.Y.0`, development on `master` moves to the next
minor. One commit (again: stage only, the user commits). Two groups of edits:

### 1. Snapshot bump

- Root `gradle.properties`: `axelixVersion` → `X.(Y+1).0-SNAPSHOT`.
- Playgrounds: **all** Axelix pins — starters *and* plugins — → `X.(Y+1).0-SNAPSHOT`, found with the same
  grep as in Phase 1.

### 2. Docs notices

While a minor is in development, docs sections shipping with it carry an `<UpcomingReleaseNotice />`
(components in `docs/src/components/`). Now that the minor is cut, each of those becomes a
`<ReleasedInNotice version="X.Y.0" />` — where `X.Y.0` is the version **just released**, not the new
snapshot.

- Find every usage: `grep -rn "UpcomingReleaseNotice" docs/`.
- In each page, replace the JSX usage **and** swap the import (`UpcomingReleaseNotice` →
  `ReleasedInNotice`, both come from `@site/src/components`).
- Every English page under `docs/docs/` has a mirrored Russian copy under
  `docs/i18n/ru/docusaurus-plugin-content-docs/current/` — update both, or the docs CI build fails on the
  `ru` locale.
- All existing notices should belong to the release just cut (they were added for it); if a notice looks
  like it belongs to a *future* release, stop and ask the user instead of converting it.
- Verify: the grep above must come back empty for `docs/docs/` and the ru mirror when done.

Verify with the same greps as Phase 1, show the summary, let the user commit.
