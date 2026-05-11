---
name: docs-writer
description: Generates user-facing English documentation in `docs/docs/` for a single feature, module, or component implemented in `master/` (the Axelix backend) or `sbs/` (the Spring Boot Starter modules). For UI-surfaced features the skill also reads `front-end/` as the source of truth for what the user actually sees. Use this skill whenever the user asks to document, write docs for, describe, or explain — for the Axelix docs site — any backend feature (an endpoint, an autoconfiguration, a service, a discovery prober, an authentication mechanism), any starter behavior (Axelix Spring Boot 2 / 3 starter, an auto-configuration, a metrics/transaction/scheduled-task integration), or any UI page surfaced in the Axelix UI. Trigger phrases include "document the X feature", "write docs for X", "add a docs page for X", "describe how X works in docs/", "we need a Docusaurus page about X". Prefer this skill over generic Markdown writing whenever the target lives in `master/`, `sbs/`, or `front-end/` and the output goes into `docs/`.
---

# docs-writer

This skill helps you author one Docusaurus page at a time for the Axelix documentation site (`docs/docs/`), describing real functionality that lives in `master/`, `sbs/`, or — for UI-surfaced features — `front-end/`. The audience is everyone who uses Axelix in daily work: developers, testers, team leads, and managers. It is not the people hacking on Axelix internals. A team lead skimming "what can my team do here" should land cleanly; a developer hunting a config key should land cleanly; both should feel the page was written for them.

The single most important rule: **everything you write must be backed by source code you actually read.** No invented endpoints, no fabricated configuration keys, no marketing claims. If you're unsure, say so plainly or omit it. Documentation that lies costs users hours of debugging trust.

## When this skill applies

Use it when the user asks to add or update a page in `docs/docs/` describing something built in `master/`, `sbs/`, or `front-end/`. Typical scopes:

- A single feature page (e.g. `features/conditions.md`) — describes one capability surfaced in the Axelix UI, backed by an endpoint in master plus a starter contribution.
- A single setup page under `setting-up-master-ui/` or `setting-up-spring-boot-service/` — describes how a user wires up one configuration aspect.
- A single starter behavior — auto-configuration, JWT auth, metrics, transaction monitoring, scheduled tasks, etc.

If the user asks for "all of master" or "all of sbs" in one go, push back: ask them to pick the smallest meaningful unit. Wide-scope documentation drifts into hand-waving and is the easiest place to drift from the source.

## Workflow

Follow these steps in order. Don't skip the source-reading step — it's the whole point of the skill.

### 1. Pin down the target

Ask the user (or confirm from context) exactly **one** of:

- A feature surfaced in the UI (Beans, Properties, Caches, Loggers, …).
- A specific master subsystem (e.g. discovery, JWT auth, MCP, exception handling).
- A specific starter module or auto-configuration class.

If the user names something vague ("the auth thing"), narrow it: ask whether they mean the master-side authentication service, the starter-side `JwtAuthAutoConfiguration`, or the docs page in `setting-up-master-ui/authentication.md` they want to extend. Documenting the wrong thing wastes everyone's time.

### 2. Read the source — really read it

Before writing a single sentence, gather the truth from the right places. Treat these as a layered source: each layer covers what the others can't. Read them in order — backend first (so you know what data exists), then front-end (to see how it's surfaced), then screenshots (as a final sanity check on your reading).

**Backend (always relevant):**

- Locate the relevant code under `master/src/main/java/com/axelixlabs/axelix/master/...` or `sbs/axelix-spring-boot-{2,3}/src/main/java/...` (and the shared `common/` / `sbs/starter-domain/` modules where applicable).
- Read the entry points by module:
    - **In `master/`:** REST controllers (`api/external`, `api/internal`), services (`service/...`), autoconfiguration (auth, discovery, web), filters.
    - **In `sbs/`:** auto-configuration classes (`autoconfiguration/...`), Actuator endpoints registered by the starter, `@ConfigurationProperties` exposed to the consumer application.
    - **In both, where applicable:** shared types under `common/` and `sbs/starter-domain/`.
- Note real values: HTTP paths, property keys, default values, supported environments, conditional bean activation (`@ConditionalOn...`).
- Check `application*.yaml`, `META-INF/spring/...AutoConfiguration.imports`, and `axelix-spring-boot-3/src/main/resources/META-INF` for what is actually wired up.

