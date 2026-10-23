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

export const ContactHero = () => {
    return (
        <section className={styles.MainWrapper}>
            <div className={`MainContainer ${styles.Inner}`}>
                <SectionLabel label="Contact" />
                <h1 className={styles.Title}>
                    Talk to the people who <span className={styles.Accent}>build Axelix</span>.
                </h1>
                <p className={styles.Description}>
                    Every message reaches the core team. Pick the right channel below and we&apos;ll get back to you
                    within a working day.
                </p>
            </div>
        </section>
    );
};
