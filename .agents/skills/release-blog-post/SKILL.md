---
name: release-blog-post
description: Write an Axelix release blog post. Use when asked to write, draft, or add a blog post for a release ("write a blog post for 1.3.0", "add blog post for the release", "announce 1.2.1 on the blog").
---

# Release Blog Post

## Step 1 — Gather source material

The GitHub release page (`https://github.com/axelixlabs/axelix/releases/tag/vX.Y.Z`) is the foundation.
Its text is the basis for the blog post — copy and adapt it, don't rewrite from scratch.

Then read `blog/content/blog/axelix-1-2-1/index.mdx` for the formatting and structure to follow.

## Step 2 — Create the files

- Post: `blog/content/blog/axelix-X-Y-Z/index.mdx`
- Hero image: generate it with:
  ```bash
  node blog/scripts/generate-preview.mjs X.Y.Z
  ```
  This produces `blog/public/posts/axelix-X-Y-Z/preview.png` from the SVG template.

## Step 3 — Frontmatter

```mdx
---
title: "Axelix X.Y.Z: <short tagline>"
date: YYYY-MM-DD
authors: ["Display Name"]
tags: ["Releases"]
heroImagePath: /posts/axelix-X-Y-Z/preview.png
description: "<one sentence, copy to metaDescription>"
metaDescription: "<same as description>"
---
```

## Step 4 — Sections

Write each section in this order. Skip a section if the release has nothing for it.

**Intro** — one paragraph: what kind of release (patch/minor/major), compatible or not, lockstep (starters + plugins + Master ship together).

**`## Security`** — one `###` per advisory.
- Advisory link format: `[CVSS X.X · Severity · CWE-NNN](https://github.com/axelixlabs/axelix/security/advisories/GHSA-xxxx-xxxx-xxxx)`
- Affected packages link format: `https://central.sonatype.com/artifact/com.axelixlabs/<artifact-id>`
- Three starter IDs: `axelix-spring-boot-2-starter`, `axelix-spring-boot-3-starter`, `axelix-spring-boot-4-starter`
- End with: `**If you cannot upgrade immediately:** <mitigation>.`

**`## New features`** / **`## Changes`** — one `###` per feature or change. Two to three sentences describing what it does and why it's useful. If the feature has a docs page, link to it: `[feature name](https://axelix.io/docs/...)`.

**`## Bug fixes`** — one `###` per bug. Two to three sentences: symptom, cause, fix.

**`## Artifacts`**

```mdx
| Artifact | Link |
|---|---|
| JAR | [axelix-X.Y.Z.jar](https://github.com/axelixlabs/axelix/releases/download/vX.Y.Z/axelix-X.Y.Z.jar) |
| Docker Hub | [axelixlabs/axelix](https://hub.docker.com/r/axelixlabs/axelix) — `docker pull axelixlabs/axelix:X.Y.Z` |
| GHCR | `docker pull ghcr.io/axelixlabs/axelix:X.Y.Z` |
| Helm chart | [axelix-X.Y.Z.tgz](https://github.com/axelixlabs/axelix/releases/download/vX.Y.Z/axelix-X.Y.Z.tgz) · [Artifact Hub](https://artifacthub.io/packages/helm/axelix/axelix) |
```

**`## Upgrading`** — one paragraph. Link to `https://axelix.io/docs/setting-up-master-ui/migrations/migrations` before upgrading Master.

**`## Full changelog`**

```mdx
**Full Changelog**: https://github.com/axelixlabs/axelix/compare/vPREV...vX.Y.Z

Thanks to all contributors who made this release happen!
```

## Step 5 — Author

Author slug = display name lowercased with spaces replaced by hyphens (e.g. "Sergey Cherkasov" → `sergey-cherkasov`).

If the author has no entry in `AUTHOR_PROFILES` in `blog/src/lib/authors.ts`, add one. Photo goes in `blog/public/authors/<slug>.jpeg`.

## Step 6 — Commit

```bash
git checkout -b feature/GH-XXXX
git add blog/content/blog/axelix-X-Y-Z/ blog/public/posts/axelix-X-Y-Z/ blog/src/lib/authors.ts blog/public/authors/<slug>.jpeg
git commit -m "Add release blog post for X.Y.Z"
```
