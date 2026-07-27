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
import { formatDate } from "@/lib/format";
import { computeReadingTime } from "@/lib/reading-time";
import { BlogPage } from "@/lib/source";

import { Authors } from "../Authors";
import { PlainTag } from "../PlainTag";
import { ReadingTime } from "../ReadingTime";

interface IProps {
    data: BlogPage["data"];
}

export const ArticleHeader = async ({ data }: IProps) => {
    const { title, description, authors, tags, date } = data;

    const raw = await data.getText("raw");
    const readingMinutes = computeReadingTime(raw);

    return (
        <>
            <header className="art-hero">
                <h1>{title}</h1>
                {description && <p className="standfirst">{description}</p>}
                <div className="art-meta">
                    <Authors authors={authors} />
                    <span className="sep" />
                    <span className="m">{formatDate(date)}</span>
                    <span className="sep" />
                    <ReadingTime minutes={readingMinutes} className="m" />
                </div>
                <div className="rtags">
                    {(tags ?? []).map((tag) => (
                        <PlainTag label={tag} href={`/?tag=${encodeURIComponent(tag)}`} key={tag} />
                    ))}
                </div>
            </header>
        </>
    );
};
