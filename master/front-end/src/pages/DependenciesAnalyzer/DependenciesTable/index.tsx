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

import { EmptyHandler } from "@/components";
import type { IResolvedDependency } from "@/models";

import { DependencyRow } from "./DependencyRow";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The dependencies to display, already filtered.
     */
    dependencies: IResolvedDependency[];

    /**
     * The {@code group:artifact:version} coordinates of the analyzed application, i.e. the root of every
     * resolution path.
     */
    rootCoordinates: string;
}

export const DependenciesTable = ({ dependencies, rootCoordinates }: IProps) => {
    const { t } = useTranslation();

    if (dependencies.length === 0) {
        return <EmptyHandler isEmpty />;
    }

    return (
        <>
            {/* TODO: Improve in the future */}
            <div className={styles.MainWrapper}>
                <div className={styles.Table}>
                    <div className={`TextUltraSmall ${styles.HeaderRow}`}>
                        <span>{t("DependenciesAnalyzer.columns.dependency")}</span>
                        <span className={styles.AlignEnd}>{t("DependenciesAnalyzer.columns.version")}</span>
                        <span className={styles.AlignEnd}>{t("DependenciesAnalyzer.columns.scope")}</span>
                    </div>

                    {dependencies.map((dependency) => (
                        <DependencyRow
                            dependency={dependency}
                            rootCoordinates={rootCoordinates}
                            key={`${dependency.dependency.library.groupId}:${dependency.dependency.library.artifactId}`}
                        />
                    ))}
                </div>
            </div>
        </>
    );
};
