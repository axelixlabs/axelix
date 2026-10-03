# Fumadocs mechanics for the Axelix docs site

How pages in `docs/content/docs/` are built. The principles of *what* to write live in `SKILL.md`; this file is *how* to write it so the site builds and renders. When a rule here disagrees with the site's own config, the config wins: `docs/source.config.ts`, `docs/src/components/mdx.tsx`, `docs/src/lib/source.ts`.

## Files and frontmatter

- Every page is a pair in the same folder: `<page>.mdx` (English) and `<page>.ru.mdx` (Russian).
- Every page starts with frontmatter holding `title` and a quoted `description`. Nothing else is used.
- There is no `# H1` in the body: Fumadocs renders `title` as the page heading.
- Filenames are lowercase and hyphenated, matching neighbours.

## Sidebar: `meta.json`

Each folder has a `meta.json` and a `meta.ru.json`. The order of `pages` is the order in the sidebar; entries are file names without `.mdx`; `"...name"` inlines a subfolder's pages. **A page that isn't listed in `pages` doesn't appear in the sidebar.** Add a new page to both files at the same slot:

```json
{
    "title": "Spring Framework",
    "defaultOpen": false,
    "pages": ["properties", "beans", "configuration-properties", "scheduled-tasks"]
}
```

- A new folder gets both meta files; `title` in `meta.ru.json` is Russian.
- Never add `"root": true`: it splits the single site-wide sidebar.
- Never put link entries like `[Name](/url)` into `pages` for internal pages: they hijack the sidebar from the real page.
- The root `docs/content/docs/meta.json` holds the `---Section---` separators. Touch it only when adding a new top-level topic.

## Components

Available on every page **without imports** (`docs/src/components/mdx.tsx`): `Tabs`, `Tab`, `Image`, `ReleasedInNotice`, `UpcomingReleaseNotice`, `LegacyNotice`, `ScreenshotsNotice`, plus the Fumadocs defaults. Import only co-located files that already exist next to the page (`import styles from './styles.module.css';`, a small `.tsx`). Some older `.ru.mdx` pages still import `Tab, Tabs`; don't copy that, and don't clean it up unless asked.

## Images and icons

- Files live in `docs/public/img/`; link them from the site root: `/img/feature/<area>/<file>.png`.
- Screenshots go inside `ScreenshotsNotice`, with the caption line right below it. Alt text describes what the image shows in 4–8 words. The Russian page uses `text="Скриншоты соответствуют последнему минорному релизу Axelix."`.

  ```mdx
  <ScreenshotsNotice text="Screenshots show the latest minor release of Axelix.">

  ![properties main page](/img/feature/properties/properties-main-page.png)

  </ScreenshotsNotice>

  ***Properties as presented in Axelix UI***
  ```
- Don't put inline icons of UI controls in the prose (§5 of SKILL.md: describe functionality, not design). Older pages still use `<Image src="/img/feature/icons/…"/>`; drop them when you rewrite such a page.
- Reference only files that exist (`ls`). If a needed screenshot is missing, leave it out and mention it in the handoff.

## Admonitions

`:::type` or `:::type[Title]`, closed with `:::`. Only the types mapped in `docs/source.config.ts` render: today `note`, `info`, `important`, `tip`, `warning`. Anything else, including `danger` and `caution`, shows up as plain text.

## Tabs

````mdx
<Tabs groupId="spring-config" items={['application.properties', 'application.yaml']}>
  <Tab value="application.properties">

```properties
management.endpoints.web.exposure.include=axelix-scheduled-tasks
```

  </Tab>
  <Tab value="application.yaml">

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - axelix-scheduled-tasks
```

  </Tab>
</Tabs>
````

- Keep blank lines around code fences inside `<Tab>`, otherwise MDX won't parse the block.
- `value` of each `Tab` matches an entry in `items`.
- Reuse a `groupId` the site already has, so the reader's choice carries across pages: `grep -rho 'groupId="[^"]*"' docs/content/docs | sort | uniq -c`. Spring config always uses `spring-config` with both forms. Introduce a new `groupId` only for a genuinely new kind of choice.

## Code blocks

Always tag the language. Add `title="<file>"` when the reader has to know which file the snippet goes into: ` ```kotlin title="build.gradle.kts" `, ` ```yaml title="docker-compose.yaml" `.

## Release notices

Place directly under the heading they qualify:

- `<ReleasedInNotice version="1.1.0" />` — the section exists since that release.
- `<UpcomingReleaseNotice />` — merged, not released yet. Swap for `ReleasedInNotice` once it ships.
- `<LegacyNotice version="1.2.0" reason={<>Why it no longer applies.</>} />` — only needed before that release. `reason` is optional; localize it on the `.ru.mdx` page.

## Links

- Link other pages by relative path to the `.mdx` file, extension included, optional anchor: `../../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection`.
- From a `.ru.mdx` page, still link the base `.mdx` file, never `.ru.mdx`; Fumadocs resolves the locale.
- Anchors are slugs of heading text. On a Russian page they are slugs of its Russian headings (`authentication.mdx#роли-и-полномочия`).

