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
import { useState } from "react";

import { getEntityFindings } from "@/helpers";
import type { IMappedEntity } from "@/models";

export const useEntityBrowser = (entities: IMappedEntity[]) => {
    const [search, setSearch] = useState("");
    const [selectedChecks, setSelectedChecks] = useState<string[]>([]);
    const [selectedName, setSelectedName] = useState("");

    const query = search.trim().toLowerCase();
    const hasCheckFilter = selectedChecks.length > 0;
    const inventory = entities.map((entity) => ({ entity, findings: getEntityFindings(entity) }));

    const results = inventory
        .filter(({ entity }) => [entity.name, entity.table].some((field) => field.toLowerCase().includes(query)))
        .map(({ entity, findings }) => ({
            entity,
            findings: hasCheckFilter ? findings.filter(({ check }) => selectedChecks.includes(check)) : findings,
        }))
        .filter(({ findings }) => !hasCheckFilter || findings.length > 0);

    const withFindings = results.filter(({ findings }) => findings.length > 0);
    const withoutFindings = results.filter(({ findings }) => findings.length === 0);
    const selected =
        results.find(({ entity }) => entity.name === selectedName) ?? withFindings[0] ?? withoutFindings[0];

    return {
        search,
        setSearch,
        selectedChecks,
        setSelectedChecks,
        setSelectedName,
        withFindings,
        withoutFindings,
        selected,
    };
};
