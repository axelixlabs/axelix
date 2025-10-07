import apiFetch from "api/apiFetch";
import type { IUpdateEnvConfigData } from "models";

export const getEnvironmentData = (id: string) => {
  return apiFetch.get(`env/feed/${id}`);
};

export const updateEnvConfigProperty = (instanceId: string, data: IUpdateEnvConfigData) => {
  const { propertyName, newValue } = data;

  return apiFetch.post(`/property-management/${instanceId}`, {
    propertyName,
    newValue,
  });
};