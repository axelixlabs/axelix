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
import type { TFunction } from "i18next";

import { EBeanOrigin, EProxyType, type IBean } from "models";

export const resolveProxying = (t: TFunction, proxyType: EProxyType | null): string => {
    if (!proxyType) {
        return t("Beans.unknownProxyingType");
    }

    let message: string;

    switch (proxyType) {
        case EProxyType.CGLIB: {
            message = t("Beans.cglibProxy");
            break;
        }
        case EProxyType.JDK_PROXY: {
            message = t("Beans.jdkProxy");
            break;
        }
        case EProxyType.NO_PROXYING: {
            message = t("Beans.noProxy");
            break;
        }
    }

    return message;
};

export const filterBeans = (beans: IBean[], search: string): IBean[] => {
    const formattedSearch = search.toLowerCase().trim();

    return beans.filter(({ beanName, className, aliases }) => {
        const lowerBeanName = beanName.toLowerCase();
        if (lowerBeanName.includes(formattedSearch)) {
            return true;
        }

        const lowerClassName = className.toLowerCase();
        if (lowerClassName.includes(formattedSearch)) {
            return true;
        }

        return aliases.some((alias) => alias.toLowerCase().includes(formattedSearch));
    });
};

export const defineBeanScopeColor = (scope: string): string => {
    switch (scope) {
        case "singleton":
            return "blue";
        case "prototype":
            return "orange";
        case "request":
            return "cyan";
        case "session":
            return "lime green";
        case "application":
            return "gold";
        case "websocket":
            return "purple";
        case "refresh":
            return "volcano";
        default:
            return "magenta";
    }
};

export const getBeanSourceTranslatedTitle = (
    beanSource: IBean["beanSource"],
    isConfigPropsBean: boolean,
    t: TFunction,
): string => {
    if (beanSource.origin === EBeanOrigin.UNKNOWN && isConfigPropsBean) {
        return t(`Beans.beanSource.CONFIG_PROPS_BEAN`);
    }

    return t(`Beans.beanSource.${beanSource.origin}`);
};
