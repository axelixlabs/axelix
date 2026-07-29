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
import { ReactNode, isValidElement } from "react";

/** Recursively extracts the text of a heading's children (for the slug id). */
function extractText(node: ReactNode): string {
    if (typeof node === "string") {
        return node;
    }

    if (typeof node === "number") {
        return String(node);
    }

    if (Array.isArray(node)) {
        return node.map(extractText).join("");
    }

    if (isValidElement(node)) {
        return extractText((node.props as { children?: ReactNode }).children);
    }

    return "";
}

export const ArticleHeading2 = ({ id, children }: { id?: string; children?: ReactNode }) => {
    const providedId = typeof id === "string" ? id : "";

    const resolvedId =
        providedId ||
        extractText(children)
            .trim()
            .toLowerCase()
            .replace(/\s+/g, "-")
            .replace(/[^a-z0-9-]/g, "")
            .replace(/-+/g, "-")
            .replace(/^-|-$/g, "");

    return (
        <h2 id={resolvedId} className="group flex scroll-mt-28 items-center gap-2">
            <a href={`#${resolvedId}`}>{children}</a>
        </h2>
    );
};
