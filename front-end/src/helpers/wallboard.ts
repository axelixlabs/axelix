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

import { EWallboardFilterOperator, type IInstanceCard, type IWallboardSingleOperandFilter } from "models";
import { getWallboardFilterDefinitions } from "utils";

export const filterInstances = (instances: IInstanceCard[], search: string): IInstanceCard[] => {
    const formattedSearch = search.toLowerCase().trim();

    return instances.filter(({ name }) => name.toLowerCase().includes(formattedSearch));
};

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

export const isJavaMatch = (instance: IInstanceCard, filter: IWallboardSingleOperandFilter): boolean => {
    if (!filter) {
        return true;
    }

    const [cardMajorVersion] = parseVersion(instance.javaVersion);
    const [filterMajorVersion] = parseVersion(filter.operand);

    if (filter.operator === EWallboardFilterOperator.EQUAL) {
        return cardMajorVersion === filterMajorVersion;
    }

    if (filter.operator === EWallboardFilterOperator.GREATER_THAN_EQUAL) {
        return cardMajorVersion >= filterMajorVersion;
    }

    if (filter.operator === EWallboardFilterOperator.LESS_THAN_EQUAL) {
        return cardMajorVersion <= filterMajorVersion;
    }

    return true;
};

export const isSpringBootMatch = (instance: IInstanceCard, filter: IWallboardSingleOperandFilter): boolean => {
    if (!filter) {
        return true;
    }

    const [cardMajorVersion, cardMinorVersion] = parseVersion(instance.springBootVersion);
    const [filterMajorVersion, filterMinorVersion] = parseVersion(filter.operand);

    if (filter.operator === EWallboardFilterOperator.EQUAL) {
        return cardMajorVersion === filterMajorVersion && cardMinorVersion === filterMinorVersion;
    }

    if (filter.operator === EWallboardFilterOperator.GREATER_THAN_EQUAL) {
        return (
            cardMajorVersion > filterMajorVersion ||
            (cardMajorVersion === filterMajorVersion && cardMinorVersion >= filterMinorVersion)
        );
    }

    if (filter.operator === EWallboardFilterOperator.LESS_THAN_EQUAL) {
        return (
            cardMajorVersion < filterMajorVersion ||
            (cardMajorVersion === filterMajorVersion && cardMinorVersion <= filterMinorVersion)
        );
    }

    return true;
};

export const filterWallboardInstances = (
    instances: IInstanceCard[],
    filters: IWallboardSingleOperandFilter[],
    t: TFunction,
): IInstanceCard[] => {
    const lastFilters: Record<string, IWallboardSingleOperandFilter> = {};

    filters.forEach((filter) => {
        lastFilters[filter.key] = filter;
    });

    return instances.filter((instance) =>
        Object.values(lastFilters).every((filter) => {
            const definition = getWallboardFilterDefinitions(t)[filter.key];
            if (!definition) {
                return true;
            }

            return definition.match(instance, filter);
        }),
    );
};
