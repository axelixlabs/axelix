import apiFetch from "api/apiFetch";
import type { ILoginSubmitValue } from "models";

export const login = (data: ILoginSubmitValue) => {
    return apiFetch.post("users/login", data);
};
