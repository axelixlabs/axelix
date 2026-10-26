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

import { Loader, PageSearch } from "@/components";
import type { EDependencyEcosystem, IDependenciesDashboardResponseBody } from "@/models";

import { CategoryFilter } from "./CategoryFilter";
import { LibrariesFooter } from "./LibrariesFooter";
import { LibrariesHeader } from "./LibrariesHeader";
import { StatusFilter } from "./StatusFilter";
import { UsedLibraryRow } from "./UsedLibraryRow";
import styles from "./styles.module.css";
import { useLibrariesPage } from "./useLibrariesPage";

interface IProps {
    totalLibraries: number;
    librariesByStatus: IDependenciesDashboardResponseBody["librariesByStatus"];
    activeEcosystems: EDependencyEcosystem[];
    onEcosystemsChange: (ecosystems: EDependencyEcosystem[]) => void;
}

export const UsedLibraries = ({ totalLibraries, librariesByStatus, activeEcosystems, onEcosystemsChange }: IProps) => {
    const { t } = useTranslation();

    const { libraries, page, setPage, activeStatuses, changeSearch, toggleStatus } = useLibrariesPage(activeEcosystems);

    const rows = libraries.response?.libraries ?? [];
    const matchingLibraries = libraries.response?.matchingLibraries ?? 0;

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
                                setSearch={changeSearch}
                                addonAfter={`${matchingLibraries} / ${totalLibraries}`}
                                removeBottomGutter
                            />

                            <CategoryFilter activeEcosystems={activeEcosystems} onChange={onEcosystemsChange} />
                        </div>

                        <StatusFilter
                            librariesByStatus={librariesByStatus}
                            activeStatuses={activeStatuses}
                            onToggle={toggleStatus}
                        />
                    </div>

                    <div className={styles.Scroll}>
                        <LibrariesHeader />

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

                    <LibrariesFooter
                        page={page}
                        shownLibraries={rows.length}
                        matchingLibraries={matchingLibraries}
                        onPageChange={setPage}
                    />
                </div>
            </div>
        </>
    );
};
