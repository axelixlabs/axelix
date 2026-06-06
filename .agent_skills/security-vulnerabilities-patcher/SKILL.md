---
name: security-vulnerabilities-patcher
description: Create batched Dependabot-style pull requests for GitHub security findings in `axelixlabs/axelix`, grouped by dependency surface such as `master/front-end`, `master/build.gradle.kts`, or starter Gradle builds. Use this skill whenever the user asks to fix CodeQL, Trivy, SARIF, Dependabot, SAST, SCA, CVE, GHSA, or `security-check` findings for the Axelix repository and wants patch-release-safe PRs that bundle related vulnerabilities by manifest or module area instead of one PR per CVE.
---

# Security Vulnerabilities Patcher

Turn GitHub security alerts into **batched, Dependabot-style pull requests** for **`axelixlabs/axelix`**.

The key idea is **batch by Axelix dependency surface**, not by individual CVE. The desired shape is:

- one PR for `master/front-end` dependency updates,
- one PR for `master/build.gradle.kts` and directly coupled master backend build files,
- one PR for starter Gradle build files,
- and separate PRs for other clearly distinct dependency surfaces when needed.

The goal is still patch-release safety: each PR should stay locally understandable and safe to review, but it may fix several vulnerabilities at once if they belong to the same surface.

## What this skill is for

Use this workflow for **`axelixlabs/axelix`** when the repository has security findings from:

- **CodeQL** code scanning alerts.
- **Trivy** SARIF alerts uploaded into GitHub code scanning.
- **Dependabot alerts** or other GitHub dependency alerts surfaced through the GitHub API.
- A CI job such as `security-check` that is expected to fail on open HIGH/CRITICAL findings, and the user wants AI-authored remediation PRs.

This skill is intentionally **Axelix-specific**. It assumes the target repository is **`axelixlabs/axelix`** and that its security workflow uploads Trivy SARIF into GitHub code scanning, so GitHub API queries can see both **CodeQL** and **Trivy** findings.

## Core operating rules

1. **Batch by dependency surface, not by CVE.**
   Group vulnerabilities that are fixed by the same manifest, lockfile, Gradle build, or tightly coupled module area.

2. **Do not batch across unrelated Axelix surfaces.**
   `master/front-end` does not belong in the same PR as starter Gradle files. `master/build.gradle.kts` does not belong in the same PR as Docker base image remediation unless the same build surface truly requires it.

3. **Prefer patch-safe remediations.**
   Favor:
   - same-major dependency upgrades,
   - lockfile refreshes that follow from those upgrades,
   - minimal container base image bumps,
   - tiny local hardening patches that do not alter public behavior.

4. **Do not break public APIs or contracts.**
   The fix must be safe for a patch release. Avoid changes to public DTOs, REST contracts, starter configuration contracts, public Java APIs, or user-facing workflows unless the user explicitly accepts that risk.

5. **Do not hide the problem.**
   Do not "fix" alerts by suppressing them, loosening the scanner, or adding ignores unless the user explicitly asks for that path and understands the trade-off.

6. **Do not invent vulnerability identifiers.**
   Prefer real CVE identifiers when they exist. If some findings are GHSA-only, say so plainly instead of fabricating CVEs.

## Preferred Axelix batch boundaries

Use these batch boundaries by default unless the live alert data strongly suggests a better split:

1. **`master/front-end`**
   Group vulnerabilities remediated through `master/front-end/package.json`, `master/front-end/package-lock.json`, or equivalent front-end dependency files.

2. **Master backend Gradle**
   Group vulnerabilities remediated through `master/build.gradle.kts`, root Gradle version catalogs or shared Gradle declarations that primarily affect master backend dependencies.

3. **Starters Gradle**
   Group vulnerabilities remediated through `sbs/build.gradle.kts`, `sbs/axelix-spring-boot-2/build.gradle.kts`, `sbs/axelix-spring-boot-3/build.gradle.kts`, or directly coupled starter build files.

