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

import { DashboardPagesFirstSection, EmptyHandler, Loader } from "components";
import { fetchData, toChartData } from "helpers";
import { type IChartData, type IDashboardJavaResponseBody, type IGCDistributionData, StatefulRequest } from "models";
import { getDashboardJavaData } from "services";

import { DashboardGCDistribution } from "./DashboardGCDistribution";
import { DashboardGauge } from "./DashboardGauge";
import { DashboardProjectLeyden } from "./DashboardProjectLeyden";
import styles from "./styles.module.css";

const toGcDistributionData = (garbageCollectorDistribution: IGCDistributionData): IChartData[] => {
    return Object.entries(garbageCollectorDistribution)
        .map(([categoryName, value]) => ({ categoryName, value }))
        .sort((left, right) => right.value - left.value);
};

const DashboardJava = () => {
    const [dashboardJavaState, setDashboardJavaState] = useState(StatefulRequest.loading<IDashboardJavaResponseBody>());

    useEffect(() => {
        fetchData(setDashboardJavaState, () => getDashboardJavaData());
    }, []);

    if (dashboardJavaState.loading) {
        return <Loader />;
    }

    if (dashboardJavaState.error) {
        return <EmptyHandler isEmpty />;
    }

    const { projectLeyden, gc, garbageCollectorDistribution, projectLilliput } = dashboardJavaState.response!;

    const projectLeydenData = toChartData(projectLeyden);
    const gcDistributionData = toGcDistributionData(garbageCollectorDistribution);

    return (
        <>
            <DashboardPagesFirstSection
                title="Java"
                subtitle="Real-time JVM insights · Project Leyden · Garbage Collection"
            />

            <div className={styles.ChartsWrapper}>
                <DashboardProjectLeyden projectLeydenData={projectLeydenData} />
                <DashboardGCDistribution gcDistributionData={gcDistributionData} />
                <DashboardGauge data={gc} title="Garbage Collector Logging" subtitle="Log output coverage" />
                <DashboardGauge
                    data={projectLilliput}
                    title="Project Liliput Adoption"
                    subtitle="Compact object headers"
                />
            </div>
        </>
    );
};

export default DashboardJava;