**Front-end (whenever the feature is surfaced in the Axelix UI — every page in `docs/docs/features/`):**

**Skip this whole layer if the target is backend-only** (an autoconfiguration with no UI surface, an internal master service, a discovery prober). Reading front-end you don't need is a waste of context.

For UI-surfaced features, the front-end is non-negotiable. The backend tells you what data is *available*; the front-end tells you what is *shown*, *labelled*, and *interactive*. Documenting only from backend code produces docs that name fields the user never sees and miss buttons the user clicks every day.

- Locate the corresponding screen and components under `front-end/src/pages/...` and `front-end/src/components/...`.
- Cross-check the API call — `front-end/src/api/...` or `front-end/src/services/...` — to confirm which master endpoint feeds this page; that is the bridge between the UI you're describing and the backend behavior you read above.
- **Copy UI labels verbatim as the user sees them.** If the codebase routes labels through `front-end/src/i18n/` (calls like `t('beans.scope.title')`), the JSX gives you the key, but the actual displayed string lives in the i18n locale file (`front-end/src/i18n/locales/en.json` or equivalent). Resolve the key to the rendered English string before quoting it in docs — never paraphrase, and never quote a translation key as if it were a label.
- Note conditional rendering, empty/error/loading states, feature flags, role-gated controls.
- Note navigation paths: route definitions in `front-end/src/routes/` tell you the URL structure to reference.

When the front-end and the backend disagree (e.g. backend exposes a field the UI hides), document what the **user sees**, and only mention the backend field if it's directly user-relevant. The reader is the UI's user, not the API's consumer.

**Existing screenshots — use them as a visual sanity check on your reading:**

If `docs/static/img/feature/<area>/...` already contains screenshots for the page you're writing, open them with the Read tool (it renders images for you) and compare them with what you've gathered from the JSX/i18n layer. They are not the primary source — code is — but they catch a class of mistakes nothing else does:

- Sections you omitted because you missed them in the JSX.
- Visual groupings (a card, a side panel, a status pill) that change how you should structure the prose.
- States the page can be in that the code makes hard to enumerate.

If the screenshot and the front-end source disagree, **trust the source** — the screenshot may be stale — but flag the discrepancy to the user when you hand off, so the screenshot can be refreshed.

**Write down what you confirmed before drafting.** Note the file path next to each fact (e.g. `<UI label> — <repo-relative path>:<line>`). If a claim isn't traceable to a file you read or a screenshot you viewed, don't make it.

**Read targeted, not exhaustive.** This step can balloon the context window if you treat it as "read everything in `master/`, `sbs/`, and `front-end/`". Don't. The skill is scoped to one feature precisely so this stays cheap:

- Start with Grep, not Read. Grep for the feature name, the endpoint path, the i18n key, or the Java class name to land on the 2–4 files that actually matter.
- Read those files in full. Skim related neighbours only if the first pass leaves a real gap.
- Open a screenshot only if it exists for the exact page you're writing — don't browse the whole `static/img/` tree.
- If you find yourself reading an 8th file, stop and ask: am I still answering a concrete question about this page, or am I exploring? If exploring, the marginal utility of one more file is low; pause and start drafting — gaps will be obvious then.

A typical single-feature page should need ~2–4 backend files, ~2–4 front-end files, and 0–2 screenshots. If your context is filling up faster than that, you're over-reading.

### 3. Pick the right place in `docs/docs/`

Match the user's intent to the existing site structure:

- **`docs/docs/features/`** — capabilities exposed in the Axelix UI (one `*.md` per feature; subdirectories exist for grouped features like `loggers/`).
- **`docs/docs/setting-up-master-ui/`** — installation/configuration of the master UI itself.
- **`docs/docs/setting-up-spring-boot-service/`** — how end users add the SBS to their own Spring Boot applications.
- **`docs/docs/ui-guide/`** — UI-walkthrough material that doesn't belong to a single feature.
- Top-level (`architecture.md`, `glossary.md`, `motivation.md`, `compatibility-matrix.md`, `introduction.md`) — only edit these when the user explicitly asks.

If a fitting page already exists, edit it. Don't create a duplicate. If no fitting page exists, create one with a sensible filename (lowercase, hyphenated, no spaces — match neighbours).

If the same feature ships in both `axelix-spring-boot-2` and `axelix-spring-boot-3`, write **one** page; surface version-specific differences (snippet for SB2, snippet for SB3) with `<Tabs>` from §5 — don't fork the page.

