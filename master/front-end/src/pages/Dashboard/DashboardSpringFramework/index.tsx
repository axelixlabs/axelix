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

import { DashboardGauge, DashboardPagesFirstSection, EmptyHandler, Loader } from "components";
import { fetchData } from "helpers";
import { type IDashboardSpringFrameworkResponseBody, StatefulRequest } from "models";
import { getDashboardSpringFramework } from "services";

import styles from "./styles.module.css";

const OSIV_FEATURE_ID = "OSIV";

const DashboardSpringFramework = () => {
    const [springFramework, setSpringFramework] = useState(
        StatefulRequest.loading<IDashboardSpringFrameworkResponseBody>(),
    );

    useEffect(() => {
        fetchData(setSpringFramework, () => getDashboardSpringFramework());
    }, []);

    if (springFramework.loading) {
        return <Loader />;
    }

    if (springFramework.error) {
        return <EmptyHandler isEmpty />;
    }

    const { features } = springFramework.response!;
    const osiv = features.filter(({ featureId }) => featureId === OSIV_FEATURE_ID);

    return (
        <>
            <DashboardPagesFirstSection
                title="Spring Framework"
                subtitle="Ecosystem-wide Spring Framework insights · Open Session in View"
            />

            <div className={styles.ChartsWrapper}>
                <DashboardGauge data={osiv} title="Open Session in View (OSIV)" subtitle="Enabled services coverage" />
            </div>
        </>
    );
};

export default DashboardSpringFramework;
