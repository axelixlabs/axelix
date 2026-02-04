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
import {
    EWallboardFilterComparisons,
    EWallboardFilterTechnologies,
    type IInstanceCard,
    type ISelectOptionData,
    type IWallboardFilterEntity,
} from "models";

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

export const getWallboardFilterVersions = (versions: string[]): ISelectOptionData[] => {
    return versions.map((version) => ({
        value: version,
        label: version,
    }));
};

const parseVersion = (version: string): number[] => {
    return version.split(".").map(Number);
};

function isJavaMatch(cardVersion: string, filter: IWallboardFilterEntity | undefined): boolean {
    if (!filter) {
        return true;
    }

    const [cardMajorVersion] = parseVersion(cardVersion);
    const [filterMajorVersion] = parseVersion(filter.version);

    if (filter.comparison === EWallboardFilterComparisons.EQUAL) {
        return cardMajorVersion === filterMajorVersion;
    }
    if (filter.comparison === EWallboardFilterComparisons.GREATER_THAN_EQUAL) {
        return cardMajorVersion >= filterMajorVersion;
    }
    if (filter.comparison === EWallboardFilterComparisons.LESS_THAN_EQUAL) {
        return cardMajorVersion <= filterMajorVersion;
    }

    return true;
}

function isSpringBootMatch(cardVersion: string, filter: IWallboardFilterEntity | undefined): boolean {
    if (!filter) {
        return true;
    }

    const [cardMajorVersion, cardMinorVersion] = parseVersion(cardVersion);
    const [filterMajorVersion, filterMinorVersion] = parseVersion(filter.version);

    if (filter.comparison === EWallboardFilterComparisons.EQUAL) {
        return cardMajorVersion === filterMajorVersion && cardMinorVersion === filterMinorVersion;
    }
    if (filter.comparison === EWallboardFilterComparisons.GREATER_THAN_EQUAL) {
        return (
            cardMajorVersion > filterMajorVersion ||
            (cardMajorVersion === filterMajorVersion && cardMinorVersion >= filterMinorVersion)
        );
    }
    if (filter.comparison === EWallboardFilterComparisons.LESS_THAN_EQUAL) {
        return (
            cardMajorVersion < filterMajorVersion ||
            (cardMajorVersion === filterMajorVersion && cardMinorVersion <= filterMinorVersion)
        );
    }

    return true;
}

export const filterWallboardInstances = (
    instances: IInstanceCard[],
    filters: IWallboardFilterEntity[],
): IInstanceCard[] => {
    const allJavaFilters = filters.filter(({ technology }) => technology === EWallboardFilterTechnologies.JAVA);
    const allSpringBootFilters = filters.filter(
        ({ technology }) => technology === EWallboardFilterTechnologies.SPRING_BOOT,
    );

    const lastJavaFilter = allJavaFilters.at(-1);
    const lastSpringBootFilter = allSpringBootFilters.at(-1);

    return instances.filter((card) => {
        const javaMatch = isJavaMatch(card.javaVersion, lastJavaFilter);
        const springBootMatch = isSpringBootMatch(card.springBootVersion, lastSpringBootFilter);
        return javaMatch && springBootMatch;
    });
};
