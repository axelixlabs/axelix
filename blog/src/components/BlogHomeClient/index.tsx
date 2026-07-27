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
import { filterByTag, getAllTags, getCurrentPage, getCurrentTag, getFeaturedAndPosts } from "@/helpers";
import { PAGE_SIZE } from "@/lib/pagination";
import type { IBlogCardItem } from "@/lib/source";
import { SHOW_ALL } from "@/lib/tags";

import { useSearchParams } from "next/navigation";

import { BlogMeta } from "./BlogMeta";
import { FeaturedPost } from "./FeaturedPost";
import { Pagination } from "./Pagination";
import { PostRow } from "./PostRow";
import { Toolbar } from "./Toolbar";
import styles from "./styles.module.css";

interface IProps {
    items: IBlogCardItem[];
}

export const BlogHomeClient = ({ items }: IProps) => {
    const searchParams = useSearchParams();

    const allTags = getAllTags(items);
    const currentTag = getCurrentTag(searchParams, allTags);
    const byTag = filterByTag(items, currentTag);

    const isDefault = currentTag === SHOW_ALL;
    const totalPages = Math.max(1, Math.ceil(byTag.length / PAGE_SIZE));
    const currentPage = getCurrentPage(searchParams, totalPages);

    const { featured, posts } = getFeaturedAndPosts(byTag, isDefault, currentPage);

    return (
        <>
            <Toolbar currentTag={currentTag} tags={allTags} />
            <main className={styles.PostsWrapper}>
                <div className="wrap">
                    <BlogMeta byTag={byTag} currentTag={currentTag} />

                    {byTag.length === 0 ? (
                        <div className={styles.EmptyWrapper}>
                            <b className={styles.EmptyTitle}>No articles found</b>
                            Nothing here yet. Try another topic.
                        </div>
                    ) : (
                        <div>
                            {featured && <FeaturedPost item={featured} />}

                            <div className={styles.PostsWrapper}>
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
