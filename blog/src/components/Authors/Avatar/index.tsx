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

import Image from "next/image";
import { useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Author display name.
     */
    authorRef: string;
}

export const Avatar = ({ authorRef }: IProps) => {
    const { color, initials, name, slug } = getAuthor(authorRef);
    const candidates = authorImageCandidates(slug);
    const [index, setIndex] = useState<number>(0);

    if (index >= candidates.length) {
        return (
            <>
                <span className={styles.Avatar} style={{ backgroundColor: color }}>
                    {initials}
                </span>
            </>
        );
    }

    return (
        <>
            <Image
                width={35}
                height={35}
                className={styles.Avatar}
                src={withBlogBasePathForImageSrc(candidates[index])}
                alt={name}
                onError={() => setIndex((idx) => idx + 1)}
            />
        </>
    );
};