When you create a new subdirectory, add a `_category_.json` next to the pages so Docusaurus labels the sidebar group correctly. Match the existing pattern from `docs/docs/features/_category_.json`:

```json
{
  "label": "Features",
  "position": 5,
  "link": {
    "type": "generated-index"
  }
}
```

### 4. Match the existing page style

Look at 2–3 sibling pages before writing. The conventions are:

- **Frontmatter** with `sidebar_position` controlling order, e.g.

  ```markdown
  ---
  sidebar_position: 5
  ---
  ```

  Pick a position that fits the existing sequence; don't reshuffle siblings unless asked.

- **One H1** that matches the page topic (`# Beans`, `# Properties`).
- **Lead paragraph**: one or two sentences stating what the page is and what users get from it. No throat-clearing.
- **Section headings** (`##`) per coherent subtopic, with `{#anchor}` slugs when other pages link in.
- **Screenshots** from `docs/static/img/feature/...` referenced relatively. Path depth depends on the page: `../../static/img/feature/...` from `docs/docs/<page>.md`, `../../../static/img/feature/...` from a subdirectory like `docs/docs/features/loggers/<page>.md`. Only reference images that exist on disk — verify with `ls`. If a screenshot is needed but doesn't exist yet, see the **Missing-screenshot placeholder** rule in §7 — never reference a missing image path. Alt text describes what the image shows in 4–8 words ("beans main page"), not the filename.
- **Icons for UI controls.** When the prose names a button, switch, or icon-only control, embed the icon inline so the reader sees what they will click. Icons live in `docs/static/img/feature/icons/` and are served at the URL path `/img/feature/icons/<name>`. Use a raw HTML `<img>` tag — Markdown image syntax has no width/height controls. The canonical pattern from `features/scheduled-tasks.md`:

  ```html
  <img src="/img/feature/icons/switch-on-icon.png" alt="switch-on-icon" width="32" height="15"/>
  ```

  Sizes already in use across the docs: switch icons are 32×15, action icons (play, save, cancel, overwrite, redirect, settings) are 20×20. Match the existing convention rather than picking new sizes. Browse `docs/static/img/feature/icons/` to see what is available before naming a control. If the icon you want is missing, leave a note in your handoff and either describe the control in prose or use the closest available icon — never reference a path that does not exist on disk.

- **Code blocks** with language hints (` ```yaml `, ` ```java `, ` ```bash `) — no untagged blocks.
- **Lists with bold labels** for property dictionaries, mirroring the existing pattern in `features/beans.md` and `features/properties.md`:

  ```markdown
  - **Name**: short description.
  - **Default**: `value`.
  ```

  For parallel data (compatibility, types, defaults), prefer a Markdown table — see §5.

- **Pair description with example — for things the reader will copy.** When you list configuration keys or code the user pastes into their project, place the demonstrating snippet **immediately after the list, within the same section, before any other heading**. The reader should never have to scroll to map a config key in the prose to its appearance in the snippet. Description and snippet are read together; a heading between them breaks the connection. **Do not, however, follow a list of UI-field descriptions with a JSON dump of the underlying API response.** Feature pages describe what the user sees on screen, not the contract underneath; a raw JSON shape adds bytes the reader doesn't recognise from any view they actually use.

- **Cross-links** to glossary or related features as relative Markdown links (`[Properties](../features/properties.md)`).

- **Hold a single style across the whole page, and a single shape across parallel rows.** Consistency is non-negotiable; a reader who learns the shape of one section should be able to read the next without recalibrating. Apply this at two granularities:

    - **Page-wide.** Once you pick a way of describing things — sentence length, level of detail, where worked examples appear, how you mark "what the user does" vs "what's automatic", how cross-links phrase themselves (`see [...]`), how config snippets are wrapped — keep that style for every section of the page. If section 2 ends each bullet with a `(e.g. ...)`, section 5 does too. Switching styles mid-page makes the reader feel they've crossed into a different document and forces them to re-learn the conventions.

    - **Parallel sections.** When the page has sibling sections describing the same kind of thing (e.g. Cron / Fixed delay / Fixed rate), every row that names the same field, control, or concept must use the same wording, the same level of example, the same anchor links, the same admonitions. If `Status` in one section links to `#status`, every other `Status` row links to `#status`. If `Interval (ms)` in one section ships a worked numeric example, every other `Interval (ms)` row ships a parallel one — same numbers, same shape, only the underlying rule differs. Asymmetry only earns its place when the underlying thing is genuinely different; if you find yourself describing the same field two different ways, you have either an inconsistency to fix, or a real difference to surface explicitly.

For admonitions and richer Docusaurus elements, see §5.

### 5. Use Docusaurus features when they help

The site is built on Docusaurus, which means more than plain Markdown is available. Reach for these when they make a page genuinely easier to read or navigate — not for decoration.

- **Admonitions** (`:::note`, `:::info`, `:::tip`, `:::warning`, `:::danger`, `:::caution`) — pull a single important caveat out of the flow so the reader can't miss it. One per logical section is plenty for caveats and guidance; a page wallpapered in admonitions trains readers to ignore them. (This is a guideline for caveat/guidance admonitions; the missing-screenshot placeholder in §7 is a structural marker, not a caveat, so it follows §7's own rule of "one per missing image".)
- **Tabs** (`<Tabs>` / `<TabItem>`) — use when the same instruction has parallel forms the reader picks one of (Spring Boot 2 vs 3, Maven vs Gradle, Docker vs Kubernetes). Don't use tabs to hide content the reader needs to see together.
    - **Spring application config: always offer both `application.yaml` and `application.properties`** via `<Tabs>`. Spring Boot accepts either format, and which the user maintains is a project convention you can't predict. Showing only one form silently asks every reader on the other format to do the conversion themselves. Template:

      ````mdx
      <Tabs groupId="spring-config">
        <TabItem value="yaml" label="application.yaml">
          ```yaml
          management:
            endpoints:
              web:
                exposure:
                  include: axelix-scheduled-tasks
          ```
        </TabItem>
        <TabItem value="properties" label="application.properties">
          ```properties
          management.endpoints.web.exposure.include=axelix-scheduled-tasks
          ```
        </TabItem>
      </Tabs>
      ````

      Use `groupId="spring-config"` consistently so a reader who picks one format on one page sees it preselected on the next.
