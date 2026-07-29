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
import { getAuthorName } from "@/helpers";
import { getAuthors } from "@/lib/authors";

import { Avatar } from "./Avatar";
import styles from "./styles.module.css";

interface IProps {
    authors: string[];
}

export const Authors = ({ authors }: IProps) => {
    const authorsList = getAuthors(authors);
    const names = authorsList.map(({ name }) => name);
    const authorName = getAuthorName(names);
    const visibleAuthors = authorsList.slice(0, 3)

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={styles.AvatarsWrapper}>
                    {visibleAuthors.map(({ name, slug }) => (
                        <Avatar key={slug} authorRef={name} />
                    ))}
                </span>
                <span className={styles.AuthorName}>{authorName}</span>
            </div>
        </>
    );
};
