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

import type { IDependenciesDashboardResponseBody } from "@/models";
import { SUPPORT_STATUS_ORDER } from "@/utils";

import { HealthStatusBadge } from "../HealthStatusBadge";

import styles from "./styles.module.css";

interface IProps {
    totalApplications: number;
    applicationsByStatus: IDependenciesDashboardResponseBody["applicationsByStatus"];
}

export const HealthSummary = ({ totalApplications, applicationsByStatus }: IProps) => {
    const { t } = useTranslation();

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.TotalWrapper}>
                    <span className={`TextDisplay ${styles.TotalValue}`}>{totalApplications}</span>
                    <span className={`TextUltraSmall ${styles.TotalLabel}`}>
                        {t("Dashboard.Dependencies.applications")}
                    </span>
                </div>
                <div className={styles.Breakdown}>
                    {SUPPORT_STATUS_ORDER.map((status) => (
                        <span key={status} className={styles.BreakdownRow}>
                            <span className={styles.Count}>{applicationsByStatus[status] ?? 0}</span>
                            <HealthStatusBadge status={status} label={t(`Dashboard.Dependencies.badges.${status}`)} />
                            <span className={`TextUltraSmall ${styles.Hint}`}>
                                {t(`Dashboard.Dependencies.usesStatus.${status}`)}
                            </span>
                        </span>
                    ))}
                </div>
            </div>
        </>
    );
};
