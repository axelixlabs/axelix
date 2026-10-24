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
import { Pagination, Select } from "antd";
import { useState } from "react";
import { useTranslation } from "react-i18next";

import { PageSearch } from "@/components";
import { type EDependencyEcosystem, ESupportStatus, type ITrackedProject } from "@/models";
import {
    DEPENDENCY_ECOSYSTEM_ORDER,
    DEPENDENCY_HEALTH_PAGE_SIZE,
    SUPPORT_STATUS_ORDER,
    dependencyEcosystemLabelKey,
    dependencyHealthColor,
    supportSignalLabelKey,
} from "@/utils";

import { TrackedProjectRow } from "./TrackedProjectRow";
import styles from "./styles.module.css";

interface IProps {
    trackedProjects: ITrackedProject[];
    activeEcosystems: EDependencyEcosystem[];
    onEcosystemsChange: (ecosystems: EDependencyEcosystem[]) => void;
}

const matchesSearch = ({ softwareProject }: ITrackedProject, search: string): boolean => {
    const query = search.trim().toLowerCase();

    return (
        !query ||
        softwareProject.displayName.toLowerCase().includes(query) ||
        softwareProject.libraries.some(({ groupId, artifactId }) =>
            `${groupId}:${artifactId}`.toLowerCase().includes(query),
        )
    );
};

export const TrackedProjects = ({ trackedProjects, activeEcosystems, onEcosystemsChange }: IProps) => {
    const { t } = useTranslation();

    const [search, setSearch] = useState<string>("");
    const [activeStatuses, setActiveStatuses] = useState<ESupportStatus[]>([]);
    const [page, setPage] = useState<number>(1);

    const toggleStatus = (status: ESupportStatus): void => {
        setPage(1);
        setActiveStatuses((prev) =>
            prev.includes(status) ? prev.filter((current) => current !== status) : [...prev, status],
        );
    };

    const filtered = trackedProjects.filter(
        (project) =>
            matchesSearch(project, search) &&
            (activeEcosystems.length === 0 || activeEcosystems.includes(project.softwareProject.ecosystem)) &&
            (activeStatuses.length === 0 || activeStatuses.includes(project.softwareProject.status)),
    );

    // The risky projects first, each group keeps the most widespread first order of the response
    const sorted = [
        ...filtered.filter(({ softwareProject }) => softwareProject.status !== ESupportStatus.ACTIVE),
        ...filtered.filter(({ softwareProject }) => softwareProject.status === ESupportStatus.ACTIVE),
    ];

    const pages = Math.max(1, Math.ceil(sorted.length / DEPENDENCY_HEALTH_PAGE_SIZE));
    const currentPage = Math.min(page, pages);
    const pageStart = (currentPage - 1) * DEPENDENCY_HEALTH_PAGE_SIZE;
    const pageRows = sorted.slice(pageStart, pageStart + DEPENDENCY_HEALTH_PAGE_SIZE);

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.CaptionRow}`}>
                    <span className={styles.Caption}>{t("Dashboard.Dependencies.trackedCaption")}</span>
                    <span>{t("Dashboard.Dependencies.trackedLegend")}</span>
                </div>

                <div className={styles.Table}>
                    <div className={styles.Toolbar}>
                        <div className={styles.ToolbarRow}>
                            <PageSearch
                                setSearch={(value) => {
                                    setPage(1);
                                    setSearch(value);
                                }}
                                addonAfter={`${filtered.length} / ${trackedProjects.length}`}
                                removeBottomGutter
                            />

                            <Select
                                mode="multiple"
                                allowClear
                                className={styles.CategorySelect}
                                placeholder={t("Dashboard.Dependencies.categoryPlaceholder")}
                                value={activeEcosystems}
                                onChange={(ecosystems) => {
                                    setPage(1);
                                    onEcosystemsChange(ecosystems);
                                }}
                                options={DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => ({
                                    value: ecosystem,
                                    label: t(dependencyEcosystemLabelKey[ecosystem]),
                                }))}
                            />
                        </div>

                        <div className={`TextUltraSmall ${styles.ToolbarRow}`}>
                            <span className={styles.FilterLabel}>{t("Dashboard.Dependencies.statusFilter")}</span>
                            {SUPPORT_STATUS_ORDER.map((status) => (
                                <button
                                    key={status}
                                    type="button"
                                    onClick={() => toggleStatus(status)}
                                    className={`${styles.Chip} ${styles[status]} ${activeStatuses.includes(status) ? styles.ActiveChip : ""}`}
                                >
                                    <span
                                        className={styles.ChipDot}
                                        style={{ backgroundColor: dependencyHealthColor[status] }}
                                    />
                                    {t(supportSignalLabelKey[status])}
                                    <span className={styles.ChipCount}>
                                        {
                                            trackedProjects.filter(
                                                ({ softwareProject }) => softwareProject.status === status,
                                            ).length
                                        }
                                    </span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className={styles.Scroll}>
                        <div className={`TextUltraSmall ${styles.HeaderRow}`}>
                            <span />
                            <span>{t("Dashboard.Dependencies.columns.project")}</span>
                            <span>{t("Dashboard.Dependencies.columns.status")}</span>
                            <span>{t("Dashboard.Dependencies.columns.succession")}</span>
                            <span>{t("Dashboard.Dependencies.columns.category")}</span>
                            <span className={styles.AlignEnd}>{t("Dashboard.Dependencies.columns.services")}</span>
                            <span>{t("Dashboard.Dependencies.share")}</span>
                        </div>

                        {pageRows.map((trackedProject) => (
                            <TrackedProjectRow
                                key={trackedProject.softwareProject.id.value}
                                trackedProject={trackedProject}
                            />
                        ))}
                    </div>

                    <div className={`TextUltraSmall ${styles.Footer}`}>
                        <span>
                            {sorted.length
                                ? t("Dashboard.Dependencies.footer", {
                                      from: pageStart + 1,
                                      to: pageStart + pageRows.length,
                                      total: sorted.length,
                                  })
                                : t("Dashboard.Dependencies.noMatch")}
                        </span>
                        <Pagination
                            size="small"
                            current={currentPage}
                            pageSize={DEPENDENCY_HEALTH_PAGE_SIZE}
                            total={sorted.length}
                            showSizeChanger={false}
                            onChange={setPage}
                        />
                    </div>
                </div>
            </div>
        </>
    );
};
