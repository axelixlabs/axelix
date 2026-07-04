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

interface IProps {
    enabledPercent: number;
}

export const StatCards = ({ enabledPercent }: IProps) => {
    const disabledPercent = 100 - enabledPercent;

    const statCardsData = [
        {
            label: "Logging Enabled",
            value: enabledPercent,
            cardStyles: styles.EnabledLoggingCard,
            valueStyles: styles.EnabledValue,
        },
        {
            label: "Logging Disabled",
            value: disabledPercent,
            cardStyles: styles.DisabledLoggingCard,
            valueStyles: styles.DisabledValue,
        },
    ];

    return (
        <div className={styles.MainWrapper}>
            {statCardsData.map(({ label, value, cardStyles, valueStyles }) => (
                <div key={label} className={`${styles.Card} ${cardStyles}`}>
                    <span className={`${styles.Value} ${valueStyles}`}>{value}%</span>
                    <span className={styles.Label}>{label}</span>
                </div>
            ))}
        </div>
    );
};
