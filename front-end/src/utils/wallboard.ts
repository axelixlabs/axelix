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

import { getAllJavaVersions, getAllSpringBootVersions, semVerMatch } from "helpers";
import {
    EWallboardFilterKey,
    EWallboardFilterOperator,
    type ISelectOptionData,
    type IWallboardFilterDefinition,
} from "models";

export const filteringKeys = Object.values(EWallboardFilterKey).map((source) => {
    return {
        value: source,
        label: source,
    };
});

const getOperators = (t: TFunction): ISelectOptionData[] => [
    { value: EWallboardFilterOperator.EQUAL, label: t("Wallboard.filter.equal") },
    { value: EWallboardFilterOperator.GREATER_THAN_EQUAL, label: t("Wallboard.filter.greaterThanOrEqual") },
    { value: EWallboardFilterOperator.LESS_THAN_EQUAL, label: t("Wallboard.filter.lessThanOrEqual") },
];

export const getWallboardFilterDefinitions = (
    t: TFunction,
): Record<EWallboardFilterKey, IWallboardFilterDefinition> => {
    return {
        [EWallboardFilterKey.JAVA]: {
            operatorOptions: getOperators(t),
            getOperandsOptions: (instances) =>
                getAllJavaVersions(instances).map((version) => ({ value: version, label: version })),
            match: (instance, filter) => semVerMatch(instance.javaVersion, filter),
        },

        [EWallboardFilterKey.SPRING_BOOT]: {
            operatorOptions: getOperators(t),
            getOperandsOptions: (instances) =>
                getAllSpringBootVersions(instances).map((version) => ({ value: version, label: version })),
            match: (instance, filter) => semVerMatch(instance.springBootVersion, filter),
        },
    };
};

export const componentsForWallboardFilter: Record<string, EWallboardFilterKey> = {
    SpringBoot: EWallboardFilterKey.SPRING_BOOT,
    Java: EWallboardFilterKey.JAVA,
};