4. **Shared/common Gradle**
   If vulnerabilities live primarily in `common/` modules or shared build logic, use a separate PR when that keeps the blast radius smaller and clearer.

5. **Docker/image surface**
   If Trivy image findings are fixed via Docker base image changes rather than application manifests, keep that in its own PR unless the user explicitly wants it combined.

When in doubt, ask: "Would a maintainer naturally review these updates together like a single Dependabot PR?" If not, split them.

## Supported alert sources

### 1. Code scanning alerts

Use GitHub's code scanning alerts API for:

- **CodeQL** findings.
- **Trivy** findings that were uploaded as SARIF.

Recommended command:

```bash
export GH_TOKEN="$GITHUB_PAT"
gh api "repos/OWNER/REPO/code-scanning/alerts?state=open&per_page=100" --paginate
```

Typical fields worth extracting:

- `number`
- `html_url`
- `tool.name`
- `most_recent_instance.category`
- `rule.id`
- `rule.description`
- `rule.security_severity_level`
- `most_recent_instance.location.path`
- `most_recent_instance.ref`

For this repository, expect Trivy categories such as `trivy-artifacts` and `trivy-image`.

### 2. Dependabot alerts

Use GitHub's Dependabot alerts API for dependency vulnerabilities that include package and patched-version information.

Recommended command:

```bash
export GH_TOKEN="$GITHUB_PAT"
gh api "repos/OWNER/REPO/dependabot/alerts?state=open&per_page=100" --paginate
```

Typical fields worth extracting:

- `number`
- `html_url`
- `dependency.package.name`
- `dependency.manifest_path`
- `security_advisory.ghsa_id`
- advisory identifiers or aliases that include a `CVE` when available
- `security_advisory.cwes`
- `security_advisory.severity`
- `security_vulnerability.first_patched_version.identifier`
- `security_vulnerability.package.ecosystem`

GitHub alert payloads evolve. If an exact key is different in the live API response, use the equivalent returned field and do not invent missing data.

### 3. Out-of-scope by default

If a workflow failed but the finding is visible only in raw logs or artifacts and **not** in GitHub alerts, say that you are outside the API-driven path of this skill. You can still help manually, but do not pretend it came from the GitHub alerts API.

## Step 1 - Resolve repository and permissions

This skill must use the **`GITHUB_PAT`** environment variable for GitHub API access.

Before any `gh api`, `gh pr`, `gh repo view`, or other GitHub network call:

1. Check whether `GITHUB_PAT` is present in the environment.
2. If `GITHUB_PAT` is missing or empty, **stop immediately**.
3. Report back to the user that GitHub access cannot proceed because `GITHUB_PAT` is not set.
4. Do **not** try the GitHub API unauthenticated. Do **not** probe first "just to see if it works."

Preferred pattern:

```bash
export GH_TOKEN="$GITHUB_PAT"
```

Resolve `OWNER/REPO` from git first:

```bash
git remote get-url origin
```

Expect the default answer to be **`axelixlabs/axelix`**. If the current workspace points somewhere else, stop and confirm with the user before proceeding, because this skill is written specifically for the Axelix monorepo and its release-safety constraints.

Then confirm GitHub CLI authentication using `GITHUB_PAT`:

```bash
export GH_TOKEN="$GITHUB_PAT"
gh auth status
```

You need enough access to:

- read security alerts,
- read repository contents,
- push a branch,
- create a PR,
- edit PR labels.

If authentication is missing or insufficient, stop and explain what permission is needed rather than guessing.

Also resolve the default branch:

```bash
export GH_TOKEN="$GITHUB_PAT"
gh repo view OWNER/REPO --json defaultBranchRef
```

## Step 2 - Fetch and summarize the open security backlog

Fetch open alerts from every supported source that applies.

Before choosing what to fix, summarize counts by:

- source,
- severity,
- tool,
- whether a CVE is present.

The summary should look like:

- `critical`: N
- `high`: N
- `medium`: N
- `low`: N

