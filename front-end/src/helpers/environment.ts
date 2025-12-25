/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import type { IEnvProperty, IEnvironmentPropertySource, IInjectionPoint } from "models";

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
export const buildAutoCompleteOptions = (propertySources: IEnvironmentPropertySource[]) => {
    return [...new Set(propertySources.flatMap(({ properties }) => properties).map((p) => p.name))].map((value) => {
        return {
            value: value,
        };
    });
};

export const uniqueInjectionPointsBeanNames = (injectionPoints: IInjectionPoint[]): string[] => {
    return injectionPoints ? [...new Set(injectionPoints.map(({ beanName }) => beanName))] : [];
};
