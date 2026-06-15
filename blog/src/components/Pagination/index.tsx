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
import { ChevronLeftIcon, ChevronRightIcon } from "@/assets";
import { getPaginationSequence } from "@/lib/pagination";
import { SHOW_ALL } from "@/lib/tags";

import Link from "next/link";

import styles from "./styles.module.css";

function hrefFor(tag: string, page: number): string {
    const params = new URLSearchParams();
    if (tag !== SHOW_ALL) params.set("tag", tag);
    if (page > 1) params.set("page", String(page));
    const query = params.toString();
    return query ? `/?${query}` : "/";
}

interface IProps {
    tag: string;
    currentPage: number;
    totalPages: number;
}

export const Pagination = ({ tag, currentPage, totalPages }: IProps) => {
    if (totalPages <= 1) return null;
    const sequence = getPaginationSequence(totalPages, currentPage);
    const prevDisabled = currentPage <= 1;
    const nextDisabled = currentPage >= totalPages;

    return (
        <nav className={styles.Pager}>
            {prevDisabled ? (
                <span className={styles.PgArrow}>
                    <ChevronLeftIcon />
                    <span>Prev</span>
                </span>
            ) : (
                <Link className={styles.PgArrow} href={hrefFor(tag, currentPage - 1)}>
                    <ChevronLeftIcon />
                    <span>Prev</span>
                </Link>
            )}

            <div className={styles.PgNums}>
                {sequence.map((entry, index) =>
                    entry === "ellipsis" ? (
                        <span key={`e-${index}`} className={styles.PgEllipsis}>
                            …
                        </span>
                    ) : (
                        <Link
                            key={entry}
                            className={`${styles.PgNum}${entry === currentPage ? ` ${styles.Active}` : ""}`}
                            href={hrefFor(tag, entry)}
                        >
                            {String(entry).padStart(2, "0")}
                        </Link>
                    ),
                )}
            </div>

            {nextDisabled ? (
                <span className={styles.PgArrow}>
                    <span>Next</span>
                    <ChevronRightIcon />
                </span>
            ) : (
                <Link className={styles.PgArrow} href={hrefFor(tag, currentPage + 1)}>
                    <span>Next</span>
                    <ChevronRightIcon />
                </Link>
            )}
        </nav>
    );
};
