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
import { IAxelixVersionData } from "@/models";

import { AxelixVersion } from "../AxelixVersion";
import styles from "../shared.module.css";

interface IProps {
    refEl: any;
    axelixVersionData: IAxelixVersionData;
}

export const BareMetal = ({ refEl, axelixVersionData }: IProps) => {
    return (
        <pre
            className={styles.Snippet}
            ref={(el) => {
                refEl.current = el;
            }}
        >
            <code>
                <span className={styles.Line}>
                    <span className={styles.Co}>
                        #{" "}
                        <a href="https://github.com/axelixlabs/axelix/releases" className={styles.ClickableLink}>
                            Download
                        </a>{" "}
                        and run the Axelix Master JAR
                    </span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Co}>
                        # Uses the built-in JWT key: fine for a trial, set your own for production
                    </span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>
                        java -jar axelix-
                        <AxelixVersion axelixVersionData={axelixVersionData} />
                        .jar
                    </span>
                </span>
            </code>
        </pre>
    );
};
