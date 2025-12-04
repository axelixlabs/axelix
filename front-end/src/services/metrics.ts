import apiFetch from "api/apiFetch";
import type { IGetSingleMetricRequestData } from "models";

export const getMetricsData = (instanceId: string) => {
    return apiFetch.get(`metrics/${instanceId}`);
};

export const getSingleMetricData = (data: IGetSingleMetricRequestData) => {
    const { instanceId, metric, tags } = data;

    const params = {
        tag: tags,
    };

    return apiFetch.get(`metrics/${instanceId}/${metric}`, { params: params });
};
