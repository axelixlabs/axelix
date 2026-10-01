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

import { getOldestStarterServiceCount, getOldestStarterVersion } from "@/helpers";
import type { IUpgradesData } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
}

export const UpgradesHeadroomPanelRightPart = ({ data }: IProps) => {
    const { t } = useTranslation();

    const { masterVersion, compatibilityWindow, starterVersions } = data;

    const oldestStarterVersion = getOldestStarterVersion(starterVersions);
    const oldestStarterServiceCount = getOldestStarterServiceCount(starterVersions);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.PanelMetaBlock}>
                    <span className={`TextUltraSmall ${styles.GreyText}`}>
                        {t("Upgrades.HeadroomPanel.masterRunning")}
                    </span>
                    <span className={`TextSmall ${styles.WhiteText}`}>{masterVersion}</span>
                </div>
                <div className={styles.PanelMetaBlock}>
                    <span className={`TextUltraSmall ${styles.GreyText}`}>
                        {t("Upgrades.HeadroomPanel.oldestStarter")}
                    </span>
                    <div className={styles.ValueContainer}>
                        <div className={`TextSmall ${styles.WhiteText}`}>
                            {oldestStarterVersion}{" "}
                            <span className={`TextUltraSmall ${styles.GreyText}`}>
                                {t("Upgrades.HeadroomPanel.oldestStarterServices", {
                                    count: oldestStarterServiceCount,
                                })}
                            </span>
                        </div>
                    </div>
                </div>
                <div className={styles.PanelMetaBlock}>
                    <span className={`TextUltraSmall ${styles.GreyText}`}>
                        {t("Upgrades.HeadroomPanel.compatibilityWindow")}
                    </span>
                    <span className={`TextSmall ${styles.WhiteText}`}>
                        {t("Upgrades.HeadroomPanel.minorReleases", { count: compatibilityWindow })}
                    </span>
                </div>
            </div>
        </>
    );
};
