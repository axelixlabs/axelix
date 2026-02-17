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
import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { Cell, Legend, Pie, PieChart, type PieLabelRenderProps, ResponsiveContainer, Tooltip } from "recharts";

import {
    calculateInnerValueCoordinates,
    createWallboardFilterSearchParam,
    prepareDistributionDataPerChart,
} from "helpers";
import { EWallboardFilterOperator, type IDistribution } from "models";
import { SEARCH_PARAMS_FILTER, componentsForWallboardFilter } from "utils";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Represents a distribution of a software component detailing the component name and the versions available.
     */
    distributions: IDistribution[];
}

export function Distributions({ distributions }: IProps) {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const components = prepareDistributionDataPerChart(distributions);

    /**
     * Function that renders an inner label (the actual value for the given category)
     */
    const renderInnerLabel = (props: PieLabelRenderProps, totalCategoriesCount: number): JSX.Element => {
        const [x, y, value] = calculateInnerValueCoordinates(props, totalCategoriesCount);

        return (
            <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central">
                {value}
            </text>
        );
    };

    const clickHandler = (softwareComponentName: string, version: string): void => {
        const wallboardFilterComponent = componentsForWallboardFilter[softwareComponentName];
        if (!wallboardFilterComponent) {
            return;
        }

        const wallboardFilterSearchParam = createWallboardFilterSearchParam(
            wallboardFilterComponent,
            EWallboardFilterOperator.EQUAL,
            version,
        );

        const filterParams = new URLSearchParams();
        filterParams.set(SEARCH_PARAMS_FILTER, wallboardFilterSearchParam);

        navigate({
            pathname: "/wallboard",
            search: `?${filterParams}`,
        });
    };

    return (
        <div className={styles.MainWrapper}>
            <div className={`TextMedium ${styles.Title}`}>{t("Dashboard.distributions")}</div>
            <div className={styles.ChartsWrapper}>
                {components.map(({ softwareComponentName, versions }) => (
                    <div className={styles.SingleChartWrapper} key={softwareComponentName}>
                        <div className={styles.CardTitle}>{t(`Dashboard.components.${softwareComponentName}`)}</div>

                        <ResponsiveContainer height={330} width="100%">
                            <PieChart>
                                <Pie
                                    data={versions}
                                    nameKey="name"
                                    dataKey="value"
                                    cx="50%"
                                    cy="50%"
                                    outerRadius={100}
                                    label={(props: PieLabelRenderProps) => {
                                        let sum = 0;

                                        for (const version of versions) {
                                            sum += version.value;
                                        }

                                        return renderInnerLabel(props, sum);
                                    }}
                                    labelLine={false}
                                    stroke={versions.length > 1 ? "#fff" : "none"}
                                >
                                    {versions.map(({ name, versionColor }) => (
                                        <Cell
                                            key={versionColor}
                                            fill={versionColor}
                                            onClick={() => clickHandler(softwareComponentName, name)}
                                        />
                                    ))}
                                </Pie>
                                <Tooltip />
                                <Legend />
                            </PieChart>
                        </ResponsiveContainer>
                    </div>
                ))}
            </div>
        </div>
    );
}
