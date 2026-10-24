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
import { Trans, useTranslation } from "react-i18next";

import { type EDependencyEcosystem, ESupportStatus, type ITrackedProject } from "@/models";
import {
    DEPENDENCY_ECOSYSTEM_ORDER,
    SUPPORT_STATUS_ORDER,
    dependencyEcosystemLabelKey,
    dependencyHealthColor,
    supportSignalLabelKey,
} from "@/utils";

import { HealthCard } from "../HealthCard";

import styles from "./styles.module.css";

interface IProps {
    trackedProjects: ITrackedProject[];
    activeEcosystems: EDependencyEcosystem[];
    onToggle: (ecosystem: EDependencyEcosystem) => void;
    lastScan: string;
}

export const CategoriesHealthCard = ({ trackedProjects, activeEcosystems, onToggle, lastScan }: IProps) => {
    const { t } = useTranslation();

    const riskyProjects = trackedProjects
        .map(({ softwareProject }) => softwareProject)
        .filter(({ status }) => status !== ESupportStatus.ACTIVE);

    return (
        <>
            <HealthCard
                eyebrow={t("Dashboard.Dependencies.categoriesEyebrow")}
                title={t("Dashboard.Dependencies.projects")}
                descriptor={
                    <Trans
                        t={t}
                        i18nKey="Dashboard.Dependencies.categoriesDescriptor"
                        values={{ risky: riskyProjects.length, total: trackedProjects.length }}
                        components={[<b key="0" />, <b key="1" />]}
                    />
                }
                footerLeft={t("Dashboard.Dependencies.categoriesFooter")}
                footerRight={lastScan}
            >
                <div className={styles.Body}>
                    <div className={`TextUltraSmall ${styles.Caption}`}>
                        <span className={styles.CaptionLabel}>
                            {t("Dashboard.Dependencies.allCategories", { count: DEPENDENCY_ECOSYSTEM_ORDER.length })}
                        </span>
                        <span className={styles.Legend}>
                            {SUPPORT_STATUS_ORDER.map((status) => (
                                <span key={status} className={styles.LegendItem}>
                                    <span
                                        className={styles.LegendSwatch}
                                        style={{ backgroundColor: dependencyHealthColor[status] }}
                                    />
                                    {t(supportSignalLabelKey[status])}
                                </span>
                            ))}
                        </span>
                    </div>

                    <div className={styles.Tiles}>
                        {DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => {
                            const projects = riskyProjects.filter((project) => project.ecosystem === ecosystem);
                            const active = activeEcosystems.includes(ecosystem);

                            return (
                                <button
                                    key={ecosystem}
                                    type="button"
                                    onClick={() => onToggle(ecosystem)}
                                    className={`${styles.Tile} ${active ? styles.ActiveTile : ""} ${projects.length === 0 ? styles.EmptyTile : ""}`}
                                >
                                    <span className={`TextUltraSmall ${styles.TileHeader}`}>
                                        <span>{t(dependencyEcosystemLabelKey[ecosystem])}</span>
                                        <span className={styles.TileCount}>{projects.length}</span>
                                    </span>
                                    <span className={styles.TileBar}>
                                        {SUPPORT_STATUS_ORDER.map((status) => (
                                            <span
                                                key={status}
                                                style={{
                                                    flex: projects.filter((project) => project.status === status)
                                                        .length,
                                                    backgroundColor: dependencyHealthColor[status],
                                                }}
                                            />
                                        ))}
                                    </span>
                                </button>
                            );
                        })}
                    </div>
                </div>
            </HealthCard>
        </>
    );
};
