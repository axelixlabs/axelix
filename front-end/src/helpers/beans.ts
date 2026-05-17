/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import type { TFunction } from "i18next";

import { EProxyType, ESearchSubject, type IBean } from "models";

import { normalizeHtmlElementId } from "./globals";

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

export const filterBeans = (beans: IBean[], selectedBeanName: string | null, search: string): IBean[] => {
    const formattedSearch = search.toLowerCase().trim();

    return beans
        .filter(({ beanName }) => {
            return selectedBeanName == null || selectedBeanName === beanName;
        })
        .filter(({ beanName, className, aliases }) => {
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

export const getEffectiveBeans = (selectedBeanName: string | null, search: string, beansFeed: IBean[]): IBean[] => {
    if (selectedBeanName || search) {
        return filterBeans(beansFeed, selectedBeanName, search);
    }

    return beansFeed;
};

export const findBeanBySearchSubject = (
    query: string,
    searchSubject: ESearchSubject,
    beansFeed: IBean[],
): IBean | null => {
    const bean = beansFeed.find(({ className, beanName, aliases }) => {
        if (searchSubject === ESearchSubject.BEAN_CLASS) {
            return className === query;
        }

        if (searchSubject === ESearchSubject.BEAN_NAME_BY_HASH) {
            const normalizedQuery = normalizeHtmlElementId(query);
            const normalizedBeanName = normalizeHtmlElementId(beanName);

            return normalizedBeanName === normalizedQuery;
        }

        if (searchSubject === ESearchSubject.BEAN_NAME_OR_ALIAS) {
            return beanName === query || aliases.includes(query);
        }

        return false;
    });

    if (!bean) {
        return null;
    }

    return bean;
};

export const getBeanShortName = (beanName: string): string => {
    return beanName.split(".").at(-1) ?? beanName;
};
