import apiFetch from "api/apiFetch";

export const getDetailsData = (instanceId: string) => {
    return apiFetch.get(`details/${instanceId}`);
};
