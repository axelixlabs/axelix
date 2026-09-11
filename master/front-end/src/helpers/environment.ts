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
    EPropertyTriageTag,
    type IAutocompletionOption,
    type IEnvProperty,
    type IEnvironmentPropertySource,
    type IInjectionPoint,
    type IPropertyOccurrence,
    type TPrecedenceIndex,
} from "@/models";

import { canonicalize } from "./globals";

const EMPTY_PRECEDENCE_CHAIN: IPropertyOccurrence[] = [];

const hasTriageTag = (property: IEnvProperty, triageTag: EPropertyTriageTag): boolean => {
    switch (triageTag) {
        case EPropertyTriageTag.DEPRECATED:
            return !!property.deprecation;
        case EPropertyTriageTag.SUPPRESSED:
            return !property.isPrimary;
    }
};

/**
 * A property matches when it carries at least one of the requested tags. An empty selection is not a
 * filter, so everything matches it.
 */
export const matchesTriageTags = (property: IEnvProperty, triageTags: EPropertyTriageTag[]): boolean => {
    return !triageTags.length || triageTags.some((triageTag) => hasTriageTag(property, triageTag));
};

export const countTriageTag = (
    propertySources: IEnvironmentPropertySource[],
    triageTag: EPropertyTriageTag,
): number => {
    return propertySources.reduce(
        (count, { properties }) => count + properties.filter((property) => hasTriageTag(property, triageTag)).length,
        0,
    );
};

export const filterPropertySources = (
    propertySources: IEnvironmentPropertySource[],
    search: string,
    triageTags: EPropertyTriageTag[] = [],
): IEnvironmentPropertySource[] => {
    const formattedSearch = canonicalize(search);

    return propertySources.reduce<IEnvironmentPropertySource[]>((result, propertySource) => {
        const { name, description, properties } = propertySource;

        const isNameMatch = name.includes(search.trim());

        if (isNameMatch && !triageTags.length) {
            result.push(propertySource);
            return result;
        }

        const filteredProperties = properties.filter(
            (property) =>
                (isNameMatch || canonicalize(property.name).includes(formattedSearch)) &&
                matchesTriageTags(property, triageTags),
        );

        if (filteredProperties.length) {
            result.push({
                name: name,
                description: description,
                properties: filteredProperties,
            });
        }

        return result;
    }, []);
};

/**
 * Indexes every occurrence of every property across all the property sources.
 *
 * The property sources arrive from the Spring Boot starter in their precedence order, the highest
 * precedence first, and that order is preserved here - so the first occurrence recorded for a
 * property is the one that wins. Names are canonicalized, so that an environment variable such as
 * SPRING_JPA_OPEN_IN_VIEW lands on the same key as spring.jpa.open-in-view.
 *
 * Must be built from the full, unfiltered list of property sources: a chain assembled from a
 * searched or triaged subset would omit the very sources that explain who wins.
 */
export const buildPrecedenceIndex = (propertySources: IEnvironmentPropertySource[]): TPrecedenceIndex => {
    const precedenceIndex: TPrecedenceIndex = new Map();

    propertySources.forEach(({ name, properties }) => {
        properties.forEach((property) => {
            const key = canonicalize(property.name);
            const occurrence: IPropertyOccurrence = { propertySourceName: name, value: property.value };
            const occurrences = precedenceIndex.get(key);

            if (occurrences) {
                occurrences.push(occurrence);
            } else {
                precedenceIndex.set(key, [occurrence]);
            }
        });
    });

    return precedenceIndex;
};

export const precedenceChainOf = (precedenceIndex: TPrecedenceIndex, propertyName: string): IPropertyOccurrence[] => {
    return precedenceIndex.get(canonicalize(propertyName)) ?? EMPTY_PRECEDENCE_CHAIN;
};

export const isDropdownNeededProperty = (property: IEnvProperty, precedenceChainLength = 0): boolean => {
    const { configPropsBeanName, deprecation, description, injectionPoints } = property;

    return !!(deprecation || description || injectionPoints || configPropsBeanName) || precedenceChainLength > 1;
};

/**
 * Spit passed properties into two parts - properties that are supposed to have the drop-down and those that do not.
 */
export const splitProperties = (
    properties: IEnvProperty[],
    precedenceIndex: TPrecedenceIndex,
): [IEnvProperty[], IEnvProperty[]] => {
    const withDropDown: IEnvProperty[] = [];
    const withoutDropDown: IEnvProperty[] = [];

    properties.forEach((property) => {
        if (isDropdownNeededProperty(property, precedenceChainOf(precedenceIndex, property.name).length)) {
            withDropDown.push(property);
        } else {
            withoutDropDown.push(property);
        }
    });

    return [withDropDown, withoutDropDown];
};

/**
 * Applies deduplication in case the property name is present in multiple property sources with the same name
 */
export const buildAutoCompleteOptions = (propertySources: IEnvironmentPropertySource[]): IAutocompletionOption[] => {
    return [...new Set(propertySources.flatMap(({ properties }) => properties).map((p) => p.name))].map((value) => {
        return {
            value: value,
        };
    });
};

export const uniqueInjectionPointsBeanNames = (injectionPoints: IInjectionPoint[]): string[] => {
    return injectionPoints ? [...new Set(injectionPoints.map(({ beanName }) => beanName))] : [];
};
