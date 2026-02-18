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
import type { TFunction } from "i18next";

import {
    EWallboardFilterKey,
    EWallboardFilterOperator,
    type IInstanceCard,
    type IWallboardSingleOperandFilter,
    type WallboardParsedFilter,
} from "models";
import { SEARCH_PARAMS_FILTER, getWallboardFilterDefinitions } from "utils";

export const getAllJavaVersions = (instances: IInstanceCard[]): string[] => {
    const allVersions = new Set<string>();

    instances.forEach((instance) => {
        const [major] = instance.javaVersion.split(".");
        allVersions.add(major);
    });

    return Array.from(allVersions);
};

export const getAllSpringBootVersions = (instances: IInstanceCard[]): string[] => {
    const allVersions = new Set<string>();

    instances.forEach((instance) => {
        const [major, minor] = instance.springBootVersion.split(".");
        allVersions.add(`${major}.${minor}`);
    });

    return Array.from(allVersions);
};

const parseVersion = (version: string): number[] => {
    return version.split(".").map(Number);
};

/**
 * Check if the given {@label candidateSemVer} matches the version specified in the {@label filter}.
 *
 * @param candidateSemVer
 * @param filter
 */
// eslint-disable-next-line complexity
export const semVerMatch = (candidateSemVer: string, filter: IWallboardSingleOperandFilter): boolean => {
    if (!filter) {
        return true;
    }

    // eslint-disable-next-line prefer-const
    let [major, minor, patch] = parseVersion(candidateSemVer);
    // eslint-disable-next-line prefer-const
    let [filterMajor, filterMinor, filterPatch] = parseVersion(filter.operand);

    if (filter.operator == EWallboardFilterOperator.EQUAL) {
        return (
            major == filterMajor &&
            (filterMinor === undefined || filterMinor == minor) &&
            (filterPatch === undefined || filterPatch == patch)
        );
    }

    minor = minor ?? 0;
    patch = patch ?? 0;
    filterMinor = filterMinor ?? 0;
    filterPatch = filterPatch ?? 0;

    if (filter.operator === EWallboardFilterOperator.GREATER_THAN_EQUAL) {
        // 5.0.0 >= 4.0.0 - true
        if (major > filterMajor) {
            return true;
        }

        // 4.0.0 >= 5.0.0 - false
        if (major < filterMajor) {
            return false;
        }

        // 7.7.0 >= 7.5.0 - true
        if (minor > filterMinor) {
            return true;
        }

        // 7.3 >= 7.5.0 - false
        if (minor < filterMinor) {
            return false;
        }

        // 7.2.8 >= 7.2.4 - true
        return patch >= filterPatch;
    }

    if (filter.operator === EWallboardFilterOperator.LESS_THAN_EQUAL) {
        // 6.0.0 <= 7.0.0 - true
        if (major < filterMajor) {
            return true;
        }

        // 6.0.0 > 7.0.0 - false
        if (major > filterMajor) {
            return false;
        }

        // 6.1.0 < 6.2.0 - true
        if (minor < filterMinor) {
            return true;
        }

        // 6.3.0 < 6.2.0 - false
        if (minor > filterMinor) {
            return false;
        }

        // 6.2.1 <= 6.2.2 - true
        return patch <= filterPatch;
    }

    return true;
};

export const filterWallboardInstances = (
    instances: IInstanceCard[],
    searchQuery: string,
    filters: IWallboardSingleOperandFilter[],
    t: TFunction,
): IInstanceCard[] => {
    const normalizedQuery = searchQuery.toLowerCase().trim();

    return instances
        .filter((value) => value.name.toLowerCase().includes(normalizedQuery.toLowerCase()))
        .filter((instance) =>
            Object.values(filters).every((filter) => {
                const definition = getWallboardFilterDefinitions(t)[filter.key];

                if (!definition) {
                    return true;
                }

                return definition.match(instance, filter);
            }),
        );
};

export const createWallboardFilterId = (
    key: EWallboardFilterKey,
    operator: EWallboardFilterOperator,
    operand: string,
): string => {
    return `${key}-${operator}-${operand}`;
};

export const parseWallboardFilters = (searchParams: URLSearchParams): IWallboardSingleOperandFilter[] => {
    const filtersFromUrl = searchParams.getAll(SEARCH_PARAMS_FILTER);

    if (filtersFromUrl.length === 0) {
        return [];
    }

    const parsedFilters: IWallboardSingleOperandFilter[] = [];

    filtersFromUrl.forEach((filter) => {
        const [key, operator, operand] = filter.split(":") as WallboardParsedFilter;

        if (!key || !operator || !operand) {
            return;
        }

        const filterId = createWallboardFilterId(key, operator, operand);
        parsedFilters.push({
            id: filterId,
            key: key,
            operator: operator,
            operand: operand,
        });
    });

    return parsedFilters;
};

/**
 * Removes the {@link EWallboardFilterKey} by provided targetId from passed {@link URLSearchParams}.
 * Performs a noop if provided {@link URLSearchParams} does not contain the requested filter.
 *
 * @param searchParams to remove filter from
 * @param targetId id of filter to remove
 */
export const removeFilterById = (searchParams: URLSearchParams, targetId: string) => {
    const remainingFilters = searchParams.getAll(SEARCH_PARAMS_FILTER).filter((filter) => {
        console.log(`Evaluating ${filter}`);

        const [key, operator, operand] = filter.split(":") as WallboardParsedFilter;

        console.log(`Got ${key}, ${operator}, ${operand}`);

        if (!key || !operator || !operand) {
            return;
        }

        const filterId = createWallboardFilterId(key, operator, operand);

        console.log(`Got ${filterId}`);

        return filterId !== targetId;
    });

    searchParams.delete(SEARCH_PARAMS_FILTER);
    remainingFilters.forEach((filter) => searchParams.append(SEARCH_PARAMS_FILTER, filter));
};
