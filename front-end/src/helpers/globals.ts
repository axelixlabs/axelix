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
import { notification } from "antd";
import type { AxiosResponse } from "axios";
import { t } from "i18next";
import type { Dispatch, SetStateAction } from "react";

import {
    EMimeTypes,
    type IConfigPropsBean,
    type IEnvironmentPropertySource,
    type MenuItem,
    StatefulRequest,
} from "models";
import { UNKNOWN_ERROR } from "utils";

export const findOpenKeys = (items: MenuItem[]): string[] => {
    const parent = items.filter((item) => item && "children" in item);
    return parent ? [...parent.map((it) => it!.key as string)] : [];
};

export type SetRequestState<S> = Dispatch<SetStateAction<StatefulRequest<S>>>;

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
