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
import { IArticle } from "@/models";

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
    articles: IArticle[];
}

export const BlogHomeClient = ({ articles }: IProps) => {
    const searchParams = useSearchParams();

    const allTagsFromArticles = articles.flatMap(({ tags }) => tags);
    const uniqueTags = new Set(allTagsFromArticles);
    const uniqueTagsArray = Array.from(uniqueTags);
    const allTags = uniqueTagsArray.sort();

    const tagFromUrl = searchParams.get("tag") ?? "";
    const currentTag = allTags.includes(tagFromUrl) ? tagFromUrl : SHOW_ALL;

    const filteredArticles =
        currentTag === SHOW_ALL ? articles : articles.filter(({ tags }) => tags.includes(currentTag));

    const isAllTagsSelected = currentTag === SHOW_ALL;
    const totalPages = Math.max(1, Math.ceil(filteredArticles.length / PAGE_SIZE));
    const currentPage = Math.max(1, Math.min(parsePage(searchParams.get("page")), totalPages));

    const showFeatured = isAllTagsSelected && currentPage === 1 && filteredArticles.length;
    const featured = showFeatured ? filteredArticles[0] : undefined;
    const posts = showFeatured
        ? filteredArticles.slice(1, PAGE_SIZE)
        : filteredArticles.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

    const showMeta = currentTag !== SHOW_ALL;

    return (
        <>
            <Toolbar currentTag={currentTag} tags={allTags} />
            <main className={styles.Feed}>
                <div className="wrap">
                    {showMeta && <BlogMeta filteredArticles={filteredArticles} currentTag={currentTag} />}

                    {!filteredArticles.length ? (
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