And then a short source breakdown such as:

- `code-scanning / CodeQL`
- `code-scanning / Trivy`
- `dependabot`

Also produce a **surface grouping summary**. For each alert, map it to the most likely Axelix batch boundary:

- `master/front-end`
- `master backend gradle`
- `starters gradle`
- `common/shared gradle`
- `docker/image`
- `other / needs review`

The point of the summary is to show both the severity queue and the likely Dependabot-style PR buckets before you start fixing anything.

## Step 3 - Choose one batch to remediate

Do **not** choose a single vulnerability. Choose **one batch**.

Selection order:

1. Prefer the batch containing the highest-severity fixable alerts.
2. Prefer batches with a clear, local remediation path in one dependency surface.
3. Prefer batches that can be fixed with same-major upgrades and lockfile refreshes.
4. Skip batches that appear to require:
   - a major-version upgrade,
   - a breaking public API change,
   - a broad cross-surface refactor,
   - speculative redesign.

If the most severe batch is not safely fixable under patch-release constraints, explain why and move to the next eligible batch. Do not force a risky fix just to satisfy queue order.

## Step 4 - Build the handoff package for the fixing subagent

Do not send the subagent in blind. Collect and pass the exact context it needs:

- repository: `OWNER/REPO`
- default branch
- chosen batch name, for example `master/front-end` or `starters gradle`
- why the alerts belong in the same batch
- raw alert facts for every alert in the batch:
  - severity,
  - CVE,
  - GHSA if present,
  - package or rule id,
  - affected path / manifest / module / image,
  - current version,
  - first patched version if available,
  - alert URL
- the exact files likely involved
- any current dirty-worktree warning in the parent workspace
- required verification commands
- explicit instruction to preserve public APIs and contracts

If the workspace is dirty, prefer an **isolated worktree subagent** so the remediation branch does not interfere with unrelated local changes.

## Step 5 - Launch one subagent for one batch

Prefer a `best-of-n-runner` subagent when available because it works in an isolated git worktree and is a good fit for one-branch-per-batch security work. If that is unavailable, use a normal writable subagent and be careful around local changes.

The subagent's job is to:

1. create a dedicated branch for the chosen batch,
2. implement the smallest safe grouped fix for that surface,
3. run focused verification,
4. commit the change with the actual AI agent as git author,
5. push the branch,
6. open the PR,
7. assign the `security` label to that PR,
8. identify in the PR body which AI agent created the PR.

Do **not** launch multiple fixing subagents in parallel unless the user explicitly asks for multiple independent batched PRs and the repository state makes that safe.

## Subagent prompt template

Use a prompt in this shape, filling in the real batch details:

