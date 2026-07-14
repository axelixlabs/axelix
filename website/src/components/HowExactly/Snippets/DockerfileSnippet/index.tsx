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

export const DockerfileSnippet = () => {
    return (
        <CodeBlock fileName="Dockerfile" tag="suboptimal">
            <pre className={styles.Snippet}>
                <code>
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>FROM</span> eclipse-temurin:25-jdk
                    </span>
                    <br />
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>WORKDIR</span> /app
                    </span>
                    <br />
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>COPY</span> target/orders.jar app.jar
                    </span>
                    <br />
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>ENV </span> JAVA_OPTS=&quot;-Xmx512m -X...&quot;
                    </span>
                    <br />
                    <span className={styles.Line}>
                        <span className={styles.Keyword}>ENTRYPOINT </span> [&quot;sh&quot;, &quot;-c&quot;,
                        <span>{' "java $JAVA_OPTS -jar app.jar"]'}</span>
                    </span>
                </code>
            </pre>
        </CodeBlock>
    );
};
