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

import { buildRulerTicks, getOldestStarterVersion } from "@/helpers";
import type { IUpgradesData } from "@/models";

import { UpgradesHeadroomPanelRightPart } from "./UpgradesHeadroomPanelRightPart";
import { UpgradesRuler } from "./UpgradesRuler";
import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
}

export const UpgradesHeadroomPanel = ({ data }: IProps) => {
    const { t } = useTranslation();

    const { masterVersion, starterVersions, compatibilityWindow } = data;

    const oldestStarterVersion = getOldestStarterVersion(starterVersions);

    if (!oldestStarterVersion) {
        return null;
    }

    const { headroomOffset } = buildRulerTicks({
        masterVersion: masterVersion,
        oldestStarterVersion: oldestStarterVersion,
        compatibilityWindowSize: compatibilityWindow,
    });

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.TagRow}`}>
                    <div className={styles.Tag}>{t("Upgrades.HeadroomPanel.tagUpgrade")}</div>
                    <div className={styles.Tag}>{t("Upgrades.HeadroomPanel.tagHeadroom")}</div>
                </div>

                <div className={styles.FirstSectionWrapper}>
                    <div>
                        <div className={`TextMedium ${styles.Title}`}>
                            {t("Upgrades.HeadroomPanel.title", {
                                count: headroomOffset,
                                version: masterVersion,
                            })}
                        </div>

                        <div className={`TextSmall ${styles.Description}`}>
                            {t("Upgrades.HeadroomPanel.description", {
                                count: headroomOffset,
                                version: masterVersion,
                            })}
                        </div>
                    </div>

                    <UpgradesHeadroomPanelRightPart data={data} />
                </div>

                <div className={styles.Divider} />

                <UpgradesRuler data={data} />
            </div>
        </>
    );
};
