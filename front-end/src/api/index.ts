/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import axios, { AxiosError } from "axios";

import { extractErrorCode, showErrorNotification } from "helpers";
import type { IErrorResponse } from "models";
import { IS_AUTH } from "utils";

/**
 * Browser will route such requests (with no host:port) to the same origin as the current
 */
export const apiFetch = axios.create({
    baseURL: "/api/external",
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    },
    paramsSerializer: {
        indexes: null,
    },
});

apiFetch.interceptors.response.use(
    (response) => response,

    (error: AxiosError<IErrorResponse>) => {
        const errorCode: string | undefined = extractErrorCode(error?.response?.data);

        if (errorCode !== "INVALID_JWT_EXCEPTION") {
            showErrorNotification(errorCode);
        }

        if (error.response?.status === 401 && window.location.pathname !== "/login") {
            localStorage.removeItem(IS_AUTH);
            window.location.href = "/login";
        }

        return Promise.reject(error);
    },
);
