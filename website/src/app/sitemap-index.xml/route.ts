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
import { SITE_URL } from "@/utils";

export const revalidate = false;

/**
 * Child sitemaps this index points at. Each Axelix project (landing, docs, blog)
 * owns and serves its own sitemap under its basePath; this umbrella only needs to
 * know where they live, not how they are structured.
 */
const CHILD_SITEMAPS = ["/sitemap.xml", "/docs/sitemap.xml", "/blog/sitemap.xml"];

export function GET() {
    const entries = CHILD_SITEMAPS.map(
        (path) => `
        <sitemap>
            <loc>${new URL(path, SITE_URL).toString()}</loc>
        </sitemap>`,
    ).join("\n");

    const xml =
    `<?xml version="1.0" encoding="UTF-8"?>
        <sitemapindex xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            ${entries}
        </sitemapindex>
    `;

    return new Response(xml, {
        headers: { "Content-Type": "application/xml; charset=utf-8" },
    });
}
