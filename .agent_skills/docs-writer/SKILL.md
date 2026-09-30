---
name: docs-writer
description: Writes and updates user-facing Axelix documentation — Fumadocs `.mdx` pages in `docs/content/docs/`, always as an English page plus its `.ru.mdx` twin — for anything implemented in `master/` (the Axelix backend), `sbs/` (the Spring Boot Starter modules), `plugins/` (the build plugins), or surfaced in `master/front-end/` (the UI). Covers every kind of page, such as UI screens (Beans, Scheduled Tasks, Wallboard), setup and configuration references (Configuring Master, Authentication, starter and build plugin configuration), step-by-step walkthroughs (Getting Started), and troubleshooting entries. Use this skill whenever the user asks to document, write docs for, describe, or explain something for the Axelix docs site, and equally for keeping existing pages current - updating, refreshing, syncing, or fixing a page after a code change, a renamed property, a new UI control, or a removed feature. Trigger phrases include "document the X feature", "write docs for X", "document the configuration of X", "add a troubleshooting entry for X", "update the docs for X", "the X page is outdated", "sync docs with PR #N", "actualize the X docs". Prefer this skill over generic Markdown writing whenever the output goes into `docs/content/docs/`.
---

# docs-writer

This skill helps you author or update one Fumadocs page at a time for the Axelix documentation site (`docs/content/docs/`), describing real functionality that lives in `master/`, `sbs/`, `plugins/`, or — for UI-surfaced features — `master/front-end/`. Every page exists in two languages: `<page>.mdx` (English) and `<page>.ru.mdx` (Russian). The audience is everyone who uses Axelix in daily work: developers, testers, team leads, operators, and managers. It is not the people hacking on Axelix internals. A team lead skimming "what can my team do here" should land cleanly; an operator hunting a config key should land cleanly; both should feel the page was written for them.

The single most important rule: **everything you write must be backed by source code you actually read.** No invented endpoints, no fabricated configuration keys, no marketing claims. If you're unsure, say so plainly or omit it. Documentation that lies costs users hours of debugging trust.

**Before writing or editing MDX, read `references/fumadocs.md`.** It holds the site mechanics (frontmatter, sidebar, components, images, tabs, admonitions, links), the page template, and the mechanical checklist.

## When this skill applies

The skill has two modes:

- **Create** — a new page for something that has no page yet.
- **Update** — bring an existing page in line with the code: a property was renamed, a control was added, a default changed, a feature shipped or went away, or the user just says "this page is outdated".

If the user asks for "all of master" or "all of sbs" in one go, push back: ask them to pick the smallest meaningful unit — one page, or one section of a large reference page. Wide-scope documentation drifts into hand-waving and is the easiest place to drift from the source.

## Workflow

Follow these steps in order. Don't skip the source-reading step — it's the whole point of the skill.

### 1. Pin down the target, the mode, and the kind of page

Confirm exactly **one** target: a UI screen, a master subsystem, a starter or build-plugin behavior, a setup task, or one problem a user hits. If the user names something vague ("the auth thing"), narrow it: the master-side sign-in options, the starter-side JWT signing key, or the roles table on the authentication page? Documenting the wrong thing wastes everyone's time.

**Mode.** Grep `docs/content/docs/` for the topic first — if a fitting page exists, you are in **Update** mode even if the user said "write docs". Don't create a duplicate. In Update mode, read the existing `.mdx` and `.ru.mdx` in full before touching the source. If the user pointed at a PR or commit, read its diff (`gh pr diff <number>`, `git show <sha>`); if they pointed at a class, read its source file, plus `git log -p -- <path-to-file>` for recent changes. This tells you which claims on the page are now at risk.

**Kind of page.** Decide what the page describes, because that decides what you read in §2 and which template blocks you use. A page can mix kinds.

- **UI screen** — what the user sees and does on one screen. Spine: the main view, then one section per screen area with its fields, then the actions the user can take. Source: backend, front-end, and i18n.
- **Configuration / setup** — what the user configures and how. Spine: prerequisites, then one section per task with a property table and a snippet the reader can copy. Source: `@ConfigurationProperties` classes, `application*.yaml`, auto-configurations, build plugins.
- **Walkthrough** — the path from nothing to a working result. Spine: numbered steps, each with the minimal command or config and a link to the reference for everything else.
- **Troubleshooting** — a problem the user hits. Spine: symptom, most likely cause, solution. Source: the code that fails or logs the error.
- **Concept** — what Axelix is and why. Only when the user explicitly asks.

For the exact structure, open 2–3 neighbouring pages in the folder where the page will live and follow them: their section order, how they name the closing links section, how their tables look. The neighbours are the house style; this list only tells you what to look for.

