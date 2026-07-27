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
import { PAGE_SIZE } from "@/lib/pagination";
import type { IBlogCardItem } from "@/lib/source";
import { SHOW_ALL } from "@/lib/tags";
import { IFeaturedAndPosts } from "@/models";

import type { CSSProperties } from "react";

export const chipColorStyle = (color: string): CSSProperties => {
    return { ["--chip" as string]: color };
};

const parsePageFromUrl = (value: string | null): number => {
    const defaultPage = 1;
    const parsedValue = Number.parseInt(value ?? String(defaultPage), 10);

    const isInvalidPage = Number.isNaN(parsedValue) || parsedValue < defaultPage;

    if (isInvalidPage) {
        return defaultPage;
    }

    return parsedValue;
};

export const getAllTags = (items: IBlogCardItem[]): string[] => {
    const allTags = items.flatMap(({ tags }) => tags);
    const uniqueTags = new Set(allTags);

    return Array.from(uniqueTags).sort();
};

export const getCurrentTag = (searchParams: URLSearchParams, allTags: string[]): string => {
    const tagFromUrl = searchParams.get("tag") ?? "";
    const tagExists = allTags.includes(tagFromUrl);

    if (tagExists) {
        return tagFromUrl;
    }

    return SHOW_ALL;
};

export const filterByTag = (items: IBlogCardItem[], currentTag: string): IBlogCardItem[] => {
    const isShowAll = currentTag === SHOW_ALL;

    if (isShowAll) {
        return items;
    }

    return items.filter(({ tags }) => tags.includes(currentTag));
};

export const getCurrentPage = (searchParams: URLSearchParams, totalPages: number): number => {
    const pageFromUrl = parsePageFromUrl(searchParams.get("page"));
    const clampedToMax = Math.min(pageFromUrl, totalPages);

    return Math.max(1, clampedToMax);
};

export const getFeaturedAndPosts = (
    byTag: IBlogCardItem[],
    isDefault: boolean,
    currentPage: number,
): IFeaturedAndPosts => {
    const showFeatured = isDefault && currentPage === 1 && byTag.length > 0;

    if (showFeatured) {
        return {
            featured: byTag[0],
            posts: byTag.slice(1, PAGE_SIZE),
        };
    }

    return {
        featured: undefined,
        posts: byTag.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE),
    };
};
