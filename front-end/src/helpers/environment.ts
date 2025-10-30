import type { IEnvironmentPropertySource } from "models";

export const filterPropertySources = (
    propertySources: IEnvironmentPropertySource[],
    search: string,
): IEnvironmentPropertySource[] => {
    const formattedSearch = search.toLowerCase().trim();

    return propertySources.reduce<IEnvironmentPropertySource[]>((result, propertySource) => {
        const { name, properties } = propertySource;

        const sourceNameLower = propertySource.name.toLowerCase();

        if (sourceNameLower.includes(formattedSearch)) {
            result.push(propertySource);
            return result;
        }

        const filteredProperties = properties.filter((property) =>
            property.name.toLowerCase().includes(formattedSearch),
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