### 2. Read the source — really read it

Before writing a single sentence, gather the truth from the right places. Treat these as a layered source: each layer covers what the others can't. Read backend first (so you know what exists), then front-end (to see how it's surfaced), then screenshots (as a final sanity check).

**Backend (always relevant):**

- Locate the code under `master/src/main/java/com/axelixlabs/axelix/master/...`, `sbs/axelix-spring-boot-{2,3,4}-starter/src/main/java/...`, `plugins/axelix-{gradle,maven}-plugin/`, and the shared `common/` / `sbs/starter-domain/` modules where applicable.
- Read the entry points by module:
    - **In `master/`:** REST controllers (`api/external`, `api/internal`), services (`service/...`), autoconfiguration (auth, discovery, web), filters, MCP tools.
    - **In `sbs/`:** auto-configuration classes (`autoconfiguration/...`), Actuator endpoints registered by the starter, `@ConfigurationProperties` exposed to the consumer application.
    - **In `plugins/`:** the plugin extension/parameters and the tasks or goals they register.
- Note real values: HTTP paths, property keys, default values, supported environments, conditional bean activation (`@ConditionalOn...`).
- Check `master/src/main/resources/application*.yaml` and each starter's `META-INF/spring/...AutoConfiguration.imports` for defaults and for what is actually wired up.

**When the page documents configuration,** the properties class is the primary source. Walk every field of the `@ConfigurationProperties` class you document: its key, type, default (field initializer or `application.yaml`), and validation. A reference table that silently skips keys or guesses defaults is worse than no table. Environment-variable forms follow Spring's relaxed binding (`axelix.master.auth.jwt.signing-key` → `AXELIX_MASTER_AUTH_JWT_SIGNINGKEY`).

**Front-end (when the page describes a UI screen):**

**Skip this whole layer when nothing on the page is something the user clicks or sees.** Reading front-end you don't need is a waste of context.

For UI screens, the front-end is non-negotiable. The backend tells you what data is *available*; the front-end tells you what is *shown*, *labelled*, and *interactive*. Documenting only from backend code produces docs that name fields the user never sees and miss buttons the user clicks every day.

- Locate the screen and components under `master/front-end/src/pages/...` and `master/front-end/src/components/...`.
- Cross-check the API call in `master/front-end/src/api/...` or `master/front-end/src/services/...` to confirm which master endpoint feeds the screen.
- **Copy UI labels verbatim as the user sees them.** Labels go through `master/front-end/src/i18n/`: the JSX gives you the key (`t('beans.scope.title')`), the displayed string lives in `master/front-end/src/i18n/locales/en.json` — and `ru.json` for the Russian page. Resolve the key before quoting it — never paraphrase, and never quote a translation key as if it were a label.
- Note conditional rendering, empty/error/loading states, feature flags, role-gated controls.
- Note navigation paths: route definitions in `master/front-end/src/routes/` tell you the URL structure to reference.

When the front-end and the backend disagree (e.g. backend exposes a field the UI hides), document what the **user sees**, and only mention the backend field if it's directly user-relevant.

**Existing screenshots — a visual sanity check.** If `docs/public/img/` already contains screenshots for the page, open them with the Read tool and compare them with what you gathered from the code. They catch sections you missed in the JSX, visual groupings that should shape the prose, and states that are hard to enumerate from code. If the screenshot and the source disagree, **trust the source**, and flag the stale screenshot at handoff.

**Write down what you confirmed before drafting.** Note the file path next to each fact (e.g. `<UI label> — <repo-relative path>:<line>`). If a claim isn't traceable to a file you read or a screenshot you viewed, don't make it.

**Read targeted, not exhaustive.**

- Start with Grep, not Read. Grep for the feature name, the property prefix, the endpoint path, the i18n key, or the class name to land on the files that actually matter.
- Read those files in full. Skim related neighbours only if the first pass leaves a real gap.
- If you find yourself reading an 8th file, stop and ask: am I still answering a concrete question about this page, or am I exploring? If exploring, pause and start drafting — gaps will be obvious then.

A typical UI screen page needs ~2–4 backend files, ~2–4 front-end files, and 0–2 screenshots. A configuration reference needs every properties class in its scope, but little else. In Update mode read only what verifies the claims the page makes or should make.

### 3. Update mode — correct the page, don't rewrite it

Skip this step when creating a page. When updating:

