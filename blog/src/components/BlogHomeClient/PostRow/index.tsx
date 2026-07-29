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
import type { IBlogCardItem } from "@/models";

import Image from "next/image";
import Link from "next/link";

import { Authors } from "../../Authors";
import { DateMeta } from "../../DateMeta";
import { TagRow } from "../../TagRow";

import styles from "./styles.module.css";

interface IProps {
    item: IBlogCardItem;
}

export const PostRow = ({ item }: IProps) => {
    const { authors, coverSrc, date, description, href, readingMinutes, tags, title } = item;

    return (
        <>
            <Link className={styles.MainWrapper} href={href}>
                <div className={styles.ContentWrapper}>
                    <TagRow tags={tags} />
                    <DateMeta date={date} readingMinutes={readingMinutes} />
                    <h3 className={styles.Title}>{title}</h3>
                    {description && <p className={styles.Description}>{description}</p>}
                    <Authors authors={authors} />
                </div>

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
            </Link>
        </>
    );
};
