import type { MetadataRoute } from "next";
import { getSortedPosts } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

export default function sitemap(): MetadataRoute.Sitemap {
  const base = getBaseUrl();
  const abs = (path: string) => new URL(withBlogBasePath(path), base).toString();

  const posts = getSortedPosts().map((page) => ({
    url: abs(`/${page.slugs.join("/")}`),
    lastModified: page.data.lastModified ?? page.data.date,
  }));

  return [{ url: abs("/"), lastModified: new Date() }, ...posts];
}
