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
import { GithubIcon } from "@/assets";
import { DOCS_URL, GITHUB_URL } from "@/utils";

import Link from "next/link";

import styles from "./styles.module.css";

interface IProps {
    onLinkClick?: () => void;
    fullWidth?: boolean;
}

export const ExternalLinks = ({ onLinkClick, fullWidth }: IProps) => {
    const linkClassName = fullWidth ? styles.LinkFullWidth : "";

    return (
        <div className={`${styles.MainWrapper} ${fullWidth ? styles.MainWrapperFullWidth : ""}`}>
            <a
                href={DOCS_URL}
                className={`${styles.DocsLink} ${linkClassName}`}
                target="_blank"
                rel="noopener noreferrer"
                onClick={onLinkClick}
            >
                Docs
            </a>
            <Link href="/" className={`${styles.BlogLink} ${linkClassName}`}>
                Blog
            </Link>
            <a
                href={GITHUB_URL}
                target="_blank"
                rel="noopener noreferrer"
                className={`${styles.GitHubIcon} ${linkClassName}`}
                onClick={onLinkClick}
            >
                <GithubIcon width="11" height="11" />
                GitHub
            </a>
        </div>
    );
};
