---
name: security-fix-prs
description: Create isolated pull requests for GitHub security findings in `axelixlabs/axelix`, one vulnerability at a time. Use this skill whenever the user asks to fix CodeQL, Trivy, SARIF, Dependabot, SAST, SCA, CVE, GHSA, or `security-check` findings for the Axelix repository, especially when the goal is to query GitHub security alerts, summarize severities, delegate a single vulnerability to a subagent, and open a patch-release-safe PR that names the CVE and severity.
---

# Security Fix PRs

Turn GitHub security alerts into narrowly scoped pull requests.

The important constraint is **isolation**: do not try to clear the whole security backlog in one go. Work on **one vulnerability at a time**, open **one PR per vulnerability**, and keep each fix small enough that maintainers can confidently ship it in a **patch release**.

## What this skill is for

Use this workflow for **`axelixlabs/axelix`** when the repository has security findings from:

- **CodeQL** code scanning alerts.
- **Trivy** SARIF alerts uploaded into GitHub code scanning.
- **Dependabot alerts** or other GitHub dependency alerts surfaced through the GitHub API.
- A CI job such as `security-check` that is expected to fail on open HIGH/CRITICAL findings, and the user wants AI-authored remediation PRs.

This skill is intentionally **Axelix-specific**. It assumes the target repository is **`axelixlabs/axelix`** and that its security workflow uploads Trivy SARIF into GitHub code scanning, so GitHub API queries can see both **CodeQL** and **Trivy** findings.

## Core operating rules

1. **One vulnerability per PR.**
   If a single minimal fix remediates the same CVE in several files or manifests, that is still one PR. Do not batch unrelated CVEs together.

2. **Prefer patch-safe remediations.**
   Favor:
   - same-major dependency upgrades,
   - minimal container base image bumps,
   - tiny local hardening patches that do not alter public behavior.

3. **Do not break public APIs or contracts.**
   The fix must be safe for a patch release. Avoid changes to public DTOs, REST contracts, starter configuration contracts, public Java APIs, or user-facing workflows unless the user explicitly accepts that risk.

4. **Do not hide the problem.**
   Do not "fix" alerts by suppressing them, loosening the scanner, or adding ignores unless the user explicitly asks for that path and understands the trade-off.

5. **Do not invent vulnerability identifiers.**
   Prefer alerts that have a real **CVE** identifier. If an alert only has a GHSA and no CVE alias, say so explicitly instead of fabricating a CVE.

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

Then use `gh` commands with that token in the environment.

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
- create a PR.

If authentication is missing or insufficient, stop and explain what permission is needed rather than guessing.

Also resolve the default branch:

```bash
export GH_TOKEN="$GITHUB_PAT"
gh repo view OWNER/REPO --json defaultBranchRef
```

## Step 2 - Fetch and summarize the open security backlog

Fetch open alerts from every supported source that applies.

Before choosing anything to fix, summarize counts by:

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

The point of the summary is to show the user the queue shape before you pick the next candidate. If there are `5 critical`, `10 high`, and `15 medium`, say so plainly.

## Step 3 - Select exactly one candidate to remediate

Selection order:

1. Prefer **CRITICAL** over **HIGH**, **HIGH** over **MEDIUM**, and **MEDIUM** over **LOW**.
2. Within the same severity, prefer an alert with:
   - a concrete **CVE** identifier,
   - a clear remediation path,
   - a minimal, patch-safe fix,
   - a localized blast radius.
3. Prefer fixes that can be implemented as:
   - same-major dependency upgrades,
   - isolated Docker base image updates,
   - narrowly scoped code changes.
4. Skip candidates that appear to require:
   - a major-version upgrade,
   - a breaking public API change,
   - broad refactoring across unrelated modules,
   - speculative redesign.

If the most severe finding is not safely fixable under patch-release constraints, explain why and move to the next eligible candidate. Do not force a risky fix just to satisfy the queue order.

## Step 4 - Build the handoff package for the fixing subagent

Do not send the subagent in blind. Collect and pass the exact context it needs:

- repository: `OWNER/REPO`
- default branch
- chosen alert source and alert URL
- raw alert facts:
  - severity,
  - CVE,
  - GHSA if present,
  - package or rule id,
  - affected path / manifest / module / image,
  - current version,
  - first patched version if available
