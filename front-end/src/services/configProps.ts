import apiFetch from "api/apiFetch";

export const getConfigPropsData = (id: string) => {
    return apiFetch.get(`configprops/feed/${id}`);
};
