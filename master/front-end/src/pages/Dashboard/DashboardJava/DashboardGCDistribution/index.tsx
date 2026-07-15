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

import { DashboardDonutChart } from "components";
import type { IChartData } from "models";

interface IProps {
    gcDistributionData: IChartData[];
}

export const DashboardGCDistribution = ({ gcDistributionData }: IProps) => {
    const { t } = useTranslation();

    const mostUsedGc = gcDistributionData.reduce(
        (max, item) => (item.value > max.value ? item : max),
        gcDistributionData[0],
    );

    return (
        <DashboardDonutChart
            data={gcDistributionData}
            heading={{
                title: t("Dashboard.Java.gcDistributionChartTitle"),
                subtitle: t("Dashboard.Java.gcDistributionChartSubtitle"),
            }}
            centre={{
                title: mostUsedGc.categoryName,
                subtitle: t("Dashboard.Java.gcDistributionChartCentreSubtitle"),
            }}
            rest={{
                show: false,
            }}
        />
    );
};
