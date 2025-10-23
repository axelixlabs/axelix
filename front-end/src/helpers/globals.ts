import { notification } from "antd";
import type { AxiosResponse } from "axios";
import { t } from "i18next";
import type { Dispatch, RefObject, SetStateAction } from "react";

import {
    type IConfigPropsBean,
    type ICustomTooltipState,
    type IEnvironmentPropertySource,
    type MenuItem,
    StatefulRequest,
} from "models";
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
        message: t("Error.title"),
        description: t(`Error.codes.${errorCode}`),
        placement: "top",
        duration: 4.5,
        showProgress: true,
    });
}

export const commonNormalize = (string: string): string => {
    return canonicalize(string);
};

export const calculateTooltipPosition = (
    triggerRef: RefObject<HTMLElement | null>,
    tooltipRef: RefObject<HTMLElement | null>,
    setTooltipState: Dispatch<SetStateAction<ICustomTooltipState>>,
): void => {
    const element = triggerRef.current;
    const tooltip = tooltipRef.current;

    if (!element || !tooltip) {
        return;
    }

    const rect = element.getBoundingClientRect();
    const tipRect = tooltip.getBoundingClientRect();
    const margin = 8;

    let left = rect.left + rect.width / 2 - tipRect.width / 2;
    left = Math.max(8, Math.min(left, innerWidth - tipRect.width - 8));

    const top = rect.top - tipRect.height - margin;

    setTooltipState({ top: Math.round(top), left: Math.round(left) });
};

export const isTooltipTruncatedText = (element: HTMLElement | null): boolean => {
    if (!element) {
        return false;
    }

    return element.scrollWidth > element.clientWidth || element.scrollHeight > element.clientHeight;
};
