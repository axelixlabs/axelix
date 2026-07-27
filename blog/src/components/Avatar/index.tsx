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

import { authorImageCandidates, getAuthor } from "@/lib/authors";
import { withBlogBasePathForImageSrc } from "@/lib/url";

import { useState } from "react";

interface IProps {
    /** Author display name. */
    authorRef: string;
}

/** Author avatar: tries the convention photos `public/authors/<slug>.{png,jpg,jpeg,svg}`
 *  in order, falling back to an initials circle once none load (no broken images). */
export const Avatar = ({ authorRef }: IProps) => {
    const a = getAuthor(authorRef);
    const candidates = authorImageCandidates(a.slug);
    const [idx, setIdx] = useState(0);

    if (idx >= candidates.length) {
        return (
            <span className="avatar" style={{ background: a.color }}>
                {a.initials}
            </span>
        );
    }

    return (
        // eslint-disable-next-line @next/next/no-img-element
        <img
            className="avatar"
            src={withBlogBasePathForImageSrc(candidates[idx])}
            alt={a.name}
            onError={() => setIdx((i) => i + 1)}
        />
    );
};
