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
"use client";

import { PAGE_SIZE } from "@/lib/pagination";
import { SHOW_ALL } from "@/lib/tags";
import { IBlogCardItem } from "@/models";

import { useSearchParams } from "next/navigation";

import { Pagination } from "../Pagination";
import { PostRow } from "../PostRow";
import { Toolbar } from "../Toolbar";

import { BlogMeta } from "./BlogMeta";
import { FeaturedPost } from "./FeaturedPost";
import styles from "./styles.module.css";

function parsePage(value: string | null): number {
    const parsed = Number.parseInt(value ?? "1", 10);
    return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed;
}

interface IProps {
    items: IBlogCardItem[];
}

export const BlogHomeClient = ({ items }: IProps) => {
    const searchParams = useSearchParams();

    // Tags are open: the filter vocabulary is just the union of tags across posts.
    const allTags = Array.from(new Set(items.flatMap((item) => item.tags))).sort();

    const tagParam = searchParams.get("tag") ?? "";
    const currentTag = allTags.includes(tagParam) ? tagParam : SHOW_ALL;

    const byTag = currentTag === SHOW_ALL ? items : items.filter((item) => item.tags.includes(currentTag));

    const isDefault = currentTag === SHOW_ALL;
    const totalPages = Math.max(1, Math.ceil(byTag.length / PAGE_SIZE));
    const currentPage = Math.max(1, Math.min(parsePage(searchParams.get("page")), totalPages));

    const showFeatured = isDefault && currentPage === 1 && byTag.length > 0;
    const featured = showFeatured ? byTag[0] : undefined;
    const posts = showFeatured
        ? byTag.slice(1, PAGE_SIZE)
        : byTag.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

    const showMeta = currentTag !== SHOW_ALL;

    return (
        <>
            <Toolbar currentTag={currentTag} tags={allTags} />
            <main className={styles.Feed}>
                <div className="wrap">
                    {showMeta && <BlogMeta byTag={byTag} currentTag={currentTag} />}

                    {byTag.length ? (
                        <div className={styles.Empty}>
                            <b>No articles found</b>
                            Nothing here yet. Try another topic.
                        </div>
                    ) : (
                        <div className={styles.VIndex}>
                            {featured && <FeaturedPost item={featured} />}
                            <div className={styles.Rowlist}>
                                {posts.map((item) => (
                                    <PostRow key={item.slug} item={item} />
                                ))}
                            </div>
                            <Pagination tag={currentTag} currentPage={currentPage} totalPages={totalPages} />
                        </div>
                    )}
                </div>
            </main>
        </>
    );
};
