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
import { HintTooltip } from "@/components";
import type { IApplicationsRing } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    ring: IApplicationsRing;
}

export const LegendRow = ({ ring }: IProps) => {
    const { color, label, hint, count, percentage } = ring;

    return (
        <>
            <div className={styles.MainWrapper}>
                <span className={styles.LabelCell}>
                    <span className={styles.Dot} style={{ backgroundColor: color }} />
                    <span className={styles.Label}>{label}</span>
                    {hint && (
                        <HintTooltip
                            placement="right"
                            content={
                                <>
                                    <div className={styles.HintTitle} style={{ color }}>
                                        {label}
                                    </div>
                                    <div>{hint}</div>
                                </>
                            }
                        >
                            <span className={styles.Question}>?</span>
                        </HintTooltip>
                    )}
                </span>
                <span className={styles.Share}>
                    <b>{count}</b> <span className={styles.Muted}>({percentage}%)</span>
                </span>
            </div>
        </>
    );
};
