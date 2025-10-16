import type { TFunction } from "i18next";

import { EProxyType, type IBean } from "models";

export const resolveProxying = (t: TFunction, proxyType: EProxyType | null): string => {

    if (!proxyType) {
        return t("Beans.unknownProxyingType")
    }

    let message: string

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
}

export const filterBeans = (beans: IBean[], search: string): IBean[] => {
    const formattedSearch = search.toLowerCase().trim();

    return beans.filter(({ beanName, className, aliases }) => {
        const lowerBeanName = beanName.toLowerCase();
        if (lowerBeanName.includes(formattedSearch)) {
            return true
        }

        const lowerClassName = className.toLowerCase();
        if (lowerClassName.includes(formattedSearch)) {
            return true
        }

        return aliases.some((alias) => alias.toLowerCase().includes(formattedSearch));
    });
};
