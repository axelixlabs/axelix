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
import { Trans, useTranslation } from "react-i18next";

import { percentageOf } from "@/helpers";
import type { IApplicationsRing, TSupportStatusCounts } from "@/models";
import { DEPENDENCY_HEALTH_NONE_COLOR, SUPPORT_STATUS_ORDER, dependencyHealthColor } from "@/utils";

import { HealthCard } from "../HealthCard";

import { ApplicationsDonut } from "./ApplicationsDonut";
import { ApplicationsLegend } from "./ApplicationsLegend";
import styles from "./styles.module.css";

interface IProps {
    totalApplications: number;
    applicationsWithRiskyDependencies: number;
    applicationsByStatus: TSupportStatusCounts;
    lastScan: string;
}

export const ApplicationsHealthCard = ({
    totalApplications,
    applicationsWithRiskyDependencies,
    applicationsByStatus,
    lastScan,
}: IProps) => {
    const { t } = useTranslation();

    const healthyApplications = totalApplications - applicationsWithRiskyDependencies;

    const rings: IApplicationsRing[] = [
        ...SUPPORT_STATUS_ORDER.map((status) => {
            const count = applicationsByStatus[status] ?? 0;

            return {
                key: status,
                color: dependencyHealthColor[status],
                label: t(`Dashboard.Dependencies.rowLabels.${status}`),
                hint: t(`Dashboard.Dependencies.definitions.${status}`),
                count,
                percentage: percentageOf(count, totalApplications),
            };
        }),
        {
            key: "NONE",
            color: DEPENDENCY_HEALTH_NONE_COLOR,
            label: t("Dashboard.Dependencies.noneOfThese"),
            count: healthyApplications,
            percentage: percentageOf(healthyApplications, totalApplications),
        },
    ];

    return (
        <>
            <HealthCard
                eyebrow={t("Dashboard.Dependencies.applicationsEyebrow")}
                title={t("Dashboard.Dependencies.applications")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.applicationsDescriptor"
                        values={{ risky: applicationsWithRiskyDependencies, total: totalApplications }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                footerLeft={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.reportedBy"
                        values={{ count: totalApplications }}
                        components={[<b key="0" />]}
                    />
                }
                footerRight={lastScan}
            >
                <div className={styles.Body}>
                    <ApplicationsDonut rings={rings} />
                    <ApplicationsLegend rings={rings} totalApplications={totalApplications} />
                </div>
            </HealthCard>
        </>
    );
};
