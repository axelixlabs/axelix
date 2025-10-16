import type { IConfigPropsBean } from "models";

export const filterConfigPropsBeans = (beans: IConfigPropsBean[], search: string): IConfigPropsBean[] => {
    const formattedSearch = search.toLowerCase().trim();

    return beans.filter(({ beanName, prefix, properties }) => {
        const lowerBeanName = beanName.toLowerCase();
        if (lowerBeanName.includes(formattedSearch)) {
            return true
        }

        const lowerPrefix = prefix.toLowerCase();
        if (lowerPrefix.includes(formattedSearch)) {
            return true
        }

        return properties.some(({ key }) => (
            key.toLowerCase().includes(formattedSearch)
        ));
    });
};