- **Tables** — use for parallel data where columns make scanning easier than bulleted prose: compatibility matrix, field/type/default tables, supported environments × Spring Boot version. Plain Markdown tables work; no Docusaurus extension needed.
- **Collapsible sections** — wrap long reference dumps (full property table, full JSON example) in a `<details>` HTML block so most readers don't have to scroll past them by default.
- **Code blocks with `title=` and line highlighting** (` ```yaml title="application.yaml" {3,5-7} `) — use when you need the reader's eye to land on a specific line in a longer snippet.
- **Anchored headings** (`## Section{#section}`) — keep slugs short and stable so cross-page links don't rot.
- **MDX components / imports** — only when an existing component already lives in `docs/src/`. Don't introduce new MDX components from this skill; that's a project-wide decision.

If you're unsure whether a feature is worth pulling in, default to plain Markdown. The bar for using a Docusaurus feature is "the page is meaningfully clearer with it" — not "I haven't used this one yet today".

### 6. Visual polish — in service of clarity, never against it

Pages should look cared-for: consistent spacing, headings in a sensible hierarchy, screenshots and diagrams placed where they actually illustrate the surrounding text, lists aligned, code blocks tagged. A reader should feel the page was written deliberately.

But visual polish is the seasoning, not the dish. The hierarchy is non-negotiable:

1. **Accuracy** — the reader can act on what you wrote without being misled.
2. **Clarity** — the reader understands on first read.
3. **Findability** — the reader can scan and locate what they need.
4. **Visual quality** — the page is pleasant to read.

When two of these conflict, the higher one wins. Concretely:

- A short, plain section that states the truth beats a beautifully laid-out section that hand-waves.
- An ugly-but-correct diagram is better than a polished diagram that drops a step.
- If a screenshot doesn't yet exist, follow the **Missing-screenshot placeholder** rule in §7 — never reference a missing image to keep the page "looking complete".
- Don't pad sections to balance the page visually. Asymmetry is fine; missing information is not.
- Don't add emoji, icons, or callouts as decoration. They earn their place when they speed up scanning (e.g. a ✅/❌ in a compatibility matrix, or the 📸 in §7's placeholder); otherwise they add noise.

A good test: read the page aloud as if briefing a new teammate. If a sentence exists only to make the page "feel substantial", cut it. The page will look better lighter.

### 7. Missing-screenshot placeholder — make the gap impossible to miss

