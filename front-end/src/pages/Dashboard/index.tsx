/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        // TODO: Fix loader to be fullscreen in the future (same needed for the wallboard)
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
