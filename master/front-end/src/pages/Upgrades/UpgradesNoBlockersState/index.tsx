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

import { UpgradesFleetBars } from "../UpgradesFleetBars";

import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
}

export const UpgradesNoBlockersState = ({ data }: IProps) => {
    const { t } = useTranslation();

    const { masterVersion, starterVersions } = data;
    const oldestStarterVersion = getOldestStarterVersion(starterVersions);
    const totalServiceCount = getTotalServiceCount(starterVersions);

    return (
        <>
            <div className={styles.Card}>
                <div className={styles.CardLeftPart}>
                    <div className={`TextUltraSmall ${styles.Subtitle}`}>{t("Upgrades.NoBlockersState.label")}</div>
                    <div className={`TextMedium ${styles.Title}`}>
                        {t("Upgrades.NoBlockersState.title", {
                            count: totalServiceCount,
                            masterVersion: masterVersion,
                        })}
                    </div>
                    <div className={`TextSmall ${styles.Description}`}>{t("Upgrades.NoBlockersState.description")}</div>
                </div>
                <div className={styles.CardRightPart}>
                    <div className={styles.MetaBlock}>
                        <div className={`TextUltraSmall ${styles.MetaLabel}`}>
                            {t("Upgrades.NoBlockersState.masterRunningNow")}
                        </div>
                        <span>{masterVersion}</span>
                    </div>
                    <div className={styles.MetaBlock}>
                        <span className={`TextUltraSmall ${styles.MetaLabel}`}>
                            {t("Upgrades.NoBlockersState.oldestStarterObserved")}
                        </span>
                        <span>{oldestStarterVersion ?? "—"}</span>
                    </div>
                    <div className={styles.MetaBlock}>
                        <span className={`TextUltraSmall ${styles.MetaLabel}`}>
                            {t("Upgrades.NoBlockersState.servicesLagging")}
                        </span>
                        <span className={styles.MetaValueGreen}>{t("Upgrades.NoBlockersState.none")}</span>
                    </div>
                </div>
            </div>

            <UpgradesFleetBars data={data} isFleetUpToDate />
        </>
    );
};
