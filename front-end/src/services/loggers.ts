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
import type { IChangeLoggerGroupLevelRequestData, ISetLoggerLevelRequestData } from "models";

export const getLoggersData = (instanceId: string) => {
    return apiFetch.get(`loggers/${instanceId}`);
};

export const setLoggerLevel = (data: ISetLoggerLevelRequestData) => {
    const { instanceId, loggerName, loggingLevel } = data;

    return apiFetch.post(`loggers/${instanceId}/logger/${loggerName}`, {
        configuredLevel: loggingLevel,
    });
};

export const changeLoggerGroupLevel = (data: IChangeLoggerGroupLevelRequestData) => {
    const { instanceId, groupName, configuredLevel } = data;

    return apiFetch.post(`loggers/${instanceId}/group/${groupName}`, {
        configuredLevel: configuredLevel,
    });
};
