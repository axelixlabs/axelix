import apiFetch from "api/apiFetch";

export const getWallboardData = () => {
    return apiFetch.get("applications/grid");
};
