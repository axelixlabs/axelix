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

import { CheckIcon, LinkIcon, LinkedinIcon, XTwitterIcon } from "@/assets";

import { useState } from "react";

interface IProps {
    /** Absolute canonical URL of the post. */
    url: string;
    title: string;
}

export const BlogShare = ({ url, title }: IProps) => {
    const [copied, setCopied] = useState(false);

    const x = `https://x.com/intent/tweet?text=${encodeURIComponent(title)}&url=${encodeURIComponent(url)}`;
    const linkedin = `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`;

    const copy = () => {
        navigator.clipboard
            ?.writeText(url)
            .then(() => {
                setCopied(true);
                setTimeout(() => setCopied(false), 1400);
            })
            .catch(() => {});
    };

    return (
        <div className="share">
            <a href={x} target="_blank" rel="noopener noreferrer">
                <XTwitterIcon />
            </a>
            <a href={linkedin} target="_blank" rel="noopener noreferrer">
                <LinkedinIcon />
            </a>
            <button type="button" onClick={copy}>
                {copied ? <CheckIcon /> : <LinkIcon />}
            </button>
        </div>
    );
};
