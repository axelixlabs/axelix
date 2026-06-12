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

import Link from "next/link";
import { useSearchParams } from "next/navigation";

import { Authors } from "../Authors";
import { DateMeta } from "../DateMeta";
import { Pagination } from "../Pagination";
import { PostRow } from "../PostRow";
import { TagRow } from "../TagRow";
import { Toolbar } from "../Toolbar";

import styles from "./styles.module.css";

function parsePage(value: string | null): number {
    const parsed = Number.parseInt(value ?? "1", 10);
    return Number.isNaN(parsed) || parsed < 1 ? 1 : parsed;
}

interface IFeaturedPostProps {
    item: IBlogCardItem;
}

const FeaturedPost = ({ item }: IFeaturedPostProps) => {
    return (
        <Link className={styles.Featured} href={item.href}>
            {item.coverSrc ? (
                <div className={styles.Cover}>
                    {/* eslint-disable-next-line @next/next/no-img-element */}
                    <img src={item.coverSrc} alt="" />
                </div>
            ) : (
                <div className={`${styles.Cover} ${styles.CoverPh}`} />
            )}
            <div className={styles.Body}>
                <TagRow tags={item.tags} />
                <DateMeta date={item.date} readingMinutes={item.readingMinutes} />
                <h2>{item.title}</h2>
                {item.description && <p>{item.description}</p>}
                <Authors authors={item.authors} />
            </div>
        </Link>
    );
};

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
                    {showMeta && (
                        <div className={styles.ResultMeta}>
                            <b>{byTag.length}</b> {byTag.length === 1 ? "article" : "articles"} tagged{" "}
                            <b>{currentTag}</b>
                        </div>
                    )}

                    {byTag.length === 0 ? (
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
