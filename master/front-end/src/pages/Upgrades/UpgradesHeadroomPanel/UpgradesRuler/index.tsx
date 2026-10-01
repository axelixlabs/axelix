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

import { buildRulerTicks, getOldestStarterServiceCount, getOldestStarterVersion } from "@/helpers";
import type { IUpgradesData } from "@/models";

import { UpgradesBundleVersionInput } from "./UpgradesBundleVersionInput";
import { UpgradesRulerLabels } from "./UpgradesRulerLabels";
import { UpgradesRulerTrack } from "./UpgradesRulerTrack";
import styles from "./styles.module.css";

interface IProps {
    data: IUpgradesData;
}

export const UpgradesRuler = ({ data }: IProps) => {
    const { t } = useTranslation();
    const { masterVersion, starterVersions, compatibilityWindow } = data;
    const oldestStarterVersion = getOldestStarterVersion(starterVersions);

    if (!oldestStarterVersion) {
        return null;
    }

    const oldestStarterServiceCount = getOldestStarterServiceCount(starterVersions);

    const { ticks, dropVersion } = buildRulerTicks({
        masterVersion: masterVersion,
        oldestStarterVersion: oldestStarterVersion,
        compatibilityWindowSize: compatibilityWindow,
    });

    return (
        <>
            <UpgradesRulerTrack ticks={ticks} />
            <UpgradesRulerLabels ticks={ticks} droppedServiceCount={oldestStarterServiceCount} />
            <div className={`TextUltraSmall ${styles.LegendWrapper}`}>
                <span className={`${styles.LegendDot} ${styles.LegendDotGreen}`} />
                <span className={styles.LegendText}>{t("Upgrades.Ruler.legendSupported")}</span>
                <span className={`${styles.LegendDot} ${styles.LegendDotOutside}`} />
                <span className={styles.LegendText}>{t("Upgrades.Ruler.legendOutside")}</span>
            </div>
            <UpgradesBundleVersionInput
                masterVersion={masterVersion}
                dropVersion={dropVersion}
                oldestStarterVersion={oldestStarterVersion}
                oldestStarterServiceCount={oldestStarterServiceCount}
            />
        </>
    );
};
