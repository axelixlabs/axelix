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

import { Accordion } from "@/components";
import { isDirectDependency } from "@/helpers";
import type { IResolvedDependency } from "@/models";
import { supportSignalClassToken } from "@/utils";

import { SupportSignalChip } from "../../SupportSignalChip";

import { DependencyDetail } from "./DependencyDetail";
import styles from "./styles.module.css";

interface IProps {
    /**
     * The dependency to render.
     */
    dependency: IResolvedDependency;

    /**
     * The {@code group:artifact:version} coordinates of the analyzed application, i.e. the root of the
     * resolution path.
     */
    rootCoordinates: string;
}

export const DependencyRow = ({ dependency, rootCoordinates }: IProps) => {
    const { t } = useTranslation();

    const direct = isDirectDependency(dependency);
    const signalToken = dependency.softwareProject
        ? styles[supportSignalClassToken[dependency.softwareProject.status]]
        : "";

    return (
        <>
            <Accordion
                wrapperStyles={`${styles.MainWrapper} ${signalToken}`}
                headerStyles={styles.Row}
                contentStyles={styles.Content}
                header={
                    <>
                        <div className={styles.Dependency}>
                            <span className={`TextSmall ${styles.Coordinates}`}>
                                {`${dependency.dependency.library.groupId}:${dependency.dependency.library.artifactId}`}
                            </span>
                            <span className={`TextUltraSmall ${styles.Project}`}>
                                {dependency.softwareProject?.displayName}
                            </span>
                            {dependency.softwareProject && <SupportSignalChip signal={dependency.softwareProject} />}
                        </div>
                        <span className={`TextSmall ${styles.Version}`}>{dependency.dependency.version}</span>
                        <span className={`TextUltraSmall ${styles.Scope} ${direct ? styles.DirectScope : ""}`}>
                            {t(direct ? "DependenciesAnalyzer.scope.direct" : "DependenciesAnalyzer.scope.transitive")}
                        </span>
                    </>
                }
            >
                <DependencyDetail dependency={dependency} rootCoordinates={rootCoordinates} />
            </Accordion>
        </>
    );
};
