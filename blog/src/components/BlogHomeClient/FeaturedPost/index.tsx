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
import { Authors, DateMeta, TagRow } from "@/components";
import type { IBlogCardItem } from "@/lib/source";

import Image from "next/image";
import Link from "next/link";

import styles from "./styles.module.css";

interface IProps {
    item: IBlogCardItem;
}

export const FeaturedPost = ({ item }: IProps) => {
    const { href, coverSrc, tags, title, description, authors, date, readingMinutes } = item;

    return (
        <>
            <Link className={styles.MainWrapper} href={href}>
                {coverSrc ? (
                    <div className={styles.CoverImageWrapper}>
                        <Image
                            src={coverSrc}
                            alt={title}
                            fill
                            sizes="(max-width: 760px) 100vw, 50vw"
                            priority
                            className={styles.CoverImage}
                        />
                    </div>
                ) : (
                    <div className={styles.CoverImagePlaceholder} />
                )}

                <div className={styles.ContentWrapper}>
                    <TagRow tags={tags} />
                    <DateMeta date={date} readingMinutes={readingMinutes} />
                    <h2 className={styles.PostTitle}>{title}</h2>
                    {description && <p className={styles.PostDescription}>{description}</p>}
                    <Authors authors={authors} />
                </div>
            </Link>
        </>
    );
};
