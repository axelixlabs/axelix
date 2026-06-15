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
import { IArticle } from "@/models";

import { blogPosts } from "../../.source/server";
import { computeReadingTime } from "./reading-time";
import { type InferPageType, loader } from "fumadocs-core/source";
import { toFumadocsSource } from "fumadocs-mdx/runtime/server";

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
    return [...blog.getPages()].sort((a, b) => b.data.date.getTime() - a.data.date.getTime());
}

export async function toCardItem(page: BlogPage): Promise<IArticle> {
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
export async function getSortedCardItems(): Promise<IArticle[]> {
    return Promise.all(getSortedPosts().map(toCardItem));
}
