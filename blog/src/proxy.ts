import { isMarkdownPreferred, rewritePath } from "fumadocs-core/negotiation";
import { NextRequest, NextResponse } from "next/server";

const CONTENT_ROUTE = "/llms.mdx/blog";
const { rewrite: rewriteSuffix } = rewritePath("{/*path}.md", `${CONTENT_ROUTE}{/*path}/content.md`);
const { rewrite: rewritePost } = rewritePath("{/*path}", `${CONTENT_ROUTE}{/*path}/content.md`);

const reservedPaths = new Set([
  "/llms.txt",
  "/llms-full.txt",
  "/authors",
  "/rss.xml",
  "/robots.txt",
  "/sitemap.xml",
  "/icon.svg",
  "/favicon.ico",
]);
/** Blog posts have one path segment; leave all other routes and assets untouched. */
function isPostCandidate(pathname: string) {
  return /^\/[^/]+$/.test(pathname) && !reservedPaths.has(pathname);
}

export default function proxy(request: NextRequest) {
  const { pathname, basePath } = request.nextUrl;
  if (!isPostCandidate(pathname)) return NextResponse.next();

  // A .md suffix is explicit; only negotiated responses need Vary: Accept.
  const markdownPath = rewriteSuffix(pathname);
  if (markdownPath) {
    return NextResponse.rewrite(new URL(`${basePath}${markdownPath}`, request.nextUrl));
  }

  if (pathname.includes(".") || !isMarkdownPreferred(request)) return NextResponse.next();

  const negotiatedPath = rewritePost(pathname);
  if (!negotiatedPath) return NextResponse.next();

  return NextResponse.rewrite(new URL(`${basePath}${negotiatedPath}`, request.nextUrl), {
    headers: { Vary: "Accept" },
  });
}

export const config = {
  matcher: ["/((?!_next/|api/|authors/|llms.mdx/).*)"],
};
