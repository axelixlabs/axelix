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

import { isDirectDependency } from "@/helpers";
import type { IResolvedDependency } from "@/models";

import { DependencyDetailFacts } from "./DependencyDetailFacts";
import { DependencyDetailPath } from "./DependencyDetailPath";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The dependency whose expanded detail is rendered.
     */
    dependency: IResolvedDependency;

    /**
     * The {@code group:artifact:version} coordinates of the analyzed application, i.e. the root of the
     * resolution path.
     */
    rootCoordinates: string;
}

export const DependencyDetail = ({ dependency, rootCoordinates }: IProps) => {
    const { t } = useTranslation();

    const direct = isDirectDependency(dependency);

    return (
        <>
            <div className={styles.Detail}>
                <div className={styles.Section}>
                    <div className={styles.SectionHeader}>
                        <span className={`TextUltraSmall ${styles.Label}`}>
                            {t("DependenciesAnalyzer.detail.resolutionPath")}
                        </span>
                        <span className={`TextUltraSmall ${styles.PathSummary}`}>
                            {direct
                                ? t("DependenciesAnalyzer.detail.declaredDirectly")
                                : t("DependenciesAnalyzer.detail.levelsBelowRoot", {
                                      count: dependency.resolutionPath.length,
                                  })}
                        </span>
                    </div>

                    <DependencyDetailPath dependency={dependency} rootCoordinates={rootCoordinates} />

                    <DependencyDetailFacts dependency={dependency} />
                </div>
            </div>
        </>
    );
};
