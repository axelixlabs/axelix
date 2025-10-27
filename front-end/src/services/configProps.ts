import apiFetch from "api/apiFetch";

export const getConfigPropsData = (instanceId: string) => {
    return apiFetch.get(`configprops/feed/${instanceId}`);
};
