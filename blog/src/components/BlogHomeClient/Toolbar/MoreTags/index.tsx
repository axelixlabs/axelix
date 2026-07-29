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
import { ChevronIcon } from "@/assets";
import { chipColorStyle } from "@/helpers";
import { getColorForTag } from "@/lib/tags";
import { VISIBLE_TAG_COUNT } from "@/utils";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    tags: string[];
    currentTag: string;
}

export const MoreTags = ({ tags, currentTag }: IProps) => {
    const [moreOpen, setMoreOpen] = useState<boolean>(false);
    const moreRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!moreOpen) {
            return;
        }

        function onPointerDown(e: MouseEvent): void {
            const target = e.target as Node;

            if (moreRef.current && !moreRef.current.contains(target)) {
                setMoreOpen(false);
            }
        }

        function onKeyDown(e: KeyboardEvent): void {
            if (e.key === "Escape") {
                setMoreOpen(false);
            }
        }

        document.addEventListener("mousedown", onPointerDown);
        document.addEventListener("keydown", onKeyDown);

        return () => {
            document.removeEventListener("mousedown", onPointerDown);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, [moreOpen]);

    const overflowTags = tags.slice(VISIBLE_TAG_COUNT);
    const activeOverflow = overflowTags.find((tag) => tag === currentTag);

    return (
        <>
            {!!overflowTags.length && (
                <div className={`${styles.MenuWrapper}${moreOpen ? ` ${styles.MenuOpen}` : ""}`} ref={moreRef}>
                    <button
                        className={`${styles.Chip} ${activeOverflow ? ` ${styles.Active}` : ""}`}
                        onClick={() => setMoreOpen((value) => !value)}
                        style={activeOverflow ? chipColorStyle(getColorForTag(activeOverflow)) : undefined}
                    >
                        {activeOverflow && <span className={styles.ChipDot} />}
                        {activeOverflow ?? "More"}
                        <ChevronIcon />
                    </button>

                    {moreOpen && (
                        <div className={styles.Menu} role="menu">
                            {overflowTags.map((tag) => (
                                <Link
                                    key={tag}
                                    href={`/?tag=${encodeURIComponent(tag)}`}
                                    role="menuitem"
                                    className={`${styles.MenuOption} ${currentTag === tag ? ` ${styles.Active}` : ""}`}
                                    style={chipColorStyle(getColorForTag(tag))}
                                    onClick={() => setMoreOpen(false)}
                                >
                                    <span className={styles.ChipDot} />
                                    <span className={styles.Lbl}>{tag}</span>
                                </Link>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </>
    );
};
