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
import type { IConfigPropsBean } from "models";

import { canonicalize } from "./globals";

export const filterConfigPropsBeans = (beans: IConfigPropsBean[], search: string): IConfigPropsBean[] => {
    const formattedSearch = canonicalize(search);

    return beans.reduce<IConfigPropsBean[]>((result, bean) => {
        const { beanName, prefix, properties } = bean;

        const isBeanNameMatch = beanName.includes(search.trim());

        if (isBeanNameMatch) {
            result.push(bean);
            return result;
        }

        const filteredProperties = properties.filter(({ key }) => {
            return `${canonicalize(prefix)}${canonicalize(key)}`.includes(formattedSearch);
        });

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
