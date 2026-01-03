/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import axios, { AxiosError } from "axios";

import { extractErrorCode, showErrorNotification } from "helpers";
import type { IErrorResponse } from "models";
import { IS_AUTH } from "utils";

/**
 * Browser will route such requests (with no host:port) to the same origin as the current
 */
const apiFetch = axios.create({
    baseURL: `/api/axelix`,
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

        if (errorCode != "INVALID_JWT_EXCEPTION") {
            showErrorNotification(errorCode);
        }

        if (error.response?.status === 401) {
            localStorage.removeItem(IS_AUTH);
            window.location.href = "/login";
        }

        return Promise.reject(error);
    },
);

export default apiFetch;
