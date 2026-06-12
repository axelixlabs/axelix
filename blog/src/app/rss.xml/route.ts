/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { BLOG_HOME_DESCRIPTION, BLOG_HOME_TITLE } from "@/lib/blog-metadata";
import { getSortedPosts } from "@/lib/source";
import { getBaseUrl, withBlogBasePath } from "@/lib/url";

export const revalidate = false;

function escapeXml(value: string): string {
    return value
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&apos;");
}

export function GET() {
    const base = getBaseUrl();
    const abs = (path: string) => new URL(withBlogBasePath(path), base).toString();
    const home = abs("/");

    const items = getSortedPosts()
        .map((page) => {
            const link = abs(`/${page.slugs.join("/")}`);
            const description = page.data.description ?? page.data.metaDescription ?? "";
            return `    <item>
      <title>${escapeXml(page.data.title)}</title>
      <link>${escapeXml(link)}</link>
      <guid isPermaLink="true">${escapeXml(link)}</guid>
      <pubDate>${page.data.date.toUTCString()}</pubDate>
      <description>${escapeXml(description)}</description>
    </item>`;
        })
        .join("\n");

    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <title>${escapeXml(BLOG_HOME_TITLE)}</title>
    <link>${escapeXml(home)}</link>
    <description>${escapeXml(BLOG_HOME_DESCRIPTION)}</description>
${items}
  </channel>
</rss>`;

    return new Response(xml, {
        headers: { "Content-Type": "application/xml; charset=utf-8" },
    });
}
