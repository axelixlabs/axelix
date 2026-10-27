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
import { type SetStateAction, useEffect, useState } from "react";

import { fetchData, toggleItem } from "@/helpers";
import {
    type EDependencyEcosystem,
    type ESupportStatus,
    type IDependencyLibrariesResponseBody,
    StatefulRequest,
} from "@/models";
import { getDashboardDependencyLibraries } from "@/services";
import { DEPENDENCY_HEALTH_PAGE_SIZE } from "@/utils";

/**
 * Fetches the page of the used libraries that match the filters. Every filter change starts over from the first page.
 *
 * @param activeEcosystems the categories to filter by, they are also toggled outside the table
 */
export const useLibrariesPage = (activeEcosystems: EDependencyEcosystem[]) => {
    const [search, setSearch] = useState<string>("");
    const [activeStatuses, setActiveStatuses] = useState<ESupportStatus[]>([]);
    const [page, setPage] = useState<number>(1);
    const [libraries, setLibraries] = useState(StatefulRequest.loading<IDependencyLibrariesResponseBody>());

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

    const changeSearch = (value: SetStateAction<string>): void => {
        setPage(1);
        setSearch(value);
    };

    const toggleStatus = (status: ESupportStatus): void => {
        setPage(1);
        setActiveStatuses((prev) => toggleItem(prev, status));
    };

    return { libraries, page, setPage, activeStatuses, changeSearch, toggleStatus };
};
