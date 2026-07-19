---
name: config-breaking-changes
description: >-
  Review configuration property changes in the Axelix project for breaking changes
  and migration-policy compliance. Use this skill whenever a diff, PR, or commit
  touches Spring Boot configuration properties (@ConfigurationProperties classes,
  application.yml/.properties, spring-configuration-metadata) OR Helm chart values
  (values.yaml, values.schema.json, templates) — including any rename, removal,
  type/format change, or default-value change of a property or value. Trigger it
  even when the user only says "review this PR", "check for breaking changes",
  "review the config/values changes", or when a diff adds, removes, or renames keys
  in values.yaml or a configuration-properties class. It catches in-place renames,
  removal of non-deprecated properties, type/format changes, and default-value
  changes, and applies the "breaking-change" label when a breaking change is present.
---

# Config Breaking-Change Review

Configuration properties are a **public contract**. Operators pin them in their
`application.yml`, their Helm `values.yaml`, their GitOps repos, and their runbooks.
When a property silently disappears, gets renamed in place, stops accepting the value
they wrote, or quietly changes its default, their deployment breaks — often only at
the worst moment, during an upgrade in production. The whole point of this review is
to make sure every config change either preserves that contract or breaks it *loudly,
on purpose, and only at a major version boundary, with a migration path the operator
can follow.*

This skill applies the **same four rules** to two surfaces:

- **Spring Boot properties** in the `axelix` application — see `references/spring-boot.md`
- **Helm chart values** — see `references/helm.md`

A single PR can touch both. Review each surface it changes.

---

## Workflow

### 1. Gather the diff and the version context

You cannot judge most of these rules without knowing **what version this change ships in**
and **whether a property was already deprecated in a previously released version**. Removal,
in particular, is only legal at a major-version boundary for a property that was *already*
deprecated in an earlier release.

Collect:

- **The diff under review.** Prefer the PR diff; otherwise diff the current branch against
  its base (`git diff <base>...HEAD`).
- **The target version.** For Spring Boot, read the project version (`pom.xml` /
  `build.gradle`). For Helm, read `Chart.yaml` (`version` and `appVersion`). Note whether
  this PR/release crosses a **major** boundary (e.g. 1.x → 2.0).
- **Deprecation history.** For any property being removed, use `git log`/`git blame` and the
  changelog to find out *when* it was deprecated. A deprecation added in this same PR does
  **not** authorize removal in this same PR.

If you genuinely cannot determine the version context, say so explicitly in the report rather
than guessing — an unverifiable removal is a finding, not a pass.

### 2. Inventory the property changes

For each surface the diff touches, identify every property/value that was **added, removed,
renamed, retyped, or had its default changed**. The reference files tell you exactly where
these live and how to read defaults, types, and deprecation markers on each surface. Read the
relevant reference file before classifying.

### 3. Classify each change against the four rules

Walk every changed property through the rules below. Each rule defines what a **compliant**
change looks like and what counts as a **violation**.

Two outcomes are independent and you must report both:

- **Violation** — the change breaks the migration policy. The author must fix it before merge.
- **Breaking change present** — the change is breaking (even if handled correctly, like a
  default change at a major bump). This drives the `breaking-change` label.

A correctly-executed rename is still a breaking change in spirit and should be labelled; an
*incorrectly*-executed rename is both a violation **and** a breaking change.

### 4. Report (see output format below)

### 5. Apply the label if any breaking change is present (see labeling below)

---

## The four rules

### Rule 1 — Renaming a property: never in place; migrate gradually

A name is part of the contract. We **never** rename a property in place — not even when bumping
the major version — because an operator who upgrades would have their old key silently ignored.
Instead a rename is a **gradual migration that spans three major versions**, giving operators a
grace period to move over:

```
1.0.0:  Property A
2.0.0:  Property A_new  AND  Property A (old)   — A_new takes precedence, falls back to A
3.0.0:  Property A_new only
```

**The introducing PR (the 2.0.0 step) must, in the same commit:**

1. **Add the new property.** Its value takes precedence; it falls back to the old property's
   value only when the new one is unset. (Concretely, renaming `instanceName` →
   `instanceNamePrefix`: read `instanceNamePrefix`, and only if it has no value, read
   `instanceName`.)
2. **Deprecate the old property** and point operators at the replacement.

