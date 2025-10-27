import apiFetch from "api/apiFetch";

export const getEnvironmentData = (instanceId: string) => {
    return apiFetch.get(`env/feed/${instanceId}`);
};
