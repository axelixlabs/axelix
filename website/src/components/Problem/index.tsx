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
import { IncidentCard } from "./IncidentCard";
import { ProblemRightCards } from "./ProblemRightCards";
import { ProblemTopSection } from "./ProblemTopSection";
import styles from "./styles.module.css";

export const Problem = () => {
    return (
        <section className={styles.MainWrapper} id="problem">
            <div className="MainContainer">
                <ProblemTopSection />

                <div className={styles.ContentWrapper}>
                    <IncidentCard />
                    <ProblemRightCards />
                </div>

                <p className={styles.SectionFooterText}>
                    Every other tool was built to <em className={`AccentText ${styles.AccentText}`}>watch</em>{" "}
                    production. <b className={styles.BoldedText}>Axelix was built to operate it </b>
                    from a browser, or from an AI agent, with the same auth, the same audit trail.
                </p>
            </div>
        </section>
    );
};
