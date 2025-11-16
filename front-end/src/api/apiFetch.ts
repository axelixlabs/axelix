import axios, { AxiosError } from "axios";

import { extractErrorCode, showErrorNotification } from "helpers";
import type { IErrorResponse } from "models";

const apiFetch = axios.create({
    baseURL: `${import.meta.env.VITE_APP_API_URL}/api/axile`,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
});

apiFetch.interceptors.request.use(async (config) => {
    const token = localStorage.getItem("accessToken");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

apiFetch.interceptors.response.use(
    (response) => response,

    (error: AxiosError<IErrorResponse>) => {
        const errorCode: string | undefined = extractErrorCode(error?.response?.data);

        showErrorNotification(errorCode);

        if (error.response?.status === 401) {
            localStorage.removeItem("accessToken");
            window.location.href = "/login";
        }

        return Promise.reject(error);
    },
);

export default apiFetch;
