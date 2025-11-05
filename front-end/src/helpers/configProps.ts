import type { IConfigPropsBean } from "models";

import { canonicalize } from "./global";

export const filterConfigPropsBeans = (beans: IConfigPropsBean[], search: string): IConfigPropsBean[] => {
    const formattedSearch = canonicalize(search);

    return beans.reduce<IConfigPropsBean[]>((result, bean) => {
        const { beanName, prefix, properties } = bean;

        const isBeanNameMatch = canonicalize(beanName).includes(formattedSearch);
        const isPrefixMatch = canonicalize(prefix).includes(formattedSearch);

        if (isBeanNameMatch || isPrefixMatch) {
            result.push(bean);
            return result;
        }

        const filteredProperties = properties.filter(({ key }) => canonicalize(key).includes(formattedSearch));

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
