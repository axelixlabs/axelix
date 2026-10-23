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
import { BLOG_URL, DOCS_URL, GET_STARTED_HREF, GITHUB_URL } from "../../../utils";

import styles from "./styles.module.css";

interface IProps {
    onLinkClick?: () => void;
    fullWidth?: boolean;
}

export const ExternalLinks = ({ onLinkClick, fullWidth }: IProps) => {
    const linkClassName = fullWidth ? styles.LinkFullWidth : "";

    return (
        <div className={`${styles.MainWrapper} ${fullWidth ? styles.MainWrapperFullWidth : ""}`}>
            {/* The burger menu already lists Docs and Blog under Resources. */}
            {!fullWidth && (
                <>
                    <a
                        href={DOCS_URL}
                        className={styles.DocsLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        onClick={onLinkClick}
                    >
                        Docs
                    </a>
                    <a
                        href={BLOG_URL}
                        className={styles.DocsLink}
                        target="_blank"
                        rel="noopener noreferrer"
                        onClick={onLinkClick}
                    >
                        Blog
                    </a>
                </>
            )}
            <a
                href={GITHUB_URL}
                target="_blank"
                rel="noopener noreferrer"
                className={`${styles.GitHubLink} ${linkClassName}`}
                onClick={onLinkClick}
            >
                ★ GitHub
            </a>
            <a href={GET_STARTED_HREF} className={`${styles.GetStartedLink} ${linkClassName}`} onClick={onLinkClick}>
                Get started
            </a>
        </div>
    );
};
