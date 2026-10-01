import { blogPosts } from "../../.source/server";
import { type InferPageType, loader } from "fumadocs-core/source";
import { toFumadocsSource } from "fumadocs-mdx/runtime/server";
import { computeReadingTime } from "./reading-time";
import { withBlogBasePathForImageSrc } from "./url";
import { type Author, type AuthorProfile, getAuthorProfile, getAuthors } from "./authors";

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

/** A tag the author writes about, with how many of their posts carry it. */
export interface AuthorTopic {
  name: string;
  count: number;
}

/** An author plus everything derived from their posts — the shape consumed by
 *  the authors index cards and the profile header/sidebar. */
export interface AuthorSummary {
  slug: string;
  name: string;
  /** In-app path (no basePath); pass straight to next/link. */
  href: string;
  initials: string;
  color: string;
  /** Editorial role/affiliation, or null when the author has no profile. */
  title: string | null;
  bio: string | null;
  count: number;
  totalMinutes: number;
  /** ISO date of the author's oldest post, or null when they have none. */
  firstDate: string | null;
  /** ISO date of the author's newest post, or null when they have none. */
  latestDate: string | null;
  /** Tags the author writes about, most-used first. */
  topics: AuthorTopic[];
}

/** Everything the profile page needs: the derived summary, the editorial
 *  profile (links etc.), and the author's posts as cards (newest first). */
export interface AuthorPageData {
  summary: AuthorSummary;
  profile: AuthorProfile | null;
  posts: BlogCardItem[];
}

/** Builds an {@link AuthorSummary} from an author and their posts (assumed
 *  newest-first, matching {@link getSortedPosts}). */
function buildAuthorSummary(author: Author, posts: BlogCardItem[]): AuthorSummary {
  const profile = getAuthorProfile(author.slug);
  const topicCounts = new Map<string, number>();
  for (const post of posts) {
    for (const tag of post.tags) topicCounts.set(tag, (topicCounts.get(tag) ?? 0) + 1);
  }
  const topics = [...topicCounts.entries()]
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .map(([name, count]) => ({ name, count }));

  return {
    slug: author.slug,
    name: author.name,
    href: `/authors/${author.slug}`,
    initials: author.initials,
    color: author.color,
    title: profile?.title ?? null,
    bio: profile?.bio ?? null,
    count: posts.length,
    totalMinutes: posts.reduce((sum, post) => sum + post.readingMinutes, 0),
    firstDate: posts.length ? posts[posts.length - 1].date : null,
    latestDate: posts.length ? posts[0].date : null,
    topics,
  };
}

/** Groups all posts by author (newest-first within each), preserving order. */
async function getPostsByAuthor(): Promise<Map<string, { author: Author; posts: BlogCardItem[] }>> {
  const items = await getSortedCardItems();
  const bySlug = new Map<string, { author: Author; posts: BlogCardItem[] }>();
  for (const item of items) {
    for (const author of getAuthors(item.authors)) {
      const entry = bySlug.get(author.slug) ?? { author, posts: [] };
      entry.posts.push(item);
      bySlug.set(author.slug, entry);
    }
  }
  return bySlug;
}

/** Every author with at least one post, most prolific first. */
export async function getAuthorSummaries(): Promise<AuthorSummary[]> {
  const bySlug = await getPostsByAuthor();
  return [...bySlug.values()]
    .map(({ author, posts }) => buildAuthorSummary(author, posts))
    .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name));
}

/** The profile page payload for one author slug, or null if unknown. */
export async function getAuthorPageData(slug: string): Promise<AuthorPageData | null> {
  const entry = (await getPostsByAuthor()).get(slug);
  if (!entry) return null;
  return {
    summary: buildAuthorSummary(entry.author, entry.posts),
    profile: getAuthorProfile(slug),
    posts: entry.posts,
  };
}