1. **Audit.** Walk the page top to bottom and list every concrete claim: labels, property keys, defaults, roles/authorities, versions, commands, icons, links. Mark each one *confirmed*, *outdated*, or *missing* (the code has something user-relevant the page doesn't mention) against what you read in §2.
2. **Edit surgically.** Change only the outdated and missing parts. Keep the page's structure, section order, and existing style (§5) — even if you would have written it differently. Don't polish neighbouring sections. One exception: remove em dashes from the prose of the whole English page, not only from the parts you changed (§9).
3. **Protect anchors.** Other pages link to headings (`page.mdx#some-heading`). Before renaming or removing a heading, Grep `docs/content/docs/` for the anchor and fix every incoming link, in both languages.
4. **Mark releases.** A section that appears in a specific release gets a released-in notice; one that is merged but not released yet gets an upcoming notice; one that no longer applies from some release gets a legacy notice instead of silent deletion, when readers on older versions still need it. Take versions from the code/release history, never guess. Syntax is in the reference.
5. **Mirror to Russian.** Apply the same corrections to `.ru.mdx` at the same place in the page (§8).

In the handoff, list each changed fact as *old → new — source file*.

### 4. Pick the right place in `docs/content/docs/`

The site structure changes over time, so read it instead of assuming it: `docs/content/docs/meta.json` lists the top-level sections, and each folder's `meta.json` lists its pages in sidebar order. Put the page where its neighbours cover the same kind of thing.

- Every page is a pair: `<page>.mdx` and `<page>.ru.mdx` in the same folder, and both `meta.json` and `meta.ru.json` list it (see the reference).
- Don't create a new folder when an existing one fits. A bigger topic gets its own folder when it needs co-located files (styles, small components) next to it.
- If the same feature ships in several starters (Spring Boot 2 / 3 / 4), write **one** page; surface version-specific differences with tabs — don't fork the page.
- If the site already has a page that collects problems and fixes, a new problem is a new numbered section there, not a new page.
- Pages about the product itself and about compatibility and versioning are edited only when the user explicitly asks.

### 5. Match the existing page style

The neighbouring pages you opened in §1 are the style guide. Beyond them:

- **One lead paragraph** right after the frontmatter (and the main screenshot, if any): what the page is and what the reader gets from it. No throat-clearing.
- **Section headings** (`##`, `###`) per coherent subtopic. Anchors are generated from heading text, so keep headings short and stable. Write new headings in sentence case: capitalize only the first word and proper names ("Share the JWT signing key with Master", not "Share The JWT Signing Key"). Don't rename existing headings just to change their case or punctuation: other pages link to their anchors.
- **Fields vs properties.** UI fields go in a bold-label list (`- **Name**: description.`). Configuration properties go in a `Property | Default | Description` table; mark a missing default the same way the neighbouring tables do.
- **Pair description with example — for things the reader will copy.** When you list configuration keys or code the user pastes into their project, place the snippet **immediately after the list or table, in the same section, before any other heading**. **Do not, however, follow a list of UI-field descriptions with a JSON dump of the underlying API response.** UI pages describe what the user sees, not the contract underneath.
- **Closing section.** A short list of links to related pages, named the way the neighbouring pages name it.
- **Hold a single style across the whole page, and a single shape across parallel rows.** Consistency is non-negotiable; a reader who learns the shape of one section should read the next without recalibrating. Two granularities:
    - **Page-wide.** Once you pick sentence length, level of detail, where examples appear, how you phrase cross-links (`see [...]`), keep it for every section. If section 2 ends each bullet with `(e.g. ...)`, section 5 does too.
    - **Parallel sections.** When sibling sections describe the same kind of thing (Cron / Fixed delay / Fixed rate; Super-admin / Local users / OAuth2; Docker / Helm / JAR), every row naming the same field must use the same wording, level of example, anchor links, and admonitions. Asymmetry only earns its place when the underlying thing is genuinely different — then surface the difference explicitly.

### 6. Use Fumadocs features when they help

Reach for these when they make a page genuinely easier to read — not for decoration. Syntax and allowed values are in the reference.

- **Admonitions** — pull one important caveat out of the flow. One per logical section is plenty; a page wallpapered in admonitions trains readers to ignore them. Deprecations get a warning naming the replacement and removal version.
- **Tabs** — for parallel forms the reader picks one of (Spring Boot versions, build tools, deployment shapes). Don't hide content the reader needs to see together. **Spring config always gets both `application.properties` and `application.yaml`.**
- **Tables** — for property references and any parallel data where columns scan better than bullets.
- **Collapsible sections** — wrap long reference dumps in `<details><summary>…</summary>…</details>`.
- **Code block titles** — whenever the reader has to know which file the snippet goes into.
- **Components** — don't introduce new ones from this skill; that's a project-wide decision.

If you're unsure, default to plain Markdown. The bar is "the page is meaningfully clearer with it".

### 7. Visual polish — in service of clarity, never against it

Pages should look cared-for: consistent spacing, sensible heading hierarchy, screenshots placed where they illustrate the surrounding text, code blocks tagged. But polish is the seasoning, not the dish. The hierarchy is non-negotiable:

1. **Accuracy** — the reader can act on what you wrote without being misled.
2. **Clarity** — the reader understands on first read.
3. **Findability** — the reader can scan and locate what they need.
4. **Visual quality** — the page is pleasant to read.

When two conflict, the higher one wins. A short, plain section that states the truth beats a beautiful one that hand-waves. Don't pad sections to balance the page. Don't add emoji or callouts as decoration — they earn their place only when they speed up scanning (a ✅/❌ in a compatibility table). Read the page aloud as if briefing a new teammate: if a sentence exists only to make the page "feel substantial", cut it.

### 8. Russian twin — always in the same pass

Every create or update touches both `<page>.mdx` and `<page>.ru.mdx` (plus both `meta` files when the sidebar changes). Write English first, then produce the Russian page from it:

- **Same skeleton.** Same sections in the same order, same images, same tabs, same tables, same notices, same links. A reader switching languages mid-page should land in the same place.
- **Translated:** `title`, `description`, headings, prose, table descriptions, image alt text and captions, admonition titles. **Not translated:** code, commands, property keys, class and annotation names (`@Scheduled`, `TaskScheduler`), role and authority names (`EDITOR`, `SCHEDULED_TASKS_MODIFY`), file names, tab labels that name files or tools.
- **UI labels** come from `master/front-end/src/i18n/locales/ru.json`, exactly as the Russian UI shows them. Where the UI has no Russian string, use the wording already established in sibling `.ru.mdx` pages.
- **Links and anchors** follow the rules in the reference: links go to the base `.mdx`, anchors are slugs of the Russian headings.
- Natural Russian, not a word-for-word calque. The voice rules in §9 apply to both languages.

### 9. Voice and tone

The existing docs are professional but not stiff. Aim for the same register:

- Active voice, present tense. "Master discovers services" — not "services are discovered" or "services may be discovered".
- Direct second person where it helps ("To enable values, set …"). Avoid "we".
- Concrete over abstract. Use real, recognizable values, not `<placeholder>` syntax: `org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor` instead of `<fully qualified class name>`, `axelix.master.auth.jwt.signing-key` instead of `<property key>`. A concrete example answers "what does it look like" for free and signals you verified it exists.
- Short sentences. If a sentence chains three independent clauses with commas, split it.
- No marketing adjectives ("powerful", "seamless", "world-class"). State what the feature does and why a user would reach for it.
- Acknowledge limits honestly, **inside the section where the reader is thinking about it** — not in a "Limitations" block at the end. A Spring Boot version cap belongs next to the property it gates; a security implication belongs next to the step that creates it. A limit at the end of the page is a limit the reader has already missed.

**Write for a broad audience — without sacrificing precision.** Simplify *ordinary* words and sentence shapes: "use" beats "utilize", "before" beats "prior to". **Technical terms are non-negotiable.** `@Scheduled`, `TaskScheduler`, `@ConfigurationProperties`, `Bean`, `actuator endpoint`, `cron expression`, `OIDC issuer` are the precise labels for what you document; paraphrasing them ("the scheduling annotation") makes the page misleading. When a reader might not know a term, define it in one clause the first time, then keep using it. The audience scales up through simpler *prose*, not lighter *vocabulary*.

**Keep code vocabulary out of the prose.** Class names and internal terms such as "prober", "enricher", or "SBS" mean nothing to the reader. Write what the user sees or does instead. Identifiers the user actually types or reads (`@Scheduled`, a property key, an endpoint id) are fine in code formatting.

**Explain, don't just state.** Short sentences are the default, but a page of short true facts with nothing between them still leaves the reader to work out what they mean.

- **Meaning before mechanism.** The first sentence about a property, control, or command says what it is for from the reader's side; how it works comes second.
- **Conditions as scenarios.** "When you disable a task while it runs, it finishes the current run and then ignores the schedule", not "the schedule is ignored after completion if the status is off".
- **Connect related facts.** Cause and effect, condition and result, or contrast get joined with "because", "so", "when", "if", or "but". Leave sentences apart only when they are separate ideas.
- **Introduce a list by what it contains,** not by how many items it has ("Four things change it:").
- **No fragment openers.** Fold a two-to-five word announcing sentence ("One name is special.") into the sentence it announces.
- **Reasoning goes in paragraphs.** Lists are for items of the same kind the reader will scan.

**Be detailed but alive.** The right length is whatever a curious reader needs to act once. Concrete numbers, real paths, real labels make the page feel grounded rather than generic.

**Don't sound AI-generated.** Watch for the machine register and cut it:

- **No em dashes in English pages.** Use a comma, colon, period, or parentheses instead. This applies to every new page and to the whole prose of any existing page you edit (§3); headings keep their text, because anchors depend on it. Russian pages keep the dash where Russian grammar needs it ("Master — это…").
- **Repeated three-part constructions.** "X, Y, and Z" and "no A, no B, no C" pile up. Sometimes one example is enough.
- **Padding adverbs.** "Essentially", "genuinely", "specifically", "actually", "particularly" — drop them if the meaning survives.
- **Throat-clearing transitions.** "It's worth noting that", "In practice", "Importantly" — just say the thing.
- **Rhetorical "X, not Y"** in every other line.
- **Smooth uniform tone.** Vary sentence length. Allow a terse line, then a warmer one.

**Semicolons only when they earn their place.** If `.` or `, and` works without changing meaning, use it. Most pages need zero semicolons.

**Readability metrics.** Check these when drafting and again before handoff:

- **Subheading distribution.** No more than **300 words** under a single heading without a new subheading.
- **Transition words.** At least **30%** of sentences contain one ("however", "therefore", "for example", "next", "because") — spread naturally.
- **Paragraph length.** At most **150 words**; split at a logical break.
- **Sentence length.** **80%** of sentences **must** be under **20 words**. A long one is the exception.

### 10. Verify before handing off

Before reporting the page as done:

- Re-read the draft against the source. Every concrete claim (labels, property keys, defaults, versions, roles, commands, conditional behavior) must be traceable to a file you read. On configuration pages, check every row of every property table against the properties class.
- Run the **mechanical checklist** from `references/fumadocs.md`: frontmatter, links, images, admonitions, sidebar, EN/RU parity, build.
- **Anchors:** if you renamed a heading, every incoming link in both languages is fixed.
- **Style-consistency pass** (§5): parallel sections use the same wording shape, examples, anchors, and admonitions.
- **Em dashes:** `grep -n "—"` on the English page finds none outside headings and code.
- **Cold read.** Read the page top to bottom as a Spring Boot developer who has never seen the Axelix source. Mark every sentence you couldn't restate in your own words, every term used before it's explained, and every place where you'd ask "so what do I type?" and the page doesn't say. Fix the wording; never fix it by inventing a fact.
- Sanity scan: any phrase that sounds like marketing — cut it.
- If the change affects supported versions or environments, mention the compatibility page to the user; edit it only if they asked.

Tell the user: which files you wrote or edited (EN, RU, meta), what source files you grounded each section in, the changed facts (Update mode), and any honest gaps — screenshots that are missing or stale, behaviors you couldn't confirm, version constraints worth a maintainer's double-check.

## Anti-patterns to avoid

- **Plausible-sounding fabrication.** "Master uses an exponential backoff with jitter for retries" — only true if the code says so.
- **Restating the obvious.** "Click the button to perform the action it labels" adds nothing.
- **Padding with structure.** Don't stack "Overview / Details / Configuration / Examples / FAQ" onto a feature with one screen and one property. Match the page size to the topic.
- **Update turning into a rewrite.** In Update mode, the diff should be as small as the change in the code. Reshuffling sections or restyling untouched prose makes the change impossible to review.
- **Walkthrough duplicating the reference.** A step shows the minimal config and links to the reference page; copying the full property table into it means two tables to keep in sync.
- **Refactoring neighbouring pages.** Stay inside the pages you're documenting. If you spot a real issue elsewhere, mention it to the user — don't silently rewrite it.
- **Generic boilerplate intros.** "In this guide, we will explore…" — say what the page is in one direct sentence.
- **Out-of-date claims about versions or environments.** Cross-check against the compatibility page and the actual `@ConditionalOnXxx` / starter manifests.
- **Documenting the master REST API on a UI page.** A UI page describes what the user sees and clicks. HTTP paths master proxies to the service and raw JSON of its responses are an implementation detail; keep them in your research notes, not in the doc.
- **Auto-configuration conditions framed as user requirements.** When Axelix's autoconfiguration is gated by `@ConditionalOnBean(SomeFrameworkType.class)`, Spring Boot's own auto-configuration usually satisfies it already. Writing "the application **must** provide a `TaskScheduler` bean" invents a step the user doesn't have. **Document only what the user actually configures.** Ask: would a user with a vanilla Spring Boot project hit this requirement? If not, one short sentence ("Spring Boot's auto-configuration provides X automatically") or nothing.