## Page template

Text in `[brackets]` is instruction, not content. Keep the common frame, then the blocks for the kind of page you're writing (types from SKILL.md §1); a page can mix blocks. Skip sections the topic doesn't have rather than padding them. Every value that ends up on the page must be real.

````mdx
---
title: [UI screen: the name the Axelix UI shows. Otherwise task-shaped, e.g. Configuring Master]
description: "[1–2 sentences: what the page covers and what the reader can do with it.
              Plain text, no Markdown, no links. Quoted.]"
---

[Non-UI pages: the lead and the access paragraph go here, as in the UI screen block below.]

[━━ UI screen ━━]

<ScreenshotsNotice text="Screenshots show the latest minor release of Axelix.">

![[4–8 word alt]](/img/feature/[area]/[file].png)

</ScreenshotsNotice>

***[Caption] as presented in Axelix UI***

[Lead: the problem the page solves, 1–3 sentences.]

[If the code gates access: a separate short paragraph on which roles/authorities can open it or act on it.]

## What the page shows

[What the reader learns from the screen and why it matters, in prose. A short list only for parallel
facts ("For each property, you can see: …"); bold labels only for named states (**Active**, **Suppressed**).
No layout, columns, icons, colors, search, or counters.]

## [One distinct function, e.g. Dangerous property values]

[Why the reader needs it → what Axelix does → its limits.]

## [User action, if the screen has interactive controls]

[Numbered steps. Required authority and limits here, where the reader acts.]

[━━ Configuration / setup ━━]

## Before you start

[Prerequisites the reader actually has to provide. Not what Spring Boot already does for them.]

## [Task, e.g. Loading configuration from HashiCorp Vault]

[Release notice, if the section is tied to a release.]

[One or two sentences: when the reader needs this.]

| Property                    | Default   | Description                                     |
|-----------------------------|-----------|-------------------------------------------------|
| `[axelix.master.real.key]`  | `[value]` | [Effect. When it's required. Allowed values.]   |

[Snippet right below: Tabs with groupId="spring-config" for Spring config, titled code blocks
for build files and deployment manifests. See the Tabs section above.]

:::note[[Optional short title]]
[One caveat for this section.]
:::

[━━ Walkthrough ━━]

## Step [N]: [Imperative task in sentence case, e.g. Install Master on a test environment]

[What this step achieves, the minimal commands/config to do it, and a link to the reference
page for the full set of options. Don't copy the reference table here.]

[━━ Troubleshooting — one numbered block per problem ━━]

## [N]. [Problem as the reader would phrase it]

### Symptom
[What the reader sees: the exact error message, log line, or UI state.]

### Most Likely Cause
[Why it happens, traced to the code that produces the symptom.]

### The Solution:
[Concrete steps or config to fix it.]

[━━ Closing section ━━]

## [Named like on the neighbouring pages, e.g. See also or Related]

- [[Page title]](../../[topic]/[page].mdx)
````

The `.ru.mdx` twin is the same file translated per SKILL.md §8.

## Mechanical checklist

- `title` and quoted `description` present; no `# H1`; no imports of global components.
- Every link targets an existing `.mdx` file, every `#anchor` matches a heading on the target page, no link ends in `.ru.mdx`.
- Every `/img/...` path exists under `docs/public/img/`.
- Only admonition types mapped in `docs/source.config.ts`.
- A new page is listed in both `meta.json` and `meta.ru.json`, at the same slot.
- EN and RU have the same headings count, images, tabs, tables, notices, and links.
- `cd docs && npm run build` passes. If you can't run it, say so in the handoff; don't claim the page builds.
