---
name: external-contributor-issues
description: Pick open GitHub issues in the Axelix monorepo that are suitable for unpaid volunteer contributors working in their spare time. Use this skill when the user asks for “good issues for external contributors,” “interesting tickets for newcomers,” “what can volunteers pick up,” “community-friendly issues,” or wants a shortlist ranked by fit for someone with limited product context who will not commit to deadlines. Also use when comparing issues for “nice to have” vs release-critical work, or when the goal is to avoid chores/refactors/backports for volunteers. Default repo is axelixlabs/axelix (resolve from git origin when in this workspace).
---

# External contributor issue picker (Axelix)

Help maintainers **shortlist open GitHub issues** that are a good fit for **external contributors**: people donating **spare time**, with **limited insider context**, who want work that feels **worth doing**—not unpaid project maintenance.

This skill **does not** replace code review or maintainer judgment. It produces a **reasoned candidate list**; a human should still confirm scheduling, design direction, and “are we OK if this never ships.”

## Audience assumptions (read before scoring)

Treat every candidate issue as if the assignee is:

1. **Unpaid and asynchronous** — They may stop mid-flight or never open a PR. The project must not **depend** on them for a date or a release.
2. **Low tribal knowledge** — They do not know historical decisions, internal naming, or which modules secretly interact. Prefer work that is **locally understandable** from the issue text plus nearby code/docs.
3. **Motivation-sensitive** — They opted in for something **interesting or compelling**. **Chores, mechanical refactors, backports,** and “just align with X” tasks are poor fits unless the write-up reframes them as a **clear, rewarding problem** (rare).

## Rubric — three gates (all should lean “yes”)

When scoring or ranking issues, apply these gates. If an issue **fails** a gate hard, it usually should **not** be recommended for volunteers (or needs rewriting first).

### Gate A — Local context (“can a newcomer find the edges?”)

**Prefer** issues where:

- The **surface area** is bounded: one app area (`front-end/`, `master/`, one `sbs/` module, `docs/`), or a small number of files with obvious entry points.
- The issue states **acceptance criteria** or **repro steps** so someone can tell when they are done without a design meeting.
- Discovery work is **hours, not days** of archaeology across the whole monorepo.

**Be cautious** when:

- The ticket assumes **cross-cutting** changes (security, auth, deployment, shared protocol between master and starters) without a spelled-out plan.
- It references **undocumented** contracts, private services, or “ask the team in Slack” workflows.

**Verdict:** Favor issues you can summarize as: “If you read these paths and the issue, you mostly understand the task.”

### Gate B — Stakes (“nice to have,” not release load-bearing)

**Prefer** issues that are clearly **optional quality**, **polish**, **developer experience**, **small user-visible improvements**, or **isolated enhancements** where slipping or abandoning the work does not block the core team.

**Deprioritize for volunteers** when:

- The issue is tied to a **near-term milestone** or reads like **release gating** (“must ship in vX”, “blocker”, “P0”).
- It is labeled or described as **security**, **data loss**, **production outage**, or **compliance** unless maintainers explicitly want community help *and* have scoped it tightly.
- **Assignees** or **project fields** show it is **actively owned** by the core team for an imminent deliverable.

**Verdict:** Favor “we’d love this” over “we need this by Tuesday.” When unsure, **ask the maintainer** or mark the issue as **conditional** in your report.

### Gate C — Interest (“would a volunteer thank you for the suggestion?”)

**Strong fits** often look like:

- A **small feature** with a visible result (UI improvement, better error message, CLI output, docs that teach a real workflow).
- A **bug** with repro and a plausible **localized** fix.
- **Tooling** that makes *developers or users* happier in an obvious way.

**Poor fits** for unprompted volunteer routing:

- **Refactors** for consistency only, **renames across the tree**, **dependency bumps** without a motivating bug/feature.
- **Backports** or “match behavior in other module” **mechanical** work.
- **Test-only churn** or mass formatting unless the issue explains a **concrete payoff** (flaky test fixed, coverage for a known regression).

