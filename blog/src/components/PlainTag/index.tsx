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
import { chipColorStyle } from "@/helpers";
import { getColorForTag } from "@/lib/tags";

import Link from "next/link";
import type { CSSProperties } from "react";

import styles from "./styles.module.css";

interface IProps {
    label: string;
    href?: string;
}

export const PlainTag = ({ label, href }: IProps) => {
    const style: CSSProperties = chipColorStyle(getColorForTag(label));

    if (href) {
        return (
            <>
                <Link href={href} className={`${styles.Tag} ${styles.TagLink}`} style={style}>
                    {label}
                </Link>
            </>
        );
    }

    return (
        <>
            <span className={styles.Tag} style={style}>
                {label}
            </span>
        </>
    );
};
