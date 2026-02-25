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
import { Cell, Label, Legend, Pie, PieChart, Tooltip } from "recharts";

import { prepareHealthStatusesChartData } from "helpers";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Key–value map of health statuses
     */
    statuses: Record<string, number>;

    /**
     * Total number of services
     */
    statusesTotalCount: number;
}

export const HealthStatuses = ({ statuses, statusesTotalCount }: IProps) => {
    const { t } = useTranslation();

    const pieData = prepareHealthStatusesChartData(statuses);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextLarge ${styles.Title}`}>{t("Dashboard.healthStatus")}</div>
                <PieChart height={300} width={350}>
                    <Pie data={pieData} nameKey="name" dataKey="value" outerRadius={120} innerRadius={70} label>
                        {pieData.map(({ statusColor }) => (
                            <Cell fill={statusColor} key={statusColor} />
                        ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                    <Label position="center">{`${t("Dashboard.totalCount")}: ${statusesTotalCount}`}</Label>
                </PieChart>
            </div>
        </>
    );
};
