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
import type { IUpgradeVersionBar } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    versionBars: IUpgradeVersionBar[];
    totalServiceCount: number;
    isFleetUpToDate: boolean;
    oldestStarterVersion?: string;
}

export const UpgradeFleetBars = ({ versionBars, totalServiceCount, isFleetUpToDate, oldestStarterVersion }: IProps) => {
    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Header}>
                    <span className={`TextSmall ${styles.Title}`}>Starter versions across the fleet</span>
                    <span className={`TextUltraSmall ${styles.Meta}`}>{totalServiceCount} services · last 30 days</span>
                </div>

                <div className={styles.BarsContainer}>
                    {versionBars.map(({ isCurrent, serviceCount, version }) => {
                        const widthPercent = totalServiceCount > 0 ? (serviceCount / totalServiceCount) * 100 : 0;
                        const isCappingVersion = !isFleetUpToDate && version === oldestStarterVersion;

                        return (
                            <div className={styles.BarRow} key={version}>
                                <span className={`TextSmall ${styles.VersionLabel}`}>{version}</span>
                                <div className={styles.BarTrack}>
                                    <div
                                        className={`${styles.BarFill} ${isCurrent ? styles.BarFillCurrent : ""}`}
                                        style={{ width: `${widthPercent}%` }}
                                    />
                                </div>
                                <span className={`TextSmall ${styles.ServiceCount}`}>{serviceCount} services</span>

                                {isCurrent && (
                                    <span className={`TextUltraSmall ${styles.CurrentTag}`}>current release</span>
                                )}

                                {isCappingVersion && (
                                    <span className={`TextUltraSmall ${styles.CappingTag}`}>caps headroom at +1</span>
                                )}
                            </div>
                        );
                    })}
                </div>

                <p className={`TextUltraSmall ${styles.FootNote}`}>
                    After the Master upgrade this should shift toward the release you install. A stubborn cluster on an
                    old version usually means a service template nobody has refreshed.
                </p>
            </div>
        </>
    );
};
