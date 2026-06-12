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
import { colorForTag } from "@/lib/tags";

import Link from "next/link";
import type { CSSProperties } from "react";

interface IProps {
    label: string;
    href?: string;
}

/** A tag chip. With `href` it renders as a link (e.g. to the filtered home);
 *  without one it stays a plain span (safe inside card-level links). */
export const PlainTag = ({ label, href }: IProps) => {
    const style: CSSProperties = { ["--cat" as string]: colorForTag(label) };
    if (href) {
        return (
            <Link href={href} className="tag tag-link" style={style}>
                {label}
            </Link>
        );
    }
    return (
        <span className="tag" style={style}>
            {label}
        </span>
    );
};
