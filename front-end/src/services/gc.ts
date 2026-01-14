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
import apiFetch from "api/apiFetch";
import type { IEnableGCLoggingRequestData } from "models";

export const getGCLoggingStatus = (instanceId: string) => {
    return apiFetch.get(`garbage-collector/logs/${instanceId}/status`);
};

export const enableGCLogging = (data: IEnableGCLoggingRequestData) => {
    const { instanceId, level } = data;

    return apiFetch.post(`garbage-collector/logs/${instanceId}/enable`, {
        level: level,
    });
};

export const triggerGC = (instanceId: string) => {
    return apiFetch.post(`garbage-collector/${instanceId}/trigger`);
};

export const disableGCLogging = (instanceId: string) => {
    return apiFetch.post(`garbage-collector/logs/${instanceId}/disable`);
};

export const getGCLogFile = (instanceId: string) => {
    return apiFetch.get(`garbage-collector/logs/${instanceId}/file`);
};
