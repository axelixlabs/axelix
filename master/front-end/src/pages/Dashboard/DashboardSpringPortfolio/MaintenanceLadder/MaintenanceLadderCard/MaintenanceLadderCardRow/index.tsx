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
import { formatMonthYear, parseIsoDate, platformFullName } from "@/helpers";
import type { IMaintenanceWindowEntry } from "@/models";

import sharedStyles from "../shared.module.css";

import styles from "./styles.module.css";

interface IProps {
    entry: IMaintenanceWindowEntry;
    position: (time: number) => number;
}

export const MaintenanceLadderCardRow = ({ entry, position }: IProps) => {
    const releasedPosition = position(parseIsoDate(entry.releasedAt).getTime());
    const endPosition = position(parseIsoDate(entry.ossSupportEndsAt).getTime());

    return (
        <>
            <div className={sharedStyles.Row}>
                <span className={styles.LineCell}>
                    <span className={`${styles.Dot} ${entry.ossSupported ? styles.DotOk : styles.DotNo}`} />
                    <span className={`TextUltraSmall ${styles.LineLabel}`}>
                        {`${platformFullName(entry.platform)} ${entry.line}`}
                    </span>
                </span>
                <span className={styles.Track}>
                    <span
                        className={styles.Supported}
                        style={{
                            left: `${releasedPosition}%`,
                            width: `${Math.max(endPosition - releasedPosition, 0)}%`,
                        }}
                    />
                    <span className={styles.Unsupported} style={{ left: `${endPosition}%` }} />
                </span>
                <span
                    className={`TextUltraSmall ${styles.EndsCell} ${entry.ossSupported ? styles.EndsOk : styles.EndsNo}`}
                >
                    {formatMonthYear(entry.ossSupportEndsAt)}
                </span>
                <span
                    className={`TextUltraSmall ${styles.AppsCell} ${entry.ossSupported ? styles.AppsOk : styles.AppsNo}`}
                >
                    {entry.applicationCount}
                </span>
            </div>
        </>
    );
};
