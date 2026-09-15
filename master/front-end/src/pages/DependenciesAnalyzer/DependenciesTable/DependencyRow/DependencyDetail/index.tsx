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

import { LinkIcon } from "@/assets";
import { buildResolutionPath, isDirectDependency } from "@/helpers";
import type { IResolvedDependency } from "@/models";
import { dependencyNoteLabelKey } from "@/utils";

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
    const path = buildResolutionPath(rootCoordinates, dependency);
    const note = dependency.signal?.note;

    return (
        <div className={styles.Detail}>
            <div className={styles.Section}>
                <div className={styles.SectionHeader}>
                    <span className={styles.Label}>{t("DependenciesAnalyzer.detail.resolutionPath")}</span>
                    <span className={`TextUltraSmall ${styles.PathSummary}`}>
                        {direct
                            ? t("DependenciesAnalyzer.detail.declaredDirectly")
                            : t("DependenciesAnalyzer.detail.levelsBelowRoot", {
                                  count: dependency.resolutionPath.length,
                              })}
                    </span>
                </div>
                <div className={styles.Path}>
                    {path.map((node) => (
                        <div
                            key={`${node.depth}-${node.coordinates}`}
                            className={styles.PathNode}
                            style={{ paddingLeft: `${node.depth * 18}px` }}
                        >
                            <span className={styles.Guide}>{node.root ? "■" : "└"}</span>
                            <span className={`${styles.NodeId} ${node.root || node.resolved ? styles.Emphasized : ""}`}>
                                {node.coordinates}
                            </span>
                            {node.root && (
                                <span className={`${styles.Tag} ${styles.RootTag}`}>
                                    {t("DependenciesAnalyzer.detail.thisApplication")}
                                </span>
                            )}
                            {node.resolved && !node.root && (
                                <span className={styles.Tag}>{t("DependenciesAnalyzer.detail.resolvedHere")}</span>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            <div className={styles.Facts}>
                <span className={styles.Label}>{t("DependenciesAnalyzer.detail.projectStatus")}</span>
                <span className={`TextSmall ${styles.Prose}`}>{dependency.status}</span>
                {note && (
                    <>
                        <span className={styles.Label}>{t(dependencyNoteLabelKey[note.kind])}</span>
                        <span className={styles.NoteValue}>{note.value}</span>
                    </>
                )}
                <span className={styles.Label}>{t("DependenciesAnalyzer.detail.reference")}</span>
                <span className={styles.Reference}>
                    <a
                        href={dependency.referenceUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={`TextSmall ${styles.ReferenceLink}`}
                    >
                        {dependency.referenceLabel}
                    </a>
                    <LinkIcon className={styles.ReferenceIcon} />
                </span>
            </div>
        </div>
    );
};