**Verdict:** If the issue title sounds like homework, **do not** push it to volunteers without maintainer consent and a reframed description.

## Heuristics from metadata (GitHub labels, milestones, text)

Use labels and titles as **weak signals**—always reconcile with the issue body and quick code search when possible.

| Signal | Typical meaning for volunteers |
|--------|--------------------------------|
| `good first issue`, `help wanted` | Promising; still apply Gates A–C. |
| `tech-debt`, `chore`, `refactor` | Usually **Gate C fail** unless narrowly scoped and motivating. |
| `blocked`, `needs-design`, `question` | Usually **wait** until unblocked/clarified. |
| Milestone with **near** due date | **Gate B risk** — verify with maintainers. |
| Long threads, many stakeholders | Context cost high — **Gate A risk**. |

## Workflow

### 1) Resolve repository

Follow the same convention as other Axelix workspace skills:

```bash
git remote get-url origin
```

Parse `owner/repo` from the URL. If unavailable, ask the user. In this monorepo, expect **`axelixlabs/axelix`**.

### 2) Fetch open issues (issues only, not PRs)

Prefer GitHub CLI when authenticated:

```bash
gh api "repos/OWNER/REPO/issues?state=open&per_page=100" --paginate \
  --jq 'map(select(.pull_request == null))'
```

Respect rate limits; paginate. If the user gives filters (label, “updated since”, search query), apply them.

Capture at minimum: **number**, **title**, **body** (or summary), **labels**, **assignees**, **milestone**, **html_url**, **created_at**, **updated_at**.

### 3) Apply the rubric

For each candidate (or for a user-supplied batch):

1. **Restate** the task in one sentence from a newcomer’s perspective.
2. **Score** Gates A/B/C as **strong / OK / weak / fail** with **one line of evidence** each (from issue text or a **quick** repo search—do not deep-audit unless asked).
3. **Flag** dependencies: other issues, unreleased APIs, maintainer-only secrets.
4. Decide **recommend**, **recommend only if rewritten**, or **do not recommend**.

### 4) Sanity-check against the codebase (lightweight)

When the issue mentions concrete paths or features:

- Use repo search to see if the area still exists and matches the ticket.
- If the issue looks **obsolete** or **already done**, prefer pointing to **`backlog-refiner`** skill for triage instead of recommending it blindly.

### 5) Deliver the report (always use this shape)

#### Summary

- How many open issues were in scope vs how many you **ranked**.
- **Explicit caveat:** Volunteers owe **no** delivery; maintainers should not schedule around them.

#### Shortlist (ordered best-first)

For each recommended issue (cap at the **N** the user asked for, default **5–10**):

| Rank | Issue | Why it fits A/B/C | Risks / questions | Suggested first step for contributor |
|------|-------|---------------------|---------------------|--------------------------------------|

**Link** every issue (`html_url`). Keep “why” to **2–4 sentences** total per row.

#### Not recommended (optional)

Brief bullets for issues that **look** volunteer-suitable by labels but **fail** the rubric—helps avoid arguments by showing the reasoning.

#### Maintainer follow-ups

- Issues that need **rewriting** (add acceptance criteria, narrow scope, remove milestone pressure).
- Issues that are good ideas but need a **spike** by a maintainer first.

## Pitfalls to avoid

- **Do not** treat “good first issue” as sufficient—many such issues are still **boring** or **context-heavy**.
- **Do not** promise timelines or difficulty (“easy afternoon”)—you do not know the contributor’s stack depth.
- **Do not** use stale metadata alone—if the codebase moved on, say so.
- **Do not** leak tokens; never paste credentials into issues or chat.

## Quick reference — REST

- List issues: `GET /repos/{owner}/{repo}/issues?state=open&per_page=100` (filter out PRs)
- Single issue: `GET /repos/{owner}/{repo}/issues/{issue_number}`

Prefer `gh api` when available.
