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
import { CheckMarkIcon, CopyIcon, LinkedinIcon, XIcon } from "@/assets";
import { BackLink } from "@/components";

import { useState } from "react";

import styles from "./styles.module.css";

interface IProps {
    url: string;
    title: string;
}

export const ArticleFooter = ({ url, title }: IProps) => {
    const [copied, setCopied] = useState<boolean>(false);

    const x = `https://x.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`;
    const linkedin = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;

    const copy = (): void => {
        navigator.clipboard?.writeText(url).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        });
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <BackLink text=" All articles" />

                <div className={styles.ShareWrapper}>
                    <a href={x} target="_blank" rel="noopener noreferrer" className={styles.ShareEntity}>
                        <XIcon />
                    </a>
                    <a href={linkedin} target="_blank" rel="noopener noreferrer" className={styles.ShareEntity}>
                        <LinkedinIcon />
                    </a>
                    <button type="button" onClick={copy} className={styles.ShareEntity}>
                        {copied ? <CopyIcon /> : <CheckMarkIcon />}
                    </button>
                </div>
            </div>
        </>
    );
};
