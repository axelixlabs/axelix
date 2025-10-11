import type { IUpdatePropertyData } from "models";
import apiFetch from "api/apiFetch";

export const getEnvironmentData = (id: string) => {
  return apiFetch.get(`env/feed/${id}`);
};

export const updateProperty = (instanceId: string, data: Omit<IUpdatePropertyData, 'propertySourceName'>) => {
  const { propertyName, newValue } = data;

  return apiFetch.post(`/property-management/${instanceId}`, {
    propertyName,
    newValue,
  });
};