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
import { ResponsiveContainer, Tooltip, Treemap } from "recharts";

import { DashboardCard } from "components";
import type { IDashboardPersistenceStatsData, IDashboardTreemapEntity } from "models";

import { DashboardPersistenceLegend } from "../DashboardPersistenceLegend";
import { DashboardPersistenceStats } from "../DashboardPersistenceStats";
import { TreemapCell } from "../TreemapCell";
import sharedStyles from "../shared.module.css";

import { InMemoryPaginationTreemapTooltip } from "./InMemoryPaginationTreemapTooltip";
import styles from "./styles.module.css";

const paginationColors = ["#6366f1", "#8b5cf6", "#a78bfa", "#7c3aed", "#818cf8", "#c4b5fd", "#4f46e5", "#ddd6fe"];

const renderPaginationCell = (props: any) => {
    const label = `${props.size} cases`;

    return <TreemapCell {...props} fill={props.fill} valueLabel={label} />;
};

interface IProps {
    inMemoryPaginationEntries: IDashboardTreemapEntity[];
}

export const InMemoryPaginationTreemap = ({ inMemoryPaginationEntries }: IProps) => {
    const { t } = useTranslation();

    const totalOccurrences = inMemoryPaginationEntries.reduce((acc, item) => acc + item.size, 0);
    const maxCounter = inMemoryPaginationEntries.reduce(
        (acc, item) => (item.size > acc.size ? item : acc),
        inMemoryPaginationEntries[0],
    );

    const statsData: IDashboardPersistenceStatsData[] = [
        {
            label: t("Dashboard.Persistence.statFirstLabel"),
            value: `${totalOccurrences}`,
            color: "#6366f1",
        },
        {
            label: t("Dashboard.Persistence.statSecondLabel"),
            value: `${maxCounter.appName}`,
            color: "#7c3aed",
        },
    ];

    const coloredApplications = inMemoryPaginationEntries.map((data, index) => ({
        ...data,
        name: data.appName,
        fill: paginationColors[index % paginationColors.length],
    }));

    return (
        <>
            <DashboardCard
                title={t("Dashboard.Persistence.inMemoryPaginationChartTitle")}
                subtitle={t("Dashboard.Persistence.inMemoryPaginationChartSubtitle")}
                isEmpty={!inMemoryPaginationEntries.length}
            >
                <div className={sharedStyles.PersistenceCardContentWrapper}>
                    <DashboardPersistenceStats data={statsData} />

                    <div className={styles.ChartWrapper}>
                        <ResponsiveContainer width="100%" height="100%">
                            <Treemap
                                data={coloredApplications}
                                dataKey="size"
                                aspectRatio={4 / 3}
                                content={renderPaginationCell}
                            >
                                <Tooltip content={<InMemoryPaginationTreemapTooltip />} />
                            </Treemap>
                        </ResponsiveContainer>
                    </div>

                    <DashboardPersistenceLegend data={inMemoryPaginationEntries} colors={paginationColors} />
                </div>
            </DashboardCard>
        </>
    );
};
