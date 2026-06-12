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
import { getAuthors } from "@/lib/authors";

import { Avatar } from "../Avatar";

interface IProps {
    authors: string[];
}

/** One or many authors — stacked avatars + a summarized name label. */
export const Authors = ({ authors }: IProps) => {
    const list = getAuthors(authors);
    const names = list.map((a) => a.name);
    const label =
        names.length === 1
            ? names[0]
            : names.length === 2
              ? `${names[0]}, ${names[1]}`
              : `${names[0]} +${names.length - 1}`;
    return (
        <div className="authors">
            <span className="avatars">
                {list.slice(0, 3).map((a) => (
                    <Avatar key={a.slug} authorRef={a.name} />
                ))}
            </span>
            <span className="who">{label}</span>
        </div>
    );
};
