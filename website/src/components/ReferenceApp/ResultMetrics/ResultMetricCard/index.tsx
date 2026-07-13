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

interface IComparisonPoint {
    amount: string;
    widthPercent: string;
}

interface IProps {
    label: string;
    value: string;
    unit: string;
    was: IComparisonPoint;
    now: IComparisonPoint;
}

export const ResultMetricCard = ({ label, value, unit, was, now }: IProps) => {
    return (
        <div className={styles.MainWrapper}>
            <span className={styles.Label}>{label}</span>
            <div className={styles.Value}>
                {value}
                <span className={styles.Unit}>{unit}</span>
            </div>

            <div className={styles.Comparison}>
                <div className={`${styles.Row} ${styles.Was}`}>
                    <span className={styles.Cap}>was</span>
                    <span className={styles.Track}>
                        <span className={styles.FillBar} style={{ width: was.widthPercent }} />
                    </span>
                    <span className={styles.Amount}>{was.amount}</span>
                </div>
                <div className={`${styles.Row} ${styles.Now}`}>
                    <span className={styles.Cap}>now</span>
                    <span className={styles.Track}>
                        <span className={styles.FillBar} style={{ width: now.widthPercent }} />
                    </span>
                    <span className={styles.Amount}>{now.amount}</span>
                </div>
            </div>
        </div>
    );
};
