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
import type { IRankedEcosystem, TEcosystemSupportStatusCounts, TSupportStatusCounts } from "@/models";
import { DEPENDENCY_ECOSYSTEM_ORDER, LIMITED_SUPPORT_STATUS_ORDER } from "@/utils";

/**
 * @returns the {@code item} removed from the {@code items} when it is there, otherwise the {@code items} with it added
 */
export const toggleItem = <T>(items: T[], item: T): T[] => {
    return items.includes(item) ? items.filter((current) => current !== item) : [...items, item];
};

export const percentageOf = (count: number, total: number): number => {
    return total === 0 ? 0 : Math.round((count * 100) / total);
};

/**
 * @returns the sum of the counts of the risky support statuses
 */
export const sumOfRisky = (byStatus: TSupportStatusCounts): number => {
    return LIMITED_SUPPORT_STATUS_ORDER.reduce((sum, status) => sum + (byStatus[status] ?? 0), 0);
};

/**
 * Ranks the categories with the most risky libraries first, the ties keep the usual order of the categories. The
 * categories without risky libraries are left out.
 */
export const rankEcosystems = (riskyLibrariesByEcosystem: TEcosystemSupportStatusCounts): IRankedEcosystem[] => {
    return DEPENDENCY_ECOSYSTEM_ORDER.map((ecosystem) => {
        const byStatus = riskyLibrariesByEcosystem[ecosystem] ?? {};

        return { ecosystem, byStatus, total: sumOfRisky(byStatus) };
    })
        .filter(({ total }) => total > 0)
        .sort((left, right) => right.total - left.total);
};
