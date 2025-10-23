import apiFetch from "api/apiFetch";
import type { IUpdatePropertyData } from "models";

export const getEnvironmentData = (id: string) => {
    return apiFetch.get(`env/feed/${id}`);
};

export const updateProperty = (instanceId: string, data: IUpdatePropertyData) => {
    const { propertyName, newValue } = data;

    return apiFetch.post(`/property-management/${instanceId}`, {
        propertyName,
        newValue,
    });
};
