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
import { Pagination } from "antd";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

import { Loader, PageSearch } from "@/components";
import { fetchData } from "@/helpers";
import {
    type EDependencyEcosystem,
    type ESupportStatus,
    type IDependenciesDashboardResponseBody,
    type IDependencyLibrariesResponseBody,
    StatefulRequest,
} from "@/models";
import { getDashboardDependencyLibraries } from "@/services";
import {
    DEPENDENCY_HEALTH_PAGE_SIZE,
    SUPPORT_STATUS_ORDER,
    dependencyHealthColor,
    supportSignalLabelKey,
} from "@/utils";

import { CategoryFilter } from "./CategoryFilter";
import { UsedLibraryRow } from "./UsedLibraryRow";
import styles from "./styles.module.css";

interface IProps {
    totalLibraries: number;
    librariesByStatus: IDependenciesDashboardResponseBody["librariesByStatus"];
    activeEcosystems: EDependencyEcosystem[];
    onEcosystemsChange: (ecosystems: EDependencyEcosystem[]) => void;
}

export const UsedLibraries = ({ totalLibraries, librariesByStatus, activeEcosystems, onEcosystemsChange }: IProps) => {
    const { t } = useTranslation();

    const [search, setSearch] = useState<string>("");
    const [activeStatuses, setActiveStatuses] = useState<ESupportStatus[]>([]);
    const [page, setPage] = useState<number>(1);
    const [libraries, setLibraries] = useState(StatefulRequest.loading<IDependencyLibrariesResponseBody>());

    // The ecosystems are also toggled from the category tiles, every filter change starts over from the first page
    const [filteredEcosystems, setFilteredEcosystems] = useState<EDependencyEcosystem[]>(activeEcosystems);
    if (filteredEcosystems !== activeEcosystems) {
        setFilteredEcosystems(activeEcosystems);
        setPage(1);
    }

    useEffect(() => {
        // The previous page stays on screen until the requested one arrives, and a stale response is dropped
        let stale = false;

        fetchData<IDependencyLibrariesResponseBody>(
            (update) => !stale && setLibraries(update),
            () =>
                getDashboardDependencyLibraries({
                    search: search.trim() || undefined,
                    status: activeStatuses,
                    ecosystem: activeEcosystems,
                    page: page - 1,
                    size: DEPENDENCY_HEALTH_PAGE_SIZE,
                }),
        );

        return () => {
            stale = true;
        };
    }, [search, activeStatuses, activeEcosystems, page]);

    const toggleStatus = (status: ESupportStatus): void => {
        setPage(1);
        setActiveStatuses((prev) =>
            prev.includes(status) ? prev.filter((current) => current !== status) : [...prev, status],
        );
    };

    const rows = libraries.response?.libraries ?? [];
    const matchingLibraries = libraries.response?.matchingLibraries ?? 0;
    const pageStart = (page - 1) * DEPENDENCY_HEALTH_PAGE_SIZE;

    return (
        <>
            <div className={styles.MainWrapper}>
                <div className={`TextUltraSmall ${styles.CaptionRow}`}>
                    <span className={styles.Caption}>{t("Dashboard.Dependencies.librariesCaption")}</span>
                    <span>{t("Dashboard.Dependencies.librariesLegend")}</span>
                </div>

                <div className={styles.Table}>
                    <div className={styles.Toolbar}>
                        <div className={styles.ToolbarRow}>
                            <PageSearch
                                setSearch={(value) => {
                                    setPage(1);
                                    setSearch(value);
                                }}
                                addonAfter={`${matchingLibraries} / ${totalLibraries}`}
                                removeBottomGutter
                            />

                            <CategoryFilter activeEcosystems={activeEcosystems} onChange={onEcosystemsChange} />
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
                                    <span className={styles.ChipCount}>{librariesByStatus[status] ?? 0}</span>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className={styles.Scroll}>
                        <div className={`TextUltraSmall ${styles.HeaderRow}`}>
                            <span />
                            <span>{t("Dashboard.Dependencies.columns.library")}</span>
                            <span>{t("Dashboard.Dependencies.columns.status")}</span>
                            <span>{t("Dashboard.Dependencies.columns.succession")}</span>
                            <span>{t("Dashboard.Dependencies.columns.category")}</span>
                            <span className={styles.AlignEnd}>{t("Dashboard.Dependencies.columns.applications")}</span>
                            <span>{t("Dashboard.Dependencies.share")}</span>
                        </div>

                        {libraries.loading && !libraries.response ? (
                            <Loader />
                        ) : (
                            rows.map((usedLibrary) => (
                                <UsedLibraryRow
                                    key={`${usedLibrary.library.groupId}:${usedLibrary.library.artifactId}`}
                                    usedLibrary={usedLibrary}
                                />
                            ))
                        )}
                    </div>

                    <div className={`TextUltraSmall ${styles.Footer}`}>
                        <span>
                            {rows.length
                                ? t("Dashboard.Dependencies.footer", {
                                      from: pageStart + 1,
                                      to: pageStart + rows.length,
                                      total: matchingLibraries,
                                  })
                                : t("Dashboard.Dependencies.noMatch")}
                        </span>
                        <Pagination
                            size="small"
                            current={page}
                            pageSize={DEPENDENCY_HEALTH_PAGE_SIZE}
                            total={matchingLibraries}
                            showSizeChanger={false}
                            onChange={setPage}
                        />
                    </div>
                </div>
            </div>
        </>
    );
};
