import { blogPosts } from "../../.source/server";
import { type InferPageType, loader } from "fumadocs-core/source";
import { toFumadocsSource } from "fumadocs-mdx/runtime/server";
import { computeReadingTime } from "./reading-time";
import { withBlogBasePathForImageSrc } from "./url";

/** Single point of access to blog content. */
export const blog = loader({
  baseUrl: "/",
  source: toFumadocsSource(blogPosts, []),
  // Name the page-tree root "Blog" so search breadcrumbs read "Blog › …"
  // instead of fumadocs' default "Docs".
  pageTree: {
    transformers: [
      {
        root(node) {
          node.name = "Blog";
          return node;
        },
      },
    ],
  },
});

export type BlogPage = InferPageType<typeof blog>;

/** Shape consumed by the home-page cards/rows. */
export interface BlogCardItem {
  slug: string;
  /** In-app path (no basePath); pass straight to next/link. */
  href: string;
  title: string;
  description: string;
  tags: string[];
  authors: string[];
  /** ISO date string. */
  date: string;
  /** Hero image src (basePath-prefixed) or null → no image yet. */
  coverSrc: string | null;
  readingMinutes: number;
}

/** Resolves a post's hero image to a usable <img> src, or null. */
export function getCardImageSrc(page: BlogPage): string | null {
  const rel = page.data.heroImagePath ?? page.data.metaImagePath;
  if (!rel) return null;
  if (rel.startsWith("/")) return withBlogBasePathForImageSrc(rel);
  // Relative to the post folder.
  const base = page.url.endsWith("/") ? page.url.slice(0, -1) : page.url;
  const clean = rel.replace(/^\.\//, "").replace(/^\/+/, "");
  return withBlogBasePathForImageSrc(`${base}/${clean}`);
}

/** All posts, newest first. */
export function getSortedPosts(): BlogPage[] {
  return [...blog.getPages()].sort(
    (a, b) => b.data.date.getTime() - a.data.date.getTime(),
  );
}

export async function toCardItem(page: BlogPage): Promise<BlogCardItem> {
  const raw = await page.data.getText("raw");
  return {
    slug: page.slugs.join("/"),
    href: page.url,
    title: page.data.title,
    description: page.data.description ?? "",
    tags: page.data.tags ?? [],
    authors: page.data.authors,
    date: page.data.date.toISOString(),
    coverSrc: getCardImageSrc(page),
    readingMinutes: computeReadingTime(raw),
  };
}

/** All posts as card items, newest first. */
export async function getSortedCardItems(): Promise<BlogCardItem[]> {
  return Promise.all(getSortedPosts().map(toCardItem));
}
