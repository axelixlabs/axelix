import apiFetch from "api/apiFetch";
import type { IGetSingleMetricRequestData } from "models";

export const getMetricsData = (instanceId: string) => {
    return apiFetch.get(`metrics/${instanceId}`);
};

export const getSingleMetricData = (data: IGetSingleMetricRequestData) => {
    const { instanceId, metric, selectedTagParams } = data;

    const tagsQuery = selectedTagParams.length
        ? `${selectedTagParams.map((selectedTagParam) => `tag=${selectedTagParam}`).join("&")}`
        : "";

    return apiFetch.get(`metrics/${instanceId}/${metric}?${tagsQuery}`);
};
