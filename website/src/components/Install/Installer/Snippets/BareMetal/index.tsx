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

export const BareMetal = ({ refEl }: IProps) => {
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
                        # Important: Please, change the algorithm and the key for production use
                    </span>
                </span>
                <span className={styles.Line}>
                    <span className={styles.Cm}>java -jar master.jar</span> <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>--server.port=8080</span> <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>--axelix.master.auth.jwt.algorithm=HMAC256</span>{" "}
                    <span className={styles.Nl}>\</span>
                </span>
                <span className={styles.Line}>
                    {"    "}
                    <span className={styles.St}>
                        --axelix.master.auth.jwt.signing-key=8DrZJSOJ8vkbxdjUB3sSsyeiG4Xidf1sDNmJq1Slkkn
                    </span>
                </span>
            </code>
        </pre>
    );
};
