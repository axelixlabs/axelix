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
import { fetchData } from "helpers";
import { type IDashboardJavaResponseBody, type IJavaFeatureAdoption, StatefulRequest } from "models";
import { getDashboardJavaData } from "services";

import { DashboardDonutChart } from "./DashboardDonutChart";
import { DashboardGauge } from "./DashboardGauge";
import styles from "./styles.module.css";

export const mockGcDistributionData: IJavaFeatureAdoption[] = [
    { featureId: "G1GC", adoptionPercentage: 54 },
    { featureId: "ZGC", adoptionPercentage: 20 },
    { featureId: "Shenandoah", adoptionPercentage: 15 },
    { featureId: "Parallel", adoptionPercentage: 8 },
    { featureId: "Serial", adoptionPercentage: 3 },
];

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

    const { projectLeyden, gc, projectLilliput } = dashboardJavaState.response!;

    return (
        <>
            <DashboardPagesFirstSection
                title="Java"
                subtitle="Real-time JVM insights · Project Leyden · Garbage Collection"
            />

            <div className={styles.ChartsWrapper}>
                <DashboardDonutChart data={projectLeyden} title="Project Leyden Adoption" subtitle="JVM optimisation" />
                <DashboardDonutChart
                    data={mockGcDistributionData}
                    title="Garbage Collector Distribution"
                    subtitle="Runtime profile"
                    showRest={false}
                />
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
