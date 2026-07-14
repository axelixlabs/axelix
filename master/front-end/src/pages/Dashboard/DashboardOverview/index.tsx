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
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { DashboardPagesFirstSection, EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { type IDashboardResponseBody, StatefulRequest } from "models";
import { getDashboardOverviewData } from "services";

import { Distributions } from "./Distributions";

const DashboardOverview = () => {
    const { t } = useTranslation();
    const [dashboardData, setDashboardData] = useState(StatefulRequest.loading<IDashboardResponseBody>());

    useEffect(() => {
        fetchData(setDashboardData, () => getDashboardOverviewData());
    }, []);

    if (dashboardData.loading) {
        return <Loader />;
    }

    if (dashboardData.error) {
        return <EmptyHandler isEmpty />;
    }

    const distributions = dashboardData.response!.distributions;

    return (
        <>
            <DashboardPagesFirstSection
                title={t("Dashboard.distributions")}
                subtitle="Placeholder Placeholder Placeholder"
            />
            <Distributions distributions={distributions} />
        </>
    );
};

export default DashboardOverview;
