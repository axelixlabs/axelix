import apiFetch from "api/apiFetch";
import type { ILoginSubmitRequestData } from "models";

export const login = (data: ILoginSubmitRequestData) => {
    return apiFetch.post("users/login", data);
};
