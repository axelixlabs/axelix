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
import type { IAutocompletionOption, IEnvProperty, IEnvironmentPropertySource, IInjectionPoint } from "models";

import { canonicalize } from "./globals";

export const filterPropertySources = (
    propertySources: IEnvironmentPropertySource[],
    search: string,
): IEnvironmentPropertySource[] => {
    const formattedSearch = canonicalize(search);

    return propertySources.reduce<IEnvironmentPropertySource[]>((result, propertySource) => {
        const { name, description, properties } = propertySource;

        const isNameMatch = name.includes(search.trim());

        if (isNameMatch) {
            result.push(propertySource);
            return result;
        }

        const filteredProperties = properties.filter((property) =>
            canonicalize(property.name).includes(formattedSearch),
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

export const isDropdownNeededProperty = (property: IEnvProperty): boolean => {
    const { configPropsBeanName, deprecation, description, injectionPoints } = property;

    return !!(deprecation || description || injectionPoints || configPropsBeanName);
};

/**
 * Spit passed properties into two parts - properties that are supposed to have the drop-down and those that do not.
 */
export const splitProperties = (properties: IEnvProperty[]): [IEnvProperty[], IEnvProperty[]] => {
    const withDropDown: IEnvProperty[] = [];
    const withoutDropDown: IEnvProperty[] = [];

    properties.forEach((property) => {
        if (isDropdownNeededProperty(property)) {
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
