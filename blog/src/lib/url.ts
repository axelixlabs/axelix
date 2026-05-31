/**
 * Origin of the site (e.g. https://axelix.io). Used for canonical URLs,
 * OpenGraph, sitemap and RSS. `basePath` (/blog) is appended separately via
 * {@link withBlogBasePath}.
 */
export function getBaseUrl(): string {
  return process.env.NEXT_PUBLIC_AXELIX_URL ?? "https://axelix.io";
}

const BLOG_PREFIX = "/blog";

/** Prefixes an in-app path with the blog basePath, idempotently. */
export function withBlogBasePath(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  if (normalizedPath === BLOG_PREFIX || normalizedPath.startsWith(`${BLOG_PREFIX}/`)) {
    return normalizedPath;
  }
  if (normalizedPath === "/") return BLOG_PREFIX;
  return `${BLOG_PREFIX}${normalizedPath}`;
}

/** Same as {@link withBlogBasePath} but tolerant of empty / external / asset srcs. */
export function withBlogBasePathForImageSrc(src?: string | null): string {
  if (!src) return "";
  if (!src.startsWith("/")) return src;
  if (src.startsWith("/_next/")) return src;
  return withBlogBasePath(src);
}
