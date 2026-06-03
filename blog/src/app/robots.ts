import type { MetadataRoute } from "next";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: { userAgent: "*", allow: "/" },
    sitemap: new URL(withBlogBasePath("/sitemap.xml"), getBaseUrl()).toString(),
  };
}