**Violation if:** the old name is gone and the new name appears in its place (rename in place);
the new property is added but the old one is *not* deprecated; precedence is backwards (old value
wins over new); or there is no fallback so existing configs stop working.

The final removal of the old property (the 3.0.0 step) is governed by **Rule 2**.

### Rule 2 — Removing a property: deprecate first, communicate the alternative, remove only at a major boundary

A property existed for a reason, so we can never *just* remove it. Removal is the last step of a
deliberate, announced deprecation:

- **It must already have been deprecated in a previously released version.** Deprecating and
  removing in the same PR is a violation — operators never got the grace period.
- **It may only be removed in a new major release.** Removing it in a minor/patch release is a
  violation.
- **The deprecation must have communicated the alternative**, and the removal notice must repeat
  it. Always answer: *what should operators do instead?* — switch to a replacement property, use a
  new bundle of finer-grained properties, or (if the feature is genuinely obsolete) explain that no
  replacement is needed and why. "We're removing X" with no path forward is never acceptable.

**Violation if:** a non-deprecated property is removed; a property is deprecated and removed in the
same PR; a property is removed outside a major bump; or the removal/deprecation gives no alternative
or explanation.

### Rule 3 — Changing a property's type or format: introduce a new property instead

We do **not** change the accepted type or format of an existing property, because a config that was
valid yesterday must stay valid. If `heartbeatInterval` accepted a number and you now want it to
accept a string, do **not** widen or change its type in place — an operator's existing numeric value
might now parse differently or fail.

Instead, **introduce a new property that accepts the new type** (e.g. `heartbeatIntervalText`) and
then run the **Rule 1** migration: new property takes precedence with fallback to the old one, and
deprecate the old one in the same commit. The old typed property is eventually retired under Rule 2.

**Violation if:** an existing property's type or accepted format changes in place (e.g. a field's
type changes, an enum loses or repurposes a value, a parser is swapped for an incompatible one)
without a new property carrying the new type.

### Rule 4 — Changing a default value: breaking, but allowed at a major bump with no new property

Changing a default **is** a breaking change for us — full stop. An operator relying on the implicit
default (heartbeat interval 30 → 45) gets different runtime behavior after an upgrade without
changing a single line of their config, and that surprise is exactly what we refuse to ship silently.

Unlike the other rules, a default change needs **no new property and no advance signalling**: ship it
in a **new major version** and that is fine.

**Violation if:** a default changes in a minor/patch release. **Compliant** when it lands in a major
release — but it is still a breaking change, so it must be **labelled** and called out in the report
(and ideally noted in the changelog/migration guide so operators see the new behavior).

---

## Applying the `breaking-change` label

If the review finds **any** breaking change present (a violation, or a compliant-but-breaking change
like a major-version default change), the PR must carry the `breaking-change` label so downstream
release tooling and reviewers (CodeRabbit, etc.) treat it accordingly.

Apply it with whatever is available in the environment:

1. **`gh` CLI**, if installed and authenticated:
   ```bash
   gh pr edit <pr-number> --add-label "breaking-change"
   ```
2. **GitHub REST API**, otherwise (token in `$GITHUB_TOKEN` / `$GH_TOKEN`):
   ```bash
   curl -sS -X POST \
     -H "Authorization: Bearer $GITHUB_TOKEN" \
     -H "Accept: application/vnd.github+json" \
     "https://api.github.com/repos/<owner>/<repo>/issues/<pr-number>/labels" \
     -d '{"labels":["breaking-change"]}'
   ```

If neither the CLI nor a token is available (e.g. a purely local review), **do not fail** — state
clearly in the report that the PR requires the `breaking-change` label and that it could not be
applied automatically, so a human or CI can add it.

---

## Output format

Report like this:

```
## Config breaking-change review

**Surfaces reviewed:** <Spring Boot properties | Helm values | both>
**Target version:** <version> (<major bump? yes/no>)
**Breaking change present:** <yes/no>   →   label `breaking-change` <applied / needs to be applied / not needed>

### Violations (must fix before merge)
- `<property>` (<surface>): <which rule> — <what's wrong and the corrective action>

### Compliant breaking changes (allowed, labelled)
- `<property>` (<surface>): <e.g. default changed 30 → 45 in major release 2.0.0>

### Notes / unverifiable
- <anything you couldn't confirm, e.g. deprecation history unknown>
```

If there are no config property changes at all, say so plainly — a one-line "no configuration
properties changed in this diff" is the right answer, and don't apply the label.
