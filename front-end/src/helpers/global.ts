import type { AxiosResponse } from "axios";
import type { Dispatch, SetStateAction } from "react";

import { type IConfigPropsBean, type IEnvironmentPropertySource, type MenuItem, StatefulRequest } from "models";

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
    } catch {
        setDataState(() => StatefulRequest.error("Some error"));
    }
}

export const getPropertiesCount = <T extends IEnvironmentPropertySource | IConfigPropsBean>(
    propertySourcesList: T[],
): number => {
    return propertySourcesList.reduce((result, { properties }) => result + properties.length, 0);
};
