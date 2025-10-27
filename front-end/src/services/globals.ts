import apiFetch from "api/apiFetch";
import type { IUpdatePropertyRequestData } from "models";

export const updateProperty = (data: IUpdatePropertyRequestData) => {
    const { instanceId, propertyName, newValue } = data;

    return apiFetch.post(`/property-management/${instanceId}`, {
        propertyName,
        newValue,
    });
};
