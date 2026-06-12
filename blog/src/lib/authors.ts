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
import { IAuthor } from "@/models";

/** Avatar image formats tried, in order, for `public/authors/<slug>.<ext>`. */
export const AUTHOR_IMAGE_EXTENSIONS = ["png", "jpg", "jpeg", "svg"] as const;

/** Convention avatar paths to try in order; the first that loads wins (see `Avatar`). */
export function authorImageCandidates(slug: string): string[] {
    return AUTHOR_IMAGE_EXTENSIONS.map((ext) => `/authors/${slug}.${ext}`);
}

const FALLBACK_COLORS = ["#639922", "#2A6FDB", "#7c5cd6", "#1f8f6a", "#c98a1f"];

function hash(str: string): number {
    let h = 0;
    for (let i = 0; i < str.length; i += 1) h = (h * 31 + str.charCodeAt(i)) >>> 0;
    return h;
}

function initialsFromName(name: string): string {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return "?";
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

/** Normalize a display name to a comparable, ascii-ish key (mirrors Prisma). */
function normalizeAuthorName(name: string): string {
    const latinized = name.replace(/[øØ]/g, "o").replace(/[æÆ]/g, "ae").replace(/[œŒ]/g, "oe").replace(/[ß]/g, "ss");

    return latinized
        .normalize("NFD")
        .replace(/[̀-ͯ]/g, "") // strip combining diacritics
        .replace(/['‘’]/g, "") // drop apostrophes
        .replace(/[^a-zA-Z0-9\s-]/g, " ")
        .trim()
        .toLowerCase()
        .replace(/\s+/g, " ");
}

function toAuthorSlug(name: string): string {
    return normalizeAuthorName(name).replace(/\s+/g, "-");
}

/** Resolve a display name to a full {@link IAuthor} (all fields derived). */
export function getAuthor(name: string): IAuthor {
    const slug = toAuthorSlug(name);
    return {
        name,
        slug,
        initials: initialsFromName(name),
        color: FALLBACK_COLORS[hash(slug || name) % FALLBACK_COLORS.length],
    };
}

/** Resolve a list of names, de-duplicated by normalized name. */
export function getAuthors(names: string[]): IAuthor[] {
    const seen = new Set<string>();
    return names
        .filter(Boolean)
        .map((name) => name.trim())
        .filter((name) => {
            const normalized = normalizeAuthorName(name);
            if (seen.has(normalized)) return false;
            seen.add(normalized);
            return true;
        })
        .map(getAuthor);
}
