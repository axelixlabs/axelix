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
import { ReactNode, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    /**
     * The very short blurb that explains what exactly is the problem.
     */
    title: ReactNode;

    /**
     * The description that explains why exactly it is a problem.
     */
    description: ReactNode;
}

export const Problem = ({ title, description }: IProps) => {
    const [open, setOpen] = useState(false);

    return (
        <li className={styles.Item}>
            <button
                type="button"
                className={styles.Header}
                onClick={() => setOpen((prev) => !prev)}
                aria-expanded={open}
            >
                <span className={styles.Title}>{title}</span>
                <span className={`${styles.Chevron} ${open ? styles.Open : ""}`} aria-hidden="true" />
            </button>
            {open && <div className={styles.Description}>{description}</div>}
        </li>
    );
};
