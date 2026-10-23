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
import { SectionLabel } from "../../SectionLabel";

import styles from "./styles.module.css";

export const AboutHero = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className={`MainContainer ${styles.Inner}`}>
                <SectionLabel label="About" />
                <h1 className={styles.Title}>
                    Axelix is developed by <span className={styles.Accent}>Axelix Labs</span>.
                </h1>
                <p className={styles.Description}>
                    We&apos;re a small team of engineers and contributors to Spring modules. We&apos;ve spent years
                    inside Spring Boot services in production, and Axelix is the tool we kept wishing we had.
                </p>
            </div>
        </section>
    );
};
