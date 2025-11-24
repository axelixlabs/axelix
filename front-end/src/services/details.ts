import apiFetch from "api/apiFetch";
import type { IStateExportRequest } from "models";

export const getDetailsData = (instanceId: string) => {
    return apiFetch.get(`details/${instanceId}`);
};

export const exportStateData = (request: IStateExportRequest) => {
    const { instanceId, body } = request;

    return apiFetch.post(`export-state/${instanceId}`, body);
};