- why this candidate looks patch-safe
- the exact files likely involved
- any current dirty-worktree warning in the parent workspace
- required verification commands
- explicit instruction to preserve public APIs and contracts

If the workspace is dirty, prefer an **isolated worktree subagent** so the remediation branch does not interfere with unrelated local changes.

## Step 5 - Launch one subagent for one vulnerability

Prefer a `best-of-n-runner` subagent when available because it works in an isolated git worktree and is a better fit for one-branch-per-fix security work. If that is unavailable, use a normal writable subagent and be careful around local changes.

The subagent's job is to:

1. create a dedicated branch,
2. implement the smallest safe fix,
3. run focused verification,
4. commit the change,
5. push the branch,
6. open the PR as the AI-authored remediation branch for that single vulnerability,
7. assign the `security` label to that PR.

Do **not** launch multiple fixing subagents in parallel unless the user explicitly asks for multiple independent PRs and the repository state makes that safe.

## Subagent prompt template

Use a prompt in this shape, filling in the real alert details:

```text
You are fixing exactly one GitHub security vulnerability in an isolated branch.

Repository: OWNER/REPO
Base branch: DEFAULT_BRANCH

Chosen vulnerability:
- Source: code-scanning | dependabot
- Severity: HIGH
- CVE: CVE-2026-12345
- GHSA: GHSA-xxxx-yyyy-zzzz
- Alert URL: https://github.com/OWNER/REPO/security/...
- Package or rule: PACKAGE_OR_RULE
- Current version: CURRENT_VERSION
- First patched version: PATCHED_VERSION
- Affected files: FILES
- Why this is the chosen candidate: REASON

Constraints:
- Use the `GITHUB_PAT` environment variable for all GitHub access. If it is missing or empty, stop immediately and report that back to the parent agent without attempting any GitHub API call.
- Fix only this vulnerability or the tightly coupled occurrences of the same vulnerability.
- Keep the change safe for a patch release.
- Do not introduce public API or public contract changes.
- Prefer the smallest same-major dependency upgrade or similarly low-risk patch.
- If the only fix appears to require a breaking change, stop and report that instead of opening a PR.
- Do not suppress or ignore the alert.

Required work:
1. Inspect the relevant files and understand the vulnerability.
2. Implement the minimal safe fix.
3. Run targeted verification that is appropriate for the touched modules.
4. Confirm the public API and public contract remain unchanged.
5. Commit, push, open a PR, and assign the `security` label to it.

Branch naming:
- Prefer `security/cve-2026-12345-high` or a similarly clear branch name.

PR title format:
- `[SECURITY][HIGH] Fix CVE-2026-12345 in PACKAGE_OR_RULE`

PR labeling:
- Add the `security` label immediately after creating the PR.
- Prefer `gh pr edit --add-label "security"` with `GH_TOKEN="$GITHUB_PAT"` in the environment.

PR body must include:
## Summary
- what vulnerability is fixed
- what change was made
- why this is safe for a patch release

## Test plan
- [x] list each verification command actually run
- [x] state explicitly that no public API or contract changes were introduced

Return to the parent agent with:
- branch name
- commit SHA
- PR URL
- confirmation that the `security` label was applied
- verification summary
- any remaining risk or follow-up
```

## Step 6 - Verification expectations for the subagent

The subagent should verify the narrowest thing that gives real confidence:

- For Gradle dependency changes, run targeted Gradle tests for the affected module or modules.
- For front-end dependency changes, run the relevant package checks from that app.
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
2. the single alert that was chosen and why,
3. the PR URL,
4. the validation that was run,
5. the next highest-priority remaining candidate, if useful.

If no safely fixable candidate exists, say so clearly and explain the blocker instead of opening a risky PR.

## Pitfalls to avoid

- Do not attempt any GitHub API call before verifying that `GITHUB_PAT` is present.
- Do not batch unrelated alerts into one PR.
- Do not leave a security remediation PR unlabeled; it must carry the `security` label.
- Do not pick a GHSA-only alert and pretend it has a CVE.
- Do not silently move to a major upgrade when a patch release guarantee was requested.
- Do not change public contracts just because the scanner output is noisy.
- Do not ignore the repo's current dirty state; isolate the fixing branch when possible.
- Do not claim success without an actual PR URL.
