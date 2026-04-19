---
name: backlog-refiner
description: Refine and triage GitHub backlog by finding open issues that are stale, obsolete, or resolved by another path. Use this skill whenever the user asks about stale issues, backlog hygiene, issue triage, closing old tickets, reviewing open GitHub issues, whether an issue still applies, or reconciling issue text with the actual codebase—including when the ticket’s proposed fix was abandoned in favor of a different solution. Use it for axelixlabs repositories or whenever the goal is to cross-check GitHub issues against the current repo state (not just by last-updated dates). Triggers often include “refine backlog,” “which issues can we close,” “is this issue still valid,” or “audit open issues.”
---

# Backlog Re-finer

Guide the user through a **codebase-informed** review of **open GitHub issues** for the repository that matches this workspace’s `origin` remote. The goal is a **short list of likely-stale issues** with **evidence**, not a blind “old = stale” report.

## Core idea

An issue is **not** stale just because it is old or quiet. It is **stale** when investigation shows at least one of:

1. **Already addressed** — The behavior or work described is implemented, fixed, or otherwise satisfied in the current codebase (possibly in a different area than the ticket assumed).
2. **Superseded approach** — The ticket recommends or assumes a particular solution, but the project **chose another path** that solves the underlying problem; keeping the ticket open misrepresents current intent.
3. **No longer applicable** — Product/architecture/constraints changed; the ticket’s premise no longer holds.
4. **Duplicate / replaced** — Another issue, PR, or doc supersedes it (link the evidence).

If you cannot find evidence after **reasonable** code and history search, say so and treat the issue as **undetermined** rather than stale.

## When to offer this workflow

Offer or follow this skill when:

- The user wants a **backlog cleanup**, **stale issue** list, or **what can we close** review.
- The user points at **open issues** in the GitHub repo that matches **this git remote**.
- The user suspects **tickets describe the wrong solution** compared to what the code does now.

## Prerequisites and safety

- **Authentication:** Prefer the GitHub CLI (`gh`) if available and already logged in (`gh auth status`). Otherwise use a `GITHUB_TOKEN` (or `GH_TOKEN`) with at least `repo` scope for private repos, or public read access for public repos. **Never** paste tokens into chat, logs, or committed files.
- **Rate limits:** Use pagination and caching; avoid hammering the API. If listing hundreds of issues, **summarize in batches** or ask the user for filters (label, assignee, milestone, “updated before date”).
- **Scope:** Default to **open issues** only unless the user asks otherwise.

## Step 1 — Resolve `owner` and `repo` from `git`

Run in the workspace root (or ask the user for the path):

```bash
git remote get-url origin
```

Parse the remote URL into `owner` and `repo`:

- `https://github.com/OWNER/REPO.git` → `OWNER`, `REPO` (strip `.git`).
- `git@github.com:OWNER/REPO.git` → same.

If there is no `origin`, or parsing fails, ask the user for `OWNER/REPO` explicitly. For this project, **expect** `axelixlabs/axelix` when the remote is the default GitHub URL.

## Step 2 — Fetch open issues via GitHub API

**Include issues only:** GitHub’s “issues” listing **includes pull requests**. Filter out items that have a `pull_request` field in the API response, or use GraphQL/REST fields that distinguish issues.

**Recommended (CLI):**

```bash
gh api "repos/OWNER/REPO/issues?state=open&per_page=100" --paginate \
  --jq 'map(select(.pull_request == null))'
```

Use query parameters on `GET` requests (`state`, `per_page`, `labels`, etc.). Use `-f` only when the endpoint expects a body (for example `POST`).

Adjust `OWNER`/`REPO` or use `/repos/{owner}/{repo}/issues` with pagination in the tool the environment provides.

Capture for each issue at minimum: **number**, **title**, **body**, **html_url**, **labels**, **assignees**, **created_at**, **updated_at**, **author**, **state** (should be `open`).

## Step 3 — Decide investigation order

Unless the user specifies otherwise:

1. Prefer issues **least recently updated** first (often higher stale risk *given* you will still verify in code).
2. Or prioritize issues the user names, or those with labels like `tech-debt`, `bug`, `feature`.

## Step 4 — Investigate each issue (the mandatory part)

For each issue under review:

1. **Restate the intent** in one sentence (problem vs proposed implementation).
2. **Search the codebase** — Use repo search tools for distinctive terms from the title/body; open relevant files. Trace modules mentioned (e.g. `master/`, `sbs/`, `front-end/`) from CLAUDE.md/AGENTS.md if helpful.
3. **Check for superseded solutions** — If the ticket says “we should do X,” look for evidence that the problem was solved by **Y** instead (commits, ADRs, newer components, feature flags, renamed packages). Cite file paths or PR/issue links if found via `gh` or git history.
4. **Check completion** — Tests, feature toggles, removed code paths, or comments referencing the GitHub issue number can support “done.”
5. **Conclusion** — One of: **likely stale (close or rewrite)** · **not stale** · **undetermined (needs human)**.

If exploration is inconclusive, say **undetermined** and list what would confirm either way (e.g. product decision, external dependency).

## Step 5 — Report (always use this shape)

Produce a report so maintainers can act without re-deriving your reasoning.

### Summary

- Total open issues considered (and scope: entire backlog vs subset).
- Counts: **likely stale**, **not stale**, **undetermined**.

### Table — Likely stale

For each issue, include:

| Issue | Title | Stale reason category | Evidence (paths, brief notes) | Suggested next step |

Suggested next steps examples: “Close as completed,” “Close as not planned,” “Rewrite issue to match approach Y,” “Split into new scoped ticket.”

### Table — Not stale (optional, brief)

Only if useful for the user—short list or counts.

### Notes

- API/tool limits, skipped issues, or filters applied.

## Pitfalls to avoid

- **Do not** mark stale solely from `updated_at` or age.
- **Do not** assume the ticket’s proposed implementation is still the plan—verify against code and recent changes.
- **Do not** leak or commit secrets; avoid dumping full issue bodies if the user only wants a summary.

## Quick reference — REST endpoints

- List issues: `GET /repos/{owner}/{repo}/issues?state=open&per_page=100`
- Single issue: `GET /repos/{owner}/{repo}/issues/{issue_number}`

(Prefer `gh api` when available; same endpoints under the hood.)
