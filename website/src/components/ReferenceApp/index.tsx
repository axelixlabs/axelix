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
import ReferenceAppTopSection from "./ReferenceAppTopSection";
import { ResultMetrics } from "./ResultMetrics";
import { ServiceStackCard } from "./ServiceStackCard";
import styles from "./styles.module.css";

export const ReferenceApp = () => {
    return (
        <section className={styles.MainWrapper} id="reference-app">
            <div className="MainContainer">
                <ReferenceAppTopSection />

                <div className={styles.ContentWrapper}>
                    <ServiceStackCard />
                    <ResultMetrics />
                </div>

                <p className={styles.ClosingText}>
                    No massive rewrite, no new framework.{" "}
                    <b className={styles.ClosingTextBold}>
                        Just apply the suggestions Axelix provides (either manually, or with AI Agents with the help of
                        an embedded MCP server)
                    </b>{" "}
                    to a regular Java Spring Boot application and watch the results.
                </p>
            </div>
        </section>
    );
};
