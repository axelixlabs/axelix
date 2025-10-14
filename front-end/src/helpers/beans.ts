import type { TFunction } from "i18next";

import { EProxyType } from "models";

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