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
import { buildRulerTicks } from "@/helpers";
import type { IUpgradeResponseBody } from "@/models";

import { UpgradeHeadroomPanelRightPart } from "./UpgradeHeadroomPanelRightPart";
import { UpgradeRuler } from "./UpgradeRuler";
import styles from "./styles.module.css";

interface IProps {
    data: IUpgradeResponseBody;
}

export const UpgradeHeadroomPanel = ({ data }: IProps) => {
    const { masterVersion, oldestStarterVersion, compatibilityWindowSize } = data;

    const { ticks } = buildRulerTicks({
        masterVersion,
        oldestStarterVersion,
        compatibilityWindowSize,
    });

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.TagRow}`}>
                    <span className={styles.Tag}>UPGRADE</span>
                    <span className={styles.Tag}>HEADROOM</span>
                </div>

                <div className={styles.FirstSectionWrapper}>
                    <div>
                        <div className={`TextMedium ${styles.Title}`}>
                            Master can move 1 minor release ahead of 1.4 with nothing falling out of support
                        </div>

                        <div className={`TextSmall ${styles.Description}`}>
                            This instance makes no external calls, so it cannot know which releases exist — it counts
                            releases instead of naming one. Take the newest Master bundle you have, up to 1 minor ahead
                            of 1.4, and it is safe today. Dev teams can bump their starters afterwards, at their own
                            pace.
                        </div>
                    </div>

                    <UpgradeHeadroomPanelRightPart data={data} />
                </div>

                <div className={styles.Divider} />

                <UpgradeRuler ticks={ticks} masterVersion={masterVersion} />
            </div>
        </>
    );
};