```text
You are fixing one batched security update for axelixlabs/axelix in an isolated branch.

Repository: OWNER/REPO
Base branch: DEFAULT_BRANCH

Chosen batch:
- Batch name: master/front-end | master backend gradle | starters gradle | common/shared gradle | docker/image
- Why this batch is grouped together: REASON
- Affected files: FILES

Alerts in this batch:
- ALERT 1: severity, CVE/GHSA, package or rule, current version, patched version, alert URL
- ALERT 2: severity, CVE/GHSA, package or rule, current version, patched version, alert URL
- ...

Constraints:
- Use the `GITHUB_PAT` environment variable for all GitHub access. If it is missing or empty, stop immediately and report that back to the parent agent without attempting any GitHub API call.
- Attribute the PR to the actual AI agent that created it, for example `Cursor`, `Claude`, `Codex`, or `Gemini`. Do not use a generic `AI` label when the runtime identity is known.
- The git commit author must also be that actual AI agent identity. Do not leave the commit authored by a human account or local default identity.
- Do not modify git config to achieve this. Use per-commit author metadata such as `git commit --author="ACTUAL_AI_AGENT_NAME <ACTUAL_AI_AGENT_NAME@local>"`.
- Fix only this dependency surface. Do not opportunistically update unrelated manifests or modules.
- Keep the change safe for a patch release.
- Do not introduce public API or public contract changes.
- Prefer the smallest same-major dependency upgrades or similarly low-risk grouped patch.
- If the only fix appears to require a breaking change, stop and report that instead of opening a PR.
- Do not suppress or ignore the alerts.

Required work:
1. Inspect the relevant files and understand the batch of vulnerabilities.
2. Implement the minimal safe grouped fix for this surface.
3. Run targeted verification that is appropriate for the touched modules.
4. Confirm the public API and public contract remain unchanged.
5. Commit using the actual AI agent as the git author, push, open a PR, assign the `security` label to it, and include explicit AI-agent attribution in the PR body.

Branch naming:
- Prefer `security/master-front-end-batch`
- Or `security/master-gradle-batch`
- Or `security/starters-gradle-batch`

PR title format:
- `[SECURITY][BATCH] Update master/front-end dependencies`
- `[SECURITY][BATCH] Update master backend Gradle dependencies`
- `[SECURITY][BATCH] Update starter Gradle dependencies`

PR labeling:
- Add the `security` label immediately after creating the PR.
- Prefer `gh pr edit --add-label "security"` with `GH_TOKEN="$GITHUB_PAT"` in the environment.

PR authorship:
- The git commit author must name the actual AI agent that created the patch.
- Prefer `git commit --author="Cursor <cursor@local>"` or `git commit --author="Claude <claude@local>"` with the truthful agent name for the current runtime.
- Use a clearly non-human local or noreply-style address if needed, but do not pretend to be a human contributor.
- The PR body must contain an `Authored by` line naming the actual AI agent that opened the PR.

PR body must include:
## Summary
- what dependency surface was updated
- which vulnerabilities or packages were addressed
- why this batch belongs together
- why this is safe for a patch release

## Test plan
- [x] list each verification command actually run
- [x] state explicitly that no public API or contract changes were introduced

## Attribution
- `Authored by: ACTUAL_AI_AGENT_NAME`

Return to the parent agent with:
- branch name
- commit author used
- commit SHA
- PR URL
- confirmation that the `security` label was applied
- confirmation of the AI agent name used in the PR attribution
- verification summary
- any remaining risk or follow-up
```

## Step 6 - Verification expectations for the subagent

The subagent should verify the narrowest thing that gives real confidence:

- For `master/front-end` dependency changes, run the relevant package checks from that app.
- For master backend Gradle changes, run targeted Gradle checks for the affected backend modules.
- For starters Gradle changes, run targeted Gradle checks for the touched starter modules.
- For Docker or image updates, run the narrowest build or smoke check that proves the image still builds and the packaging contract still holds.

Do not add broad, expensive verification if a focused check is sufficient. Do not skip verification if any focused check is available.

Patch-release safety means:

- no public Java API signature changes,
- no REST contract changes,
- no starter configuration contract changes,
- no user-visible behavior changes beyond the security fix,
- no major dependency jumps unless the user explicitly approves them.

## Step 7 - Report back to the user

After the subagent finishes, report:

1. the backlog summary,
2. the batch that was chosen and why,
3. the PR URL,
4. the validation that was run,
5. the next highest-priority remaining batch, if useful.

If no safely fixable batch exists, say so clearly and explain the blocker instead of opening a risky PR.

## Pitfalls to avoid

- Do not attempt any GitHub API call before verifying that `GITHUB_PAT` is present.
- Do not batch unrelated dependency surfaces into one PR.
- Do not leave a security remediation PR unlabeled; it must carry the `security` label.
- Do not leave the PR attribution generic when the actual agent identity is known.
- Do not create the commit with a human author identity or the ambient local git identity.
- Do not pretend every alert needs its own PR; this skill is intentionally batch-oriented.
- Do not silently move to a major upgrade when a patch release guarantee was requested.
- Do not change public contracts just because the scanner output is noisy.
- Do not ignore the repo's current dirty state; isolate the fixing branch when possible.
- Do not claim success without an actual PR URL.
