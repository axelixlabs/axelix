import type { IEnvironmentPropertySource } from "models";

import { canonicalize } from "./global";

export const filterPropertySources = (
    propertySources: IEnvironmentPropertySource[],
    search: string,
): IEnvironmentPropertySource[] => {
    const formattedSearch = canonicalize(search);

    return propertySources.reduce<IEnvironmentPropertySource[]>((result, propertySource) => {
        const { name, properties } = propertySource;

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
                properties: filteredProperties,
            });
        }

        return result;
    }, []);
};
