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

import { EmptyHandler, Loader } from "components";
import { fetchData, getTotalStatusesCount } from "helpers";
import { type IDashboardResponseBody, StatefulRequest } from "models";
import { getDashboardData } from "services";

import { Distributions } from "./Distributions";
import { HealthStatuses } from "./HealthStatuses";
import { MemoryCards } from "./MemoryCards";
import styles from "./styles.module.css";

const Dashboard = () => {
    const [dashboardData, setDashboardData] = useState(StatefulRequest.loading<IDashboardResponseBody>());

    useEffect(() => {
        fetchData(setDashboardData, () => getDashboardData());
    }, []);

    if (dashboardData.loading) {
        return <Loader />;
    }

    if (dashboardData.error) {
        return <EmptyHandler isEmpty />;
    }

    const distributions = dashboardData.response!.distributions;
    const statuses = dashboardData.response!.healthStatus.statuses;
    const memoryUsage = dashboardData.response!.memoryUsage;

    const statusesTotalCount = getTotalStatusesCount(statuses);

    return (
        <>
            <Distributions distributions={distributions} />
            <div className={styles.DashboardSecondSectionWrapper}>
                <HealthStatuses statuses={statuses} statusesTotalCount={statusesTotalCount} />
                <MemoryCards memoryUsage={memoryUsage} statusesTotalCount={statusesTotalCount} />
            </div>
        </>
    );
};

export default Dashboard;
