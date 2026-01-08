/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import type { JSX } from "react";
import { useTranslation } from "react-i18next";
import { Cell, Legend, Pie, PieChart, type PieLabelRenderProps, ResponsiveContainer, Tooltip } from "recharts";

import { calculateInnerValueCoordinates, prepareDistributionDataPerChart } from "helpers";
import type { IDistribution } from "models";

import styles from "./styles.module.css";

interface IProps {
    /**
     * Represents a distribution of a software component detailing the component name and the versions available.
     */
    distributions: IDistribution[];
}

export function Distributions({ distributions }: IProps) {
    const { t } = useTranslation();

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
                                    {versions.map(({ versionColor }) => (
                                        <Cell key={versionColor} fill={versionColor} />
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
