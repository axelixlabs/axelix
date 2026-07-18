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

export const HeroHeadline = () => {
    return (
        <>
            <h1 className={styles.Title}>
                <h1 className={styles.Title}>
                    Make sure your <span className={styles.GreenText}>Java</span> microservices are ready for the{" "}
                    <span className={styles.GreenText}>AI</span> era.
                </h1>
            </h1>

            <p className={styles.Lede}>
                The <span className={styles.GreenTextBold}>AI-Native</span>, Open Core solution for the AI era: its
                open-source core <span className={styles.GreenTextBold}>pinpoints</span> what holds your Java back,
                while Axelix Enterprise <span className={styles.GreenTextBold}>enforces</span> optimization policies
                across the whole ecosystem. <span className={styles.GreenTextBold}>At scale</span>.
            </p>
        </>
    );
};
