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
import styles from "../shared.module.css";

interface IProps {
    refEl: any;
}

export const K8sYamlSnippet = ({ refEl }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.At}>axelix</span>:
                </span>
                <span className={styles.Line}>
                    {"  "}
                    <span className={styles.At}>sbs</span>:
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.At}>auth</span>:
                </span>
                <span className={styles.Line}>
                    {"      "}
                    <span className={styles.At}>jwt</span>:
                </span>
                <span className={styles.Line}>
                    {"        "}
                    <span className={styles.At}>algorithm</span>: <span className={styles.St}>HMAC256</span>
                </span>
                <span className={styles.Line}>
                    {"        "}
                    <span className={styles.At}>signing-key</span>:{" "}
                    <span className={styles.St}>8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn</span>
                </span>
            </code>
        </pre>
    );
};
