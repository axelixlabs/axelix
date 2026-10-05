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
import { Fragment } from "react";

import styles from "./styles.module.css";

const LEGEND_ROWS = new Array(3).fill(null);

export const SkeletonCard = () => {
    return (
        <>
            <div className={styles.Card}>
                <div className={styles.Header}>
                    <div className={`${styles.Bar} ${styles.Eyebrow}`} />
                    <div className={`${styles.Bar} ${styles.Title}`} />
                </div>

                <div className={styles.Body}>
                    <div className={styles.Ring} />

                    <div className={styles.Legend}>
                        {LEGEND_ROWS.map((_, index) => {
                            return (
                                <Fragment key={index}>
                                    <div className={styles.Bar} />
                                    <div className={styles.Bar} />
                                </Fragment>
                            );
                        })}
                    </div>
                </div>
            </div>
        </>
    );
};
