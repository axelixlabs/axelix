import type { IEnvironmentPropertySource } from "models";

export const filterPropertySources = (
    propertySources: IEnvironmentPropertySource[],
    search: string,
): IEnvironmentPropertySource[] => {
    const formattedSearch = search.toLowerCase().trim();

    return propertySources.filter(({ name, properties }) => {
        const lowerName = name.toLowerCase();
        if (lowerName.includes(formattedSearch)) {
            return true;
        }

        return properties.some(({ name }) => name.toLowerCase().includes(formattedSearch));
    });
};
