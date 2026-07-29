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
import { SHOW_ALL, getColorForTag } from "@/lib/tags";
import { DEFAULT_CHIP_STYLE } from "@/utils";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    tags: string[];
    currentTag: string;
}

export const TagSelect = ({ tags, currentTag }: IProps) => {
    const [open, setOpen] = useState<boolean>(false);
    const selectRef = useRef<HTMLDivElement>(null);

    const options = [
        { id: SHOW_ALL, label: "All", href: "/" },
        ...tags.map((tag) => ({
            id: tag,
            label: tag,
            href: `/?tag=${encodeURIComponent(tag)}`,
        })),
    ];

    const current = currentTag === SHOW_ALL ? { id: SHOW_ALL, label: "All" } : { id: currentTag, label: currentTag };

    useEffect(() => {
        if (!open) {
            return;
        }

        function onPointerDown(e: MouseEvent): void {
            const target = e.target as Node;

            if (selectRef.current && !selectRef.current.contains(target)) {
                setOpen(false);
            }
        }

        function onKeyDown(e: KeyboardEvent): void {
            if (e.key === "Escape") {
                setOpen(false);
            }
        }

        document.addEventListener("mousedown", onPointerDown);
        document.addEventListener("keydown", onKeyDown);

        return () => {
            document.removeEventListener("mousedown", onPointerDown);
            document.removeEventListener("keydown", onKeyDown);
        };
    }, [open]);

    return (
        <>
            <div className={`${styles.MainWrapper}${open ? ` ${styles.Open}` : ""}`} ref={selectRef}>
                <button
                    type="button"
                    className={styles.SelectOpenTrigger}
                    onClick={() => setOpen((value) => !value)}
                    style={current.id === SHOW_ALL ? DEFAULT_CHIP_STYLE : chipColorStyle(getColorForTag(current.id))}
                >
                    <span className={styles.ChipDot} />
                    <span className={styles.Label}>{current.label}</span>
                    <ChevronIcon />
                </button>

                {open && (
                    <div role="menu">
                        {options.map(({ id, label, href }) => (
                            <Link
                                key={id}
                                href={href}
                                role="menuitem"
                                className={`${styles.SelectOption}${current.id === id ? styles.Active : ""}`}
                                style={id === SHOW_ALL ? DEFAULT_CHIP_STYLE : chipColorStyle(getColorForTag(id))}
                                onClick={() => setOpen(false)}
                            >
                                <span className={styles.ChipDot} />
                                <span className={styles.Lbl}>{label}</span>
                            </Link>
                        ))}
                    </div>
                )}
            </div>
        </>
    );
};
