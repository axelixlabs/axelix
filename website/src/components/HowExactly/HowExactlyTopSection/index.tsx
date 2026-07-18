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

export const HowExactlyTopSection = () => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Eyebrow}>How exactly?</span>
            <h2 className={styles.Title}>
                No magic. Axelix pinpoints the <span className={styles.AccentText}>real problems</span>, on various
                layers
            </h2>
            <p className={styles.Lead}>
                Every gain above starts with a concrete problem Axelix detected and surfaced: from the JVM up through
                the framework and into the data layer. The examples below are simplified, yet they generally reflect the
                real-world conditions based on our experience.
            </p>
        </div>
    );
};
