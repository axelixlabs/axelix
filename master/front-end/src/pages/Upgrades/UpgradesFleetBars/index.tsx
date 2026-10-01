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
import { useTranslation } from "react-i18next";

import { getOldestStarterVersion, getTotalServiceCount } from "@/helpers";
import type { IUpgradesData } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
    isFleetUpToDate: boolean;
}

export const UpgradesFleetBars = ({ data, isFleetUpToDate }: IProps) => {
    const { t } = useTranslation();

    const { masterVersion, starterVersions } = data;

    const totalServiceCount = getTotalServiceCount(starterVersions);

    const oldestStarterVersion = getOldestStarterVersion(starterVersions);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.Header}>
                    <span className={`TextSmall ${styles.Title}`}>{t("Upgrades.FleetBars.title")}</span>
                    <span className={`TextUltraSmall ${styles.Meta}`}>
                        {t("Upgrades.FleetBars.meta", { count: totalServiceCount })}
                    </span>
                </div>

                <div className={styles.Card}>
                    <div className={styles.BarsContainer}>
                        {starterVersions.map(({ serviceCount, version }) => {
                            const isCurrentVersion = version === masterVersion;
                            const widthPercent = totalServiceCount > 0 ? (serviceCount / totalServiceCount) * 100 : 0;
                            const isCappingVersion = !isFleetUpToDate && version === oldestStarterVersion;

                            return (
                                <div className={styles.BarRow} key={version}>
                                    <div className={`TextSmall ${styles.VersionLabel}`}>{version}</div>
                                    <div className={styles.BarTrack}>
                                        <div
                                            className={`${styles.BarFill} ${isCurrentVersion ? styles.BarFillCurrent : ""}`}
                                            style={{ width: `${widthPercent}%` }}
                                        />
                                    </div>
                                    <div className={`TextSmall ${styles.ServiceCount}`}>
                                        {t("Upgrades.FleetBars.serviceCount", { count: serviceCount })}
                                    </div>

                                    <div>
                                        {isCurrentVersion && (
                                            <div className={`TextUltraSmall ${styles.CurrentTag}`}>
                                                {t("Upgrades.FleetBars.currentRelease")}
                                            </div>
                                        )}

                                        {isCappingVersion && (
                                            <div className={`TextUltraSmall ${styles.CappingTag}`}>
                                                {t("Upgrades.FleetBars.cappingTag")}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    <div className={`TextUltraSmall ${styles.FootNote}`}>{t("Upgrades.FleetBars.footNote")}</div>
                </div>
            </div>
        </>
    );
};
