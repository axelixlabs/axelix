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
import type { IRulerTick } from "@/models";

import { UpgradeBundleVersionInput } from "./UpgradeBundleVersionInput";
import { UpgradeRulerLabels } from "./UpgradeRulerLabels";
import { UpgradeRulerTrack } from "./UpgradeRulerTrack";
import styles from "./styles.module.css";

interface IProps {
    ticks: IRulerTick[];
    masterVersion: string;
}

export const UpgradeRuler = ({ ticks, masterVersion }: IProps) => {
    return (
        <>
            <UpgradeRulerTrack ticks={ticks} />

            <UpgradeRulerLabels ticks={ticks} />

            <div className={`TextUltraSmall ${styles.LegendWrapper}`}>
                <span className={`${styles.LegendDot} ${styles.LegendDotGreen}`} />
                <span className={styles.LegendText}>Master versions every observed starter supports</span>
                <span className={`${styles.LegendDot} ${styles.LegendDotOutside}`} />
                <span className={styles.LegendText}>Outside the window · not necessarily released</span>
            </div>

            <UpgradeBundleVersionInput masterVersion={masterVersion} />
        </>
    );
};
