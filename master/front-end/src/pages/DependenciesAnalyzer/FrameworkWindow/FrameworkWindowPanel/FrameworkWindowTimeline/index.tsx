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

import { buildSupportTimeline } from "@/helpers";
import type { IFrameworkSupportWindow } from "@/models";

import { FrameworkWindowLegend } from "./FrameworkWindowLegend";
import styles from "./styles.module.css";

interface IProps {
    framework: IFrameworkSupportWindow;
    outOfOssMaintenance: boolean;
}

export const FrameworkWindowTimeline = ({ framework, outOfOssMaintenance }: IProps) => {
    const { t } = useTranslation();

    const timeline = buildSupportTimeline(framework);

    return (
        <>
            <div className={styles.Timeline}>
                <div className={styles.Bar}>
                    <span className={styles.OssSegment} style={{ width: `${timeline.ossPercentage}%` }} />
                    <span className={styles.CommercialSegment} style={{ width: `${timeline.commercialPercentage}%` }} />
                </div>
                <div className={styles.TodayTrack}>
                    <span className={styles.TodayMark} style={{ left: `${timeline.todayPercentage}%` }} />
                    <span
                        className={`TextUltraSmall ${styles.TodayLabel}`}
                        style={{ left: `${timeline.todayPercentage}%` }}
                    >
                        {t("DependenciesAnalyzer.framework.today")}
                    </span>
                </div>

                <FrameworkWindowLegend framework={framework} outOfOssMaintenance={outOfOssMaintenance} />
            </div>
        </>
    );
};
