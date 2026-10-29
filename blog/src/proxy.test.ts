import { unstable_doesMiddlewareMatch } from "next/experimental/testing/server";
import { NextRequest } from "next/server";
import { describe, expect, it } from "vitest";
import proxy, { config } from "./proxy";

function rewriteFor(path: string, accept?: string) {
  const request = new NextRequest(`https://axelix.io${path}`, {
    headers: accept ? { Accept: accept } : undefined,
  });
  const response = proxy(request);

  return {
    rewrite: response.headers.get("x-middleware-rewrite"),
    vary: response.headers.get("Vary"),
  };
}

describe("blog Markdown negotiation", () => {
  it("rewrites an explicit .md suffix without varying by Accept", () => {
    expect(rewriteFor("/axelix-goes-ga.md", "text/html")).toEqual({
      rewrite: "https://axelix.io/llms.mdx/blog/axelix-goes-ga/content.md",
      vary: null,
    });
  });

  it("rewrites a post when Markdown is preferred", () => {
    expect(rewriteFor("/axelix-goes-ga", "text/markdown, text/html;q=0.5")).toEqual({
      rewrite: "https://axelix.io/llms.mdx/blog/axelix-goes-ga/content.md",
      vary: "Accept",
    });
  });

  it("leaves HTML requests unchanged", () => {
    expect(rewriteFor("/axelix-goes-ga", "text/html").rewrite).toBeNull();
  });

  it.each([
    "/",
    "/llms.txt",
    "/llms-full.txt",
    "/rss.xml",
    "/robots.txt",
    "/sitemap.xml",
    "/authors",
    "/authors/name",
    "/api/search",
    "/icon.svg",
    "/llms.mdx/blog/post/content.md",
  ])(
    "does not rewrite %s",
    (path) => {
      expect(rewriteFor(path, "text/markdown").rewrite).toBeNull();
    },
  );

  it("matches post URLs with the configured basePath", () => {
    expect(
      unstable_doesMiddlewareMatch({
        config,
        nextConfig: { basePath: "/blog" },
        url: "/blog/axelix-goes-ga.md",
      }),
    ).toBe(true);
  });
});
