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

export const OrderServiceSnippet = () => {
    return (
        <CodeBlock fileName="OrderService.java" tag="transaction issue">
            <pre className={styles.Snippet}>
                <code>
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>@Transactional</span>
                    </span>
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>public</span> List&lt;OrderDto&gt; recent() {"{"}
                    </span>
                    <span className={styles.Line}>
                        {"  "}
                        <span className={styles.Keyword}>return</span> repo.findAll()
                    </span>
                    <span className={styles.Line}>{"    .stream()"}</span>
                    <span className={styles.Line}>{"    .map(o -> o.getItems()"}</span>
                    <span className={styles.Line}>{"               .size())"}</span>
                    <span className={styles.Line}>{"    .toList();"}</span>
                    <span className={styles.Line}>{"}"}</span>
                </code>
            </pre>
        </CodeBlock>
    );
};
