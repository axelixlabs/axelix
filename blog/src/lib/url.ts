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
