import apiFetch from "api/apiFetch";

export const getConditionsData = (instanceId: string) => {
    return apiFetch.get(`conditions/feed/${instanceId}`);
};