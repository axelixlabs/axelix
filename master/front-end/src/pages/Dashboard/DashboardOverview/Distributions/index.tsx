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
import { useNavigate } from "react-router";

import { DashboardDonutChart } from "components";
import { createWallboardFilterSearchParam, prepareDistributionDataPerChart } from "helpers";
import { EWallboardFilterKey, EWallboardFilterOperator, type IDistribution } from "models";
import { SEARCH_PARAMS_FILTER, mapSoftwareComponentToFilterKey } from "utils";

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

    const clickHandler = (
        e: React.MouseEvent | undefined,
        wallboardFilterComponent: EWallboardFilterKey | undefined,
        version: string | undefined,
    ): void => {
        if (!wallboardFilterComponent || version === undefined) {
            return;
        }

        const wallboardFilterSearchParam = createWallboardFilterSearchParam(
            wallboardFilterComponent,
            EWallboardFilterOperator.EQUAL,
            version,
        );

        const filterParams = new URLSearchParams();
        filterParams.set(SEARCH_PARAMS_FILTER, wallboardFilterSearchParam);

        const targetPath = `/wallboard?${filterParams}`;

        // Unfortunately, we have to handle the browser hotkeys manually below.
        // See the reasoning the comment.
        // https://github.com/axelixlabs/axelix/pull/721/changes#r2823263592
        const isModifiedEvent = e && (e.ctrlKey || e.metaKey || e.shiftKey);

        if (isModifiedEvent) {
            window.open(targetPath, "_blank");
        } else {
            navigate(targetPath);
        }
    };

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={styles.ChartsWrapper}>
                    {components.map(({ softwareComponentName, versions }) => {
                        const wallboardFilterComponent = mapSoftwareComponentToFilterKey(softwareComponentName);
                        const isPieClickable = Boolean(wallboardFilterComponent);

                        let mostUsedCategoryName = "";

                        if (versions.length > 0) {
                            const mostUsedVersion = versions.reduce((currentMostUsedVersion, version) => {
                                if (version.value > currentMostUsedVersion.value) {
                                    return version;
                                }

                                return currentMostUsedVersion;
                            });

                            mostUsedCategoryName = mostUsedVersion.categoryName;
                        }

                        return (
                            <DashboardDonutChart
                                data={versions}
                                heading={{
                                    title: t(`Dashboard.components.${softwareComponentName}`),
                                    subtitle: "Placeholder",
                                }}
                                centre={{
                                    title: mostUsedCategoryName,
                                    subtitle: t("Dashboard.mostUsed"),
                                }}
                                onPieClick={(version, event) => clickHandler(event, wallboardFilterComponent, version)}
                                isPieClickable={isPieClickable}
                                rest={{
                                    show: false,
                                }}
                                calculateLegendPercentages
                                key={softwareComponentName}
                            />
                        );
                    })}
                </div>
            </div>
        </>
    );
}
