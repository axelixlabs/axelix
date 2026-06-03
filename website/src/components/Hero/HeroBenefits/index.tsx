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

export const HeroBenefits = () => {
    return (
        <div className={styles.BenefitsWrapper}>
            <div>
                <div className={styles.Label}>Built for</div>
                <div className={styles.Value}>Production</div>
                <div className={styles.SmallValue}>not only sandbox envs</div>
            </div>
            <div>
                <div className={styles.Label}>Exposed to</div>
                <div className={styles.Value}>Humans &amp; agents</div>
                <div className={styles.SmallValue}>same RBAC, same audit</div>
            </div>
            <div>
                <div className={styles.Label}>Install</div>
                <div className={styles.Value}>Fast setup</div>
                <span className={styles.SmallValue}>docker · helm · Jar</span>
            </div>
        </div>
    );
};
