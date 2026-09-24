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

import { buildResolutionPath } from "@/helpers";
import type { IResolvedDependency } from "@/models";

import styles from "./styles.module.css";

interface IProps {
    dependency: IResolvedDependency;
    rootCoordinates: string;
}

export const DependencyDetailPath = ({ dependency, rootCoordinates }: IProps) => {
    const { t } = useTranslation();

    const path = buildResolutionPath(rootCoordinates, dependency);

    return (
        <>
            <div className={styles.Path}>
                {path.map(({ depth, coordinates, root, resolved }) => (
                    <div
                        key={`${depth}-${coordinates}`}
                        className={styles.PathNode}
                        style={{ paddingLeft: `${depth * 18}px` }}
                    >
                        <span className={`TextUltraSmall ${styles.Guide}`}>{root ? "■" : "└"}</span>
                        <span
                            className={`TextUltraSmall ${styles.NodeId} ${root || resolved ? styles.Emphasized : ""}`}
                        >
                            {coordinates}
                        </span>
                        {root && (
                            <span className={`${styles.Tag} ${styles.RootTag}`}>
                                {t("DependenciesAnalyzer.detail.thisApplication")}
                            </span>
                        )}
                        {resolved && !root && (
                            <span className={styles.Tag}>{t("DependenciesAnalyzer.detail.resolvedHere")}</span>
                        )}
                    </div>
                ))}
            </div>
        </>
    );
};
