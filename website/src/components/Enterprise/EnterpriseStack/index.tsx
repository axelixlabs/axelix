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

export const EnterpriseStack = () => {
    return (
        <div className={styles.MainWrapper}>
            <div className={`${styles.Card} ${styles.FirstCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 3 · Support</div>
                    <div className={styles.CardTitle}>Dedicated support &amp; SLA</div>
                    <div className={styles.Meta}>24/7 · named engineer · priority CVE</div>
                </div>
                <span className={`${styles.Badge} ${styles.FirstCardBadge}`}>contract</span>
            </div>
            <div className={`${styles.Card} ${styles.SecondCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 2 · Extensions</div>
                    <div className={styles.CardTitle}>Enterprise extensions</div>
                    <div className={styles.Meta}>Enforcement Policies · Advanced RBAC · Audit & Compliance</div>
                </div>
                <span className={styles.Badge}>licensed</span>
            </div>
            <div className={`${styles.Card} ${styles.ThirdCard}`}>
                <div className={styles.CardMainContent}>
                    <div className={styles.Tier}>Tier 1 · Foundation</div>
                    <div className={styles.CardTitle}>Axelix OSS</div>
                    <div className={styles.Meta}>LGPL-3.0 · forever free · the same console</div>
                </div>
                <span className={styles.Badge}>always open</span>
            </div>
        </div>
    );
};
