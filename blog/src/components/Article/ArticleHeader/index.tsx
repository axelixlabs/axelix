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
import { Authors, PlainTag, ReadingTime } from "@/components";
import { formatDate } from "@/lib/format";
import { computeReadingTime } from "@/lib/reading-time";
import { TBlogPage } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    data: TBlogPage["data"];
}

export const ArticleHeader = async ({ data }: IProps) => {
    const { title, description, authors, tags, date } = data;

    const raw = await data.getText("raw");
    const readingMinutes = computeReadingTime(raw);

    return (
        <>
            <header className={styles.MainWrapper}>
                <h1 className={styles.Title}>{title}</h1>
                {description && <p className={styles.Description}>{description}</p>}

                <div className={styles.Meta}>
                    <Authors authors={authors} />
                    <span className="DotSeparator" />
                    <span className={styles.Date}>{formatDate(date)}</span>
                    <span className="DotSeparator" />
                    <ReadingTime minutes={readingMinutes} className={styles.Date} />
                </div>

                <div className={styles.TagsWrapper}>
                    {(tags ?? []).map((tag) => (
                        <PlainTag label={tag} href={`/?tag=${encodeURIComponent(tag)}`} key={tag} />
                    ))}
                </div>
            </header>
        </>
    );
};
