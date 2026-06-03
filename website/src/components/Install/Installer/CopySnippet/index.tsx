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
import { CheckIcon, CopyIcon } from "@/assets";

import { useRef, useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    activeSnippetRef: any;
}

export const CopySnippet = ({ activeSnippetRef }: IProps) => {
    const [copied, setCopied] = useState(false);
    const copyTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

    async function copyMain() {
        const el = activeSnippetRef.current;
        if (!el) return;
        const text = (el.innerText || "").replace(/ /g, " ");
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            if (copyTimer.current) clearTimeout(copyTimer.current);
            copyTimer.current = setTimeout(() => setCopied(false), 1200);
        } catch {
            /* clipboard blocked */
        }
    }

    return (
        <div className={styles.MainWrapper}>
            <button className={styles.CopyFloat} type="button" title="Copy" onClick={copyMain}>
                {copied ? (
                    <>
                        <CheckIcon />
                        <span>Copied!</span>
                    </>
                ) : (
                    <>
                        <CopyIcon />
                        <span>Copy</span>
                    </>
                )}
            </button>
        </div>
    );
};
