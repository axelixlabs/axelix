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
import { GITHUB_URL } from "../../../utils";

import styles from "./styles.module.css";

export const Rules = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className="MainContainer">
                <div className={styles.Card}>
                    <div className={styles.Text}>
                        <span className={styles.Title}>Same rules in every channel</span>
                        <span className={styles.Description}>
                            We follow the Axelix Code of Conduct everywhere. Report security issues privately, never in
                            public issues.
                        </span>
                    </div>
                    <div className={styles.Links}>
                        <a
                            href={`${GITHUB_URL}/blob/master/CODE_OF_CONDUCT.md`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className={styles.Link}
                        >
                            Code of Conduct
                        </a>
                        <a
                            href={`${GITHUB_URL}/blob/master/SECURITY.md`}
                            target="_blank"
                            rel="noopener noreferrer"
                            className={styles.Link}
                        >
                            Security policy
                        </a>
                    </div>
                </div>
            </div>
        </section>
    );
};
