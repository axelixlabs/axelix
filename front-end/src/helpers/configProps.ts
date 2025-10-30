import type { IConfigPropsBean } from "models";

export const filterConfigPropsBeans = (beans: IConfigPropsBean[], search: string): IConfigPropsBean[] => {
    const formattedSearch = search.toLowerCase().trim();

    return beans.reduce<IConfigPropsBean[]>((result, bean) => {
        const { beanName, prefix, properties } = bean;

        const isBeanNameMatch = beanName.toLowerCase().includes(formattedSearch);
        const isPrefixMatch = prefix.toLowerCase().includes(formattedSearch);

        if (isBeanNameMatch || isPrefixMatch) {
            result.push(bean);
            return result;
        }

        const filteredProperties = properties.filter(({ key }) => key.toLowerCase().includes(formattedSearch));

        if (filteredProperties.length) {
            result.push({
                beanName: beanName,
                prefix: prefix,
                properties: filteredProperties,
            });
        }

        return result;
    }, []);
};
