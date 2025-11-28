import { notification } from "antd";
import type { AxiosResponse } from "axios";
import { t } from "i18next";
import type { Dispatch, SetStateAction } from "react";

import { type IConfigPropsBean, type IEnvironmentPropertySource, type MenuItem, StatefulRequest } from "models";
import { UNKNOWN_ERROR } from "utils";

export const findOpenKeys = (items: MenuItem[], path: string): string[] => {
    const parent = items.find(
        (item) => item && "children" in item && item.children?.some((child) => child?.key === path),
    );
    return parent ? [parent.key as string] : [];
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
    return data?.code ?? UNKNOWN_ERROR;
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
