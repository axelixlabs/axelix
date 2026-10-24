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
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { DashboardNoData, EmptyHandler, Loader, PagesFirstSection } from "@/components";
import { fetchData } from "@/helpers";
import { type EDependencyEcosystem, type IDependenciesDashboardResponseBody, StatefulRequest } from "@/models";
import { getDashboardDependencies } from "@/services";
import { DEPENDENCY_DATE_FORMAT } from "@/utils";

import { CategoriesHealthCard } from "./CategoriesHealthCard";
import { HealthSummary } from "./HealthSummary";
import { ServicesHealthCard } from "./ServicesHealthCard";
import { TrackedProjects } from "./TrackedProjects";
import styles from "./styles.module.css";

const DashboardDependencies = () => {
    const { t } = useTranslation();

    const [dashboard, setDashboard] = useState(StatefulRequest.loading<IDependenciesDashboardResponseBody>());
    const [activeEcosystems, setActiveEcosystems] = useState<EDependencyEcosystem[]>([]);

    useEffect(() => {
        fetchData(setDashboard, () => getDashboardDependencies());
    }, []);

    if (dashboard.loading) {
        return <Loader />;
    }

    if (dashboard.error) {
        return <EmptyHandler isEmpty />;
    }

    const { totalServices, servicesWithRiskyDependencies, servicesByStatus, trackedProjects, lastScannedAt } =
        dashboard.response!;

    const firstSection = (
        <PagesFirstSection title={t("Dashboard.Dependencies.title")} subtitle={t("Dashboard.Dependencies.subtitle")} />
    );

    if (totalServices === 0) {
        return (
            <>
                {firstSection}

                <DashboardNoData />
            </>
        );
    }

    const lastScan = t("Dashboard.Dependencies.lastScan", {
        date: dayjs(lastScannedAt).format(DEPENDENCY_DATE_FORMAT),
    });

    const toggleEcosystem = (ecosystem: EDependencyEcosystem): void => {
        setActiveEcosystems((prev) =>
            prev.includes(ecosystem) ? prev.filter((current) => current !== ecosystem) : [...prev, ecosystem],
        );
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.FirstSectionWrapper}>
                    {firstSection}

                    <HealthSummary totalServices={totalServices} servicesByStatus={servicesByStatus} />
                </div>

                <div className={styles.CardsWrapper}>
                    <ServicesHealthCard
                        totalServices={totalServices}
                        servicesWithRiskyDependencies={servicesWithRiskyDependencies}
                        servicesByStatus={servicesByStatus}
                        lastScan={lastScan}
                    />

                    <CategoriesHealthCard
                        trackedProjects={trackedProjects}
                        activeEcosystems={activeEcosystems}
                        onToggle={toggleEcosystem}
                        lastScan={lastScan}
                    />
                </div>

                <TrackedProjects
                    trackedProjects={trackedProjects}
                    activeEcosystems={activeEcosystems}
                    onEcosystemsChange={setActiveEcosystems}
                />
            </div>
        </>
    );
};

export default DashboardDependencies;
