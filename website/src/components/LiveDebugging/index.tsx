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
import { SectionLabel } from "../SectionLabel";

import { AgentSession } from "./AgentSession";
import styles from "./styles.module.css";

const CAPABILITIES = [
    "Inspect beans, effective configuration, transactions, threads and GC of the running service",
    "Change log levels and caches live, without a redeploy",
    "Give AI agents the same view through the built-in MCP server",
];

export const LiveDebugging = () => {
    return (
        <section className={styles.MainWrapper} id="debug">
            <div className={`MainContainer ${styles.Inner}`}>
                <div>
                    <SectionLabel index="03" label="Live debugging with AI agents" />
                    <h2 className={styles.Title}>Debug the live service. Cut mean time to recovery.</h2>
                    <p className={styles.Description}>
                        When an incident hits, look inside the running application: beans, effective configuration, live
                        transactions, loggers, thread dumps and GC. Engineers use the web console; AI agents get the
                        same data through the embedded MCP server, under one role model.
                    </p>
                    <ul className={styles.List}>
                        {CAPABILITIES.map((capability) => (
                            <li key={capability} className={styles.Item}>
                                <span className={styles.Check}>✓</span>
                                <span>{capability}</span>
                            </li>
                        ))}
                    </ul>
                </div>
                <AgentSession />
            </div>
        </section>
    );
};
