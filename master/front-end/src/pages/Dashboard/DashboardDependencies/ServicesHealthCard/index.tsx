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

import { HintTooltip } from "@/components";
import type { IDependenciesDashboardResponseBody } from "@/models";
import { DEPENDENCY_HEALTH_NONE_COLOR, SUPPORT_STATUS_ORDER, dependencyHealthColor } from "@/utils";

import { HealthCard } from "../HealthCard";

import styles from "./styles.module.css";

interface IProps {
    totalServices: number;
    servicesWithRiskyDependencies: number;
    servicesByStatus: IDependenciesDashboardResponseBody["servicesByStatus"];
    lastScan: string;
}

interface IRing {
    key: string;
    color: string;
    label: string;
    hint?: string;
    count: number;
    percentage: number;
}

const OUTER_RADIUS = 62;
const RING_STEP = 14;

const percentageOf = (count: number, total: number) => (total === 0 ? 0 : Math.round((count * 100) / total));

export const ServicesHealthCard = ({
    totalServices,
    servicesWithRiskyDependencies,
    servicesByStatus,
    lastScan,
}: IProps) => {
    const { t } = useTranslation();

    const healthyServices = totalServices - servicesWithRiskyDependencies;

    const rings: IRing[] = [
        ...SUPPORT_STATUS_ORDER.map((status) => {
            const count = servicesByStatus[status] ?? 0;

            return {
                key: status,
                color: dependencyHealthColor[status],
                label: t(`Dashboard.Dependencies.rowLabels.${status}`),
                hint: t(`Dashboard.Dependencies.definitions.${status}`),
                count,
                percentage: percentageOf(count, totalServices),
            };
        }),
        {
            key: "NONE",
            color: DEPENDENCY_HEALTH_NONE_COLOR,
            label: t("Dashboard.Dependencies.noneOfThese"),
            count: healthyServices,
            percentage: percentageOf(healthyServices, totalServices),
        },
    ];

    const percentageSum = rings.reduce((sum, ring) => sum + ring.percentage, 0);

    return (
        <>
            <HealthCard
                eyebrow={t("Dashboard.Dependencies.servicesEyebrow")}
                title={t("Dashboard.Dependencies.services")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.servicesDescriptor"
                        values={{ risky: servicesWithRiskyDependencies, total: totalServices }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                footerLeft={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.reportedBy"
                        values={{ count: totalServices }}
                        components={[<b key="0" />]}
                    />
                }
                footerRight={lastScan}
            >
                <div className={styles.Body}>
                    <svg width="150" height="150" viewBox="0 0 140 140" className={`${styles.SVG} DonutSpinSettle`}>
                        {rings.map(({ key, color, percentage }, index) => {
                            const radius = OUTER_RADIUS - index * RING_STEP;
                            const circumference = 2 * Math.PI * radius;
                            const length = (percentage / 100) * circumference;

                            return (
                                <g key={key}>
                                    <circle cx="70" cy="70" r={radius} className={styles.Track} />
                                    {length > 0 && (
                                        <circle
                                            cx="70"
                                            cy="70"
                                            r={radius}
                                            className={styles.Arc}
                                            stroke={color}
                                            strokeDasharray={`${length} ${circumference}`}
                                        />
                                    )}
                                </g>
                            );
                        })}
                    </svg>

                    <div className={`TextUltraSmall ${styles.Legend} DonutLegendFade`}>
                        <div className={styles.LegendHeader}>
                            <span>{t("Dashboard.Dependencies.uses")}</span>
                            <span className={styles.AlignEnd}>{t("Dashboard.Dependencies.share")}</span>
                        </div>

                        {rings.map(({ key, color, label, hint, count, percentage }) => (
                            <div key={key} className={styles.Row}>
                                <span className={styles.LabelCell}>
                                    <span className={styles.Dot} style={{ backgroundColor: color }} />
                                    <span className={styles.Label}>{label}</span>
                                    {hint && (
                                        <HintTooltip
                                            placement="right"
                                            content={
                                                <>
                                                    <div className={styles.HintTitle} style={{ color }}>
                                                        {label}
                                                    </div>
                                                    <div>{hint}</div>
                                                </>
                                            }
                                        >
                                            <span className={styles.Question}>?</span>
                                        </HintTooltip>
                                    )}
                                </span>
                                <span className={styles.Share}>
                                    <b>{count}</b> <span className={styles.Muted}>({percentage}%)</span>
                                </span>
                            </div>
                        ))}

                        {percentageSum > 100 && (
                            <div className={styles.OverlapNote}>
                                <HintTooltip
                                    content={
                                        <>
                                            <div className={styles.HintTitle}>
                                                {t("Dashboard.Dependencies.overlapTitle")}
                                            </div>
                                            <div>
                                                {t("Dashboard.Dependencies.overlapBody", { total: totalServices })}
                                            </div>
                                        </>
                                    }
                                >
                                    <span className={styles.Info}>i</span>
                                </HintTooltip>
                                <span className={styles.Muted}>
                                    {t("Dashboard.Dependencies.overlapNote", { sum: percentageSum })}
                                </span>
                            </div>
                        )}
                    </div>
                </div>
            </HealthCard>
        </>
    );
};
