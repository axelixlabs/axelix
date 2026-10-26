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

import type { IApplicationsRing } from "@/models";

import { LegendRow } from "./LegendRow";
import { OverlapNote } from "./OverlapNote";
import styles from "./styles.module.css";

interface IProps {
    rings: IApplicationsRing[];
    totalApplications: number;
}

export const ApplicationsLegend = ({ rings, totalApplications }: IProps) => {
    const { t } = useTranslation();

    // An application may use the libraries of several statuses, so the shares may add up to more than 100%
    const percentageSum = rings.reduce((sum, ring) => sum + ring.percentage, 0);

    return (
        <>
            <div className={`TextUltraSmall ${styles.MainWrapper} DonutLegendFade`}>
                <div className={styles.Header}>
                    <span>{t("Dashboard.Dependencies.uses")}</span>
                    <span className={styles.AlignEnd}>{t("Dashboard.Dependencies.share")}</span>
                </div>

                {rings.map((ring) => (
                    <LegendRow key={ring.key} ring={ring} />
                ))}

                {percentageSum > 100 && (
                    <OverlapNote percentageSum={percentageSum} totalApplications={totalApplications} />
                )}
            </div>
        </>
    );
};
