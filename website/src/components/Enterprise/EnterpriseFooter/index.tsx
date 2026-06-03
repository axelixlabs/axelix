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

export const EnterpriseFooter = () => {
    return (
        <div className={styles.MainWrapper}>
            <div>
                <span className={styles.Eyebrow}>Early access · Q3 2026</span>
                <h3 className={styles.Title}>Want a seat in the first pilots?</h3>
                <p className={styles.Description}>
                    We&apos;re picking a handful of teams to shape what ships first. Tell us about your fleet —
                    we&apos;ll be in touch within a working day.
                </p>
            </div>
            <a href="mailto:enterprise@axelix.io" className={styles.Button}>
                enterprise@axelix.io <span className={styles.Arrow}>→</span>
            </a>
        </div>
    );
};
