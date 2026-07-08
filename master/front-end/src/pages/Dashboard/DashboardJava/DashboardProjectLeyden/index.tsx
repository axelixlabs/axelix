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

import type { IChartData } from "models";
import { AOT_CACHE_FEATURE_ID, APP_CDS_FEATURE_ID } from "utils";

import { DashboardDonutChart } from "../DashboardDonutChart";

interface IProps {
    projectLeydenData: IChartData[];
}

export const DashboardProjectLeyden = ({ projectLeydenData }: IProps) => {
    const { t } = useTranslation();

    const totalProjectLeydenAdoption = projectLeydenData
        .filter((value) => [APP_CDS_FEATURE_ID, AOT_CACHE_FEATURE_ID].includes(value.categoryName))
        .reduce((acc, item) => acc + item.value, 0);

    return (
        <DashboardDonutChart
            data={projectLeydenData}
            heading={{
                title: t("Dashboard.Java.leydenChartTitle"),
                subtitle: t("Dashboard.Java.leydenChartSubtitle"),
            }}
            centre={{
                title: `${totalProjectLeydenAdoption}%`,
                subtitle: t("Dashboard.Java.leydenChartCentreSubtitle"),
            }}
            rest={{
                show: true,
                title: t("Dashboard.Java.leydenChartRestTitle"),
            }}
        />
    );
};