A missing screenshot is not a small thing. The whole point of a screenshot is to anchor the reader's mental model to the actual UI; if it's absent the page is incomplete, and the page must say so loudly. Hidden HTML comments (`<!-- TODO ... -->`) are forbidden here — they vanish in the rendered docs, and the gap quietly ships.

When you would have placed an image but the file doesn't exist, drop a `:::danger` admonition exactly where the image belonged. Use this template verbatim, replacing only the bracketed parts:

```markdown
:::danger[📸 SCREENSHOT REQUIRED]
**A screenshot is missing here and must be added before this page ships.**

- **What to capture:** [concrete description — which page, which section, which state of the UI].
- **Suggested file path:** `docs/static/img/feature/[area]/[name].png`.
- **Why it matters:** [one short line explaining what the reader cannot understand without it].
:::
```

Rules for this placeholder:

- It is the **only** acceptable way to mark a missing screenshot. No HTML comments, no italicized "(screenshot pending)" inline notes, no empty `![]()` tags.
- Use `:::danger` (red) — not `:::info` or `:::warning`. The visual loudness is the whole point: a maintainer scrolling the page must see it instantly.
- Always include the camera prefix `📸 SCREENSHOT REQUIRED` in the title. Consistent with §6, the emoji earns its place because it speeds scanning — it isn't decoration.
- Place it **at the position the image would have occupied** in the reading flow, not at the top or bottom of the page. The reader needs to see "what's missing right here", in context.
- One placeholder per missing screenshot. If three images are missing, write three placeholders — don't consolidate, because each marks a different point in the reading flow. (This is why §5's "one admonition per section" guideline doesn't apply: those are caveats; these are structural markers.)
- Do not write surrounding prose that pretends the screenshot is there ("As shown above…"). Adjust the prose to acknowledge the gap, or move the prose so it still reads correctly without the image.

When reporting back to the user, list every placeholder you inserted and the file path each one expects, so the screenshots can be captured and dropped in without re-reading the page.

### 8. Voice and tone

The existing docs are professional but not stiff. Aim for the same register:

- Active voice, present tense. "Master discovers services" — not "services are discovered" (passive) or "services may be discovered" (vague).
- Direct second person where it helps the reader ("To enable values, set …"). Avoid first person plural ("we").
- Concrete over abstract. Show the actual property key, the actual JSON shape, the actual UI label — not a paraphrase. Use real, recognizable values, not `<placeholder>` syntax: `org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor` instead of `<fully qualified class name>`, `axelix.master.url` instead of `<property key>`. Placeholders force the reader to imagine what a real value looks like; a concrete example answers that for free, and signals that you actually verified the example exists.
- Prefer short sentences. If a sentence chains three independent clauses with commas, split it; one comma-clause is fine.
- No marketing adjectives ("powerful", "seamless", "world-class"). State what the feature does and why a user would reach for it. The value should be obvious from the description.
- Acknowledge limits honestly, **and place each limit inside the section where the reader is thinking about it** — not in a separate "Limitations" block at the end of the page. A Spring Boot version cap belongs next to the version selector or the property it gates; a security implication belongs next to the configuration step that creates it; "values are hidden by default" belongs next to the value-display description, not in a footnote. A limit at the end of the page is a limit the reader has already missed.
- For deprecated features or properties, place a `:::warning Deprecated since X.Y` admonition naming the replacement and the planned removal version. Don't bury the deprecation in prose — readers skimming for an answer will miss it.

If you're tempted to write something you can't substantiate from the code — delete it.

**Write for a broad audience — without sacrificing precision.** Developers, testers, team leads, and managers all read these pages. Simplify *ordinary* words and sentence shapes when you can: "use" beats "utilize", "before" beats "prior to", a clear short sentence beats a long winding one. **Technical terms are different — they are non-negotiable.** Names like `@Scheduled`, `TaskScheduler`, `@ConfigurationProperties`, `Bean`, `actuator endpoint`, `cron expression`, `Singleton`, `Prototype` are the precise labels for what you are documenting. Replacing them with paraphrases ("the scheduling annotation", "a task-running bean") makes the page misleading. Use the real names. When a reader might not know a term, give a short definition the first time it appears — one clause, not a tutorial — and then keep using the term. The audience scales up through simpler *prose*, not through lighter *vocabulary*.

**Be detailed but alive.** Don't make the page short by stripping useful context, but don't pad it either. The right length is whatever a curious reader needs to act on the feature once. Concrete numbers, real paths, real labels — these are what make the page feel grounded rather than generic.

