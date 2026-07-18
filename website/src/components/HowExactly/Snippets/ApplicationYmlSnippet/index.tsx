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
import { CodeBlock } from "../CodeBlock";
import styles from "../shared.module.css";

export const ApplicationYmlSnippet = () => {
    return (
        <CodeBlock fileName="application.yml" tag="misconfigured">
            <pre className={styles.Snippet}>
                <code>
                    <span className={styles.Line}>spring:</span>
                    <span className={styles.Line}>{"  jpa:"}</span>
                    <span className={styles.Line}>
                        {"    open-in-view: "}
                        <span className={styles.Keyword}>true</span>
                    </span>
                    <span className={styles.Line}>
                        {"    show-sql: "}
                        <span className={styles.Keyword}>true</span>
                    </span>
                    <br />
                    <span className={styles.Line}>management:</span>
                    <span className={styles.Line}>{"  endpoints:"}</span>
                    <span className={styles.Line}>{"    web:"}</span>
                    <span className={styles.Line}>{"      exposure:"}</span>
                    <span className={styles.Line}>
                        {"        include: "}
                        <span className={styles.Keyword}>{'"*"'}</span>
                    </span>
                </code>
            </pre>
        </CodeBlock>
    );
};
