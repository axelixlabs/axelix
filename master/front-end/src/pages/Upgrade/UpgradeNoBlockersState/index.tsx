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
import type { IUpgradeResponseBody } from "@/models";

import { UpgradeFleetBars } from "../UpgradeFleetBars";

import styles from "./styles.module.css";

interface IProps {
    data: IUpgradeResponseBody;
}

export const UpgradeNoBlockersState = ({ data }: IProps) => {
    return (
        <>
            <div className={styles.Card}>
                <div className={styles.CardLeftPart}>
                    <p className={`TextUltraSmall ${styles.Subtitle}`}>FLEET UP TO DATE</p>
                    <h2 className={`TextMedium ${styles.Title}`}>
                        Nothing to do. All 61 services run starter 1.6, the same version as Master.
                    </h2>
                    <p className={`TextSmall ${styles.Description}`}>
                        No service constrains the upgrade path. Whenever your next Master release arrives it will be
                        compatible with the whole fleet, and you can install it without coordinating with any dev team
                        rst.
                    </p>
                </div>
                <div className={styles.CardRightPart}>
                    <div className={styles.MetaBlock}>
                        <span className={`TextUltraSmall ${styles.MetaLabel}`}>Master running now</span>
                        <span>{data.masterVersion}</span>
                    </div>
                    <div className={styles.MetaBlock}>
                        <span className={`TextUltraSmall ${styles.MetaLabel}`}>Oldest starter observed</span>
                        <span>{data.oldestStarterVersion}</span>
                    </div>
                    <div className={styles.MetaBlock}>
                        <span className={`TextUltraSmall ${styles.MetaLabel}`}>Services lagging</span>
                        <span className={styles.MetaValueGreen}>None</span>
                    </div>
                </div>
            </div>

            <UpgradeFleetBars
                versionBars={data.versionBars}
                totalServiceCount={data.totalServiceCount}
                isFleetUpToDate
            />
        </>
    );
};