**Don't sound AI-generated.** The default machine register has obvious tells. Watch for them and cut them out:

- **Em-dash overuse.** Where a period or comma works, use that. One em-dash per paragraph is enough; pages where every sentence has one read like a script.
- **Three-part parallel constructions repeated.** "X, Y, and Z" patterns and "no A, no B, no C" rhetorical lists pile up. Vary the shape — sometimes one example is enough, sometimes a single direct sentence beats a balanced trio.
- **Padding adverbs.** "Essentially", "genuinely", "specifically", "actually", "particularly". Read the sentence without them; if the meaning is unchanged, drop them.
- **Throat-clearing transitions.** "It's worth noting that", "In practice", "Importantly", "As a rule" — just say the thing.
- **Rhetorical "X, not Y"** balanced sentences. Useful sometimes, tiring when every other line does it.
- **Smooth uniform tone.** Vary sentence length. Mix short sentences with longer ones. Allow yourself a terse line, then a warmer one. A page that reads like one perfectly even paragraph reads like a machine wrote it.

**Semicolons only when they earn their place.** Use `;` to join two independent clauses whose relationship is tight and implicit. If you can replace `;` with `.` (period + space) or `, and` without changing meaning, the simpler punctuation is the better choice. Most pages need zero semicolons.

### 9. Standard page skeleton

When a page needs to be built from scratch, this skeleton fits the existing site. Skip sections that don't apply rather than padding them. Outer fences are quadrupled so the inner code blocks render correctly.

````markdown
---
sidebar_position: <N>
---

# <Feature Name>

<One- or two-sentence lead: what this page covers, who it helps.>

![<feature> main view](../../static/img/feature/<feature>/<screenshot>.png)
***<Caption>***

<!-- If the image file doesn't exist on disk, replace the line above with the
     :::danger[📸 SCREENSHOT REQUIRED] placeholder from §7. Don't ship a dead path. -->

<Short paragraph: what the user sees in Axelix and where the data comes from.>

## Overview{#overview}

<What the feature does at a glance. 2–4 bullets or a short paragraph.>

## <Detail section, e.g. "Property fields">{#details}

- **<Field>**: <description, with real values where helpful>.
- **<Field>**: <description>.

## Configuration{#configuration}

<If the feature is gated by a property or starter setting, document the exact key, the default, and how to change it. Use a yaml block.>

```yaml
axelix:
  <real.property.key>: <real-default>
```

## Related{#related}

- [<Related feature>](../features/<page>.md)
- [<Setup page>](../setting-up-spring-boot-service/<page>.md)
````

Drop sections the actual feature doesn't have. A small page with three accurate sections beats a large page with two invented ones.

### 10. Verify before handing off

Before reporting the page as done:

- Re-read your draft against the source. Every concrete claim (paths, property keys, defaults, supported versions, conditional behavior) must be traceable to a file you read.
- For every Markdown link on the page, confirm the target file exists on disk relative to the page's location.
- Check every image path exists under `docs/static/img/...`. If any file is missing, replace the broken `![]()` reference with the `:::danger[📸 SCREENSHOT REQUIRED]` placeholder from §7 — never leave a dead path or a hidden HTML comment.
- Confirm the `sidebar_position` is unique among siblings. List the values with `grep -h '^sidebar_position' docs/docs/<dir>/*.md` and pick a number that's unused in that directory.
- If the user explicitly asked for it, or if this task targets `compatibility-matrix.md` (or an adjacent matrix-related doc), update `compatibility-matrix.md` to reflect a new feature, version, or environment. Otherwise keep changes scoped to the target file and mention the matrix gap to the user at handoff — consistent with §3 ("only edit top-level pages when the user explicitly asks") and the anti-pattern below ("stay inside the file you're documenting").
- Run a quick sanity scan: any phrase that sounds like marketing or that you can't point to in the code — cut it.
- **Style-consistency pass.** Read the page once with the consistency rule from §4 in mind. For every parallel section (Cron / Fixed delay / Fixed rate, or any analogous trio), check that every row describing the same field/control/concept uses the same wording shape, the same level of example, the same anchor links, the same admonitions. If section A has an `(e.g. ...)` and section B doesn't, fix it before handoff — that's exactly the kind of difference that erodes the reader's trust.

