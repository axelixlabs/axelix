import remarkDirective from "remark-directive";
import {
  remarkDirectiveAdmonition,
  remarkImage,
  remarkMdxFiles,
  remarkMdxMermaid,
} from "fumadocs-core/mdx-plugins";
import { defineCollections, defineConfig, frontmatterSchema } from "fumadocs-mdx/config";
import lastModified from "fumadocs-mdx/plugins/last-modified";
import { z } from "zod";
import convert from "npm-to-yarn";

/**
 * Frontmatter fields available in `content/blog/<slug>/index.mdx`.
 *
 * Inherited from fumadocs' `frontmatterSchema`:
 * - `title`        (string, required) — post title; shown on cards, the article
 *                  header, and used as the default <title>/OG title.
 * - `description`  (string, optional) — short summary; used as the card text, the
 *                  article standfirst, and the RSS <description>.
 *
 * Added below:
 * - `authors`      (string[], required) — author display names. The avatar is the
 *                  convention image `public/authors/<slug>.png` (slug derived from
 *                  the name); missing file → initials circle. See src/lib/authors.
 * - `date`         (date, required) — publish date; drives ordering (newest first)
 *                  and which post is "featured" on the home page.
 * - `tags`         (string[], optional) — free-form tags. The home filter's tag
 *                  list is derived from all posts; there is no fixed vocabulary.
 * - `heroImagePath`(string, optional) — cover image. Must be an absolute path
 *                  served from `public/` (e.g. `/posts/<slug>/cover.png`); Next
 *                  only serves static files from `public/`, not from `content/`.
 *                  No image → neutral placeholder.
 * - `metaTitle`    (string, optional) — overrides <title>/OG title for SEO only.
 * - `metaDescription`(string, optional) — overrides the meta/OG/Twitter
 *                  description for SEO only (falls back to `description`).
 * - `metaImagePath`(string, optional) — fallback cover image, used only when
 *                  `heroImagePath` is absent (see getCardImageSrc).
 */
export const blogPosts = defineCollections({
  type: "doc",
  dir: "content/blog",
  schema: frontmatterSchema.extend({
    authors: z.array(z.string()),
    date: z.coerce.date(),
    tags: z.array(z.string()).optional(),
    heroImagePath: z.string().optional(),
    metaTitle: z.string().optional(),
    metaDescription: z.string().optional(),
    metaImagePath: z.string().optional(),
  }),
  // Exposes page.data.getText("processed") for RSS and the future LLM endpoints.
  postprocess: { includeProcessedMarkdown: true },
});

export default defineConfig({
  plugins: [lastModified()],
  // MDX pipeline mirrored 1:1 from the reference blog (my-assets/blog). These
  // remark plugins MERGE with fumadocs' defaults (Shiki highlighting, heading
  // slugs, TOC, GFM, code tabs): `:::` admonitions, image/basePath handling,
  // .mdx includes, ```mermaid blocks, and npm→pnpm/yarn/bun command conversion.
  mdxOptions: {
    remarkPlugins: [
      remarkDirective,
      remarkDirectiveAdmonition,
      [remarkImage, { useImport: false }],
      remarkMdxFiles,
      remarkMdxMermaid,
    ],
    remarkCodeTabOptions: { parseMdx: true },
    remarkNpmOptions: {
      persist: { id: "package-manager" },
      // Custom package managers to add --bun flag for bunx commands.
      packageManagers: [
        {
          command: (cmd: string) => convert(cmd.replace(/^npm init -y$/, "npm init"), "npm"),
          name: "npm",
        },
        {
          command: (cmd: string) => convert(cmd.replace(/^npm init -y$/, "npm init"), "pnpm"),
          name: "pnpm",
        },
        {
          command: (cmd: string) => convert(cmd.replace(/^npm init -y$/, "npm init"), "yarn"),
          name: "yarn",
        },
        {
          command: (cmd: string) => {
            const converted = convert(cmd.replace(/^npm init -y$/, "npm init"), "bun");
            if (!converted) return undefined;
            return converted.replace(/^bun x /, "bunx --bun ");
          },
          name: "bun",
        },
      ],
    },
  },
});
