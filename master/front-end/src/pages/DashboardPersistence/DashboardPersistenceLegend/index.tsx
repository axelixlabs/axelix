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
import type { IDashboardTreemapEntity } from "models";

import styles from "./styles.module.css";

interface IProps {
    data: IDashboardTreemapEntity[];
    colors: string[];
}

export const DashboardPersistenceLegend = ({ data, colors }: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                {data.map(({ name, size }, index) => {
                    const legendColor = colors[index % colors.length];

                    return (
                        <div key={name} className={styles.LegendEnity}>
                            <span>
                                <span
                                    className={styles.Color}
                                    style={{
                                        backgroundColor: legendColor,
                                    }}
                                />
                                <span className={`TextUltraSmall ${styles.Label}`}>{name}</span>
                            </span>
                            <span className={`TextUltraSmall ${styles.Value}`}>{size}</span>
                        </div>
                    );
                })}
            </div>
        </>
    );
};