Tell the user: which file you wrote/edited, what source files you grounded each section in, and any honest gaps (missing screenshots, behaviors you couldn't confirm, version constraints you'd recommend a maintainer double-check).

## Anti-patterns to avoid

These mistakes show up easily; watch for them:

- **Plausible-sounding fabrication.** "Master uses an exponential backoff with jitter for retries" — only true if the code says so. Read first; if it doesn't, don't write it.
- **Restating the obvious.** "Click the button to perform the action it labels" adds no value. Assume the reader can read.
- **Padding with structure.** Don't add an "Overview", "Details", "Configuration", "Examples", "FAQ", "Troubleshooting" stack to a feature that has a single screen and one property. Match the page size to the feature.
- **Refactoring neighbouring pages.** Stay inside the file you're documenting. If you spot a real issue elsewhere, mention it to the user — don't silently rewrite it.
- **Generic boilerplate intros.** "In this guide, we will explore…" — just say what the page is in one direct sentence.
- **Out-of-date claims about versions or environments.** Cross-check against `compatibility-matrix.md` and the actual `@ConditionalOnXxx` / starter manifests before writing version statements.
- **Documenting the master REST API on a feature page.** Feature pages describe the UI surface — what the user sees, what they click, what they configure. The HTTP paths master proxies to the service are an implementation detail the reader doesn't touch through this page; an "API" or "REST surface" table belongs in a dedicated API reference (if/when one is added), not bolted onto a feature page. If you read endpoint paths during research (§2 explicitly tells you to), keep them in your notes — don't surface them in the doc.
- **Raw JSON of API responses on a feature page.** Same reason. The user reads a feature page to understand a screen, not the bytes underneath. A JSON shape on a feature page invites them to think about a layer they didn't sign up to debug.
- **Auto-configuration conditions framed as user requirements.** When Axelix's autoconfiguration is gated by `@ConditionalOnBean(SomeFrameworkType.class)` or similar, that condition is usually *already satisfied* by Spring Boot's own auto-configuration in any normal setup — the user doesn't do anything to provide it. Listing it as "the application **must** provide a `TaskScheduler` bean" makes the reader think they have a step to perform when in reality Spring Boot has already done it. **Document only what the user actually configures.** Background prerequisites that Spring Boot satisfies on their behalf belong in one short sentence ("Spring Boot's auto-configuration provides X automatically"), or are simply omitted. To tell the difference: ask "would a user with a vanilla Spring Boot project hit this requirement, or has Spring Boot already handled it?" If Spring Boot handled it, don't make the reader feel they have a checkbox to tick.

## Reference: layout cheatsheet

```text
docs/docs/
├── architecture.md
├── compatibility-matrix.md
├── glossary.md
├── introduction.md
├── motivation.md
├── features/
│   ├── _category_.json
│   ├── beans.md
│   ├── caches.md
│   ├── conditions.md
│   ├── configuration-properties.md
│   ├── details.md
│   ├── garbage-collector.md
│   ├── loggers/        <- subdirectory for grouped feature
│   ├── metrics.md
│   ├── properties.md
│   ├── scheduled-tasks.md
│   ├── thread-dump.md
│   └── transaction-control.md
├── setting-up-master-ui/
│   ├── _category_.json
│   ├── authentication.md
│   ├── configuring-master.md
│   └── what-is-master.md
├── setting-up-spring-boot-service/
└── ui-guide/

master/src/main/java/com/axelixlabs/axelix/master/
├── api/{external,internal,error}     <- REST controllers, error mapping
├── autoconfiguration/{auth,discovery,probers,web}
├── domain/                            <- master-side domain
├── exception/{auth,...}
├── filter/
├── mcp/
├── repository/{,dialect}
├── service/{auth,convert,discovery,export,serde,state,transport}
└── utils/

sbs/
├── axelix-spring-boot-2/              <- starter for Spring Boot 2
├── axelix-spring-boot-3/              <- starter for Spring Boot 3
│   └── src/main/.../autoconfiguration/  <- AxelixXxxAutoConfiguration classes
└── starter-domain/                    <- shared starter domain

front-end/src/
├── api/                               <- API client layer (calls to master)
├── components/                        <- shared UI components
├── i18n/                              <- displayed string source of truth
├── pages/                             <- one folder per UI screen
├── routes/                            <- URL → page mapping
└── services/                          <- non-API client logic
```

Use this as a map. The truth is in the code; this skill exists to make sure your docs reflect it.
