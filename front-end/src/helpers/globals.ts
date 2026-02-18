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
import { notification } from "antd";
import type { AxiosResponse } from "axios";
import { t } from "i18next";

import {
    EMimeTypes,
    EWallboardFilterKey,
    EWallboardFilterOperator,
    type IConfigPropsBean,
    type IEnvironmentPropertySource,
    type SetRequestState,
    StatefulRequest,
} from "models";
import { UNKNOWN_ERROR } from "utils";

/**
 * A universal function that retrieves data from the backend.
 *
 * @param setDataState - the React function to set the state of the request
 * @param dataFetcher - the actual data fetcher function, i.e. the function that
 *                      executes an http request to the backend
 */
export async function fetchData<S>(setDataState: SetRequestState<S>, dataFetcher: () => Promise<AxiosResponse>) {
    try {
        const result = await dataFetcher();

        setDataState(() => StatefulRequest.success(result.data));
        // TODO: Fix type in future
    } catch (error: any) {
        const errorCode = extractErrorCode(error?.response?.data);
        setDataState(() => StatefulRequest.error(errorCode));
    }
}

export const getPropertiesCount = <T extends IEnvironmentPropertySource | IConfigPropsBean>(
    propertySourcesList: T[],
): number => {
    return propertySourcesList.reduce((result, { properties }) => result + properties.length, 0);
};

/**
 * @param data any JSON response body that was received from the server
 */
export const extractErrorCode = (data: any): string => {
    return data?.errorCode ?? UNKNOWN_ERROR;
};

/**
 * The "canonicalization" is the process of normalizing the name of the given object
 * to adhere to {@link https://github.com/spring-projects/spring-boot/wiki/relaxed-binding-2.0 relaxed binding rules of the properties in Spring Boot}.
 */
export const canonicalize = (string: string): string => {
    return string.toLowerCase().replace(/[^\p{L}\p{N}]/gu, "");
};

export const normalizeHtmlElementId = (elementId: string): string => {
    return canonicalize(elementId);
};

export function showErrorNotification(errorCode: string): void {
    notification.error({
        title: t("Error.title"),
        description: t(`Error.codes.${errorCode}`),
        placement: "top",
        duration: 4.5,
        showProgress: true,
    });
}

export const commonNormalize = (string: string): string => {
    return canonicalize(string);
};

export const downloadFile = (data: Blob | string): void => {
    const mimeType = typeof data === "string" ? EMimeTypes.TEXT_PLAIN : EMimeTypes.ZIP;

    const blob = new Blob([data], { type: mimeType });

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;

    link.setAttribute("download", "");

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
};

/**
 * @param value milliseconds from epoch
 */
export const toFormattedTime = (value: number): string => {
    return new Date(value).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
    });
};

export const createWallboardFilterSearchParam = (
    key: EWallboardFilterKey,
    operator: EWallboardFilterOperator,
    operand: string,
): string => {
    return `${key}:${operator}:${operand}`;
};
