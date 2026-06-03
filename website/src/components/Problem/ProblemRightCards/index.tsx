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

export const ProblemRightCards = () => {
    return (
        <aside className={styles.MainWrapper}>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>01 The observability gap</div>
                <h3 className={styles.CardSubtitle}>You can see a spike. You can&apos;t change a logger.</h3>
                <p className={styles.CardDescription}>
                    Metrics tell you something is wrong. They never let you reach into the JVM and adjust anything live.
                </p>
            </div>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>02 The access gap</div>
                <h3 className={styles.CardSubtitle}>Actuator is in every Spring Boot app and exposed in none.</h3>
                <p className={styles.CardDescription}>
                    The introspection is already there. It&apos;s locked behind a port nobody trusts to leave open.
                </p>
            </div>
            <div className={styles.Card}>
                <div className={styles.CardTitle}>03 The agent gap</div>
                <h3 className={styles.CardSubtitle}>
                    Your on-call AI can read the dashboard. It can&apos;t act on it.
                </h3>
                <p className={styles.CardDescription}>
                    Copilots ingest screenshots. They can&apos;t flip a logger, pull a dump, or rotate a pool.
                </p>
            </div>
        </aside>
    );
};
