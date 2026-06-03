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

import styles from "./styles.module.css";

export const ExternalLinks = () => {
    return (
        <div className={styles.ExternalLinksWrapper}>
            <a
                href="https://axelix.io/docs/product/introduction"
                className={styles.ExternalLink}
                target="_blank"
                rel="noopener noreferrer"
            >
                Docs
            </a>
            <a
                href="https://github.com/axelixlabs/axelix"
                target="_blank"
                rel="noopener noreferrer"
                className={styles.GitHubIcon}
            >
                <GithubIcon width="11" height="11" />
                GitHub
            </a>
        </div>
    );
};
