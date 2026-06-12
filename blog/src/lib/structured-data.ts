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
import { IBlogPostingInput } from "@/models";

import { GITHUB_URL, SITE_NAME } from "./blog-metadata";

import { getAuthors } from "./authors";
import { getBaseUrl, withBlogBasePath } from "./url";

function absolute(path: string): string {
    return new URL(withBlogBasePath(path), getBaseUrl()).toString();
}

const ORGANIZATION = {
    "@type": "Organization",
    name: SITE_NAME,
    url: getBaseUrl(),
    sameAs: [GITHUB_URL],
};

/** BlogPosting JSON-LD for a post (only when title + description exist). */
export function getBlogPostingJsonLd(input: IBlogPostingInput) {
    const authors = getAuthors(input.authors).map((a) => ({
        "@type": "Person",
        name: a.name,
    }));
    return {
        "@context": "https://schema.org",
        "@type": "BlogPosting",
        headline: input.title,
        description: input.description,
        datePublished: input.date,
        dateModified: input.modified ?? input.date,
        url: absolute(`/${input.slug}`),
        author: authors.length === 1 ? authors[0] : authors,
        publisher: ORGANIZATION,
        ...(input.image ? { image: new URL(input.image, getBaseUrl()).toString() } : {}),
    };
}
