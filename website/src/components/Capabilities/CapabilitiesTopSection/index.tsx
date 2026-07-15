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
import styles from "./styles.module.css";

export const CapabilitiesTopSection = () => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <div>
                    <span className={styles.Eyebrow}>Capabilities</span>
                    <h2 className={styles.Title}>
                        Secure Spring Boot Diagnostics for both <span className={styles.GreenText}>Humans</span> &{" "}
                        <span className={styles.BlueText}>AI Agents</span>
                    </h2>
                </div>
            </div>
            <p className={styles.IntroText}>
                Axelix also allows you to look inside the living Spring Boot application - see its beans, properties,
                transactions, loggers and so on. Every capability listed below is exposed twice.{" "}
                <em className={`AccentText ${styles.AccentText}`}>Engineers</em> reach it through a web console.{" "}
                <em className={`AccentText ${styles.AccentText}`}>AI agents</em> get the same information through an
                embedded MCP server. A single role model gates both - each identity, human or agent, sees only the data
                and actions its role permits.
            </p>
        </>
    );
};
